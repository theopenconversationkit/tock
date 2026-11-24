/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.nlp.api

import ai.tock.nlp.front.service.UnknownApplicationException
import ai.tock.nlp.front.shared.ApplicationConfiguration
import ai.tock.nlp.front.shared.ApplicationMonitor
import ai.tock.nlp.front.shared.ApplicationRuntimeConfiguration
import ai.tock.nlp.front.shared.Parser
import ai.tock.nlp.front.shared.asRuntimeConfiguration
import ai.tock.nlp.front.shared.codec.CreateApplicationQuery
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.evaluation.EntityEvaluationQuery
import ai.tock.nlp.front.shared.merge.ValuesMergeQuery
import ai.tock.nlp.front.shared.monitoring.MarkAsUnknownQuery
import ai.tock.nlp.front.shared.parser.ParseQuery
import ai.tock.shared.http.HttpHandler
import ai.tock.shared.http.HttpRequest
import ai.tock.shared.http.HttpResponse
import ai.tock.shared.http.HttpRoutes
import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging

/**
 * The routes of the NLP API that the bots call at runtime, independently of the server, for a runtime without
 * Vert.x. The long-running server keeps [NlpVerticle]: these routes answer with the same status codes and the
 * same JSON bodies, but they have no authentication (`tock_nlp_protect_path`) and no dump import.
 */
object NlpApiRoutes {
    private val logger = KotlinLogging.logger {}

    private class ClientException(
        val status: Int,
        message: String,
    ) : RuntimeException(message)

    private data class ErrorMessage(
        val message: String,
    )

    private data class BooleanResponse(
        val success: Boolean = true,
    )

    private fun badRequest(message: String): Nothing = throw ClientException(400, message)

    /**
     * The routes, under [rootPath] (`tock_nlp_root`, `/rest/nlp` by default), on the complete configuration of a
     * process that also runs the admin.
     */
    fun routes(
        rootPath: String,
        parser: Parser,
        configuration: ApplicationConfiguration,
        monitor: ApplicationMonitor,
    ): HttpRoutes = routes(rootPath, parser, configuration.asRuntimeConfiguration(), { configuration.save(it) }, monitor)

    /**
     * The routes, under [rootPath], on the runtime part of the configuration (a serverless process).
     *
     * Without [createApplication], the applications are created in the admin only, and `application/create`
     * answers 501. Without [monitor], `logs/count` answers 501: the logs are searched in the admin.
     */
    fun routes(
        rootPath: String,
        parser: Parser,
        configuration: ApplicationRuntimeConfiguration,
        createApplication: ((ApplicationDefinition) -> ApplicationDefinition)? = null,
        monitor: ApplicationMonitor? = null,
    ): HttpRoutes =
        HttpRoutes {
            post(
                "$rootPath/parse",
                json<ParseQuery> { query ->
                    if (query.queries.isEmpty()) {
                        badRequest("please set queries field with at least one query")
                    }
                    try {
                        parser.parse(query)
                    } catch (e: UnknownApplicationException) {
                        badRequest(e.message ?: "")
                    }
                },
            )

            post("$rootPath/evaluate", json<EntityEvaluationQuery> { parser.evaluateEntities(it) })

            post("$rootPath/merge", json<ValuesMergeQuery> { parser.mergeValues(it) })

            post("$rootPath/unknown", json<MarkAsUnknownQuery> { parser.incrementUnknown(it) })

            get(
                "$rootPath/intents",
                jsonWithoutBody { request ->
                    val (namespace, name) = namespaceAndName(request)
                    configuration
                        .getApplicationByNamespaceAndName(namespace, name)
                        ?.run { configuration.getIntentsByApplicationId(_id) }
                        ?: emptyList<Any>()
                },
            )

            get(
                "$rootPath/application",
                jsonWithoutBody { request ->
                    val (namespace, name) = namespaceAndName(request)
                    configuration.getApplicationByNamespaceAndName(namespace, name)
                },
            )

            post(
                "$rootPath/application/create",
                json<CreateApplicationQuery> { query ->
                    if (createApplication == null) {
                        throw ClientException(501, "the applications are created in the admin")
                    }
                    if (configuration.getApplicationByNamespaceAndName(query.namespace, query.name) == null) {
                        createApplication(
                            ApplicationDefinition(
                                query.name,
                                query.label ?: query.name,
                                query.namespace,
                                supportedLocales = setOf(query.locale),
                            ),
                        )
                    } else {
                        null
                    }
                },
            )

            post(
                "$rootPath/logs/count",
                json<ParseRequestLogCountQueryModel> { query ->
                    if (monitor == null) {
                        throw ClientException(501, "the logs are searched in the admin")
                    }
                    try {
                        val app = configuration.getApplicationByNamespaceAndName(query.namespace, query.applicationName)
                        if (app == null) {
                            throw ClientException(401, "Unauthorized")
                        } else {
                            monitor.search(query.toParseRequestLogCountQuery(app._id)).logs
                        }
                    } catch (e: UnknownApplicationException) {
                        badRequest(e.message ?: "")
                    }
                },
            )
        }

    private fun namespaceAndName(request: HttpRequest): Pair<String, String> {
        val namespace = request.queryParameter("namespace")
        val name = request.queryParameter("name")
        return if (namespace == null || name == null) {
            badRequest("One of the parameters name or namespace is invalid")
        } else {
            namespace to name
        }
    }

    private inline fun <reified I : Any> json(crossinline handler: (I) -> Any?): HttpHandler = jsonWithoutBody { request -> handler(mapper.readValue<I>(request.body)) }

    /**
     * Runs the blocking [handler] on the IO dispatcher, and answers as WebVerticle.endJson does:
     * 204 for null, `{"success":…}` for a boolean, the JSON of the result otherwise.
     */
    private fun jsonWithoutBody(handler: (HttpRequest) -> Any?): HttpHandler =
        { request ->
            try {
                when (val result = withContext(Dispatchers.IO) { handler(request) }) {
                    null -> HttpResponse(204, mapOf("content-type" to listOf("application/json; charset=utf-8")))
                    is Boolean -> HttpResponse.json(mapper.writeValueAsString(BooleanResponse(result)))
                    else -> HttpResponse.json(mapper.writeValueAsString(result))
                }
            } catch (e: ClientException) {
                HttpResponse.json(mapper.writeValueAsString(ErrorMessage(e.message ?: "")), e.status)
            } catch (e: Exception) {
                logger.error(e) { "${request.method} ${request.path} failed" }
                HttpResponse.json(mapper.writeValueAsString(ErrorMessage("Internal Server Error")), 500)
            }
        }
}

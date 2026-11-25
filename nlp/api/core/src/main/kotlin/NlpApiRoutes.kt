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
import ai.tock.nlp.front.shared.Parser
import ai.tock.nlp.front.shared.codec.CreateApplicationQuery
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.evaluation.EntityEvaluationQuery
import ai.tock.nlp.front.shared.merge.ValuesMergeQuery
import ai.tock.nlp.front.shared.monitoring.MarkAsUnknownQuery
import ai.tock.nlp.front.shared.parser.ParseQuery
import ai.tock.shared.booleanProperty
import ai.tock.shared.http.HttpHandler
import ai.tock.shared.http.HttpRequest
import ai.tock.shared.http.HttpResponse
import ai.tock.shared.http.HttpRoutes
import ai.tock.shared.jackson.mapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging

/**
 * The routes of the NLP API that the bots call at runtime, independently of the server, for a runtime without
 * Vert.x. The long-running server keeps `NlpVerticle`: these routes answer with the same status codes and the
 * same JSON bodies, check the namespace of the caller as `tock_nlp_protect_path` does (see [NlpApiSecurity]),
 * and have no dump import.
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

    private fun unauthorized(): Nothing = throw ClientException(401, "Unauthorized")

    /** The caller of a route: [authorize] answers 401 when it may not access a namespace. */
    private class Caller(
        private val protected: Boolean,
        private val namespace: String?,
    ) {
        fun authorize(requested: String?) {
            if (protected && (namespace == null || namespace != requested)) unauthorized()
        }
    }

    /**
     * The routes, under [rootPath] (`tock_nlp_root`, `/rest/nlp` by default), protected by [security]: there is
     * no default, so that a runtime does not expose the NLP API without authentication by mistake.
     *
     * @throws IllegalStateException if [security] is [NlpApiSecurity.Unprotected] while
     * `nlpverticle_tock_nlp_protect_path` is set
     */
    fun routes(
        rootPath: String,
        parser: Parser,
        configuration: ApplicationConfiguration,
        monitor: ApplicationMonitor,
        security: NlpApiSecurity,
    ): HttpRoutes {
        check(security !is NlpApiSecurity.Unprotected || !booleanProperty("nlpverticle_tock_nlp_protect_path", false)) {
            "nlpverticle_tock_nlp_protect_path is set: the NLP API routes need an NlpApiSecurity.Namespace"
        }
        val caller: suspend (HttpRequest) -> Caller =
            when (security) {
                NlpApiSecurity.Unprotected -> { _ -> Caller(false, null) }
                is NlpApiSecurity.Namespace -> { request -> Caller(true, security.namespace(request)) }
            }
        return routes(rootPath, parser, configuration, monitor, caller)
    }

    private fun routes(
        rootPath: String,
        parser: Parser,
        configuration: ApplicationConfiguration,
        monitor: ApplicationMonitor,
        caller: suspend (HttpRequest) -> Caller,
    ): HttpRoutes {
        fun <I : Any> json(
            type: Class<I>,
            handler: Caller.(I) -> Any?,
        ): HttpHandler = jsonWithoutBody(caller) { request -> handler(mapper.readValue(request.body, type)) }

        return HttpRoutes {
            post(
                "$rootPath/parse",
                json(ParseQuery::class.java) { query ->
                    authorize(query.namespace)
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

            post(
                "$rootPath/evaluate",
                json(EntityEvaluationQuery::class.java) {
                    authorize(it.namespace)
                    parser.evaluateEntities(it)
                },
            )

            post(
                "$rootPath/merge",
                json(ValuesMergeQuery::class.java) {
                    authorize(it.namespace)
                    parser.mergeValues(it)
                },
            )

            post(
                "$rootPath/unknown",
                json(MarkAsUnknownQuery::class.java) {
                    authorize(it.namespace)
                    parser.incrementUnknown(it)
                },
            )

            get(
                "$rootPath/intents",
                jsonWithoutBody(caller) { request ->
                    authorize(request.queryParameter("namespace"))
                    val (namespace, name) = namespaceAndName(request)
                    configuration
                        .getApplicationByNamespaceAndName(namespace, name)
                        ?.run { configuration.getIntentsByApplicationId(_id) }
                        ?: emptyList<Any>()
                },
            )

            get(
                "$rootPath/application",
                jsonWithoutBody(caller) { request ->
                    authorize(request.queryParameter("namespace"))
                    val (namespace, name) = namespaceAndName(request)
                    configuration.getApplicationByNamespaceAndName(namespace, name)
                },
            )

            post(
                "$rootPath/application/create",
                json(CreateApplicationQuery::class.java) { query ->
                    authorize(query.namespace)
                    if (configuration.getApplicationByNamespaceAndName(query.namespace, query.name) == null) {
                        configuration.save(
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
                json(ParseRequestLogCountQueryModel::class.java) { query ->
                    authorize(query.namespace)
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

    /**
     * Resolves the caller, runs the blocking [handler] on the IO dispatcher, and answers as WebVerticle.endJson does:
     * 204 for null, `{"success":…}` for a boolean, the JSON of the result otherwise.
     */
    private fun jsonWithoutBody(
        caller: suspend (HttpRequest) -> Caller,
        handler: Caller.(HttpRequest) -> Any?,
    ): HttpHandler =
        { request ->
            try {
                val authenticated = caller(request)
                when (val result = withContext(Dispatchers.IO) { authenticated.handler(request) }) {
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

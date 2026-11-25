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

package ai.tock.bot.connector.openai

import ai.tock.bot.engine.ConnectorController
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.security.auth.spi.WebSecurityHandler
import ai.tock.shared.security.auth.spi.WebSecurityMode
import ai.tock.shared.serialization.writeJson
import ai.tock.shared.vertx.WebSecurityCookiesHandler
import ai.tock.shared.vertx.WebSecurityEncryptedCookiesHandler
import ai.tock.shared.vertx.WebSecurityPassthroughHandler
import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpMethod.GET
import io.vertx.core.http.HttpMethod.OPTIONS
import io.vertx.core.http.HttpMethod.POST
import io.vertx.ext.web.handler.CorsHandler
import mu.KotlinLogging

/**
 * The Vert.x routes of an [OpenAIConnector]: the former behaviour, kept when the project binds its own
 * [WebSecurityHandler].
 */
internal object OpenAIVertxServices {
    private val logger = KotlinLogging.logger {}

    private val tockHandlers = setOf(WebSecurityCookiesHandler::class, WebSecurityEncryptedCookiesHandler::class, WebSecurityPassthroughHandler::class)

    /** The security of the Vert.x routes, when the Vert.x handler bound for [mode] is not one of Tock; null otherwise. */
    fun security(mode: WebSecurityMode): OpenAIConnectorSecurity.Vertx? =
        try {
            injector.provide<WebSecurityHandler>(tag = mode.name)
        } catch (_: Exception) {
            null
        }?.takeUnless { it::class in tockHandlers }?.let { OpenAIConnectorSecurity.Vertx(it) }

    fun register(
        connector: OpenAIConnector,
        controller: ConnectorController,
        security: OpenAIConnectorSecurity.Vertx,
    ) {
        val webSecurityHandler = security.handler
        val path = connector.path
        controller.coRegisterServices(path) { router ->
            logger.debug("deploy Open API connector services for root path $path ")

            val corsHandler =
                CorsHandler
                    .create()
                    .addOriginWithRegex(OpenAIConnector.corsPattern)
                    .allowedMethods(setOf(OPTIONS, GET, POST))
                    .allowedHeaders(OpenAIConnector.corsHeaders)
                    // browsers do not send or save cookies unless credentials are allowed
                    .allowCredentials(webSecurityHandler is WebSecurityCookiesHandler)

            router
                .route("$path*")
                .handler(corsHandler)
                .handler(webSecurityHandler)

            router.get("$path/health").handler { context ->
                context
                    .response()
                    .end()
            }

            router.get("$path/models").handler { context ->
                context
                    .response()
                    .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .end(writeJson(listOf(OpenAIConnector.defaultModel)))
            }

            router.get("$path/models/:modelId").handler { context ->
                context
                    .response()
                    .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .end(writeJson(OpenAIConnector.defaultModel))
            }

            router.post("$path/chat/completions").coHandler { context ->
                val body = context.body()?.asString()
                if (body == null) {
                    logger.warn { "null body for chat completion" }
                    context.fail(500)
                } else {
                    connector.handleRequest(
                        controller,
                        body,
                        context.request().getHeader("X-OpenWebUI-Chat-Id"),
                        context.request().getHeader("Accept-Language"),
                        VertxOpenAIResponder(context),
                    )
                }
            }
        }
    }
}

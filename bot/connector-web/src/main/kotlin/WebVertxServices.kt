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

package ai.tock.bot.connector.web

import ai.tock.bot.connector.web.WebConnector.Companion.CONNECTOR_ID_CONTEXT_KEY
import ai.tock.bot.connector.web.WebConnector.Companion.CONNECTOR_PUBLIC_PATH_CONTEXT_KEY
import ai.tock.bot.connector.web.sse.SseEndpoint
import ai.tock.bot.definition.DialogContext
import ai.tock.bot.engine.ConnectorController
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.security.auth.spi.TOCK_USER_ID
import ai.tock.shared.security.auth.spi.WebSecurityHandler
import ai.tock.shared.security.auth.spi.WebSecurityMode
import ai.tock.shared.vertx.WebSecurityCookiesHandler
import ai.tock.shared.vertx.WebSecurityEncryptedCookiesHandler
import ai.tock.shared.vertx.WebSecurityPassthroughHandler
import ai.tock.shared.vertx.setupSSE
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.handler.CorsHandler
import mu.KotlinLogging

/**
 * The Vert.x routes of a [WebConnector]: the former behaviour, kept when the project binds its own
 * [WebSecurityHandler], and for the server-sent events of tock-bot-connector-web-sse (`tock_web_sse`).
 */
internal object WebVertxServices {
    private val logger = KotlinLogging.logger {}

    /** The server-sent events of tock-bot-connector-web-sse: created only when they are enabled. */
    private val sseEndpoint by lazy { SseEndpoint(WebConnector.webMapper) }

    private val tockHandlers = setOf(WebSecurityCookiesHandler::class, WebSecurityEncryptedCookiesHandler::class, WebSecurityPassthroughHandler::class)

    /**
     * The security of the Vert.x routes: with the server-sent events of tock-bot-connector-web-sse ([sse]), or when
     * the Vert.x handler bound for [mode] is not one of Tock (a handler of the project); null otherwise.
     */
    fun security(
        mode: WebSecurityMode,
        sse: Boolean,
    ): WebConnectorSecurity.Vertx? {
        val handler = boundHandler(mode)
        return when {
            sse -> WebConnectorSecurity.Vertx(handler ?: error("no WebSecurityHandler for $mode"))
            handler != null && handler::class !in tockHandlers -> WebConnectorSecurity.Vertx(handler)
            else -> null
        }
    }

    /** Sends a response to the server-sent events channel of the user (tock-bot-connector-web-sse). */
    fun sendSse(
        connectorId: String,
        recipientId: String,
        response: WebConnectorResponseContract,
    ) {
        sseEndpoint.sendResponse(connectorId, recipientId, response)
    }

    private fun boundHandler(mode: WebSecurityMode): WebSecurityHandler? =
        try {
            injector.provide<WebSecurityHandler>(tag = mode.name)
        } catch (_: Exception) {
            null
        }

    fun register(
        connector: WebConnector,
        controller: ConnectorController,
        security: WebConnectorSecurity.Vertx,
    ) {
        val webSecurityHandler = security.handler
        val path = connector.path
        controller.coRegisterServices(path) { router ->
            logger.debug("deploy web connector services for root path $path ")

            if (webSecurityHandler is WebSecurityCookiesHandler && WebConnector.corsPattern == WebConnector.WILDCARD_CORS_PATTERN) {
                logger.warn { "CORS config is allowing any website, requests are susceptible to CSRF!! Please set ${WebConnector.CORS_PATTERN_PROPERTY} to match only the domains you trust." }
            }

            val corsHandler =
                CorsHandler
                    .create()
                    .addOriginWithRegex(WebConnector.corsPattern)
                    .allowedMethod(HttpMethod.POST)
                    .run {
                        if (WebConnector.sseEnabled || WebConnector.directSseEnabled) allowedMethod(HttpMethod.GET) else this
                    }.allowedHeader("Access-Control-Allow-Origin")
                    .allowedHeader("Content-Type")
                    .allowedHeader("X-Requested-With")
                    .apply {
                        webConnectorExtraHeaders.forEach {
                            this.allowedHeader(it)
                        }
                    }
                    // browsers do not send or save cookies unless credentials are allowed
                    .allowCredentials(webSecurityHandler is WebSecurityCookiesHandler)

            router
                .routeWithRegex("${Regex.escape(path)}(/.*)?")
                .order(-10)
                // Apply CORS Handler for all paths and all methods (OPTIONS handled automatically)
                .handler(corsHandler)
                // add context for additional handlers
                .handler { context ->
                    context.put(CONNECTOR_PUBLIC_PATH_CONTEXT_KEY, connector.publicPath)
                    context.put(CONNECTOR_ID_CONTEXT_KEY, connector.connectorId)
                    context.next()
                }

            if (WebConnector.sseEnabled) {
                sseEndpoint.configureRoute(router, "$path/sse", connector.connectorId, webSecurityHandler)
            }

            if (WebConnector.directSseEnabled) {
                router
                    .route("$path/sse/direct")
                    .coHandler { context ->
                        try {
                            val body =
                                context.request().getHeader("message")
                                    ?: context.body().asString() ?: error("message is mandatory and is missing")
                            context.response().setupSSE()

                            connector.handleRequest(controller, WebRequestInfos(context.request()), body, DialogContext.EMPTY, VertxWebResponder(context))
                        } catch (t: Throwable) {
                            context.fail(t)
                        }
                    }
            }

            // Main connector endpoint
            router
                .post(path)
                .handler(webSecurityHandler)
                .coHandler { context ->
                    // Override the user on the request body
                    val tockUserId: String? = context.get<String>(TOCK_USER_ID)
                    val transientContext: DialogContext = context.get(WebSecurityHandler.TRANSIENT_DIALOG_CONTEXT_KEY) ?: DialogContext.EMPTY
                    val body =
                        tockUserId?.let {
                            val jsonBody = context.body().asJsonObject() ?: JsonObject()
                            jsonBody.put("userId", tockUserId)
                            jsonBody.toString()
                        } ?: context.body().asString()

                    // Handle the request
                    connector.handleRequest(controller, WebRequestInfos(context.request()), checkNotNull(body), transientContext, VertxWebResponder(context))
                }
        }
    }
}

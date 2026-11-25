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

import ai.tock.bot.connector.ConnectorBase
import ai.tock.bot.connector.ConnectorCallback
import ai.tock.bot.connector.ConnectorData
import ai.tock.bot.connector.ConnectorFeature.CAROUSEL
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.media.MediaMessage
import ai.tock.bot.definition.DialogContext
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.definition.StoryStepDef
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.BotRepository
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.ActionNotificationType
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.event.MetadataEvent
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType.bot
import ai.tock.bot.engine.user.UserPreferences
import ai.tock.shared.booleanProperty
import ai.tock.shared.defaultLocale
import ai.tock.shared.http.DeferredHttpResponse
import ai.tock.shared.http.HttpCors
import ai.tock.shared.http.HttpMethod
import ai.tock.shared.http.HttpRequest
import ai.tock.shared.http.HttpResponse
import ai.tock.shared.http.HttpRoutes
import ai.tock.shared.property
import ai.tock.shared.security.auth.spi.HttpWebSecurity
import ai.tock.shared.security.auth.spi.HttpWebSecurityContext
import ai.tock.shared.security.web.HttpWebSecurityCookies
import ai.tock.shared.serialization.readJson
import ai.tock.shared.serialization.writeJson
import com.aallam.openai.api.model.Model
import com.aallam.openai.api.model.ModelId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.util.Locale

internal const val OPEN_AI_CONNECTOR_ID = "openai"

/**
 * The OpenAI connector type.
 */
val openAIConnectorType = ConnectorType(OPEN_AI_CONNECTOR_ID)

private val sseEnabled = booleanProperty("tock_web_sse", false)
internal val supportUnstreamed = booleanProperty("tock_openai_support_unstreamed", false)

class OpenAIConnector internal constructor(
    val connectorId: String,
    val path: String,
    private val security: OpenAIConnectorSecurity,
) : ConnectorBase(openAIConnectorType, setOf(CAROUSEL)) {
    companion object {
        private val logger = KotlinLogging.logger {}
        internal val defaultModel = Model(id = ModelId("tock"))
        internal val corsPattern = property("tock_web_cors_pattern", ".*")
        internal val corsHeaders =
            setOf(
                "Access-Control-Allow-Origin",
                "Content-Type",
                "X-Requested-With",
                "Accept-Language",
                "X-OpenWebUI-Chat-Id",
                "X-OpenWebUI-User-Name",
                "X-OpenWebUI-User-Id",
                "X-OpenWebUI-User-Email",
                "X-OpenWebUI-User-Role",
            )
    }

    override fun register(controller: ConnectorController) {
        when (security) {
            is OpenAIConnectorSecurity.Vertx -> OpenAIVertxServices.register(this, controller, security)
            is OpenAIConnectorSecurity.Http -> controller.registerRoutes(path, routes(controller, security.security))
        }
    }

    /**
     * The routes of the OpenAI API. They answer as the former Vert.x routes did: the CORS rules and the security on
     * every path, the models, and the chat completions in JSON or in server-sent events.
     */
    internal fun routes(
        controller: ConnectorController,
        security: HttpWebSecurity,
    ): HttpRoutes {
        logger.debug("deploy Open API connector services for root path $path ")
        val cors =
            HttpCors(
                corsPattern,
                setOf(HttpMethod.OPTIONS, HttpMethod.GET, HttpMethod.POST),
                corsHeaders,
                // browsers do not send or save cookies unless credentials are allowed
                allowCredentials = security is HttpWebSecurityCookies,
            )
        val securityContext = HttpWebSecurityContext(connectorId, path)

        fun HttpRoutes.secured(
            method: HttpMethod,
            routePath: String,
            handler: suspend (HttpRequest) -> HttpResponse,
        ) = route(method, routePath) { request ->
            cors.handle(request) {
                val authentication =
                    try {
                        withContext(Dispatchers.IO) { security.authenticate(request, securityContext) }
                    } catch (t: Throwable) {
                        logger.error(t) { "the authentication failed" }
                        return@handle HttpResponse.text("Internal Server Error", 500)
                    }
                authentication.setCookies.fold(handler(request)) { response, cookie -> response.withHeader("Set-Cookie", cookie) }
            }
        }

        return HttpRoutes {
            // the preflight requests
            listOf(path, "$path/*").forEach { route(HttpMethod.OPTIONS, it) { request -> cors.handle(request) { HttpResponse.status(404) } } }
            secured(HttpMethod.GET, "$path/health") { HttpResponse.status() }
            secured(HttpMethod.GET, "$path/models") { json(writeJson(listOf(defaultModel))) }
            secured(HttpMethod.GET, "$path/models/:modelId") { json(writeJson(defaultModel)) }
            secured(HttpMethod.POST, "$path/chat/completions") { request ->
                val response = DeferredHttpResponse()
                val body = request.bodyAsString
                if (body.isEmpty()) {
                    logger.warn { "null body for chat completion" }
                    HttpResponse.text("Internal Server Error", 500)
                } else {
                    handleRequest(controller, body, request.header("X-OpenWebUI-Chat-Id"), request.header("Accept-Language"), HttpOpenAIResponder(response))
                    response.await()
                }
            }
        }
    }

    private fun json(body: String) = HttpResponse(headers = mapOf("Content-Type" to listOf("application/json")), body = body.toByteArray())

    /** Handles a chat completion: the answer is sent by [responder]. */
    internal suspend fun handleRequest(
        controller: ConnectorController,
        body: String,
        chatId: String?,
        acceptLanguage: String?,
        responder: OpenAIResponder,
    ) {
        val timerData = BotRepository.requestTimer.start("openai_webhook")
        try {
            logger.debug { "Open AI request input : $body" }
            val request: ChatCompletionRequestWithStream = readJson(body)
            if (request.stream) {
                responder.startStream()
            } else {
                if (!supportUnstreamed) {
                    // TODO Open WebUI send a not streamed system prompt - for now just ignore
                    logger.debug { "ignore unstreamed message" }
                    responder.empty()
                    return
                }
            }
            val locale = acceptLanguage?.let { Locale.forLanguageTag(it) } ?: defaultLocale
            val event = request.toEvent(connectorId, chatId)
            handleEvent(connectorId, locale, event, controller, responder, emptyMap(), DialogContext.EMPTY)
        } catch (t: Throwable) {
            BotRepository.requestTimer.throwable(t, timerData)
            responder.fail(t)
        } finally {
            BotRepository.requestTimer.end(timerData)
        }
    }

    private suspend fun handleEvent(
        applicationId: String,
        locale: Locale,
        event: Event,
        controller: ConnectorController,
        responder: OpenAIResponder?,
        headersMetadata: Map<String, String>,
        transientDialogContext: DialogContext,
    ) {
        val callback =
            OpenAIConnectorCallback(
                applicationId = applicationId,
                locale = locale,
                responder = responder,
                eventId = event.id.toString(),
                streamedResponse = (event as? Action)?.metadata?.streamedResponse == true,
            )
        controller.handleUserEvent(
            event,
            ConnectorData(
                callback = callback,
                metadata = headersMetadata,
                transientContext = transientDialogContext,
            ),
        )
    }

    override suspend fun notify(
        controller: ConnectorController,
        recipientId: PlayerId,
        intent: IntentAware,
        step: StoryStepDef?,
        parameters: Map<String, String>,
        transientContext: DialogContext,
        notificationType: ActionNotificationType?,
        errorListener: (Throwable) -> Unit,
    ) {
        if (!sseEnabled) {
            throw UnsupportedOperationException("OpenAI Connector only supports notifications when SSE is enabled")
        }
        handleEvent(
            applicationId = connectorId,
            locale = defaultLocale,
            event =
                SendChoice(
                    recipientId,
                    connectorId,
                    PlayerId(connectorId, bot),
                    intent.wrappedIntent().name,
                    step,
                    parameters,
                ),
            controller = controller,
            responder = null,
            headersMetadata = emptyMap(),
            transientDialogContext = transientContext,
        )
    }

    override fun send(
        event: Event,
        callback: ConnectorCallback,
        delayInMs: Long,
    ) {
        when (event) {
            is Action -> {
                when (callback) {
                    is OpenAIConnectorCallback -> handleCallback(callback, event)
                    else -> error("unsupported callback: $callback")
                }
            }

            is MetadataEvent -> {
                (callback as? OpenAIConnectorCallback)?.addMetadata(event)
            }

            else -> {
                logger.trace { "unsupported event: $event" }
            }
        }
    }

    private fun handleCallback(
        callback: OpenAIConnectorCallback,
        event: Action,
    ) {
        if (callback.streamedResponse) {
            callback.sendStreamedResponse(event)
        } else {
            callback.addAction(event)
            if (event.metadata.lastAnswer) {
                callback.sendResponse()
            }
        }
    }

    override val persistProfileLoaded: Boolean = booleanProperty("tock_openai_connector_persist_profile", false)

    override fun loadProfile(
        callback: ConnectorCallback,
        userId: PlayerId,
    ): UserPreferences =
        when (callback) {
            is OpenAIConnectorCallback -> UserPreferences().apply { locale = callback.locale }
            else -> UserPreferences()
        }

    override fun addSuggestions(
        text: CharSequence,
        suggestions: List<CharSequence>,
    ): BotBus.() -> ConnectorMessage? =
        {
            OpenAIConnectorMessage(text.toString(), suggestions.map { it.toString() })
        }

    override fun addSuggestions(
        message: ConnectorMessage,
        suggestions: List<CharSequence>,
    ): BotBus.() -> ConnectorMessage? =
        {
            (message as? OpenAIConnectorMessage)?.run { copy(suggestions = this.suggestions + suggestions.map { it.toString() }) }
        }

    override fun toConnectorMessage(message: MediaMessage): BotBus.() -> List<ConnectorMessage> =
        {
            listOf(OpenAIConnectorMessage(mediaMessage = message))
        }
}

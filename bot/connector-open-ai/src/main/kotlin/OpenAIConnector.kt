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
import ai.tock.shared.property
import ai.tock.shared.security.auth.spi.WebSecurityHandler
import ai.tock.shared.serialization.readJson
import ai.tock.shared.serialization.writeJson
import ai.tock.shared.vertx.WebSecurityCookiesHandler
import ai.tock.shared.vertx.setupSSE
import com.aallam.openai.api.model.Model
import com.aallam.openai.api.model.ModelId
import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpMethod.GET
import io.vertx.core.http.HttpMethod.OPTIONS
import io.vertx.core.http.HttpMethod.POST
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.CorsHandler
import mu.KotlinLogging
import java.util.Locale

internal const val OPEN_AI_CONNECTOR_ID = "openai"

/**
 * The OpenAI connector type.
 */
val openAIConnectorType = ConnectorType(OPEN_AI_CONNECTOR_ID)

private val corsPattern = property("tock_web_cors_pattern", ".*")
private val sseEnabled = booleanProperty("tock_web_sse", false)
private val supportUnstreamed = booleanProperty("tock_openai_support_unstreamed", false)

class OpenAIConnector internal constructor(
    val connectorId: String,
    val path: String,
    private val webSecurityHandler: WebSecurityHandler
) : ConnectorBase(openAIConnectorType, setOf(CAROUSEL)) {

    companion object {
        private val logger = KotlinLogging.logger {}
        internal val defaultModel = Model(id = ModelId("tock"))
    }

    override fun register(controller: ConnectorController) {

        controller.registerServices(path) { router ->
            logger.debug("deploy Open API connector services for root path $path ")

            val corsHandler = CorsHandler.create()
                .addOriginWithRegex(corsPattern)
                .allowedMethods(setOf(OPTIONS, GET, POST))
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedHeader("Content-Type")
                .allowedHeader("X-Requested-With")
                .allowedHeader("Accept-Language")
                .allowedHeader("X-OpenWebUI-Chat-Id")
                .allowedHeader("X-OpenWebUI-User-Name")
                .allowedHeader("X-OpenWebUI-User-Id")
                .allowedHeader("X-OpenWebUI-User-Email")
                .allowedHeader("X-OpenWebUI-User-Role")
                // browsers do not send or save cookies unless credentials are allowed
                .allowCredentials(webSecurityHandler is WebSecurityCookiesHandler)

            router
                .route("$path*")
                .handler(corsHandler)
                .handler(webSecurityHandler)

            router.get("$path/health").handler { context ->
                context.response()
                    .end()
            }

            router.get("$path/models").handler { context ->
                val response =
                    listOf(
                        defaultModel,
                    )
                context.response()
                    .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .end(writeJson(response))
            }

            router.get("$path/models/:modelId").handler { context ->
                context.response()
                    .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .end(writeJson(defaultModel))
            }

            router.post("$path/chat/completions").handler { context ->
                val body = context.body()?.asString()
                if (body == null) {
                    logger.warn { "null body for chat completion" }
                    context.fail(500)
                } else {
                    handleRequest(controller, context, body)
                }
            }
        }
    }

    private fun handleRequest(
        controller: ConnectorController,
        context: RoutingContext,
        body: String
    ) {
        val timerData = BotRepository.requestTimer.start("openai_webhook")
        try {
            logger.debug { "Open AI request input : $body" }
            val request: ChatCompletionRequestWithStream = readJson(body)
            if (request.stream) {
                val response = context.response()
                response.setupSSE(addEndHandler = true, keepAlive = false)
            } else {
                if (!supportUnstreamed) {
                    //TODO Open WebUI send a not streamed system prompt - for now just ignore
                    logger.debug { "ignore unstreamed message" }
                    context.end()
                    return
                }
            }
            val chatId = context.request().getHeader("X-OpenWebUI-Chat-Id")
            val locale = context.request().getHeader("Accept-Language")
                ?.let { Locale.forLanguageTag(it) } ?: defaultLocale
            val event = request.toEvent(connectorId, chatId)
            handleEvent(connectorId, locale, event, controller, context, emptyMap())
        } catch (t: Throwable) {
            BotRepository.requestTimer.throwable(t, timerData)
            context.fail(t)
        } finally {
            BotRepository.requestTimer.end(timerData)
        }
    }

    private fun handleEvent(
        applicationId: String,
        locale: Locale,
        event: Event,
        controller: ConnectorController,
        context: RoutingContext?,
        headersMetadata: Map<String, String>,
    ) {
        val callback = OpenAIConnectorCallback(
            applicationId = applicationId,
            locale = locale,
            context = context,
            eventId = event.id.toString(),
            streamedResponse = (event as? Action)?.metadata?.streamedResponse == true
        )
        controller.handle(
            event,
            ConnectorData(
                callback = callback,
                metadata = headersMetadata
            )
        )
    }

    override fun notify(
        controller: ConnectorController,
        recipientId: PlayerId,
        intent: IntentAware,
        step: StoryStepDef?,
        parameters: Map<String, String>,
        notificationType: ActionNotificationType?,
        errorListener: (Throwable) -> Unit
    ) {
        if (!sseEnabled) {
            throw UnsupportedOperationException("OpenAI Connector only supports notifications when SSE is enabled")
        }
        handleEvent(
            applicationId = connectorId,
            locale = defaultLocale,
            event = SendChoice(
                recipientId,
                connectorId,
                PlayerId(connectorId, bot),
                intent.wrappedIntent().name,
                step,
                parameters
            ),
            controller = controller,
            context = null,
            headersMetadata = emptyMap(),
        )
    }

    override fun send(event: Event, callback: ConnectorCallback, delayInMs: Long) {
        when (event) {
            is Action -> {
                when (callback) {
                    is OpenAIConnectorCallback -> handleCallback(callback, event)
                    else -> error("unsupported callback: $callback")
                }
            }

            is MetadataEvent -> (callback as? OpenAIConnectorCallback)?.addMetadata(event)
            else -> {
                logger.trace { "unsupported event: $event" }
            }
        }
    }

    private fun handleCallback(callback: OpenAIConnectorCallback, event: Action) {
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

    override fun loadProfile(callback: ConnectorCallback, userId: PlayerId): UserPreferences {
        return when (callback) {
            is OpenAIConnectorCallback -> UserPreferences().apply { locale = callback.locale }
            else -> UserPreferences()
        }
    }

    override fun addSuggestions(
        text: CharSequence,
        suggestions: List<CharSequence>
    ): BotBus.() -> ConnectorMessage? = {
        OpenAIConnectorMessage(text.toString(), suggestions.map { it.toString() })
    }

    override fun addSuggestions(
        message: ConnectorMessage,
        suggestions: List<CharSequence>
    ): BotBus.() -> ConnectorMessage? = {
        (message as? OpenAIConnectorMessage)?.run { copy(suggestions = this.suggestions + suggestions.map { it.toString() }) }
    }

    override fun toConnectorMessage(message: MediaMessage): BotBus.() -> List<ConnectorMessage> = {
        listOf(OpenAIConnectorMessage(mediaMessage = message))
    }
}

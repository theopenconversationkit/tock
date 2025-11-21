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

import ai.tock.bot.connector.ConnectorBase
import ai.tock.bot.connector.ConnectorCallback
import ai.tock.bot.connector.ConnectorData
import ai.tock.bot.connector.ConnectorFeature.CAROUSEL
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.media.MediaAction
import ai.tock.bot.connector.media.MediaCard
import ai.tock.bot.connector.media.MediaCarousel
import ai.tock.bot.connector.media.MediaMessage
import ai.tock.bot.connector.web.channel.Channels
import ai.tock.bot.connector.web.security.WebSecurityCookiesHandler
import ai.tock.bot.connector.web.send.PostbackButton
import ai.tock.bot.connector.web.send.UrlButton
import ai.tock.bot.connector.web.send.WebCard
import ai.tock.bot.connector.web.send.WebCarousel
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
import ai.tock.bot.engine.user.PlayerType.user
import ai.tock.bot.engine.user.UserPreferences
import ai.tock.bot.orchestration.bot.secondary.OrchestrationCallback
import ai.tock.bot.orchestration.bot.secondary.RestOrchestrationCallback
import ai.tock.bot.orchestration.connector.OrchestrationConnector
import ai.tock.bot.orchestration.connector.OrchestrationHandlers
import ai.tock.bot.orchestration.shared.AskEligibilityToOrchestratedBotRequest
import ai.tock.bot.orchestration.shared.OrchestrationMetaData
import ai.tock.bot.orchestration.shared.ResumeOrchestrationRequest
import ai.tock.bot.orchestration.shared.SecondaryBotEligibilityResponse
import ai.tock.shared.Dice
import ai.tock.shared.booleanProperty
import ai.tock.shared.defaultLocale
import ai.tock.shared.jackson.mapper
import ai.tock.shared.listProperty
import ai.tock.shared.longProperty
import ai.tock.shared.property
import ai.tock.shared.propertyOrNull
import ai.tock.shared.security.auth.spi.TOCK_USER_ID
import ai.tock.shared.security.auth.spi.WebSecurityHandler
import ai.tock.shared.vertx.sendSseMessage
import ai.tock.shared.vertx.sendSsePing
import ai.tock.shared.vertx.setupSSE
import ai.tock.shared.vertx.vertx
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import com.fasterxml.jackson.module.kotlin.readValue
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.CorsHandler
import mu.KotlinLogging
import java.time.Duration
import java.util.Locale
import java.util.UUID

internal const val WEB_CONNECTOR_ID = "web"

/**
 * The web (REST) connector type.
 */
val webConnectorType = ConnectorType(WEB_CONNECTOR_ID)

private val corsPattern = property("tock_web_cors_pattern", ".*")
private val sseEnabled = booleanProperty("tock_web_sse", false)
private val directSseEnabled = booleanProperty("tock_web_direct_sse", false)
private val sseKeepaliveDelay = longProperty("tock_web_sse_keepalive_delay", 10)

private val webConnectorBridgeEnabled = booleanProperty("tock_web_connector_bridge_enabled", false)

val webConnectorExtraHeaders = listProperty("tock_web_connector_extra_headers", emptyList())
val webConnectorUseExtraHeadersAsMetadata: Boolean =
    booleanProperty("tock_web_connector_use_extra_header_as_metadata_request", false)

class WebConnector internal constructor(
    val connectorId: String,
    val path: String,
    private val webSecurityHandler: WebSecurityHandler
) : ConnectorBase(webConnectorType, setOf(CAROUSEL)), OrchestrationConnector {
    @Deprecated("Use the more aptly named connectorId field", ReplaceWith("connectorId"))
    val applicationId: String get() = connectorId

    companion object {
        private val logger = KotlinLogging.logger {}
        private val webMapper = mapper.copy().registerModules(
            SimpleModule().apply {
                // fallback for serializing CharSequence
                addSerializer(CharSequence::class.java, ToStringSerializer())
            }
        )
        private val messageProcessor = WebMessageProcessor(
            processMarkdown = propertyOrNull("tock_web_enable_markdown")?.toBoolean()
            // Fallback to previous property name for backward compatibility
                ?: propertyOrNull("allow_markdown").toBoolean()
        )
        private val channels by lazy { Channels() }

        internal fun HttpServerResponse.sendSseResponse(webConnectorResponse: WebConnectorResponse) =
            sendSseMessage(webMapper.writeValueAsString(webConnectorResponse))

    }

    override fun register(controller: ConnectorController) {

        controller.registerServices(path) { router ->
            logger.debug("deploy web connector services for root path $path ")

            val corsHandler = CorsHandler.create()
                .addOriginWithRegex(corsPattern)
                .allowedMethod(HttpMethod.POST)
                .run {
                    if (sseEnabled || directSseEnabled) allowedMethod(HttpMethod.GET) else this
                }
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedHeader("Content-Type")
                .allowedHeader("X-Requested-With").apply {
                    webConnectorExtraHeaders.forEach {
                        this.allowedHeader(it)
                    }
                }
                // browsers do not send or save cookies unless credentials are allowed
                .allowCredentials(webSecurityHandler is WebSecurityCookiesHandler)

            // Apply CORS Handler for all paths and all methods (OPTIONS handled automatically)
            router.route("$path*").handler(corsHandler)

            if (sseEnabled) {
                router.route("$path/sse")
                    .handler(webSecurityHandler)
                    .handler { context ->
                        try {
                            val userId = context.get<String>(TOCK_USER_ID) ?: context.queryParams()["userId"]
                            val response = context.response()
                            response.setupSSE()
                            val timerId = vertx.setPeriodic(Duration.ofSeconds(sseKeepaliveDelay).toMillis()) {
                                response.sendSsePing()
                            }
                            val channelId = channels.register(connectorId, userId) { webConnectorResponse ->
                                response.sendSseResponse(webConnectorResponse)
                            }
                            response.closeHandler {
                                vertx.cancelTimer(timerId)
                                channels.unregister(channelId)
                            }
                        } catch (t: Throwable) {
                            context.fail(t)
                        }
                    }
            }
            if (directSseEnabled) {
                router.route("$path/sse/direct")
                    .handler { context ->
                        try {
                            val body = context.request().getHeader("message")
                                ?: context.body().asString() ?: error("message is mandatory and is missing")
                            context.response().setupSSE()

                            handleRequest(controller, context, body)
                        } catch (t: Throwable) {
                            context.fail(t)
                        }
                    }
            }

            // Main connector endpoint
            router.post(path)
                .handler(webSecurityHandler)
                .handler { context ->
                    // Override the user on the request body
                    val tockUserId = context.get<String>(TOCK_USER_ID)
                    val body = tockUserId?.let {
                        val jsonBody = context.body().asJsonObject() ?: JsonObject()
                        jsonBody.put("userId", tockUserId)
                        jsonBody.toString()
                    } ?: context.body().asString()

                    // Handle the request
                    handleRequest(controller, context, body)
                }
        }
    }

    override fun getOrchestrationHandlers(): OrchestrationHandlers =
        OrchestrationHandlers(
            eligibilityHandler = this::handleEligibility,
            proxyHandler = this::handleProxy
        )

    private fun handleRequest(
        controller: ConnectorController,
        context: RoutingContext,
        body: String
    ) {
        val timerData = BotRepository.requestTimer.start("web_webhook")
        try {
            logger.debug { "Web request input : $body" }
            val request: WebConnectorRequest = mapper.readValue(body)

            val applicationId =
                if (request.connectorId?.isNotBlank() == true)
                    if (webConnectorBridgeEnabled)
                        request.connectorId.also { logger.debug { "Web bridge: $connectorId -> $it" } }
                    else
                        connectorId.also { logger.warn { "Web bridge disabled." } }
                else
                    connectorId

            val event = request.toEvent(applicationId)
            val requestInfos = WebRequestInfos(context.request())
            WebRequestInfosByEvent.put(event.id.toString(), requestInfos)
            handleEvent(applicationId, request.locale, event, controller, context, extraHeadersAsMetadata(requestInfos))
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
        val callback = WebConnectorCallback(
            applicationId = applicationId,
            locale = locale,
            context = context,
            webMapper = webMapper,
            eventId = event.id.toString(),
            messageProcessor = messageProcessor,
            streamedResponse = (event as? Action)?.metadata?.streamedResponse == true
        )
        if (sseEnabled) {
            // Uniquely identify each response, so they can be reconciliated between SSE and POST
            callback.addMetadata(MetadataEvent.responseId(UUID.randomUUID(), applicationId))
        }
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
            throw UnsupportedOperationException("Web Connector only supports notifications when SSE is enabled")
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

    /**
     * add extra configured Header to Metadata
     * accessible if "tock_web_connector_use_extra_header_as_metadata_request" is true
     * @param requestInfos [WebRequestInfos]
     */
    private fun extraHeadersAsMetadata(requestInfos: WebRequestInfos): Map<String, String> {
        val metaDataExtraHeaders: MutableMap<String, String> = mutableMapOf()
        if (webConnectorUseExtraHeadersAsMetadata) {
            webConnectorExtraHeaders.forEach { header ->
                requestInfos.firstHeader(header)?.let {
                    metaDataExtraHeaders.putIfAbsent(header, it)
                }
            }
        }
        return metaDataExtraHeaders
    }

    private fun handleProxy(
        controller: ConnectorController,
        context: RoutingContext
    ) {
        val timerData = BotRepository.requestTimer.start("web_webhook_orchestred")
        try {
            logger.debug { "Web proxy request input : ${context.body().asString()}" }
            val request: ResumeOrchestrationRequest = mapper.readValue(context.body().asString())
            val callback = RestOrchestrationCallback(
                webConnectorType,
                applicationId = connectorId,
                context = context,
                orchestrationMapper = webMapper
            )

            controller.handle(request.toAction(), ConnectorData(callback))
        } catch (t: Throwable) {
            RestOrchestrationCallback(webConnectorType, connectorId, context = context).sendError()
            BotRepository.requestTimer.throwable(t, timerData)
        } finally {
            BotRepository.requestTimer.end(timerData)
        }
    }

    private fun handleEligibility(
        controller: ConnectorController,
        context: RoutingContext
    ) {
        val timerData = BotRepository.requestTimer.start("web_webhook_support")
        try {
            logger.debug { "Web support request input : ${context.body().asString()}" }
            val request: AskEligibilityToOrchestratedBotRequest = mapper.readValue(context.body().asString())
            val callback = RestOrchestrationCallback(
                webConnectorType,
                connectorId,
                context = context,
                orchestrationMapper = webMapper
            )

            val support = controller.support(request.toAction(connectorId), ConnectorData(callback))
            val sendEligibility = SecondaryBotEligibilityResponse(
                support,
                OrchestrationMetaData(
                    playerId = PlayerId(connectorId, bot),
                    applicationId = connectorId,
                    recipientId = request.metadata?.playerId ?: PlayerId(Dice.newId(), user)
                )
            )
            callback.sendResponse(sendEligibility)
        } catch (t: Throwable) {
            RestOrchestrationCallback(webConnectorType, connectorId, context = context).sendError()
            BotRepository.requestTimer.throwable(t, timerData)
        } finally {
            BotRepository.requestTimer.end(timerData)
        }
    }

    override fun send(event: Event, callback: ConnectorCallback, delayInMs: Long) {
        when (event) {
            is Action -> {
                when (callback) {
                    is WebConnectorCallback -> handleWebConnectorCallback(callback, event)
                    is OrchestrationCallback -> handleOrchestrationCallback(callback, event)
                }
            }

            is MetadataEvent -> (callback as? WebConnectorCallback)?.addMetadata(event)
            else -> {
                logger.trace { "unsupported event: $event" }
            }
        }
    }

    private fun handleWebConnectorCallback(callback: WebConnectorCallback, event: Action) {
        if (callback.streamedResponse) {
            if (event.metadata.lastAnswer) {
                callback.addMetadata(MetadataEvent.lastAnswer(event.applicationId))
            }
            callback.sendStreamedResponse(event)
        } else {
            callback.addAction(event)
            if (sseEnabled) {
                channels.send(event.applicationId, event.recipientId, callback.createResponse(listOf(event)))
            }
            if (event.metadata.lastAnswer) {
                callback.sendResponse()
            }
        }
    }

    private fun handleOrchestrationCallback(callback: OrchestrationCallback, event: Action) {
        callback.actions.add(event)
        if (event.metadata.lastAnswer) {
            callback.sendResponse()
        }
    }

    override val persistProfileLoaded: Boolean = booleanProperty("tock_web_connector_persist_profile", false)

    override fun loadProfile(callback: ConnectorCallback, userId: PlayerId): UserPreferences {
        return when (callback) {
            is WebConnectorCallback -> UserPreferences().apply { locale = callback.locale }
            else -> UserPreferences()
        }
    }

    override fun addSuggestions(text: CharSequence, suggestions: List<CharSequence>): BotBus.() -> ConnectorMessage? =
        { WebMessage(text.toString(), suggestions.map { webNlpQuickReply(it) }) }

    override fun addSuggestions(
        message: ConnectorMessage,
        suggestions: List<CharSequence>
    ): BotBus.() -> ConnectorMessage? = {
        (message as? WebMessage)?.let {
            if (it.card != null && it.card.buttons.isEmpty()) {
                it.copy(card = it.card.copy(buttons = suggestions.map { s -> webNlpQuickReply(s) }))
            } else if (it.card == null && it.buttons.isEmpty()) {
                it.copy(buttons = suggestions.map { s -> webNlpQuickReply(s) })
            } else {
                null
            }
        } ?: message
    }

    override fun toConnectorMessage(message: MediaMessage): BotBus.() -> List<ConnectorMessage> = {
        listOfNotNull(
            when (message) {
                is MediaCard -> {
                    WebMessage(
                        card = WebCard(
                            title = message.title,
                            subTitle = message.subTitle,
                            file = message.file?.toWebMediaFile(),
                            buttons = message.actions.map { button -> button.toButton() }
                        )
                    )
                }

                is MediaCarousel -> {
                    WebMessage(
                        carousel = WebCarousel(
                            message.cards.map { mediaCard ->
                                WebCard(
                                    title = mediaCard.title,
                                    subTitle = mediaCard.subTitle,
                                    file = mediaCard.file?.toWebMediaFile(),
                                    buttons = mediaCard.actions.map { button -> button.toButton() }
                                )
                            }
                        )
                    )
                }

                else -> null
            }
        )
    }

    private fun MediaAction.toButton() =
        if (url == null) {
            PostbackButton(title.toString(), null)
        } else {
            UrlButton(title.toString(), url.toString())
        }
}

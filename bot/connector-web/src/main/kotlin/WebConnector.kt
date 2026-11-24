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
import ai.tock.bot.connector.web.send.PostbackButton
import ai.tock.bot.connector.web.send.UrlButton
import ai.tock.bot.connector.web.send.WebCard
import ai.tock.bot.connector.web.send.WebCarousel
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
import ai.tock.bot.engine.user.UserState
import ai.tock.bot.orchestration.bot.secondary.OrchestrationCallback
import ai.tock.bot.orchestration.connector.OrchestrationConnector
import ai.tock.bot.orchestration.connector.OrchestrationHandlers
import ai.tock.shared.booleanProperty
import ai.tock.shared.defaultLocale
import ai.tock.shared.http.DeferredHttpResponse
import ai.tock.shared.http.HttpCors
import ai.tock.shared.http.HttpMethod
import ai.tock.shared.http.HttpResponse
import ai.tock.shared.http.HttpRoutes
import ai.tock.shared.http.ServerSentEvents
import ai.tock.shared.jackson.mapper
import ai.tock.shared.listProperty
import ai.tock.shared.property
import ai.tock.shared.propertyOrNull
import ai.tock.shared.security.auth.spi.HttpWebSecurity
import ai.tock.shared.security.auth.spi.HttpWebSecurityContext
import ai.tock.shared.security.web.HttpWebSecurityCookies
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.util.Locale
import java.util.UUID

internal const val WEB_CONNECTOR_ID = "web"

/** The key of the transient dialog context in the attributes of the security (WebSecurityHandler.TRANSIENT_DIALOG_CONTEXT_KEY). */
internal const val TRANSIENT_DIALOG_CONTEXT_KEY = "tock_transient_dialog_context"

/**
 * The web (REST) connector type.
 */
val webConnectorType = ConnectorType(WEB_CONNECTOR_ID)

private val webConnectorBridgeEnabled = booleanProperty("tock_web_connector_bridge_enabled", false)

val webConnectorExtraHeaders = listProperty("tock_web_connector_extra_headers", emptyList())
val webConnectorUseExtraHeadersAsMetadata: Boolean =
    booleanProperty("tock_web_connector_use_extra_header_as_metadata_request", false)

class WebConnector internal constructor(
    val connectorId: String,
    val path: String,
    private val security: WebConnectorSecurity,
    internal val publicPath: String,
) : ConnectorBase(webConnectorType, setOf(CAROUSEL)),
    OrchestrationConnector {
    @Deprecated("Use the more aptly named connectorId field", ReplaceWith("connectorId"))
    val applicationId: String get() = connectorId

    companion object {
        const val CONNECTOR_PUBLIC_PATH_CONTEXT_KEY = "tock_connector_public_path"
        const val CONNECTOR_ID_CONTEXT_KEY = "tock_connector_id"

        internal const val WILDCARD_CORS_PATTERN = ".*"
        internal const val CORS_PATTERN_PROPERTY = "tock_web_cors_pattern"

        internal val corsPattern = property(CORS_PATTERN_PROPERTY, WILDCARD_CORS_PATTERN)
        internal val sseEnabled = booleanProperty("tock_web_sse", false)
        internal val directSseEnabled = booleanProperty("tock_web_direct_sse", false)

        private val logger = KotlinLogging.logger {}
        private val messageProcessor =
            WebMessageProcessor(
                processMarkdown =
                    propertyOrNull("tock_web_enable_markdown")?.toBoolean()
                        // Fallback to previous property name for backward compatibility
                        ?: propertyOrNull("allow_markdown").toBoolean(),
            )

        /** The mapper of the responses (SseEndpoint.webMapper, without loading the Vert.x endpoint). */
        internal val webMapper: ObjectMapper =
            mapper.copy().registerModules(
                SimpleModule().apply {
                    // fallback for serializing CharSequence
                    addSerializer(CharSequence::class.java, ToStringSerializer())
                },
            )
    }

    override fun register(controller: ConnectorController) {
        when (security) {
            is WebConnectorSecurity.Vertx -> WebVertxServices.register(this, controller, security)
            is WebConnectorSecurity.Http -> controller.registerRoutes(path, routes(controller, security.security))
        }
    }

    /**
     * The routes of the connector. They answer as the former Vert.x routes did: the CORS rules on every path, the
     * JSON answer of the bot, its server-sent events on `<path>/sse/direct` (`tock_web_direct_sse`), the cookies of
     * the security, and 500 when the request cannot be handled.
     */
    internal fun routes(
        controller: ConnectorController,
        security: HttpWebSecurity,
    ): HttpRoutes {
        val cookies = security is HttpWebSecurityCookies
        if (cookies && corsPattern == WILDCARD_CORS_PATTERN) {
            logger.warn { "CORS config is allowing any website, requests are susceptible to CSRF!! Please set $CORS_PATTERN_PROPERTY to match only the domains you trust." }
        }
        val cors =
            HttpCors(
                corsPattern,
                setOfNotNull(HttpMethod.POST, HttpMethod.GET.takeIf { directSseEnabled }),
                setOf("Access-Control-Allow-Origin", "Content-Type", "X-Requested-With") + webConnectorExtraHeaders,
                // browsers do not send or save cookies unless credentials are allowed
                allowCredentials = cookies,
            )
        val securityContext = HttpWebSecurityContext(connectorId, publicPath)
        return HttpRoutes {
            // the preflight requests
            listOf(path, "$path/*").forEach { route(HttpMethod.OPTIONS, it) { request -> cors.handle(request) { HttpResponse.status(404) } } }

            if (directSseEnabled) {
                listOf(HttpMethod.GET, HttpMethod.POST).forEach { method ->
                    route(method, "$path/sse/direct") { request ->
                        cors.handle(request) {
                            val body = request.header("message") ?: request.bodyAsString.ifEmpty { null }
                            if (body == null) {
                                logger.error { "message is mandatory and is missing" }
                                HttpResponse.text("Internal Server Error", 500)
                            } else {
                                val response = DeferredHttpResponse()
                                ServerSentEvents.start(response, keepAlive = true)
                                handleRequest(controller, WebRequestInfos.of(request), body, DialogContext.EMPTY, HttpWebResponder(response))
                                response.await()
                            }
                        }
                    }
                }
            }

            // Main connector endpoint
            post(path) { request ->
                cors.handle(request) {
                    val authentication =
                        try {
                            withContext(Dispatchers.IO) { security.authenticate(request, securityContext) }
                        } catch (t: Throwable) {
                            logger.error(t) { "the authentication failed" }
                            return@handle HttpResponse.text("Internal Server Error", 500)
                        }
                    val transientContext = authentication.attributes[TRANSIENT_DIALOG_CONTEXT_KEY] as? DialogContext ?: DialogContext.EMPTY
                    // Override the user on the request body
                    val body =
                        authentication.userId?.let { userId ->
                            val json = runCatching { mapper.readTree(request.body) as? ObjectNode }.getOrNull() ?: mapper.createObjectNode()
                            json.put("userId", userId).toString()
                        } ?: request.bodyAsString
                    val response = DeferredHttpResponse()
                    handleRequest(controller, WebRequestInfos.of(request), body, transientContext, HttpWebResponder(response))
                    authentication.setCookies.fold(response.await()) { answer, cookie -> answer.withHeader("Set-Cookie", cookie) }
                }
            }
        }
    }

    override fun getOrchestrationHandlers(): OrchestrationHandlers = WebOrchestration.handlers(this)

    /** Handles a web request: the answer is sent by [responder]. */
    internal suspend fun handleRequest(
        controller: ConnectorController,
        requestInfos: WebRequestInfos,
        body: String,
        transientContext: DialogContext,
        responder: WebResponder,
    ) {
        val timerData = BotRepository.requestTimer.start("web_webhook")
        try {
            logger.debug { "Web request input : $body" }
            val request: WebConnectorRequest = mapper.readValue(body)

            val applicationId =
                if (request.connectorId?.isNotBlank() == true) {
                    if (webConnectorBridgeEnabled) {
                        request.connectorId.also { logger.debug { "Web bridge: $connectorId -> $it" } }
                    } else {
                        connectorId.also { logger.warn { "Web bridge disabled." } }
                    }
                } else {
                    connectorId
                }

            val event = request.toEvent(applicationId)
            WebRequestInfosByEvent.put(event.id.toString(), requestInfos)
            handleEvent(applicationId, request.locale, event, controller, responder, extraHeadersAsMetadata(requestInfos), transientDialogContext = transientContext, errorListener = null)
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
        responder: WebResponder?,
        headersMetadata: Map<String, String>,
        errorListener: ((Throwable) -> Unit)?,
        transientDialogContext: DialogContext,
    ) {
        val callback =
            WebConnectorCallback(
                applicationId = applicationId,
                errorListener = errorListener,
                locale = locale,
                responder = responder,
                webMapper = webMapper,
                eventId = event.id.toString(),
                messageProcessor = messageProcessor,
                streamedResponse = (event as? Action)?.metadata?.streamedResponse == true,
            )
        if (sseEnabled) {
            // Uniquely identify each response, so they can be reconciled between SSE and POST
            callback.addMetadata(MetadataEvent.responseId(UUID.randomUUID(), applicationId))
        }
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
            throw UnsupportedOperationException("Web Connector only supports notifications when SSE is enabled")
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
            errorListener = errorListener,
            transientDialogContext = transientContext,
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

    override fun send(
        event: Event,
        callback: ConnectorCallback,
        delayInMs: Long,
    ) {
        when (event) {
            is Action -> {
                when (callback) {
                    is WebConnectorCallback -> handleWebConnectorCallback(callback, event)
                    is OrchestrationCallback -> handleOrchestrationCallback(callback, event)
                }
            }

            is MetadataEvent -> {
                (callback as? WebConnectorCallback)?.addMetadata(event)
            }

            else -> {
                logger.trace { "unsupported event: $event" }
            }
        }
    }

    private fun handleWebConnectorCallback(
        callback: WebConnectorCallback,
        event: Action,
    ) {
        if (callback.streamedResponse) {
            if (event.metadata.lastAnswer) {
                callback.addMetadata(MetadataEvent.lastAnswer(event.connectorId))
            }
            callback.sendStreamedResponse(event)
        } else {
            callback.addAction(event)
            if (sseEnabled) {
                WebVertxServices.sendSse(event.connectorId, event.recipientId.id, callback.createResponse(listOf(event)))
            }
            if (event.metadata.lastAnswer) {
                callback.sendResponse()
            }
        }
    }

    private fun handleOrchestrationCallback(
        callback: OrchestrationCallback,
        event: Action,
    ) {
        callback.actions.add(event)
        if (event.metadata.lastAnswer) {
            callback.sendResponse()
        }
    }

    override val persistProfileLoaded: Boolean = booleanProperty("tock_web_connector_persist_profile", false)

    override fun loadProfile(
        callback: ConnectorCallback,
        userId: PlayerId,
    ): UserPreferences =
        when (callback) {
            is WebConnectorCallback -> UserPreferences().apply { locale = callback.locale }
            else -> UserPreferences()
        }

    override fun shouldRefreshProfile(
        profile: UserPreferences,
        state: UserState,
        connectorData: ConnectorData,
    ): Boolean {
        (connectorData.callback as? WebConnectorCallback)?.let {
            if (it.locale != profile.locale) {
                return true
            }
        }
        return super.shouldRefreshProfile(profile, state, connectorData)
    }

    override fun refreshProfile(
        callback: ConnectorCallback,
        userId: PlayerId,
    ): UserPreferences? =
        when (callback) {
            is WebConnectorCallback -> UserPreferences(locale = callback.locale)
            else -> super.refreshProfile(callback, userId)
        }

    override fun addSuggestions(
        text: CharSequence,
        suggestions: List<CharSequence>,
    ): BotBus.() -> ConnectorMessage? = { WebMessage(text.toString(), suggestions.map { webNlpQuickReply(it) }) }

    override fun addSuggestions(
        message: ConnectorMessage,
        suggestions: List<CharSequence>,
    ): BotBus.() -> ConnectorMessage? =
        {
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

    override fun toConnectorMessage(message: MediaMessage): BotBus.() -> List<ConnectorMessage> =
        {
            listOfNotNull(
                when (message) {
                    is MediaCard -> {
                        WebMessage(
                            card =
                                WebCard(
                                    title = message.title,
                                    subTitle = message.subTitle,
                                    file = message.file?.toWebMediaFile(),
                                    buttons = message.actions.map { button -> button.toButton() },
                                ),
                        )
                    }

                    is MediaCarousel -> {
                        WebMessage(
                            carousel =
                                WebCarousel(
                                    message.cards.map { mediaCard ->
                                        WebCard(
                                            title = mediaCard.title,
                                            subTitle = mediaCard.subTitle,
                                            file = mediaCard.file?.toWebMediaFile(),
                                            buttons = mediaCard.actions.map { button -> button.toButton() },
                                        )
                                    },
                                ),
                        )
                    }

                    else -> {
                        null
                    }
                },
            )
        }

    private fun MediaAction.toButton() =
        if (url == null) {
            PostbackButton(title.toString(), null)
        } else {
            UrlButton(title.toString(), url.toString())
        }
}

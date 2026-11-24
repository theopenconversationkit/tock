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

package ai.tock.bot.connector.iadvize

import ai.tock.bot.connector.ConnectorBase
import ai.tock.bot.connector.ConnectorCallback
import ai.tock.bot.connector.ConnectorData
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorQueue
import ai.tock.bot.connector.iadvize.model.payload.TextPayload
import ai.tock.bot.connector.iadvize.model.request.ConversationsRequest
import ai.tock.bot.connector.iadvize.model.request.IadvizeRequest
import ai.tock.bot.connector.iadvize.model.request.MessageRequest
import ai.tock.bot.connector.iadvize.model.request.MessageRequest.MessageRequestJson
import ai.tock.bot.connector.iadvize.model.request.TypeMessage
import ai.tock.bot.connector.iadvize.model.request.UnsupportedRequest
import ai.tock.bot.connector.iadvize.model.request.UnsupportedRequest.UnsupportedRequestJson
import ai.tock.bot.connector.iadvize.model.response.AvailabilityStrategies
import ai.tock.bot.connector.iadvize.model.response.AvailabilityStrategies.Strategy.customAvailability
import ai.tock.bot.connector.iadvize.model.response.Bot
import ai.tock.bot.connector.iadvize.model.response.BotUpdated
import ai.tock.bot.connector.iadvize.model.response.Healthcheck
import ai.tock.bot.connector.iadvize.model.response.conversation.QuickReply
import ai.tock.bot.connector.iadvize.model.response.conversation.RepliesResponse
import ai.tock.bot.connector.iadvize.model.response.conversation.reply.IadvizeMessage
import ai.tock.bot.connector.iadvize.model.response.conversation.reply.IadvizeReply
import ai.tock.bot.connector.iadvize.model.response.conversation.reply.IadvizeTransfer
import ai.tock.bot.connector.media.MediaMessage
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.I18nTranslator
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.action.SendSentenceWithFootnotes
import ai.tock.bot.engine.event.Event
import ai.tock.iadvize.client.graphql.ChatbotActionOrMessageInput
import ai.tock.iadvize.client.graphql.ChatbotMessageInput
import ai.tock.iadvize.client.graphql.IadvizeGraphQLClient
import ai.tock.shared.Executor
import ai.tock.shared.defaultLocale
import ai.tock.shared.error
import ai.tock.shared.http.HttpMethod
import ai.tock.shared.http.HttpRequest
import ai.tock.shared.http.HttpResponse
import ai.tock.shared.http.HttpRoutes
import ai.tock.shared.injector
import ai.tock.shared.jackson.mapper
import ai.tock.shared.propertyOrNull
import com.fasterxml.jackson.annotation.JsonInclude
import com.github.salomonbrys.kodein.instance
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.apache.commons.lang3.LocaleUtils
import java.time.LocalDateTime
import java.util.Locale

private const val QUERY_ID_OPERATOR: String = "idOperator"
private const val QUERY_ID_CONVERSATION: String = "idConversation"
private const val TYPE_TEXT: String = "text"
private const val ROLE_OPERATOR: String = "operator"

// This is related to and iAdvize issue DERCBOT-850, iAdvize ticket number 119048.
// RAG responses are proactive, very time-consuming, we don't have UX for that
// So we're compensating with this message. It will be removed when the iAdvize problem is solved.
private const val PROPERTY_TOCK_BOT_API_PROACTIVE_START_MESSAGE = "tock_bot_api_proactive_start_message"
private val proactiveStartMessage: String? = propertyOrNull(PROPERTY_TOCK_BOT_API_PROACTIVE_START_MESSAGE)

/**
 *
 */
class IadvizeConnector internal constructor(
    val applicationId: String,
    val path: String,
    val editorUrl: String,
    val firstMessage: String,
    val distributionRule: String?,
    val secretToken: String?,
    val distributionRuleUnavailableMessage: String,
    val localeCode: String?,
) : ConnectorBase(IadvizeConnectorProvider.connectorType) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    val locale: Locale = localeCode?.let { getLocale(localeCode) } ?: defaultLocale

    private val executor: Executor by injector.instance()
    private val queue: ConnectorQueue = ConnectorQueue(executor)
    private var proactiveAnswerEnabled: Boolean = false
    private var proactiveParameters: Map<String, String> = emptyMap()

    override fun register(controller: ConnectorController) {
        controller.registerRoutes(path, routes(controller))
    }

    /**
     * The routes of the iAdvize bot API. They answer as the former Vert.x handlers did: the JSON of the answer,
     * 400 for an invalid signature, and 500 without an explicit message when the handler fails, so as not to
     * expose vulnerabilities.
     */
    internal fun routes(controller: ConnectorController): HttpRoutes =
        HttpRoutes {
            iadvizeRoute(HttpMethod.GET, "$path/external-bots", controller, handlerGetBots)
            iadvizeRoute(HttpMethod.GET, "$path/bots/:idOperator", controller, handlerGetBot)
            iadvizeRoute(HttpMethod.PUT, "$path/bots/:idOperator", controller, handlerUpdateBot)
            iadvizeRoute(HttpMethod.GET, "$path/availability-strategies", controller, handlerStrategies)
            iadvizeRoute(HttpMethod.GET, "$path/bots/:idOperator/conversation-first-messages", controller, handlerFirstMessage)
            iadvizeRoute(HttpMethod.POST, "$path/conversations", controller, handlerStartConversation)
            iadvizeRoute(HttpMethod.POST, "$path/conversations/:idConversation/messages", controller, handlerConversation)
            iadvizeRoute(HttpMethod.GET, "$path/healthcheck", controller, handlerHealthcheck)
        }

    private fun HttpRoutes.iadvizeRoute(
        method: HttpMethod,
        routePath: String,
        controller: ConnectorController,
        iadvizeHandler: IadvizeHttpHandler,
    ) {
        route(method, routePath) { request ->
            val response = CompletableDeferred<HttpResponse>()
            val responder =
                object : IadvizeResponder {
                    override fun json(response: Any) {
                        completeJson(response)
                    }

                    private fun completeJson(body: Any) {
                        response.complete(HttpResponse(headers = mapOf("Content-Type" to listOf("application/json")), body = iadvizeJson(body).toByteArray()))
                    }

                    override fun end() {
                        response.complete(HttpResponse.status())
                    }

                    override fun fail(throwable: Throwable) {
                        logger.error(throwable)
                        response.complete(HttpResponse.text("Internal Server Error", 500))
                    }
                }
            try {
                logRequest(request)
                // Check payloads signature
                if (!secretToken.isNullOrBlank()) {
                    IadvizeSecurity(secretToken).validatePayloads(request)
                }
                // the handlers are blocking: not on the event loop of a Vert.x server
                withContext(Dispatchers.IO) { iadvizeHandler(IadvizeExchange(request, responder), controller) }
            } catch (error: IadvizeSignatureException) {
                logger.error(error)
                response.complete(HttpResponse.text("Bad Request", 400))
            } catch (error: Throwable) {
                logger.error(error)
                response.complete(HttpResponse.text("Internal Server Error", 500))
            }
            response.await()
        }
    }

    /**
     * Trace the iadvize query
     */
    private fun logRequest(request: HttpRequest) {
        logger.debug {
            val body = runCatching { mapper.readTree(request.body) }.getOrNull()
            "request : " +
                mapper.writeValueAsString(
                    mapOf(
                        "method" to request.method.name,
                        "path" to request.path,
                        "query" to request.rawQuery,
                        "body" to body,
                        // Get only iAdvize headers
                        "headers" to request.headers.filterKeys { it.startsWith("X-") }.mapValues { it.value.firstOrNull() },
                    ),
                )
        }
    }

    private var handlerHealthcheck: IadvizeHttpHandler = { exchange, _ ->
        exchange.responder.json(Healthcheck())
    }

    internal var handlerGetBots: IadvizeHttpHandler = { exchange, controller ->
        exchange.responder.json(listOf(getBot(controller)))
    }

    internal var handlerGetBot: IadvizeHttpHandler = { exchange, controller ->
        val idOperator: String = checkNotNull(exchange.request.pathParameter(QUERY_ID_OPERATOR))
        exchange.responder.json(getBotUpdate(idOperator, controller))
    }

    internal var handlerUpdateBot: IadvizeHttpHandler = { exchange, controller ->
        val idOperator: String = checkNotNull(exchange.request.pathParameter(QUERY_ID_OPERATOR))
        exchange.responder.json(getBotUpdate(idOperator, controller))
    }

    private fun getBotUpdate(
        idOperator: String,
        controller: ConnectorController,
    ): BotUpdated = BotUpdated(idOperator, getBot(controller), LocalDateTime.now(), LocalDateTime.now())

    private fun getBot(controller: ConnectorController): Bot {
        val botId: String = controller.botDefinition.botId
        val botName: String = controller.botConfiguration.name
        return Bot(idBot = botId, name = botName, editorUrl = editorUrl)
    }

    internal var handlerStrategies: IadvizeHttpHandler = { exchange, _ ->
        exchange.responder.json(listOf(AvailabilityStrategies(strategy = customAvailability, availability = true)))
    }

    internal var handlerFirstMessage: IadvizeHttpHandler = { exchange, controller ->
        val translator: I18nTranslator = controller.botDefinition.i18nTranslator(locale, iadvizeConnectorType)
        exchange.responder.json(
            RepliesResponse(
                IadvizeMessage(
                    translator.translate(firstMessage as CharSequence).toString(),
                ),
            ),
        )
    }

    internal var handlerStartConversation: IadvizeHttpHandler = { exchange, controller ->

        val conversationRequest: ConversationsRequest =
            mapper.readValue(exchange.request.bodyAsString, ConversationsRequest::class.java)

        val callback =
            IadvizeConnectorCallback(
                applicationId,
                controller,
                localeCode?.let { getLocale(localeCode) } ?: defaultLocale,
                exchange.responder,
                conversationRequest,
                distributionRule,
                distributionRuleUnavailableMessage,
            )
        callback.answerWithResponse()
    }

    internal var handlerConversation: IadvizeHttpHandler = { exchange, controller ->
        val idConversation: String = checkNotNull(exchange.request.pathParameter(QUERY_ID_CONVERSATION))
        val iadvizeRequest: IadvizeRequest = mapRequest(idConversation, exchange.request.bodyAsString)
        if (!isOperator(iadvizeRequest)) {
            handleRequest(controller, exchange.responder, iadvizeRequest)
        } else {
            // ignore message from operator
            exchange.responder.end()
        }
    }

    /*
     * If request is a MessageRequest and the author of message have role "operator" : do not treat request.
     * in many case it's an echo, but it can be a human operator
     */
    private fun isOperator(iadvizeRequest: IadvizeRequest): Boolean =
        iadvizeRequest is MessageRequest &&
            iadvizeRequest.message.author.role == ROLE_OPERATOR

    private fun mapRequest(
        idConversation: String,
        body: String,
    ): IadvizeRequest {
        val typeMessage: TypeMessage = mapper.readValue(body, TypeMessage::class.java)
        return when (typeMessage.type) {
            // json doesn't contain idConversation, to prevent null pointer,
            // we use the inner class MessageRequestJson to enhance the json.
            TYPE_TEXT -> {
                val messageRequestJson: MessageRequestJson =
                    mapper.readValue(body, MessageRequestJson::class.java)
                MessageRequest(messageRequestJson, idConversation)
            }

            else -> {
                val unsupportedRequestJson: UnsupportedRequestJson =
                    mapper.readValue(body, UnsupportedRequestJson::class.java)
                UnsupportedRequest(unsupportedRequestJson, idConversation, typeMessage.type)
            }
        }
    }

    override fun send(
        event: Event,
        callback: ConnectorCallback,
        delayInMs: Long,
    ) {
        val iadvizeCallback = callback as? IadvizeConnectorCallback
        iadvizeCallback?.addAction(event, delayInMs)

        // Send message proactively if this mode is already started
        if (proactiveAnswerEnabled) {
            flushProactiveConversation(callback, proactiveParameters)
        } else {
            if (event is Action && event.metadata.lastAnswer) {
                iadvizeCallback?.answerWithResponse()
            }
        }
    }

    override fun startProactiveConversation(
        callback: ConnectorCallback,
        botBus: BotBus,
    ): Boolean {
        // Set proactive answer mode, and save parameters
        proactiveAnswerEnabled = true
        proactiveParameters = botBus.connectorData.metadata

        if (!proactiveStartMessage.isNullOrBlank()) {
            // Send a RAG start message
            botBus.send(proactiveStartMessage)
        }
        (callback as? IadvizeConnectorCallback)?.answerWithResponse()
        return true
    }

    override fun flushProactiveConversation(
        callback: ConnectorCallback,
        parameters: Map<String, String>,
    ) {
        val iadvizeCallback = callback as? IadvizeConnectorCallback
        iadvizeCallback?.actions?.forEach {
            queue.add(it.action, it.delayInMs) { action ->
                sendProactiveMessage(iadvizeCallback, action, parameters)
            }
        }
        iadvizeCallback?.actions?.clear()
    }

    /**
     * Send [Action] using GraphQL
     * @param callback the [IadvizeConnectorCallback]
     * @param action the action to send
     * @param parameters the key value map of parameters
     */
    private fun sendProactiveMessage(
        callback: IadvizeConnectorCallback,
        action: Action,
        parameters: Map<String, String>,
    ) {
        when (action) {
            is SendSentenceWithFootnotes -> {
                action.sendByGraphQL(parameters)
            }

            is SendSentence -> {
                if (action.messages.isEmpty()) {
                    action.text?.let {
                        // Simple message
                        IadvizeMessage(TextPayload(it)).sendByGraphQL(
                            parameters,
                            callback,
                        )
                    }
                } else {
                    // Complex message
                    action.messages
                        .filterIsInstance<IadvizeConnectorMessage>()
                        .flatMap { it.replies }
                        .map { it.sendByGraphQL(parameters, callback) }
                }
            }
        }
    }

    override fun endProactiveConversation(
        callback: ConnectorCallback,
        parameters: Map<String, String>,
    ) {
        flushProactiveConversation(callback, parameters)
        // Turn off the proactive answer mode
        proactiveAnswerEnabled = false
    }

    /**
     * Format the notification RAG message when active
     * default connector without format
     * https://docs.iadvize.dev/technologies/bots#customize-replies-with-markdown
     */
    private fun SendSentenceWithFootnotes.toMarkdown(): String {
        var counter = 1
        val sources =
            footnotes.joinToString(", ") { footnote ->
                footnote.url?.let {
                    "[${counter++}]($it)"
                } ?: footnote.title
            }

        // Add sources if footnotes are not empty
        return if (footnotes.isEmpty()) {
            text.toString()
        } else {
            "$text\n\n\n*Sources: $sources*"
        }
    }

    /**
     * Send [SendSentenceWithFootnotes] markdown using GraphQL
     * @param parameters the key value map of parameters
     */
    private fun SendSentenceWithFootnotes.sendByGraphQL(parameters: Map<String, String>) {
        IadvizeGraphQLClient().sendProactiveActionOrMessage(
            parameters.conversationId(),
            parameters.chatBotId(),
            actionOrMessage =
                ChatbotActionOrMessageInput(
                    chatbotMessage =
                        ChatbotMessageInput(
                            chatbotSimpleTextMessage = this.toMarkdown(),
                        ),
                ),
        )
    }

    /**
     * Send [IadvizeReply] using GraphQL
     * @param parameters the key value map of parameters
     * @param callback the [IadvizeConnectorCallback]
     */
    private fun IadvizeReply.sendByGraphQL(
        parameters: Map<String, String>,
        callback: IadvizeConnectorCallback,
    ) {
        val actionOrMessage =
            when (this) {
                is IadvizeTransfer -> {
                    // Check if a rule is available for distribution
                    val response = callback.addDistributionRulesOnTransfer(this)
                    if (response is IadvizeTransfer) {
                        response.toChatBotActionOrMessageInput()
                    } else {
                        // If the distribution rule is not available, send the configured message when
                        ChatbotActionOrMessageInput(
                            chatbotMessage =
                                ChatbotMessageInput(
                                    chatbotSimpleTextMessage = distributionRuleUnavailableMessage,
                                ),
                        )
                    }
                }

                else -> {
                    this.toChatBotActionOrMessageInput()
                }
            }

        // Send a proactive action or message
        IadvizeGraphQLClient().sendProactiveActionOrMessage(
            parameters.conversationId(),
            parameters.chatBotId(),
            actionOrMessage = actionOrMessage,
        )
    }

    internal fun handleRequest(
        controller: ConnectorController,
        responder: IadvizeResponder,
        iadvizeRequest: IadvizeRequest,
    ) {
        val callback =
            IadvizeConnectorCallback(
                applicationId,
                controller,
                localeCode?.let { getLocale(localeCode) } ?: defaultLocale,
                responder,
                iadvizeRequest,
                distributionRule,
                distributionRuleUnavailableMessage,
            )

        when (iadvizeRequest) {
            is MessageRequest -> {
                val event = WebhookActionConverter.toEvent(iadvizeRequest, applicationId)
                controller.handle(
                    event,
                    ConnectorData(
                        callback,
                        metadata =
                            mapOf(
                                IadvizeConnectorMetadata.CONVERSATION_ID.name to iadvizeRequest.idConversation,
                                IadvizeConnectorMetadata.OPERATOR_ID.name to iadvizeRequest.idOperator,
                                // iAdvize environment sd- or ha-
                                IadvizeConnectorMetadata.IADVIZE_ENV.name to iadvizeRequest.idOperator.split("-")[0],
                                // the operator id (=chatbotId) prefixed with the iAdvize environment
                                IadvizeConnectorMetadata.CHAT_BOT_ID.name to (iadvizeRequest.idOperator.split("-").getOrNull(1) ?: "unknown"),
                            ),
                    ),
                )
            }

            // Only MessageRequest are supported, other messages are UnsupportedMessage
            // and UnsupportedResponse can be sent immediately
            else -> {
                callback.answerWithResponse()
            }
        }
    }

    override fun addSuggestions(
        text: CharSequence,
        suggestions: List<CharSequence>,
    ): BotBus.() -> ConnectorMessage? = MediaConverter.toSimpleMessage(text, suggestions)

    override fun addSuggestions(
        message: ConnectorMessage,
        suggestions: List<CharSequence>,
    ): BotBus.() -> ConnectorMessage? =
        {
            (message as? IadvizeConnectorMessage)?.let {
                val iadvizeMessage = message.replies.last { it is IadvizeMessage } as IadvizeMessage
                iadvizeMessage.quickReplies.addAll(suggestions.map { QuickReply(translate(it).toString()) })
            }
            message
        }

    override fun toConnectorMessage(message: MediaMessage): BotBus.() -> List<ConnectorMessage> = MediaConverter.toConnectorMessage(message)

    private fun getLocale(it: String): Locale? =
        try {
            LocaleUtils.toLocale(it)
        } catch (e: Exception) {
            logger.error(e)
            null
        }
}

@JsonInclude(JsonInclude.Include.ALWAYS)
data class CustomRequest(
    val method: String,
    val path: String?,
    val query: String?,
    val body: JsonObject?,
    val headers: Map<String, String>,
)

private fun Map<String, String>.conversationId(): String = requireNotNull(this[IadvizeConnectorMetadata.CONVERSATION_ID.name]) { "missing iAdvize conversation id" }

private fun Map<String, String>.chatBotId(): Int = requireNotNull(this[IadvizeConnectorMetadata.CHAT_BOT_ID.name]?.toInt()) { "missing iAdvize chat bot id" }

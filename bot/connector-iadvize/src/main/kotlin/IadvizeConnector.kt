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
import ai.tock.bot.engine.user.PlayerId
import ai.tock.iadvize.client.graphql.ChatbotActionOrMessageInput
import ai.tock.iadvize.client.graphql.ChatbotMessageInput
import ai.tock.iadvize.client.graphql.IadvizeGraphQLClient
import ai.tock.shared.Executor
import ai.tock.shared.defaultLocale
import ai.tock.shared.error
import ai.tock.shared.exception.rest.BadRequestException
import ai.tock.shared.injector
import ai.tock.shared.jackson.mapper
import ai.tock.shared.propertyOrNull
import com.fasterxml.jackson.annotation.JsonInclude
import com.github.salomonbrys.kodein.instance
import io.vertx.core.Future
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging
import org.apache.commons.lang3.LocaleUtils
import java.time.LocalDateTime
import java.util.Locale

private const val QUERY_ID_OPERATOR: String = "idOperator"
private const val QUERY_ID_CONVERSATION: String = "idConversation"
private const val TYPE_TEXT: String = "text"
private const val ROLE_OPERATOR: String = "operator"

// Configurable error message for deferred mode timeout/error
private const val PROPERTY_TOCK_BOT_API_DEFERRED_ERROR_MESSAGE = "tock_bot_api_deferred_error_message"
private const val DEFAULT_DEFERRED_ERROR_MESSAGE = "Sorry, an error occurred while processing your request."
private val deferredErrorMessage: String = propertyOrNull(PROPERTY_TOCK_BOT_API_DEFERRED_ERROR_MESSAGE) ?: DEFAULT_DEFERRED_ERROR_MESSAGE

/**
 * iAdvize connector with support for deferred messaging.
 *
 * Implements [DeferredConnector] to support asynchronous RAG responses:
 * - Sends HTTP 200 immediately upon receiving a request
 * - Processes the request asynchronously
 * - Sends responses via GraphQL API when ready
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
) : ConnectorBase(IadvizeConnectorProvider.connectorType), DeferredConnector {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    val locale: Locale = localeCode?.let { getLocale(localeCode) } ?: defaultLocale

    private val executor: Executor by injector.instance()
    private val queue: ConnectorQueue = ConnectorQueue(executor)

    override fun register(controller: ConnectorController) {
        controller.registerServices(path) { router ->
            router.get("$path/external-bots")
                .handleAndCatchException(controller, handlerGetBots)

            router.get("$path/bots/:idOperator")
                .handleAndCatchException(controller, handlerGetBot)

            router.put("$path/bots/:idOperator")
                .handleAndCatchException(controller, handlerUpdateBot)

            router.get("$path/availability-strategies")
                .handleAndCatchException(controller, handlerStrategies)

            router.get("$path/bots/:idOperator/conversation-first-messages")
                .handleAndCatchException(controller, handlerFirstMessage)

            router.post("$path/conversations")
                .handleAndCatchException(controller, handlerStartConversation)

            router.post("$path/conversations/:idConversation/messages")
                .handleAndCatchException(controller, handlerConversation)

            router.get("$path/healthcheck")
                .handleAndCatchException(controller, handlerHealthcheck)
        }
    }

    /*
     * when an exception is produced during the processing of the handler, it must be intercepted, logged,
     *  then produce an error 500 without an explicit message to not expose vulnerabilities.
     *
     * iAdvizeHandler is wrapped in a FunctionalInterface Handler<RoutingContext>
     *  and provided to the Route.handler(...) method
     */
    private fun Route.handleAndCatchException(
        controller: ConnectorController,
        iadvizeHandler: IadvizeHandler,
    ) {
        handler { context ->
            try {
                logContextRequest(context)

                // Check payloads signature
                if (!secretToken.isNullOrBlank()) {
                    IadvizeSecurity(secretToken).validatePayloads(context)
                }
                // Invoke handler
                iadvizeHandler.invoke(context, controller)
            } catch (error: BadRequestException) {
                logger.error(error)
                context.fail(400)
            } catch (error: Throwable) {
                logger.error(error)
                context.fail(500)
            }
        }
    }

    /**
     * Trace the iadvize query
     */
    private fun logContextRequest(context: RoutingContext) {
        val requestAsString: String =
            with(context) {
                mapper.writeValueAsString(
                    CustomRequest(
                        request().method().name(),
                        request().path(),
                        request().query(),
                        body().asJsonObject(),
                        // Get only iAdvize headers
                        request().headers()
                            .filter { it.key.startsWith("X-") }
                            .associate { it.key to it.value },
                    ),
                )
            }

        logger.debug { "request : $requestAsString" }
    }

    private var handlerHealthcheck: IadvizeHandler = { context, _ ->
        context.response().endWithJson(Healthcheck())
    }

    internal var handlerGetBots: IadvizeHandler = { context, controller ->
        context.response().endWithJson(listOf(getBot(controller)))
    }

    internal var handlerGetBot: IadvizeHandler = { context, controller ->
        val idOperator: String = context.pathParam(QUERY_ID_OPERATOR)
        context.response().endWithJson(getBotUpdate(idOperator, controller))
    }

    internal var handlerUpdateBot: IadvizeHandler = { context, controller ->
        val idOperator: String = context.pathParam(QUERY_ID_OPERATOR)
        context.response().endWithJson(getBotUpdate(idOperator, controller))
    }

    private fun getBotUpdate(
        idOperator: String,
        controller: ConnectorController,
    ): BotUpdated {
        return BotUpdated(idOperator, getBot(controller), LocalDateTime.now(), LocalDateTime.now())
    }

    private fun getBot(controller: ConnectorController): Bot {
        val botId: String = controller.botDefinition.botId
        val botName: String = controller.botConfiguration.name
        return Bot(idBot = botId, name = botName, editorUrl = editorUrl)
    }

    internal var handlerStrategies: IadvizeHandler = { context, _ ->
        context.response().endWithJson(listOf(AvailabilityStrategies(strategy = customAvailability, availability = true)))
    }

    internal var handlerFirstMessage: IadvizeHandler = { context, controller ->
        val translator: I18nTranslator = controller.botDefinition.i18nTranslator(locale, iadvizeConnectorType)
        context.response().endWithJson(
            RepliesResponse(
                IadvizeMessage(
                    translator.translate(firstMessage as CharSequence).toString(),
                ),
            ),
        )
    }

    internal var handlerStartConversation: IadvizeHandler = { context, controller ->

        val conversationRequest: ConversationsRequest =
            mapper.readValue(context.body().asString(), ConversationsRequest::class.java)

        val callback =
            IadvizeConnectorCallback(
                applicationId,
                controller,
                localeCode?.let { getLocale(localeCode) } ?: defaultLocale,
                context,
                conversationRequest,
                distributionRule,
                distributionRuleUnavailableMessage,
            )
        callback.answerWithResponse()
    }

    internal var handlerConversation: IadvizeHandler = { context, controller ->
        val idConversation: String = context.pathParam(QUERY_ID_CONVERSATION)
        val iadvizeRequest: IadvizeRequest = mapRequest(idConversation, context)
        if (!isOperator(iadvizeRequest)) {
            handleRequest(controller, context, iadvizeRequest)
        } else {
            // ignore message from operator
            context.response().end()
        }
    }

    /*
     * If request is a MessageRequest and the author of message have role "operator" : do not treat request.
     * in many case it's an echo, but it can be a human operator
     */
    private fun isOperator(iadvizeRequest: IadvizeRequest): Boolean {
        return iadvizeRequest is MessageRequest &&
            iadvizeRequest.message.author.role == ROLE_OPERATOR
    }

    private fun mapRequest(
        idConversation: String,
        context: RoutingContext,
    ): IadvizeRequest {
        val typeMessage: TypeMessage = mapper.readValue(context.body().asString(), TypeMessage::class.java)
        return when (typeMessage.type) {
            // json doesn't contain idConversation, to prevent null pointer,
            // we use the inner class MessageRequestJson to enhance the json.
            TYPE_TEXT -> {
                val messageRequestJson: MessageRequestJson =
                    mapper.readValue(context.body().asString(), MessageRequestJson::class.java)
                MessageRequest(messageRequestJson, idConversation)
            }

            else -> {
                val unsupportedRequestJson: UnsupportedRequestJson =
                    mapper.readValue(context.body().asString(), UnsupportedRequestJson::class.java)
                UnsupportedRequest(unsupportedRequestJson, idConversation, typeMessage.type)
            }
        }
    }

    private fun <T> HttpServerResponse.endWithJson(response: T): Future<Void> {
        val responseAsString: String = mapper.writeValueAsString(response)
        logger.debug { "response : $responseAsString" }
        return putHeader("Content-Type", "application/json").end(responseAsString)
    }

    override fun send(
        event: Event,
        callback: ConnectorCallback,
        delayInMs: Long,
    ) {
        val iadvizeCallback = callback as? IadvizeConnectorCallback
        val coordinator = iadvizeCallback?.deferredCoordinator

        logger.debug {
            "send(): deferredMode=${coordinator != null}, " +
                "lastAnswer=${(event as? Action)?.metadata?.lastAnswer}, " +
                "eventType=${event::class.simpleName}"
        }

        if (coordinator != null && event is Action) {
            // DEFERRED MODE: collect message for later sending via GraphQL
            coordinator.collect(IadvizeConnectorCallback.ActionWithDelay(event, delayInMs))
            logger.debug { "Message collected in deferred coordinator" }

            // Detect end via lastAnswer=true
            if (event.metadata.lastAnswer) {
                logger.debug { "lastAnswer=true detected - ending deferred and flushing" }
                val params = coordinator.getParameters()
                coordinator.end { actionWithDelay ->
                    queue.add(actionWithDelay.action, actionWithDelay.delayInMs) { action ->
                        sendProactiveMessage(iadvizeCallback, action, params)
                    }
                }
                iadvizeCallback.deferredCoordinator = null // Cleanup
            }
        } else {
            // STANDARD MODE: add to callback and respond on lastAnswer
            iadvizeCallback?.addAction(event, delayInMs)
            if (event is Action && event.metadata.lastAnswer) {
                iadvizeCallback?.answerWithResponse()
            }
        }
    }

    private fun sendProactiveMessage(
        callback: IadvizeConnectorCallback,
        action: Action,
        parameters: Map<String, String>,
    ) {
        logger.debug {
            "sendProactiveMessage(): actionType=${action::class.simpleName}, " +
                "CONVERSATION_ID=${parameters[IadvizeConnectorMetadata.CONVERSATION_ID.name]}, " +
                "CHAT_BOT_ID=${parameters[IadvizeConnectorMetadata.CHAT_BOT_ID.name]}, " +
                "thread=${Thread.currentThread().name}"
        }

        val conversationId = parameters[IadvizeConnectorMetadata.CONVERSATION_ID.name]
        val chatBotId = parameters[IadvizeConnectorMetadata.CHAT_BOT_ID.name]
        if (conversationId == null || chatBotId == null) {
            logger.debug { "sendProactiveMessage(): MISSING PARAMS - CONVERSATION_ID=$conversationId, CHAT_BOT_ID=$chatBotId" }
        }

        when (action) {
            is SendSentenceWithFootnotes -> {
                logger.debug { "sendProactiveMessage(): SendSentenceWithFootnotes -> sendByGraphQL()" }
                action.sendByGraphQL(parameters)
            }
            is SendSentence -> {
                if (action.messages.isEmpty()) {
                    action.text?.let {
                        logger.debug { "sendProactiveMessage(): SendSentence -> sendByGraphQL() text='${it.take(50)}...'" }
                        IadvizeMessage(TextPayload(it)).sendByGraphQL(parameters, callback)
                    } ?: run {
                        logger.debug { "sendProactiveMessage(): SendSentence with text=null, NOTHING SENT" }
                    }
                } else {
                    logger.debug { "sendProactiveMessage(): SendSentence complex, messages.size=${action.messages.size}" }
                    action.messages
                        .filterIsInstance<IadvizeConnectorMessage>()
                        .flatMap { it.replies }
                        .map { it.sendByGraphQL(parameters, callback) }
                }
            }
            else -> {
                logger.debug { "sendProactiveMessage(): unhandled action type: ${action::class.simpleName}" }
            }
        }
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

    private fun SendSentenceWithFootnotes.sendByGraphQL(parameters: Map<String, String>) {
        val conversationId = parameters[IadvizeConnectorMetadata.CONVERSATION_ID.name]
        val chatBotIdStr = parameters[IadvizeConnectorMetadata.CHAT_BOT_ID.name]
        val chatBotId = chatBotIdStr?.toIntOrNull()
        val messageText = this.toMarkdown()
        val caller = Thread.currentThread().stackTrace.getOrNull(2)?.methodName ?: "unknown"

        logger.debug { "SendSentenceWithFootnotes.sendByGraphQL(): caller=$caller, conversationId=$conversationId, chatBotId=$chatBotId, messageLength=${messageText.length}" }

        if (conversationId == null || chatBotId == null) {
            logger.debug { "SendSentenceWithFootnotes.sendByGraphQL(): ABORT - MISSING PARAMS conversationId=$conversationId, chatBotIdStr=$chatBotIdStr, chatBotId=$chatBotId" }
            return
        }

        try {
            val startTime = System.currentTimeMillis()
            val result =
                IadvizeGraphQLClient().sendProactiveActionOrMessage(
                    conversationId,
                    chatBotId,
                    actionOrMessage =
                        ChatbotActionOrMessageInput(
                            chatbotMessage = ChatbotMessageInput(chatbotSimpleTextMessage = messageText),
                        ),
                )
            val duration = System.currentTimeMillis() - startTime
            logger.debug { "SendSentenceWithFootnotes.sendByGraphQL(): result=$result, duration=${duration}ms" }
        } catch (e: Exception) {
            logger.debug(e) { "SendSentenceWithFootnotes.sendByGraphQL(): EXCEPTION ${e.message}" }
        }
    }

    private fun IadvizeReply.sendByGraphQL(
        parameters: Map<String, String>,
        callback: IadvizeConnectorCallback,
    ) {
        val conversationId = parameters[IadvizeConnectorMetadata.CONVERSATION_ID.name]
        val chatBotIdStr = parameters[IadvizeConnectorMetadata.CHAT_BOT_ID.name]
        val chatBotId = chatBotIdStr?.toIntOrNull()
        val caller = Thread.currentThread().stackTrace.getOrNull(2)?.methodName ?: "unknown"

        logger.debug { "IadvizeReply.sendByGraphQL(): caller=$caller, replyType=${this::class.simpleName}, conversationId=$conversationId, chatBotId=$chatBotId" }

        if (conversationId == null || chatBotId == null) {
            logger.debug { "IadvizeReply.sendByGraphQL(): ABORT - MISSING PARAMS conversationId=$conversationId, chatBotIdStr=$chatBotIdStr, chatBotId=$chatBotId" }
            return
        }

        val actionOrMessage =
            when (this) {
                is IadvizeTransfer -> {
                    val response = callback.addDistributionRulesOnTransfer(this)
                    if (response is IadvizeTransfer) {
                        response.toChatBotActionOrMessageInput()
                    } else {
                        ChatbotActionOrMessageInput(
                            chatbotMessage = ChatbotMessageInput(chatbotSimpleTextMessage = distributionRuleUnavailableMessage),
                        )
                    }
                }
                else -> this.toChatBotActionOrMessageInput()
            }

        try {
            val startTime = System.currentTimeMillis()
            val result =
                IadvizeGraphQLClient().sendProactiveActionOrMessage(
                    conversationId,
                    chatBotId,
                    actionOrMessage = actionOrMessage,
                )
            val duration = System.currentTimeMillis() - startTime
            logger.debug { "IadvizeReply.sendByGraphQL(): result=$result, duration=${duration}ms" }
        } catch (e: Exception) {
            logger.debug(e) { "IadvizeReply.sendByGraphQL(): EXCEPTION ${e.message}" }
        }
    }

    internal fun handleRequest(
        controller: ConnectorController,
        context: RoutingContext,
        iadvizeRequest: IadvizeRequest,
    ) {
        val callback =
            IadvizeConnectorCallback(
                applicationId,
                controller,
                localeCode?.let { getLocale(localeCode) } ?: defaultLocale,
                context,
                iadvizeRequest,
                distributionRule,
                distributionRuleUnavailableMessage,
            )

        when (iadvizeRequest) {
            is MessageRequest -> {
                val chatBotId = iadvizeRequest.idOperator.split("-").getOrNull(1)
                val metadata =
                    mapOf(
                        IadvizeConnectorMetadata.CONVERSATION_ID.name to iadvizeRequest.idConversation,
                        IadvizeConnectorMetadata.OPERATOR_ID.name to iadvizeRequest.idOperator,
                        IadvizeConnectorMetadata.IADVIZE_ENV.name to iadvizeRequest.idOperator.split("-")[0],
                        IadvizeConnectorMetadata.CHAT_BOT_ID.name to (chatBotId ?: "unknown"),
                    )

                // Check if deferred mode can be used (chatBotId must be valid integer)
                val canUseDeferred = chatBotId?.toIntOrNull() != null

                if (canUseDeferred) {
                    // DEFERRED MODE: send HTTP 200 immediately, process async, flush via GraphQL
                    val coordinator = DeferredMessageCoordinator(callback, metadata)
                    callback.deferredCoordinator = coordinator
                    coordinator.start() // Sends HTTP 200

                    logger.debug { "Deferred mode started for conversation ${iadvizeRequest.idConversation}" }

                    val event = WebhookActionConverter.toEvent(iadvizeRequest, applicationId)
                    // Note: controller.handle() returns immediately as RAG processing is async
                    // The coordinator timeout (via TockBotBus.deferMessageSending 60s) handles errors
                    // DO NOT use finally block here - it would execute before async processing completes
                    executor.executeBlocking {
                        controller.handle(event, ConnectorData(callback, metadata = metadata))
                    }
                } else {
                    // STANDARD MODE: process sync, respond via HTTP
                    logger.debug { "Standard mode (invalid chatBotId) for conversation ${iadvizeRequest.idConversation}" }
                    val event = WebhookActionConverter.toEvent(iadvizeRequest, applicationId)
                    controller.handle(event, ConnectorData(callback, metadata = metadata))
                }
            }

            // Only MessageRequest are supported, other messages are UnsupportedMessage
            // and UnsupportedResponse can be sent immediately
            else -> callback.answerWithResponse()
        }
    }

    /**
     * Force flush the deferred coordinator (for timeout/error scenarios).
     * Creates an error action and sends it with collected messages.
     */
    private fun forceFlushCoordinator(
        callback: IadvizeConnectorCallback,
        reason: String,
        sendErrorMessage: Boolean = true,
    ) {
        val coordinator = callback.deferredCoordinator ?: return
        val params = coordinator.getParameters()

        // Create error action if requested
        val errorAction =
            if (sendErrorMessage) {
                IadvizeConnectorCallback.ActionWithDelay(
                    SendSentence(
                        playerId = PlayerId("bot"),
                        applicationId = applicationId,
                        recipientId = PlayerId("user"),
                        text = deferredErrorMessage,
                    ),
                    delayInMs = 0,
                )
            } else {
                null
            }

        coordinator.forceEnd(
            sendAction = { actionWithDelay ->
                queue.add(actionWithDelay.action, actionWithDelay.delayInMs) { action ->
                    sendProactiveMessage(callback, action, params)
                }
            },
            errorAction = errorAction,
            logMessage = reason,
        )

        callback.deferredCoordinator = null // Cleanup
    }

    // ==============================
    // DeferredConnector Implementation
    // ==============================

    override fun isDeferredMode(callback: ConnectorCallback): Boolean {
        return (callback as? IadvizeConnectorCallback)?.deferredCoordinator != null
    }

    override fun acknowledge(callback: ConnectorCallback) {
        (callback as? IadvizeConnectorCallback)?.answerWithResponse()
    }

    override fun beginDeferred(
        callback: ConnectorCallback,
        parameters: Map<String, String>,
    ) {
        val iadvizeCallback = callback as? IadvizeConnectorCallback ?: return
        val coordinator = DeferredMessageCoordinator(iadvizeCallback, parameters)
        iadvizeCallback.deferredCoordinator = coordinator
        coordinator.start()
        logger.debug { "Deferred mode started for callback" }
    }

    override fun endDeferred(callback: ConnectorCallback) {
        val iadvizeCallback = callback as? IadvizeConnectorCallback ?: return
        val coordinator = iadvizeCallback.deferredCoordinator ?: return
        val params = coordinator.getParameters()

        coordinator.end { actionWithDelay ->
            queue.add(actionWithDelay.action, actionWithDelay.delayInMs) { action ->
                sendProactiveMessage(iadvizeCallback, action, params)
            }
        }

        iadvizeCallback.deferredCoordinator = null
        logger.debug { "Deferred mode ended for callback" }
    }

    override fun forceEndDeferred(
        callback: ConnectorCallback,
        reason: String,
        sendErrorMessage: Boolean,
    ) {
        val iadvizeCallback = callback as? IadvizeConnectorCallback ?: return
        forceFlushCoordinator(iadvizeCallback, reason, sendErrorMessage)
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

    private fun getLocale(it: String): Locale? {
        return try {
            LocaleUtils.toLocale(it)
        } catch (e: Exception) {
            logger.error(e)
            null
        }
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

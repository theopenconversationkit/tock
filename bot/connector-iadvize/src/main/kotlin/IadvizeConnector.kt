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

import ai.tock.bot.connector.*
import ai.tock.bot.connector.iadvize.model.payload.TextPayload
import ai.tock.bot.connector.iadvize.model.request.*
import ai.tock.bot.connector.iadvize.model.request.MessageRequest.MessageRequestJson
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
import ai.tock.iadvize.client.graphql.*
import ai.tock.shared.*
import ai.tock.shared.exception.rest.BadRequestException
import ai.tock.shared.jackson.mapper
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
import java.util.*

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
    val localeCode: String?
) : ConnectorBase(IadvizeConnectorProvider.connectorType) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    val locale: Locale = localeCode?.let{ getLocale(localeCode) } ?: defaultLocale

    private val executor: Executor by injector.instance()
    private val queue: ConnectorQueue = ConnectorQueue(executor)
    private var proactiveAnswerEnabled: Boolean = false
    private var proactiveParameters: Map<String, String> = emptyMap()

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
    private fun Route.handleAndCatchException(controller: ConnectorController, iadvizeHandler: IadvizeHandler) {
        handler { context ->
            try {
                logContextRequest(context)

                // Check payloads signature
                if(!secretToken.isNullOrBlank()) {
                    IadvizeSecurity(secretToken).validatePayloads(context)
                }
                // Invoke handler
                iadvizeHandler.invoke(context, controller)
            } catch (error: BadRequestException){
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
                            .associate { it.key to it.value }
                    )
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

    private fun getBotUpdate(idOperator: String, controller: ConnectorController): BotUpdated {
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
                    translator.translate(firstMessage as CharSequence).toString()
                )
            )
        )
    }

    internal var handlerStartConversation: IadvizeHandler = { context, controller ->

        val conversationRequest: ConversationsRequest =
            mapper.readValue(context.body().asString(), ConversationsRequest::class.java)

        val callback = IadvizeConnectorCallback(
            applicationId,
            controller,
            localeCode?.let{ getLocale(localeCode) } ?: defaultLocale,
            context,
            conversationRequest,
            distributionRule,
            distributionRuleUnavailableMessage
        )
        callback.answerWithResponse()
    }

    internal var handlerConversation: IadvizeHandler = { context, controller ->
        val idConversation: String = context.pathParam(QUERY_ID_CONVERSATION)
        val iadvizeRequest: IadvizeRequest = mapRequest(idConversation, context)
        if (!isOperator(iadvizeRequest)) {
            handleRequest(controller, context, iadvizeRequest)
        } else {
            //ignore message from operator
            context.response().end()
        }
    }

    /*
     * If request is a MessageRequest and the author of message have role "operator" : do not treat request.
     * in many case it's an echo, but it can be a human operator
     */
    private fun isOperator(iadvizeRequest: IadvizeRequest): Boolean {
        return iadvizeRequest is MessageRequest
                && iadvizeRequest.message.author.role == ROLE_OPERATOR
    }

    private fun mapRequest(idConversation: String, context: RoutingContext): IadvizeRequest {
        val typeMessage: TypeMessage = mapper.readValue(context.body().asString(), TypeMessage::class.java)
        return when (typeMessage.type) {
            //json doesn't contain idConversation, to prevent null pointer,
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

    private fun <T> HttpServerResponse.endWithJson(response: T) : Future<Void> {
        val responseAsString: String = mapper.writeValueAsString(response)
        logger.debug { "response : $responseAsString" }
        return putHeader("Content-Type", "application/json").end(responseAsString)
    }

    override fun send(event: Event, callback: ConnectorCallback, delayInMs: Long) {
        val iadvizeCallback = callback as? IadvizeConnectorCallback
        iadvizeCallback?.addAction(event, delayInMs)

        // Send message proactively if this mode is already started
        if (proactiveAnswerEnabled) {
            flushProactiveConversation(callback, proactiveParameters)
        }else{
            if (event is Action && event.metadata.lastAnswer) {
                iadvizeCallback?.answerWithResponse()
            }
        }
    }

    override fun startProactiveConversation(callback: ConnectorCallback, botBus: BotBus): Boolean {
        // Set proactive answer mode, and save parameters
        proactiveAnswerEnabled = true
        proactiveParameters = botBus.connectorData.metadata

        if(!proactiveStartMessage.isNullOrBlank()){
            // Send a RAG start message
            botBus.send(proactiveStartMessage)
        }
        (callback as? IadvizeConnectorCallback)?.answerWithResponse()
        return true
    }

    override fun flushProactiveConversation(callback: ConnectorCallback, parameters: Map<String, String>) {
        val iadvizeCallback = callback as? IadvizeConnectorCallback
        iadvizeCallback?.actions?.forEach {
            queue.add(it.action , it.delayInMs) {action ->
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
        parameters: Map<String, String>
    ){
        when (action) {
            is SendSentenceWithFootnotes -> action.sendByGraphQL(parameters)
            is SendSentence -> {
                if (action.messages.isEmpty()) action.text?.let {
                    // Simple message
                    IadvizeMessage(TextPayload(it)).sendByGraphQL(
                        parameters,
                        callback
                    )
                }
                // Complex message
                else action.messages
                    .filterIsInstance<IadvizeConnectorMessage>()
                    .flatMap { it.replies }
                    .map { it.sendByGraphQL(parameters, callback) }
            }
        }
    }

    override fun endProactiveConversation(callback: ConnectorCallback, parameters: Map<String, String>) {
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
        val sources = footnotes.joinToString(", ") { footnote ->
            footnote.url?.let {
                "[${counter++}]($it)"
            } ?: footnote.title
        }

        // Add sources if footnotes are not empty
        return if(footnotes.isEmpty())
                text.toString()
            else
                "$text\n\n\n*Sources: $sources*"
    }

    /**
     * Send [SendSentenceWithFootnotes] markdown using GraphQL
     * @param parameters the key value map of parameters
     */
    private fun SendSentenceWithFootnotes.sendByGraphQL(parameters: Map<String, String>) {
        IadvizeGraphQLClient().sendProactiveActionOrMessage(
            parameters[IadvizeConnectorMetadata.CONVERSATION_ID.name]!!,
            parameters[IadvizeConnectorMetadata.CHAT_BOT_ID.name]?.toInt()!!,
            actionOrMessage = ChatbotActionOrMessageInput(
                chatbotMessage = ChatbotMessageInput(
                    chatbotSimpleTextMessage = this.toMarkdown(),
                )
            )
        )
    }

    /**
     * Send [IadvizeReply] using GraphQL
     * @param parameters the key value map of parameters
     * @param callback the [IadvizeConnectorCallback]
     */
    private fun IadvizeReply.sendByGraphQL(parameters: Map<String, String>, callback: IadvizeConnectorCallback) {
        val actionOrMessage = when (this) {
            is IadvizeTransfer -> {
                // Check if a rule is available for distribution
                val response = callback.addDistributionRulesOnTransfer(this)
                if (response is IadvizeTransfer){
                    response.toChatBotActionOrMessageInput()
                } else {
                    // If the distribution rule is not available, send the configured message when
                    ChatbotActionOrMessageInput(
                        chatbotMessage = ChatbotMessageInput(
                            chatbotSimpleTextMessage = distributionRuleUnavailableMessage,
                        )
                    )
                }
            }
            else -> this.toChatBotActionOrMessageInput()
        }

        // Send a proactive action or message
        IadvizeGraphQLClient().sendProactiveActionOrMessage(
            parameters[IadvizeConnectorMetadata.CONVERSATION_ID.name]!!,
            parameters[IadvizeConnectorMetadata.CHAT_BOT_ID.name]?.toInt()!!,
            actionOrMessage = actionOrMessage
        )
    }

    internal fun handleRequest(
        controller: ConnectorController,
        context: RoutingContext,
        iadvizeRequest: IadvizeRequest
    ) {

        val callback = IadvizeConnectorCallback(
            applicationId,
            controller,
            localeCode?.let{ getLocale(localeCode) } ?: defaultLocale,
            context,
            iadvizeRequest,
            distributionRule,
            distributionRuleUnavailableMessage
        )

        when (iadvizeRequest) {
            is MessageRequest -> {
                val event = WebhookActionConverter.toEvent(iadvizeRequest, applicationId)
                controller.handle(
                    event, ConnectorData(
                        callback, metadata = mapOf(
                            IadvizeConnectorMetadata.CONVERSATION_ID.name to iadvizeRequest.idConversation,
                            IadvizeConnectorMetadata.OPERATOR_ID.name to iadvizeRequest.idOperator,
                            // iAdvize environment sd- or ha-
                            IadvizeConnectorMetadata.IADVIZE_ENV.name to iadvizeRequest.idOperator.split("-")[0],
                            // the operator id (=chatbotId) prefixed with the iAdvize environment
                            IadvizeConnectorMetadata.CHAT_BOT_ID.name to (iadvizeRequest.idOperator.split("-").getOrNull(1) ?: "unknown"),
                        )
                    )
                )
            }

            // Only MessageRequest are supported, other messages are UnsupportedMessage
            // and UnsupportedResponse can be sent immediately
            else -> callback.answerWithResponse()
        }
    }

    override fun addSuggestions(
        text: CharSequence,
        suggestions: List<CharSequence>
    ): BotBus.() -> ConnectorMessage? =
        MediaConverter.toSimpleMessage(text, suggestions)

    override fun addSuggestions(
        message: ConnectorMessage,
        suggestions: List<CharSequence>
    ): BotBus.() -> ConnectorMessage? = {
        (message as? IadvizeConnectorMessage)?.let {
            val iadvizeMessage = message.replies.last { it is IadvizeMessage } as IadvizeMessage
            iadvizeMessage.quickReplies.addAll( suggestions.map{ QuickReply(translate(it).toString())} )
        }
        message
    }

    override fun toConnectorMessage(message: MediaMessage): BotBus.() -> List<ConnectorMessage> =
        MediaConverter.toConnectorMessage(message)

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
    val headers: Map<String, String>)

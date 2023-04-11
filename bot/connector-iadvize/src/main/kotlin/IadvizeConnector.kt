/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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
import ai.tock.bot.connector.iadvize.model.request.ConversationsRequest
import ai.tock.bot.connector.iadvize.model.request.IadvizeRequest
import ai.tock.bot.connector.iadvize.model.request.MessageRequest
import ai.tock.bot.connector.iadvize.model.request.MessageRequest.MessageRequestJson
import ai.tock.bot.connector.iadvize.model.request.TypeMessage
import ai.tock.bot.connector.iadvize.model.request.UnsupportedRequest
import ai.tock.bot.connector.iadvize.model.request.UnsupportedRequest.UnsupportedRequestJson
import ai.tock.bot.connector.iadvize.model.response.AvailabilityStrategies
import ai.tock.bot.connector.iadvize.model.response.Bot
import ai.tock.bot.connector.iadvize.model.response.BotUpdated
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.event.Event
import ai.tock.shared.error
import mu.KotlinLogging
import ai.tock.bot.connector.iadvize.model.response.AvailabilityStrategies.Strategy.customAvailability
import ai.tock.bot.connector.iadvize.model.response.conversation.QuickReply
import ai.tock.bot.connector.iadvize.model.response.conversation.RepliesResponse
import ai.tock.bot.connector.iadvize.model.response.conversation.reply.IadvizeMessage
import ai.tock.bot.connector.media.MediaMessage
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.action.Action
import ai.tock.shared.jackson.mapper
import io.vertx.core.Future
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import java.time.LocalDateTime

private const val QUERY_ID_OPERATOR: String = "idOperator"
private const val QUERY_ID_CONVERSATION: String = "idConversation"
private const val TYPE_TEXT: String = "text"

/**
 *
 */
class IadvizeConnector internal constructor(
    val applicationId: String,
    val path: String,
    val editorUrl: String,
    val firstMessage: String,
    val distributionRule: String?
) : ConnectorBase(IadvizeConnectorProvider.connectorType) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val ROLE_OPERATOR: String = "operator"

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
                iadvizeHandler.invoke(context, controller)
            } catch (error: Throwable) {
                logger.error(error)
                context.fail(500)
            }
        }
    }

    internal var handlerGetBots: IadvizeHandler = { context, controller ->
        logRequest("GET", "/external-bots")
        context.response().endWithJson(listOf(getBot(controller)))
    }

    internal var handlerGetBot: IadvizeHandler = { context, controller ->
        val idOperator: String = context.pathParam(QUERY_ID_OPERATOR)
        logRequest("GET", "/bots/$idOperator", context.body().asString())
        context.response().endWithJson(getBotUpdate(idOperator, controller))
    }

    internal var handlerUpdateBot: IadvizeHandler = { context, controller ->
        val idOperator: String = context.pathParam(QUERY_ID_OPERATOR)
        logRequest("PUT", "/bots/$idOperator", context.body().asString())
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

    internal var handlerStrategies: IadvizeHandler = { context, controller ->
        logRequest("GET", "/availability-strategies")
        context.response().endWithJson(listOf(AvailabilityStrategies(strategy = customAvailability, availability = true)))
    }

    internal var handlerFirstMessage: IadvizeHandler = { context, controller ->
        val idOperator: String = context.pathParam(QUERY_ID_OPERATOR)
        logRequest("GET", "/bots/$idOperator/conversation-first-messages")
        context.response().endWithJson(RepliesResponse(IadvizeMessage(firstMessage)))
    }

    internal var handlerStartConversation: IadvizeHandler = { context, controller ->
        logger.info { "request : POST /conversations\nbody : ${context.body().asString()}" }
        val conversationRequest: ConversationsRequest =
            mapper.readValue(context.body().asString(), ConversationsRequest::class.java)
        val callback = IadvizeConnectorCallback(applicationId, controller, context, conversationRequest, distributionRule)
        callback.sendResponse()
    }

    internal var handlerConversation: IadvizeHandler = { context, controller ->
        val idConversation: String = context.pathParam(QUERY_ID_CONVERSATION)
        val iadvizeRequest: IadvizeRequest = mapRequest(idConversation, context)
        if (!isOperator(iadvizeRequest)) {
            logger.info { "request : POST /conversations/$idConversation/messages\nbody : ${context.body().asString()}" }
            logger.info { context.normalizedPath() }
            logger.info { "body parsed : $iadvizeRequest" }
            handleRequest(controller, context, iadvizeRequest)
        } else {
            //ignore message from operator
            logger.info { "request echo : POST /conversations/$idConversation/messages ${context.body().asString()}" }
            context.response().end()
        }
    }

    /*
     * If request is a MessageRequest and the author of message have role "operator" : do not treat request.
     * in many case it's an echo, but it can be a human operator
     */
    private fun isOperator(iadvizeRequest: IadvizeRequest): Boolean {
        return if(iadvizeRequest is MessageRequest) {
            iadvizeRequest.message.author.role.equals(ROLE_OPERATOR)
        } else {
            false
        }
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
        val response: String = mapper.writeValueAsString(response)
        logger.info { "response : $response" }
        return putHeader("Content-Type", "application/json").end(response)
    }

    override fun send(event: Event, callback: ConnectorCallback, delayInMs: Long) {
        val iadvizeCallback = callback as? IadvizeConnectorCallback
        iadvizeCallback?.addAction(event, delayInMs)
        if (event is Action && event.metadata.lastAnswer) {
            iadvizeCallback?.sendResponse()
        }
    }

    internal fun handleRequest(
        controller: ConnectorController,
        context: RoutingContext,
        iadvizeRequest: IadvizeRequest
    ) {
        val callback = IadvizeConnectorCallback(applicationId, controller, context, iadvizeRequest, distributionRule)
        when (iadvizeRequest) {
            is MessageRequest -> {
                val event = WebhookActionConverter.toEvent(iadvizeRequest, applicationId)
                controller.handle(event, ConnectorData(callback))
            }

            //Only MessageRequest are supported, other messages are UnsupportedMessage
            // and UnsupportedResponse can be send immediatly
            else -> callback.sendResponse()
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
        (message as? IadvizeMessage)?.let {
            message.quickReplies.addAll( suggestions.map{ QuickReply(translate(it).toString())} )
        }
        message
    }

    override fun toConnectorMessage(message: MediaMessage): BotBus.() -> List<ConnectorMessage> =
        MediaConverter.toConnectorMessage(message)

    private fun logRequest(verb: String, uri: String) {
        logger.info { "request : $verb $uri}" }
    }

    private fun logRequest(verb: String, uri: String, body: String) {
        logRequest(verb, uri)
        logger.info { "body : $body" }
    }
}

/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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
import ai.tock.bot.connector.iadvize.model.request.*
import ai.tock.bot.connector.iadvize.model.request.MessageRequest.MessageRequestJson
import ai.tock.bot.connector.iadvize.model.response.AvailabilityStrategies
import ai.tock.bot.connector.iadvize.model.response.Bot
import ai.tock.bot.connector.iadvize.model.response.BotUpdated
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.event.Event
import ai.tock.shared.error
import mu.KotlinLogging
import ai.tock.bot.connector.iadvize.model.response.AvailabilityStrategies.Strategy.customAvailability
import ai.tock.bot.connector.iadvize.model.response.conversation.RepliesResponse
import ai.tock.bot.connector.iadvize.model.response.conversation.reply.IadvizeMessage
import ai.tock.bot.engine.action.Action
import ai.tock.shared.jackson.mapper
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import java.time.LocalDateTime

class IadvizeConnector internal constructor(
    val applicationId: String,
    val path: String,
    val editorUrl: String,
    val firstMessage: String) : ConnectorBase(IadvizeConnectorProvider.connectorType) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    val echo: MutableSet<String> = mutableSetOf()

    private val QUERY_ID_OPERATOR: String = "idOperator"
    private val QUERY_ID_CONVERSATION: String = "idConversation"

    override fun register(controller: ConnectorController) {
        controller.registerServices(path) { router ->
            router.get("$path/external-bots")
                .handler { context -> handlerGetBots(context, controller) }

            router.get("$path/bots/:idOperator")
                .handler { context -> handlerGetBot(context, controller) }

            router.put("$path/bots/:idOperator")
                .handler { context -> handlerUpdateBot(context, controller) }

            router.get("$path/availability-strategies")
                .handler { context -> handlerStrategies(context, controller) }

            router.get("$path/bots/:idOperator/conversation-first-messages")
                .handler { context -> handlerFirstMessage(context, controller) }

            router.post("$path/conversations")
                .handler { context -> handlerStartConversation(context, controller) }

            router.post("$path/conversations/:idConversation/messages")
                .handler { context -> handlerConversation(context, controller) }
        }
    }

    var handlerGetBots: (RoutingContext, ConnectorController) -> Unit = { context, controller ->
        try {
            logger.info { "request : GET /external-bots\nbody : ${context.getBodyAsString()}" }
            context.response().endWithJson(listOf(getBot(controller)))
        } catch (e: Throwable) {
            logger.error(e)
            context.fail(500)
        }
    }

    var handlerGetBot: (RoutingContext, ConnectorController) -> Unit = { context, controller ->
        try {
            val idOperator: String = context.pathParam(QUERY_ID_OPERATOR)
            logger.info { "request : GET /bots/$idOperator\nbody : ${context.getBodyAsString()}" }
            context.response().endWithJson(getBotUpdate(idOperator, controller))
        } catch (e: Throwable) {
            logger.error(e)
            context.fail(500)
        }
    }

    var handlerUpdateBot: (RoutingContext, ConnectorController) -> Unit = { context, controller ->
        try {
            val idOperator: String = context.pathParam(QUERY_ID_OPERATOR)
            logger.info { "request : PUT /bots/$idOperator\nbody : ${context.getBodyAsString()}" }
            context.response().endWithJson(getBotUpdate(idOperator, controller))
        } catch (e: Throwable) {
            logger.error(e)
            context.fail(500)
        }
    }

    var handlerStrategies: (RoutingContext, ConnectorController) -> Unit = { context, controller ->
        try {
            logger.info { "request : GET /availability-strategies\nbody : ${context.getBodyAsString()}" }
            context.response().endWithJson(AvailabilityStrategies(strategy = customAvailability, availability = true))
        } catch (e: Throwable) {
            logger.error(e)
            context.fail(500)
        }
    }

    var handlerFirstMessage: (RoutingContext, ConnectorController) -> Unit = { context, controller ->
        try {
            val idOperator: String = context.pathParam(QUERY_ID_OPERATOR)
            logger.info { "request : GET /bots/$idOperator/conversation-first-messages\nbody : ${context.getBodyAsString()}" }
            context.response().endWithJson(RepliesResponse(IadvizeMessage(firstMessage)))
        } catch (e: Throwable) {
            logger.error(e)
            context.fail(500)
        }
    }

    var handlerStartConversation: (RoutingContext, ConnectorController) -> Unit = { context, controller ->
        try {
            logger.info { "request : POST /conversations\nbody : ${context.getBodyAsString()}" }
            val conversationRequest: ConversationsRequest = mapper.readValue(context.getBodyAsString(), ConversationsRequest::class.java)
            val callback = IadvizeConnectorCallback(applicationId, controller, context, conversationRequest)
            callback.sendResponse()
        } catch (e: Throwable) {
            logger.error(e)
            context.fail(500)
        }
    }

    var handlerConversation: (RoutingContext, ConnectorController) -> Unit = { context, controller ->
        try {
            val idConversation: String = context.pathParam(QUERY_ID_CONVERSATION)
            if(!isEcho(idConversation)) {
                logger.info { "request : POST /conversations/$idConversation/messages\nbody : ${context.getBodyAsString()}" }
                val iadvizeRequest: IadvizeRequest = mapRequest(idConversation, context)
                logger.info { "body parsed : $iadvizeRequest" }
                // warn echo message from iadvize
                echo.add(idConversation)
                handleRequest(controller, context, iadvizeRequest)
            } else {
                logger.info { "request echo : POST /conversations/$idConversation/messages ${context.getBodyAsString()}"}
                context.response().end()
            }
        } catch (e: Throwable) {
            logger.error(e)
            context.fail(500)
        }
    }

    private fun mapRequest(idConversation: String, context: RoutingContext): IadvizeRequest {
        val typeMessage: TypeMessage = mapper.readValue(context.getBodyAsString(), TypeMessage::class.java)
        return when(typeMessage.type) {
            // json dont contains idConversation, to prevent null pointer, used inner class MessageRequestJson
            "text" -> {
                val messageRequestJson: MessageRequestJson =
                    mapper.readValue(context.getBodyAsString(), MessageRequestJson::class.java)
                MessageRequest(messageRequestJson, idConversation)
            }
            else -> null
        }!!

    }

    private fun isEcho(idConversation: String): Boolean {
        //if id conversation is in echo, it's an echo : do not treat request.
        return echo.remove(idConversation)
    }

    private fun getBotUpdate(idOperator: String, controller: ConnectorController): BotUpdated {
        return BotUpdated(idOperator, getBot(controller), LocalDateTime.now(), LocalDateTime.now())
    }

    private fun getBot(controller: ConnectorController): Bot {
        val botId: String = controller.botDefinition.botId
        val botName: String = controller.botConfiguration.name
        return Bot(idBot = botId, name = botName, editorUrl = editorUrl)
    }

    private fun <T> HttpServerResponse.endWithJson(response: T) {
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

    // internal for tests
    internal fun handleRequest(
        controller: ConnectorController,
        context: RoutingContext,
        iadvizeRequest: IadvizeRequest
    ) {
        val callback = IadvizeConnectorCallback(applicationId, controller, context, iadvizeRequest)
        val event = when(iadvizeRequest) {
            is MessageRequest ->
                WebhookActionConverter.toEvent(iadvizeRequest, applicationId)

            else -> null
        }
        controller.handle(event!!, ConnectorData(callback))
    }
}
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
import ai.tock.bot.connector.iadvize.model.request.ConversationsRequest
import ai.tock.bot.connector.iadvize.model.request.IadvizeRequest
import ai.tock.bot.connector.iadvize.model.request.MessageRequest
import ai.tock.bot.connector.iadvize.model.request.MessageRequest.MessageRequestJson
import ai.tock.bot.connector.iadvize.model.response.AvailabilityStrategies
import ai.tock.bot.connector.iadvize.model.response.Bot
import ai.tock.bot.connector.iadvize.model.response.BotUpdated
import ai.tock.bot.connector.iadvize.model.request.RequestIds
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.event.Event
import ai.tock.shared.error
import mu.KotlinLogging
import ai.tock.bot.connector.iadvize.model.response.AvailabilityStrategies.Strategy.customAvailability
import ai.tock.bot.connector.iadvize.model.response.conversation.IadvizeReplies
import ai.tock.bot.connector.iadvize.model.response.conversation.reply.IadvizeMessage
import ai.tock.bot.engine.action.Action
import ai.tock.shared.jackson.mapper
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import java.time.LocalDateTime

class IadvizeConnector(
    val applicationId: String,
    val path: String,
    val editorUrl: String,
    val firstMessage: String) : ConnectorBase(IadvizeConnectorProvider.connectorType) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val QUERY_ID_OPERATOR: String = "idOperator"
    private val QUERY_ID_CONVERSATION: String = "idConversation"
    private val QUERY_ID_CONNECTOR_VERSION: String = "idConnectorVersion"
    private val QUERY_ID_WEBSITE: String = "idWebsite"

    override fun register(controller: ConnectorController) {
        controller.registerServices(path) { router ->
            router.get("$path/external-bots").handler { context ->
                try {
                    logger.info { "request : GET /external-bots\nbody : ${context.bodyAsString}" }
                    context.response().endWithJson(listOf(getBot(controller)))
                } catch (e: Throwable) {
                    logger.error(e)
                    context.fail(500)
                }
            }

            router.get("$path/bots/:idOperator").handler { context ->
                try {
                    val idOperator: String = context.pathParam(QUERY_ID_OPERATOR)
                    logger.info { "request : GET /bots/$idOperator\nbody : ${context.bodyAsString}" }
                    context.response().endWithJson(getBotUpdate(idOperator, controller))
                } catch (e: Throwable) {
                    logger.error(e)
                    context.fail(500)
                }
            }

            router.put("$path/bots/:idOperator").handler { context ->
                try {
                    val idOperator: String = context.pathParam(QUERY_ID_OPERATOR)
                    logger.info { "request : PUT /bots/$idOperator\nbody : ${context.bodyAsString}" }
                    context.response().endWithJson(getBotUpdate(idOperator, controller))
                } catch (e: Throwable) {
                    logger.error(e)
                    context.fail(500)
                }
            }

            router.get("$path/availability-strategies").handler { context ->
                try {
                    logger.info { "request : GET /availability-strategies\nbody : ${context.bodyAsString}" }
                    context.response().endWithJson(AvailabilityStrategies(strategy = customAvailability, availability = true))
                } catch (e: Throwable) {
                    logger.error(e)
                    context.fail(500)
                }
            }


            router.get("$path/bots/:idOperator/conversation-first-messages").handler { context ->
                try {
                    val idOperator: String = context.pathParam(QUERY_ID_OPERATOR)
                    logger.info { "request : GET /bots/$idOperator/conversation-first-messages\nbody : ${context.bodyAsString}" }
                    context.response().endWithJson(IadvizeReplies(IadvizeMessage(firstMessage)))
                } catch (e: Throwable) {
                    logger.error(e)
                    context.fail(500)
                }
            }

            router.post("$path/conversations").handler { context ->
                try {
                    logger.info { "request : POST /conversations\nbody : ${context.bodyAsString}" }
                    val conversationRequest: ConversationsRequest = mapper.readValue(context.bodyAsString, ConversationsRequest::class.java)
                    val callback = IadvizeConnectorCallback(applicationId, controller, context, conversationRequest)
                    callback.sendResponse()
                } catch (e: Throwable) {
                    logger.error(e)
                    context.fail(500)
                }
            }

            router.post("$path/conversations/:idConversation/messages").handler { context ->
                try {
                    val idConversation: String = context.pathParam(QUERY_ID_CONVERSATION)
                    logger.info { "request : POST /conversations/$idConversation/messages\nbody : ${context.bodyAsString}" }
                    val messageRequest =
                        MessageRequest(
                            mapper.readValue(context.bodyAsString, MessageRequestJson::class.java),
                            idConversation)
                    logger.info { "body parsed : $messageRequest" }
                    handleRequest(controller, context, messageRequest)
                } catch (e: Throwable) {
                    logger.error(e)
                    context.fail(500)
                }
            }
        }

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
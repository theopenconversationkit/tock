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

import ai.tock.bot.connector.ConnectorCallbackBase
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.iadvize.model.payload.TextPayload
import ai.tock.bot.connector.iadvize.model.request.ConversationsRequest
import ai.tock.bot.connector.iadvize.model.request.IadvizeRequest
import ai.tock.bot.connector.iadvize.model.request.MessageRequest
import ai.tock.bot.connector.iadvize.model.request.UnsupportedRequest
import ai.tock.bot.connector.iadvize.model.response.conversation.Duration
import ai.tock.bot.connector.iadvize.model.response.conversation.MessageResponse
import ai.tock.bot.connector.iadvize.model.response.conversation.reply.IadvizeAwait
import ai.tock.bot.connector.iadvize.model.response.conversation.reply.IadvizeMessage
import ai.tock.bot.connector.iadvize.model.response.conversation.reply.IadvizeReply
import ai.tock.bot.connector.iadvize.model.response.conversation.reply.IadvizeTransfer
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.I18nTranslator
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.event.Event
import ai.tock.iadvize.client.graphql.IadvizeGraphQLClient
import ai.tock.shared.error
import ai.tock.shared.jackson.mapper
import ai.tock.shared.loadProperties
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging
import java.time.LocalDateTime
import java.util.Locale
import java.util.Properties
import java.util.concurrent.ConcurrentLinkedQueue

private const val UNSUPPORTED_MESSAGE_REQUEST = "tock_iadvize_unsupported_message_request"

class IadvizeConnectorCallback(
    override val applicationId: String,
    controller: ConnectorController,
    val locale: Locale,
    val context: RoutingContext,
    val request: IadvizeRequest,
    distributionRule: String?,
    distributionRuleUnvailableMessage: String,
) : ConnectorCallbackBase(applicationId, iadvizeConnectorType) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * Thread-safe queue for actions (used in standard mode).
     */
    val actions: ConcurrentLinkedQueue<ActionWithDelay> = ConcurrentLinkedQueue()

    /**
     * Coordinator for deferred mode (per conversation).
     * Set by IadvizeConnector.handleRequest() when deferred mode is activated.
     * Null means standard mode (HTTP response with messages).
     */
    var deferredCoordinator: DeferredMessageCoordinator? = null

    @Volatile
    internal var answered: Boolean = false

    private val translator: I18nTranslator = controller.botDefinition.i18nTranslator(locale, iadvizeConnectorType)

    private val properties: Properties = loadProperties("/iadvize.properties")

    internal var iadvizeGraphQLClient = IadvizeGraphQLClient()

    data class ActionWithDelay(val action: Action, val delayInMs: Long = 0)

    fun addAction(
        event: Event,
        delayInMs: Long,
    ) {
        if (event is Action) {
            actions.add(ActionWithDelay(event, delayInMs))
        } else {
            logger.trace { "unsupported event: $event" }
        }
    }

    override fun eventSkipped(event: Event) {
        super.eventSkipped(event)
        sendResponse(buildResponse())
    }

    override fun eventAnswered(event: Event) {
        super.eventAnswered(event)
        sendResponse(buildResponse())
    }

    /**
     * Respond to a user request with a non-empty reply,
     * based on actions already stored in the callback
     */
    fun answerWithResponse() = sendResponse(buildResponse())

    private fun sendResponse(response: MessageResponse? = null) {
        try {
            if (!answered) {
                answered = true

                response?.let {
                    context.response().endWithJson(it)
                }
                    ?: context.response().end()
            }
        } catch (t: Throwable) {
            sendTechnicalError(t)
        }
    }

    private fun buildResponse(): MessageResponse {
        val response =
            MessageResponse(
                request.idConversation,
                request.idOperator,
                LocalDateTime.now(),
                LocalDateTime.now(),
            )

        return when (request) {
            is ConversationsRequest -> response

            is MessageRequest -> {
                response.replies.addAll(toListIadvizeReply(actions.toList()))
                return response
            }

            is UnsupportedRequest -> {
                logger.error("Request type ${request.type} is not supported by connector")
                // TODO: to be replaced by a transfer to a human when this type of message is supported
                val configuredMessage: String = properties.getProperty(UNSUPPORTED_MESSAGE_REQUEST, UNSUPPORTED_MESSAGE_REQUEST)
                val message: String = translator.translate(configuredMessage).toString()
                response.replies.add(IadvizeMessage(TextPayload(message)))
                return response
            }

            else -> response
        }
    }

    private fun toListIadvizeReply(actions: List<ActionWithDelay>): List<IadvizeReply> {
        return actions.map {
            if (it.action is SendSentence) {
                val listIadvizeReply: List<IadvizeReply> = it.action.messages.filterAndEnhanceIadvizeReply()

                if (it.action.text != null) {
                    val simpleTextPayload = mapToMessageTextPayload(it.action.text!!)
                    // Combine 1 MessageTextPayload with messages enhanced IadvizeReply
                    listOf(listOf(simpleTextPayload), listIadvizeReply).flatten()
                } else {
                    // No simple MessageTextPayload
                    // translate all TextPayloads if they exist
                    listIadvizeReply.map { reply ->
                        if (reply is IadvizeMessage) {
                            reply.copy(payload = TextPayload(translator.translate((reply.payload as TextPayload).value)))
                        } else {
                            reply
                        }
                    }
                }
            } else {
                emptyList()
            }
        }.flatten()
    }

    private fun List<ConnectorMessage>.filterAndEnhanceIadvizeReply(): List<IadvizeReply> {
        // Filter Message not IadvizeConnectorMessage for other connector
        return filterIsInstance<IadvizeConnectorMessage>()
            .map { connectorMessage -> connectorMessage.replies }
            .flatten()
            .map(addDistributionRulesOnTransfer)
    }

    /*
     * For IadvizeReply instance of IadvizeTransfer
     * return new IadvizeTransfer with distribution rule configured on connector
     */
    val addDistributionRulesOnTransfer: (IadvizeReply) -> IadvizeReply = {

        if (it is IadvizeTransfer) {
            if (distributionRule == null) {
                IadvizeAwait(Duration(3, seconds))
            } else {
                val available = iadvizeGraphQLClient.isAvailable(distributionRule)

                if (available) {
                    IadvizeTransfer(distributionRule, it.transferOptions)
                } else {
                    IadvizeMessage(distributionRuleUnvailableMessage)
                }
            }
        } else {
            it
        }
    }

    private fun mapToMessageTextPayload(text: CharSequence): IadvizeMessage {
        // Extract text to a simple TextPayload
        val message: String = translator.translate(text).toString()
        return IadvizeMessage(TextPayload(message))
    }

    override fun exceptionThrown(
        event: Event,
        throwable: Throwable,
    ) {
        super.exceptionThrown(event, throwable)
        sendTechnicalError(throwable)
    }

    private fun sendTechnicalError(throwable: Throwable) {
        logger.error(throwable)
        context.fail(throwable)
    }

    private fun <T> HttpServerResponse.endWithJson(response: T?) {
        if (response != null) {
            logger.debug { "iAdvize response : $response" }

            val writeValueAsString = mapper.writeValueAsString(response)

            logger.debug { "iAdvize json response: $writeValueAsString" }

            putHeader("Content-Type", "application/json").end(writeValueAsString)
        } else {
            end()
        }
    }
}

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

package ai.tock.bot.connector.businesschat

import ai.tock.bot.connector.ConnectorBase
import ai.tock.bot.connector.ConnectorCallback
import ai.tock.bot.connector.ConnectorData
import ai.tock.bot.connector.businesschat.model.common.ReceivedModel
import ai.tock.bot.connector.businesschat.model.input.BusinessChatConnectorImageMessage
import ai.tock.bot.connector.businesschat.model.input.BusinessChatConnectorListPickerMessage
import ai.tock.bot.connector.businesschat.model.input.BusinessChatConnectorRichLinkMessage
import ai.tock.bot.connector.businesschat.model.input.BusinessChatConnectorTextMessage
import ai.tock.bot.engine.BotRepository
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.monitoring.logError
import ai.tock.shared.Executor
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.jackson.mapper
import ai.tock.shared.provide
import ai.tock.shared.trace
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KotlinLogging

/**
 * Defines a connector for BusinessChat.
 *
 * @param path base path for our business chat endpoints
 * @param connectorId the connector identifier
 * @param businessId your organization business identifier
 */
internal class BusinessChatConnector(
    private val path: String,
    private val connectorId: String,
    private val businessId: String
) : ConnectorBase(BusinessChatConnectorProvider.connectorType) {

    private val logger = KotlinLogging.logger { }

    private val cspBusinessChatClient: CSPBusinessChatClient = CSPBusinessChatClient(
        try {
            injector.provide<BusinessChatIntegrationService>()
        } catch (e: Throwable) {
            logger.error("No Business Chat Integration Service injected - fallback to default")
            logger.trace(e)
            DefaultBusinessChatIntegrationService()
        }
    )
    private val executor: Executor get() = injector.provide()

    /**
     * Called for each messages sent on the bot bus
     */
    override fun send(event: Event, callback: ConnectorCallback, delayInMs: Long) {
        when (event) {
            is SendSentence -> {
                val message = MessageConverter.toMessage(event)
                when (message) {
                    is BusinessChatConnectorTextMessage -> cspBusinessChatClient.sendMessage(message)
                    is BusinessChatConnectorImageMessage -> cspBusinessChatClient.sendAttachment(message)
                    is BusinessChatConnectorListPickerMessage -> cspBusinessChatClient.sendListPicker(message)
                    is BusinessChatConnectorRichLinkMessage -> cspBusinessChatClient.sendRichLink(message)
                    else -> logger.warn { "unknown message: $event" }
                }
            }
        }
    }

    /**
     * Defines an endpoint for receiving business chat notifications
     */
    override fun register(controller: ConnectorController) {
        controller.registerServices(path) { router ->
            router.post(path).handler { context ->
                val requestTimerData = BotRepository.requestTimer.start("business chat start")
                try {
                    val body = context.body().asString()
                    val message = mapper.readValue<ReceivedModel>(body)

                    if (businessId == message.sourceId && message.handoverData == null) {
                        logger.debug("Ignoring echo message")
                    } else {
                        executor.executeBlocking {
                            val event = MessageConverter.toEvent(message, connectorId, cspBusinessChatClient)
                            event?.let {
                                controller.handle(
                                    event,
                                    ConnectorData(
                                        BusinessChatConnectorCallback(connectorId),
                                        referer = message.intent
                                    )
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    BotRepository.requestTimer.end(requestTimerData)
                    logger.logError(e, requestTimerData)
                } finally {
                    try {
                        BotRepository.requestTimer.end(requestTimerData)
                        context.response().end()
                    } catch (e: Throwable) {
                        logger.error(e)
                    }
                }
            }
        }
    }
}

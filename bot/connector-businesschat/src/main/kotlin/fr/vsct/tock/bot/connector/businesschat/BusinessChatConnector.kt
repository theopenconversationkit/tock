/*
 * Copyright (C) 2019 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.connector.businesschat

import com.fasterxml.jackson.module.kotlin.readValue
import fr.vsct.tock.bot.connector.ConnectorBase
import fr.vsct.tock.bot.connector.ConnectorCallback
import fr.vsct.tock.bot.connector.ConnectorData
import fr.vsct.tock.bot.connector.businesschat.model.common.ReceivedModel
import fr.vsct.tock.bot.connector.businesschat.model.input.BusinessChatConnectorImageMessage
import fr.vsct.tock.bot.connector.businesschat.model.input.BusinessChatConnectorListPickerMessage
import fr.vsct.tock.bot.connector.businesschat.model.input.BusinessChatConnectorRichLinkMessage
import fr.vsct.tock.bot.connector.businesschat.model.input.BusinessChatConnectorTextMessage
import fr.vsct.tock.bot.engine.BotRepository
import fr.vsct.tock.bot.engine.ConnectorController
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.event.Event
import fr.vsct.tock.bot.engine.monitoring.logError
import fr.vsct.tock.shared.Executor
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.provide
import mu.KotlinLogging

/**
 * Defines a connector for BusinessChat
 * @param path base path for our business chat endpoints
 * @param businessId your organization Business ID
 */
internal class BusinessChatConnector(
    private val path: String,
    private val connectorId: String,
    private val businessId: String
) :
    ConnectorBase(BusinessChatConnectorProvider.connectorType) {

    private val logger = KotlinLogging.logger { }
    private val cspBusinessChatClient: CSPBusinessChatClient get() = injector.provide()
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
                    val body = context.bodyAsString
                    val message = mapper.readValue<ReceivedModel>(body)

                    if (businessId == message.sourceId && message.handoverData == null) {
                        logger.info("Ignoring echo message")
                    }
                    else {
                        executor.executeBlocking {
                            val event = MessageConverter.toEvent(message, connectorId, cspBusinessChatClient)
                            event?.let {
                                controller.handle(
                                    event,
                                    ConnectorData(
                                        BusinessChatConnectorCallback(connectorId)
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
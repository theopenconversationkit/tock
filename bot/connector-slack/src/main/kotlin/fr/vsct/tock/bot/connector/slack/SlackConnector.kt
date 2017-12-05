/*
 * Copyright (C) 2017 VSCT
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

package fr.vsct.tock.bot.connector.slack

import fr.vsct.tock.bot.connector.ConnectorBase
import fr.vsct.tock.bot.connector.ConnectorCallback
import fr.vsct.tock.bot.connector.slack.model.SlackConnectorMessage
import fr.vsct.tock.bot.connector.slack.model.SlackMessageIn
import fr.vsct.tock.bot.engine.BotRepository.requestTimer
import fr.vsct.tock.bot.engine.ConnectorController
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.event.Event
import fr.vsct.tock.bot.engine.monitoring.logError
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.vertx.vertx
import mu.KotlinLogging

class SlackConnector(val applicationId: String,
                     val path: String,
                     val defaultChannel: String,
                     val outToken1: String,
                     val outToken2: String,
                     val outToken3: String,
                     val client: SlackClient) : ConnectorBase(SlackConnectorProvider.connectorType) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun register(controller: ConnectorController) {
        controller.registerServices(path, { router ->
            router.post(path).handler { context ->
                val requestTimerData = requestTimer.start("slack_webhook")
                try {
                    val body = context.convertUrlEncodedStringToJson()
                    logger.info { "message received from slack: $body" }
                    val message = mapper.readValue<SlackMessageIn>(body, SlackMessageIn::class.java)

                    vertx.executeBlocking<Void>({
                        try {
                            val event = SlackRequestConverter.toEvent(message, applicationId)
                            if (event != null) {
                                controller.handle(event)
                            } else {
                                logger.logError("unable to convert $message to event", requestTimerData)
                            }
                        } catch (e: Throwable) {
                            logger.logError(e, requestTimerData)
                        }
                    }, false, {})
                } catch (e: Throwable) {
                    logger.logError(e, requestTimerData)
                } finally {
                    try {
                        requestTimer.end(requestTimerData)
                        context.response().end()
                    } catch (e: Throwable) {
                        logger.error(e)
                    }
                }
            }

        })
    }

    override fun send(event: Event, callback: ConnectorCallback, delayInMs: Long) {
        logger.info { event }
        logger.info { "event: $event" }
        if (event is Action) {
            val message = SlackMessageConverter.toMessageOut(event)
            if (message != null) {
                sendMessage(message)
            }
        }
    }


    private fun sendMessage(message: SlackConnectorMessage) {
        client.sendMessage(outToken1, outToken2, outToken3, message)
    }

}
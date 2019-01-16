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

package fr.vsct.tock.bot.connector.teams

import com.fasterxml.jackson.module.kotlin.readValue
import fr.vsct.tock.bot.connector.ConnectorBase
import fr.vsct.tock.bot.connector.ConnectorCallback
import fr.vsct.tock.bot.connector.ConnectorData
import fr.vsct.tock.bot.engine.BotRepository
import fr.vsct.tock.bot.engine.ConnectorController
import fr.vsct.tock.bot.engine.event.Event
import fr.vsct.tock.bot.engine.monitoring.logError
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.jackson.mapper
import mu.KotlinLogging

/**
 *
 */
class TeamsConnector(
    val applicationId: String,
    private val path: String
) : ConnectorBase(teamsConnectorType) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun register(controller: ConnectorController) {
        controller.registerServices(path) { router ->

            router.post(path).handler { context ->
                if (!requestFilter.accept(context.request())) {
                    context.response().setStatusCode(403).end()
                    return@handler
                }

                val requestTimerData = BotRepository.requestTimer.start("whatsapp_webhook")
                try {
                    val body = context.bodyAsString
                    logger.debug { "WhatsApp request input : $body" }
                    val messages: WhatsAppMessages = mapper.readValue(body)
                    messages.messages.forEach { m ->
                        executor.executeBlocking {
                            val e = WebhookActionConverter.toEvent(m, applicationId, client)
                            if (e != null) {
                                controller.handle(
                                    e,
                                    ConnectorData(
                                        WhatsAppConnectorCallback(
                                            applicationId,
                                            if (m.groupId == null) individual else group
                                        ),
                                        groupId = m.groupId
                                    )
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
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

    override fun send(event: Event, callback: ConnectorCallback, delayInMs: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
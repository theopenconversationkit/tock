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

package ai.tock.bot.connector.whatsapp

import ai.tock.bot.connector.ConnectorBase
import ai.tock.bot.connector.ConnectorCallback
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.media.MediaMessage
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotRecipientType.group
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotRecipientType.individual
import ai.tock.bot.connector.whatsapp.model.webhook.WhatsAppMessages
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.BotRepository
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.monitoring.logError
import ai.tock.shared.Executor
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.jackson.mapper
import ai.tock.shared.security.RequestFilter
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.salomonbrys.kodein.instance
import mu.KotlinLogging
import java.time.Duration

class WhatsAppConnector(
    val applicationId: String,
    private val path: String,
    url: String,
    login: String,
    password: String,
    private val requestFilter: RequestFilter
) : ConnectorBase(whatsAppConnectorType) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val client: WhatsAppClient = WhatsAppClient(url, login, password)
    private val executor: Executor by injector.instance()

    override fun register(controller: ConnectorController) {
        controller.registerServices(path) { router ->
            router.post(path).handler { context ->
                if (!requestFilter.accept(context.request())) {
                    context.response().setStatusCode(403).end()
                    return@handler
                }

                val requestTimerData = BotRepository.requestTimer.start("whatsapp_webhook")
                try {
                    val body = context.body().asString()
                    logger.debug { "WhatsApp request input : $body" }
                    val messages: WhatsAppMessages = mapper.readValue(body)
                    messages.messages.forEach { m ->
                        executor.executeBlocking {
                            val e = WebhookActionConverter.toEvent(m, applicationId, client)
                            if (e != null) {
                                controller.handle(
                                    e,
                                    WhatsAppConnectorData(
                                        WhatsAppConnectorCallback(
                                            applicationId,
                                            if (m.groupId == null) individual else group
                                        ),
                                        m.groupId,
                                        m.from
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
        if (event is Action) {
            SendActionConverter.toBotMessage(event)
                ?.also {
                    val delay = Duration.ofMillis(delayInMs)
                    executor.executeBlocking(delay) {
                        client.sendMessage(it)
                    }
                }
        }
    }

    override fun toConnectorMessage(message: MediaMessage): BotBus.() -> List<ConnectorMessage> {
        return WhatsAppMediaConverter.toConnectorMessage(message)
    }
}

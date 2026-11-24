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
import ai.tock.shared.http.HttpResponse
import ai.tock.shared.http.HttpRoutes
import ai.tock.shared.injector
import ai.tock.shared.jackson.mapper
import ai.tock.shared.security.HttpRequestFilter
import ai.tock.shared.security.RequestFilter
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.salomonbrys.kodein.instance
import mu.KotlinLogging
import java.time.Duration

class WhatsAppConnector private constructor(
    val applicationId: String,
    private val path: String,
    url: String,
    login: String,
    password: String,
    private val requestFilter: RequestFilter?,
    private val httpRequestFilter: HttpRequestFilter?,
) : ConnectorBase(whatsAppConnectorType) {
    /** The connector with a Vert.x request filter: its route is a Vert.x one. */
    constructor(
        applicationId: String,
        path: String,
        url: String,
        login: String,
        password: String,
        requestFilter: RequestFilter,
    ) : this(applicationId, path, url, login, password, requestFilter, null)

    /** The connector with a server independent route. */
    constructor(
        applicationId: String,
        path: String,
        url: String,
        login: String,
        password: String,
        requestFilter: HttpRequestFilter,
    ) : this(applicationId, path, url, login, password, null, requestFilter)

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val client: WhatsAppClient = WhatsAppClient(url, login, password)
    private val executor: Executor by injector.instance()

    override fun register(controller: ConnectorController) {
        if (requestFilter != null) {
            WhatsAppVertxServices.register(this, controller, path, requestFilter)
        } else {
            controller.registerRoutes(path, routes(controller, checkNotNull(httpRequestFilter)))
        }
    }

    /**
     * The webhook route. It answers as the former Vert.x handler did: 403 for a refused request, an empty 200
     * otherwise, and the messages are handled by the [Executor].
     */
    internal fun routes(
        controller: ConnectorController,
        filter: HttpRequestFilter,
    ): HttpRoutes =
        HttpRoutes {
            post(path) { request ->
                if (!filter.accept(request)) {
                    HttpResponse.status(403)
                } else {
                    handleWebhook(controller, request.bodyAsString)
                    HttpResponse.status()
                }
            }
        }

    /** Handles the messages of a webhook. */
    internal fun handleWebhook(
        controller: ConnectorController,
        body: String,
    ) {
        val requestTimerData = BotRepository.requestTimer.start("whatsapp_webhook")
        try {
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
                                    if (m.groupId == null) individual else group,
                                ),
                                m.groupId,
                                m.from,
                            ),
                        )
                    }
                }
            }
        } catch (e: Exception) {
            logger.logError(e, requestTimerData)
        } finally {
            try {
                BotRepository.requestTimer.end(requestTimerData)
            } catch (e: Throwable) {
                logger.error(e)
            }
        }
    }

    override fun send(
        event: Event,
        callback: ConnectorCallback,
        delayInMs: Long,
    ) {
        if (event is Action) {
            SendActionConverter
                .toBotMessage(event)
                ?.also {
                    val delay = Duration.ofMillis(delayInMs)
                    executor.executeBlocking(delay) {
                        client.sendMessage(it)
                    }
                }
        }
    }

    override fun toConnectorMessage(message: MediaMessage): BotBus.() -> List<ConnectorMessage> = WhatsAppMediaConverter.toConnectorMessage(message)
}

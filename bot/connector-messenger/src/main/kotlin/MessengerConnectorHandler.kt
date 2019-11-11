/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
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

package ai.tock.bot.connector.messenger

import ai.tock.bot.connector.ConnectorData
import ai.tock.bot.connector.messenger.model.webhook.CallbackRequest
import ai.tock.bot.connector.messenger.model.webhook.Webhook
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.monitoring.RequestTimerData
import ai.tock.bot.engine.monitoring.logError
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType.bot
import ai.tock.bot.engine.user.PlayerType.temporary
import ai.tock.shared.error
import mu.KotlinLogging

internal class MessengerConnectorHandler(
    val applicationId: String,
    val controller: ConnectorController,
    val request: CallbackRequest,
    val requestTimerData: RequestTimerData) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun handleRequest() {
        request.entry.forEach { entry ->
            try {
                val pageId = entry.id
                //retrieve the controller from the page id
                val c = MessengerConnector.pageIdConnectorIdMap[pageId]
                    ?.firstOrNull { MessengerConnector.connectorIdApplicationIdMap[it] == applicationId }
                    ?.let { MessengerConnector.connectorIdConnectorControllerMap[it] }
                    ?: (controller.apply { logger.error { "unknown page id $pageId" } })
                val conn = c.connector as MessengerConnector
                val targetConnectorId = conn.connectorId

                fun handle(m: Webhook, notifiedOnly: Boolean) {
                    try {
                        handleMessage(c, targetConnectorId, m, notifiedOnly)
                    } catch (e: Throwable) {
                        try {
                            logger.logError(e, requestTimerData)
                            controller.errorMessage(
                                m.playerId(bot),
                                applicationId,
                                m.recipientId(bot)
                            ).let {
                                conn.sendEvent(it)
                                conn.endTypingAnswer(it)
                            }
                        } catch (t: Throwable) {
                            logger.error(e)
                        }
                    }
                }

                entry.messaging?.filterNotNull()?.forEach { handle(it, false) }
                entry.standby?.filterNotNull()?.forEach { handle(it, true) }
            } catch (e: Throwable) {
                logger.logError(e, requestTimerData)
            }
        }
    }

    private fun handleMessage(
        controller: ConnectorController,
        applicationId: String,
        webhook: Webhook,
        notifiedOnly: Boolean) {
        val event = WebhookActionConverter.toEvent(webhook, applicationId)?.apply { state.notification = notifiedOnly }
        if (event != null) {
            controller.handle(
                event,
                ConnectorData(
                    MessengerConnectorCallback(event.applicationId),
                    webhook.priorMessage?.identifier?.let {
                        PlayerId(
                            it,
                            temporary
                        )
                    }
                )
            )
        } else {
            logger.logError(
                "unable to convert $webhook to event",
                requestTimerData
            )
        }
    }
}
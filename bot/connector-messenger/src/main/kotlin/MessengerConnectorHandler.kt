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

package ai.tock.bot.connector.messenger

import ai.tock.bot.connector.ConnectorData
import ai.tock.bot.connector.messenger.MessengerConnector.Companion.connectorIdApplicationIdMap
import ai.tock.bot.connector.messenger.MessengerConnector.Companion.connectorIdConnectorControllerMap
import ai.tock.bot.connector.messenger.MessengerConnector.Companion.pageIdConnectorIdMap
import ai.tock.bot.connector.messenger.model.webhook.CallbackRequest
import ai.tock.bot.connector.messenger.model.webhook.Webhook
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.monitoring.RequestTimerData
import ai.tock.bot.engine.monitoring.logError
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType.bot
import ai.tock.bot.engine.user.PlayerType.temporary
import ai.tock.bot.engine.user.PlayerType.user
import ai.tock.shared.error
import mu.KotlinLogging

internal class MessengerConnectorHandler(
    val applicationId: String,
    val controller: ConnectorController,
    val request: CallbackRequest,
    val requestTimerData: RequestTimerData,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun handleRequest() {
        request.entry.forEach { entry ->
            try {
                val pageId = entry.id

                if (entry.standby.isNullOrEmpty()) {
                    if (!pageIdConnectorIdMap.containsKey(pageId)) {
                        logger.error { "unknown page id $pageId" }
                        return@forEach
                    }
                }

                // retrieve the controller from the page id
                val c =
                    pageIdConnectorIdMap[pageId]
                        ?.firstOrNull { connectorIdApplicationIdMap[it] == applicationId }
                        ?.let { connectorIdConnectorControllerMap[it] }
                        ?: controller
                val conn = c.connector as MessengerConnector
                val targetConnectorId = conn.connectorId

                fun handle(
                    m: Webhook,
                    notifiedOnly: Boolean = false,
                ) {
                    try {
                        handleMessage(c, targetConnectorId, m, notifiedOnly) {
                            try {
                                conn.getThreadOwner(m.playerId(user))
                                    ?.let { appId ->
                                        pageIdConnectorIdMap[pageId]
                                            ?.firstOrNull { connectorIdApplicationIdMap[it] == appId }
                                            ?: appId
                                    }
                            } catch (t: Throwable) {
                                logger.error(t)
                                null
                            }
                        }
                    } catch (e: Throwable) {
                        try {
                            logger.logError(e, requestTimerData)
                            controller.errorMessage(
                                m.playerId(bot),
                                applicationId,
                                m.recipientId(user),
                            ).let {
                                conn.sendEvent(it)
                                conn.endTypingAnswer(it)
                            }
                        } catch (t: Throwable) {
                            logger.error(e)
                        }
                    }
                }

                entry.messaging?.filterNotNull()?.forEach { handle(it) }
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
        notifiedOnly: Boolean,
        threadOwnerRetriever: () -> String?,
    ) {
        val event = WebhookActionConverter.toEvent(webhook, applicationId)

        if (event != null) {
            val sourceAppChoiceId = (event as? SendChoice)?.sourceAppId()
            if (notifiedOnly || (sourceAppChoiceId != null && applicationId != sourceAppChoiceId)) {
                val sourceAppId = if (notifiedOnly) threadOwnerRetriever() else sourceAppChoiceId
                event.state.notification = true
                event.state.sourceApplicationId = sourceAppId
            }

            controller.handle(
                event,
                ConnectorData(
                    MessengerConnectorCallback(event.applicationId),
                    webhook.priorMessage?.identifier?.let {
                        PlayerId(
                            it,
                            temporary,
                        )
                    },
                ),
            )
        } else {
            logger.warn("unable to convert $webhook to event")
        }
    }
}

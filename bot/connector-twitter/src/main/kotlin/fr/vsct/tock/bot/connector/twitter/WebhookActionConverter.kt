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

package fr.vsct.tock.bot.connector.twitter

import fr.vsct.tock.bot.connector.twitter.model.incoming.DirectMessageIncomingEvent
import fr.vsct.tock.bot.connector.twitter.model.incoming.IncomingEvent
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.event.Event
import fr.vsct.tock.bot.engine.user.PlayerType
import mu.KotlinLogging

internal object WebhookActionConverter {

    private val logger = KotlinLogging.logger {}

    fun toEvent(incomingEvent: IncomingEvent, applicationId: String): Event? {
        return when (incomingEvent) {
            is DirectMessageIncomingEvent -> {
                // Ignore all application event
                if (incomingEvent.apps == null || !incomingEvent.apps.containsKey(applicationId)) {
                    incomingEvent.directMessages.firstOrNull()?.let {
                        SendSentence(
                            incomingEvent.playerId(PlayerType.user),
                            applicationId,
                            incomingEvent.recipientId(PlayerType.bot),
                            it.messageCreated.messageData.text
                        )
                    }
                } else {
                    logger.debug { "ignore event $incomingEvent from applicationId $applicationId" }
                    null
                }
            }
            else -> {
                logger.error { "unknown event $incomingEvent" }
                null
            }
        }

    }

}
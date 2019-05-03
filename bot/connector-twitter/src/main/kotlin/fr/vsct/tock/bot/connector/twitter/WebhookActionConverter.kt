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

import fr.vsct.tock.bot.connector.twitter.model.OptionsResponse
import fr.vsct.tock.bot.connector.twitter.model.incoming.DirectMessageIncomingEvent
import fr.vsct.tock.bot.connector.twitter.model.incoming.IncomingEvent
import fr.vsct.tock.bot.connector.twitter.model.incoming.TweetIncomingEvent
import fr.vsct.tock.bot.engine.action.ActionMetadata
import fr.vsct.tock.bot.engine.action.ActionVisibility
import fr.vsct.tock.bot.engine.action.SendChoice
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.event.Event
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType
import mu.KotlinLogging

internal object WebhookActionConverter {

    private val logger = KotlinLogging.logger {}

    fun toEvent(incomingEvent: IncomingEvent, applicationId: String): Event? {
        return when (incomingEvent) {
            is TweetIncomingEvent -> {
                val tweet = incomingEvent.tweets.first()
                // Ignore all replies and quoted tweets && message from account listened
                val isReplyMessage = tweet.isQuote && tweet.inReplyToStatusId == null
                val isFromAccountListened = incomingEvent.forUserId.equals(tweet.user.id)
                if (!isReplyMessage && !isFromAccountListened) {
                    SendSentence(
                        incomingEvent.playerId(PlayerType.user),
                        applicationId,
                        PlayerId(incomingEvent.forUserId, PlayerType.bot),
                        //extended entities and full_text
                        tweet.extendedTweet?.text ?: tweet.text,
                        metadata = ActionMetadata(visibility = ActionVisibility.public)
                    )
                } else {
                    logger.debug { "ignore event $incomingEvent with tweet text = [${tweet.text}] from [${tweet.user.id}][${tweet.user.name}]" }
                    null
                }
            }
            is DirectMessageIncomingEvent -> {
                // ignore direct message sent from the bot
                val firstOrNull = incomingEvent.directMessages.firstOrNull()
                if (!incomingEvent.forUserId.equals(firstOrNull?.messageCreated?.senderId)) {
                        firstOrNull?.let {
                        return if (it.messageCreated.messageData.quickReplyResponse != null) {
                            when (it.messageCreated.messageData.quickReplyResponse) {
                                is OptionsResponse -> {
                                    SendChoice.decodeChoiceId(it.messageCreated.messageData.quickReplyResponse.metadata)
                                        .let { (intentName, parameters) ->
                                            if (parameters.containsKey(SendChoice.NLP)) {
                                                SendSentence(
                                                    incomingEvent.playerId(PlayerType.user),
                                                    applicationId,
                                                    incomingEvent.recipientId(PlayerType.bot),
                                                    parameters[SendChoice.NLP],
                                                    metadata = ActionMetadata(visibility = ActionVisibility.private)
                                                )
                                            } else {
                                                SendChoice(
                                                    incomingEvent.playerId(PlayerType.user),
                                                    applicationId,
                                                    incomingEvent.recipientId(PlayerType.bot),
                                                    intentName,
                                                    parameters,
                                                    metadata = ActionMetadata(visibility = ActionVisibility.private)
                                                )
                                            }
                                        }

                                }
                                else -> {
                                    logger.debug { "unknown quick reply response type $incomingEvent" }
                                    null
                                }
                            }
                        } else {
                            SendSentence(
                                incomingEvent.playerId(PlayerType.user),
                                applicationId,
                                incomingEvent.recipientId(PlayerType.bot),
                                it.messageCreated.messageData.text,
                                metadata = ActionMetadata(visibility = ActionVisibility.private)
                            )
                        }
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
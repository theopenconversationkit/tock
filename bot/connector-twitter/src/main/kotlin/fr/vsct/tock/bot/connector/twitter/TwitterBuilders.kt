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

import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.connector.twitter.model.MessageCreate
import fr.vsct.tock.bot.connector.twitter.model.MessageData
import fr.vsct.tock.bot.connector.twitter.model.Recipient
import fr.vsct.tock.bot.connector.twitter.model.TwitterConnectorMessage
import fr.vsct.tock.bot.connector.twitter.model.outcoming.DirectMessageOutcomingEvent
import fr.vsct.tock.bot.connector.twitter.model.outcoming.OutcomingEvent
import fr.vsct.tock.bot.engine.BotBus
import fr.vsct.tock.bot.engine.I18nTranslator

internal const val TWITTER_CONNECTOR_TYPE_ID = "twitter"

/**
 * The Twitter connector type.
 */
val twitterConnectorType = ConnectorType(TWITTER_CONNECTOR_TYPE_ID)

fun BotBus.directMessage(message: CharSequence): OutcomingEvent =
        OutcomingEvent(
                DirectMessageOutcomingEvent(
                        MessageCreate(
                                target = Recipient(userId.id),
                                sourceAppId = applicationId,
                                senderId = botId.id,
                                messageData = MessageData(message as String)
                        )
                )
        )

/**
 * Adds a Twitter [ConnectorMessage] if the current connector is Twitter.
 * You need to call [BotBus.send] or [BotBus.end] later to send this message.
 */
fun BotBus.withTwitter(messageProvider: () -> TwitterConnectorMessage): BotBus {
    return withMessage(twitterConnectorType, messageProvider)
}
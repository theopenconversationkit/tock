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

import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.connector.twitter.model.MessageCreate
import fr.vsct.tock.bot.connector.twitter.model.MessageData
import fr.vsct.tock.bot.connector.twitter.model.Recipient
import fr.vsct.tock.bot.connector.twitter.model.outcoming.DirectMessageOutcomingEvent
import fr.vsct.tock.bot.connector.twitter.model.outcoming.OutcomingEvent
import fr.vsct.tock.bot.connector.twitter.model.outcoming.Tweet
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.ActionVisibility
import fr.vsct.tock.bot.engine.action.SendSentence
import mu.KotlinLogging

internal object TwitterMessageConverter {

    val logger = KotlinLogging.logger {}

    fun toEvent(action: Action): ConnectorMessage? {
        return if (action is SendSentence) {
            if(action.metadata.visibility == ActionVisibility.public) {
                if (action.hasMessage(TwitterConnectorProvider.connectorType)) {
                    action.message(TwitterConnectorProvider.connectorType) as Tweet
                } else {
                    action.stringText?.run { if (isBlank()) null else Tweet(this) }
                }
            } else {
                if (action.hasMessage(TwitterConnectorProvider.connectorType)) {
                    action.message(TwitterConnectorProvider.connectorType) as OutcomingEvent
                } else {
                    action.stringText?.run {
                        if (isBlank()) null else OutcomingEvent(
                            DirectMessageOutcomingEvent(
                                MessageCreate(
                                    target = Recipient(action.recipientId.id),
                                    sourceAppId = action.applicationId,
                                    senderId = action.playerId.id,
                                    messageData = MessageData(this)
                                )
                            )
                        )
                    }
                }
            }
        } else {
            logger.warn { "Action $action not supported" }
            null
        }
    }
}
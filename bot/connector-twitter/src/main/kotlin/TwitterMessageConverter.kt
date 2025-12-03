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

package ai.tock.bot.connector.twitter

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.twitter.model.Recipient
import ai.tock.bot.connector.twitter.model.outcoming.DirectMessageOutcomingEvent
import ai.tock.bot.connector.twitter.model.outcoming.OutcomingEvent
import ai.tock.bot.connector.twitter.model.outcoming.Tweet
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.ActionVisibility
import ai.tock.bot.engine.action.SendSentence
import mu.KotlinLogging

internal object TwitterMessageConverter {
    val logger = KotlinLogging.logger {}

    fun toEvent(action: Action): ConnectorMessage? {
        return if (action is SendSentence) {
            if (action.metadata.visibility == ActionVisibility.PUBLIC) {
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
                        if (isBlank()) {
                            null
                        } else {
                            OutcomingEvent(
                                DirectMessageOutcomingEvent.builder(
                                    Recipient(action.recipientId.id),
                                    action.playerId.id,
                                    this,
                                )
                                    .withSourceAppId(action.applicationId)
                                    .build(),
                            )
                        }
                    }
                }
            }
        } else {
            logger.warn { "Action $action not supported" }
            null
        }
    }
}

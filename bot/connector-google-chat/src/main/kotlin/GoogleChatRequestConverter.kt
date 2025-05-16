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
package ai.tock.bot.connector.googlechat

import ai.tock.bot.connector.googlechat.builder.GOOGLE_CHAT_ACTION_INTENT_PARAMETER
import ai.tock.bot.connector.googlechat.builder.GOOGLE_CHAT_ACTION_SEND_CHOICE
import ai.tock.bot.connector.googlechat.builder.GOOGLE_CHAT_ACTION_SEND_SENTENCE
import ai.tock.bot.connector.googlechat.builder.GOOGLE_CHAT_ACTION_TEXT_PARAMETER
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.event.EndConversationEvent
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.event.StartConversationEvent
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import com.google.api.services.chat.v1.model.DeprecatedEvent

internal object GoogleChatRequestConverter {

    fun toEvent(event: DeprecatedEvent, applicationId: String): Event? {
        val userId = event.user?.name ?: return null
        val playerId = PlayerId(userId)
        val botId = PlayerId(applicationId, PlayerType.bot)
        return when (event.type) {
            "ADDED_TO_SPACE" -> StartConversationEvent(playerId, botId, applicationId)
            "REMOVED_FROM_SPACE" -> EndConversationEvent(playerId, botId, applicationId)
            "MESSAGE" -> SendSentence(playerId, applicationId, botId, event.message?.text)
            "CARD_CLICKED" -> when (event.action.actionMethodName) {
                GOOGLE_CHAT_ACTION_SEND_SENTENCE -> SendSentence(
                    playerId,
                    applicationId,
                    botId,
                    event.action.parameters.first { it.key == GOOGLE_CHAT_ACTION_TEXT_PARAMETER }.value
                )
                GOOGLE_CHAT_ACTION_SEND_CHOICE -> SendChoice(
                    playerId, applicationId, botId,
                    intentName = event.action.parameters.first { it.key == GOOGLE_CHAT_ACTION_INTENT_PARAMETER }.value,
                    parameters = event.action.parameters.map { it.key to it.value }.toMap()
                )
                else -> null
            }
            else -> null
        }
    }
}

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

package ai.tock.bot.connector.openai

import ai.tock.bot.engine.action.ActionMetadata
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType.bot
import ai.tock.shared.Dice.newId
import com.aallam.openai.api.chat.ChatRole

fun ChatCompletionRequestWithStream.toEvent(connectorId: String, chatId: String?): Event {
    val query = messages.lastOrNull { it.role == ChatRole.User }?.content
    val userId = (if (chatId == null) user else "$user-$chatId") ?: newId()
    return if (query != null) {
        SendSentence(
            PlayerId(userId),
            connectorId,
            PlayerId(connectorId, bot),
            query,
            metadata = ActionMetadata(
                streamedResponse = stream
            )
        )
    } else {
        error("No user message found in chat completion request. Chat completion request must contain at least one user message.")
    }
}

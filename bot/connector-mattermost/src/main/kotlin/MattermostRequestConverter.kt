/*
 * Copyright (C) 2017/2024 e-voyageurs technologies
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

package ai.tock.bot.connector.mattermost

import ai.tock.bot.connector.mattermost.model.MattermostMessageIn
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType.bot

internal object MattermostRequestConverter {

    fun toEvent(message: MattermostMessageIn, applicationId: String): Event {
        val safeMessage = message
        safeMessage.text = message.getRealMessage()

        return SendSentence(
            playerId = PlayerId(message.userId),
            applicationId = applicationId,
            recipientId = PlayerId(applicationId, bot),
            text = safeMessage.text,
            messages = mutableListOf(safeMessage)
        )
    }
}
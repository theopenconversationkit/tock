/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.bot.engine.action

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.engine.dialog.EventState
import ai.tock.bot.engine.message.DebugMessage
import ai.tock.bot.engine.message.Message
import ai.tock.bot.engine.message.Sentence
import ai.tock.bot.engine.user.PlayerId
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.time.Instant

class SendDebug(
    playerId: PlayerId,
    applicationId: String,
    recipientId: PlayerId,
    val text: String,
    val data: Any?,
    id: Id<Action> = newId(),
    date: Instant = Instant.now(),
    state: EventState = EventState(),
    metadata: ActionMetadata = ActionMetadata(),
) : Action(playerId, recipientId, applicationId, id, date, state, metadata) {
    override fun toMessage(): Message {
        return DebugMessage(text, data)
    }
}
/*
 * Copyright (C) 2017 VSCT
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

package ai.tock.bot.engine.action

import ai.tock.bot.engine.dialog.EventState
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.message.Attachment
import ai.tock.bot.engine.message.Message
import ai.tock.bot.engine.user.PlayerId
import ai.tock.shared.security.StringObfuscatorMode
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.time.Instant

/**
 * A simple attachment file sent.
 */
class SendAttachment(playerId: PlayerId,
                     applicationId: String,
                     recipientId: PlayerId,
                     val url: String,
                     val type: AttachmentType,
                     id: Id<Action> = newId(),
                     date: Instant = Instant.now(),
                     state: EventState = EventState(),
                     metadata: ActionMetadata = ActionMetadata())
    : Action(playerId, recipientId, applicationId, id, date, state, metadata) {

    enum class AttachmentType {
        image, audio, video, file
    }

    override fun toMessage(): Message {
        return Attachment(url, type)
    }

    override fun obfuscate(mode: StringObfuscatorMode, playerId: PlayerId): Event {
        return SendAttachment(
                playerId,
                applicationId,
                recipientId,
                url,
                type,
                toActionId(),
                date,
                state,
                metadata
        )
    }
}
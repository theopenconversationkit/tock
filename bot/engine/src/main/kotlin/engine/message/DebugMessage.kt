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

package ai.tock.bot.engine.message

import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.event.EventType
import ai.tock.bot.engine.user.PlayerId

/**
 * Debug message.
 */
data class DebugMessage(
    val text: String,
    val data: Any?,
    override val delay: Long = 0,
) : Message {

    override val eventType: EventType = EventType.debug
    override fun toAction(playerId: PlayerId, applicationId: String, recipientId: PlayerId): Action {
        error("Not supported")
    }

}

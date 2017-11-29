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



package fr.vsct.tock.bot.engine.action

import fr.vsct.tock.bot.engine.dialog.EventState
import fr.vsct.tock.bot.engine.event.Event
import fr.vsct.tock.bot.engine.message.Message
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.shared.security.StringObfuscatorMode
import org.litote.kmongo.Id
import java.time.Instant

/**
 * A user (or bot) action.
 */
abstract class Action(val playerId: PlayerId,
                      val recipientId: PlayerId,
                      applicationId: String,
                      id: Id<Action>,
                      date: Instant,
                      state: EventState,
                      val metadata: ActionMetadata = ActionMetadata()) : Event(applicationId, id, date, state) {

    abstract fun toMessage(): Message

    override fun obfuscate(mode: StringObfuscatorMode): Event {
        return obfuscate(mode, playerId)
    }

    abstract fun obfuscate(mode: StringObfuscatorMode, playerId: PlayerId): Event

    @Suppress("UNCHECKED_CAST")
    fun toActionId(): Id<Action> = id as Id<Action>
}
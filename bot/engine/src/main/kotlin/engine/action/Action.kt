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

import ai.tock.bot.definition.ParameterKey
import ai.tock.bot.engine.dialog.EventState
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.message.Message
import ai.tock.bot.engine.user.PlayerId
import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.time.Instant
import org.litote.kmongo.Id
import org.litote.kmongo.newId

/**
 * A user (or bot) action.
 */
abstract class Action(
    val playerId: PlayerId,
    val recipientId: PlayerId,
    connectorId: String,
    id: Id<Action>,
    date: Instant,
    state: EventState,
    val metadata: ActionMetadata = ActionMetadata()
) : Event(connectorId, id, date, state) {
    @Deprecated("Use constructor with connectorId", ReplaceWith("Action(playerId = playerId, recipientId = recipientId, connectorId = applicationId, id = id, date = date, state = state, metadata = metadata)"))
    constructor(
        playerId: PlayerId,
        recipientId: PlayerId,
        applicationId: String,
        id: Id<Action> = newId(),
        date: Instant = Instant.now(),
        state: EventState = EventState(),
        metadata: ActionMetadata = ActionMetadata(),
        _deprecatedConstructor: Nothing? = null,
    ): this(playerId, recipientId, applicationId, id, date, state, metadata)

    abstract fun toMessage(): Message

    @Suppress("UNCHECKED_CAST")
    fun toActionId(): Id<Action> = id as Id<Action>

    /**
     * Returns the value of the specified choice parameter,
     * null if the user action is not a [SendChoice]
     * or if this parameter is not set.
     */
    fun choice(key: ParameterKey): String? =
        (this as? SendChoice)
            ?.parameters
            ?.get(key.key)

    /**
     * Returns true if the specified choice as the "true" value, false either.
     */
    fun booleanChoice(key: ParameterKey): Boolean =
        choice(key).equals("true", true)

    /**
     * Returns the value of the specified choice parameter,
     * null if the user action is not a [SendChoice]
     * or if this parameter is not set.
     */
    inline fun <reified T : Any> jsonChoice(key: ParameterKey): T? =
        choice(key)?.let { mapper.readValue(it) }
}

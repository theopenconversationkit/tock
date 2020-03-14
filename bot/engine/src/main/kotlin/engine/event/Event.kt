/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
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

package ai.tock.bot.engine.event

import ai.tock.bot.engine.dialog.EntityValue
import ai.tock.bot.engine.dialog.EventState
import ai.tock.nlp.entity.StringValue
import ai.tock.shared.security.StringObfuscatorMode
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.time.Instant

/**
 * The base class for all events or actions.
 */
abstract class Event(
    /**
     * The bot application id.
     */
    val applicationId: String,
    /**
     * The unique id of the event.
     */
    val id: Id<out Event> = newId(),
    /**
     * The creation date of the event.
     */
    val date: Instant = Instant.now(),
    /**
     * The state of the event.
     */
    val state: EventState = EventState()
) {
    /**
     * Does this event contains specified role entity?
     */
    fun hasEntity(role: String): Boolean {
        return hasSubEntity(state.entityValues, role)
    }

    /**
     * Does this event contains specified predefined value entity?
     */
    fun hasEntityPredefinedValue(role: String, value: String): Boolean {
        return state.getEntity(role).any { (it.value as? StringValue)?.value == value }
    }

    private fun hasSubEntity(entities: List<EntityValue>, role: String): Boolean {
        return entities.any { it.entity.role == role } || entities.any { hasSubEntity(it.subEntities, role) }
    }

    /**
     * Obfuscate the event - by default this method does nothing.
     */
    open fun obfuscate(mode: StringObfuscatorMode): Event = this

}
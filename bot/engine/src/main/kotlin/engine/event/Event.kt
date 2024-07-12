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

package ai.tock.bot.engine.event

import ai.tock.bot.connector.ConnectorConfiguration
import ai.tock.bot.engine.dialog.EventState
import ai.tock.bot.engine.dialog.hasEntityPredefinedValue
import ai.tock.bot.engine.dialog.hasSubEntity
import java.time.Instant
import org.litote.kmongo.Id
import org.litote.kmongo.newId

/**
 * The base class for all events or actions.
 */
abstract class Event(
    /**
     * The TOCK application id.
     *
     * This ID should match the connector identifier in TOCK Studio.
     *
     * @see ConnectorConfiguration.connectorId
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
        return hasEntityPredefinedValue(state.entityValues, role, value)
    }
}

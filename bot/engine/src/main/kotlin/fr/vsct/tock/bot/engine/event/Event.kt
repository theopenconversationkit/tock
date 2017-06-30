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

package fr.vsct.tock.bot.engine.event

import fr.vsct.tock.bot.engine.dialog.EventState
import fr.vsct.tock.shared.Dice
import java.time.Instant

/**
 * The base class for all events or actions.
 */
abstract class Event(
        val applicationId: String,
        val id: String = Dice.newId(),
        val date: Instant = Instant.now(),
        val state: EventState = EventState()
) {
    fun hasEntity(role: String): Boolean {
        return state.getEntity(role).isNotEmpty()
    }

}
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

package fr.vsct.tock.bot.engine.dialog

import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.nlp.api.client.model.Entity
import fr.vsct.tock.nlp.entity.Value

/**
 * EntityStateValue is the value of an entity with his history
 */
data class EntityStateValue(
        private var currentValue: ContextValue?,
        private val oldValues: MutableList<ArchivedEntityValue> = mutableListOf()) {

    internal constructor(action: Action, entityValue: ContextValue)
            : this(entityValue, mutableListOf(ArchivedEntityValue(entityValue, action)))

    internal constructor(entity: Entity, value: Value) : this(ContextValue(entity, value))

    internal fun changeValue(entity: Entity, newValue: Value?, action: Action? = null): EntityStateValue {
        return changeValue(ContextValue(entity, newValue), action)
    }

    internal fun changeValue(newValue: ContextValue?, action: Action? = null): EntityStateValue {
        //do not change history if previous value is exactly the same
        if (oldValues.lastOrNull()?.entityValue?.let { currentValue != it } ?: true) {
            oldValues.add(ArchivedEntityValue(currentValue, action))
        }
        currentValue = newValue

        return this
    }

    /**
     * Current entity's value
     */
    val value: ContextValue? get() = currentValue?.copy()

    /**
     * Entity's history
     */
    val history: List<ArchivedEntityValue> get() = oldValues.toList()

}
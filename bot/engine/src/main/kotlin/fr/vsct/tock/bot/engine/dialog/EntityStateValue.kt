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
import fr.vsct.tock.nlp.api.client.model.NlpEntity
import fr.vsct.tock.nlp.entity.Value
import java.time.Instant
import java.time.Instant.now

/**
 * EntityStateValue is the current value of an entity with its history.
 */
data class EntityStateValue(
    private var _value: EntityValue?,
    private val _history: MutableList<ArchivedEntityValue> = mutableListOf(),
    private var _lastUpdate: Instant = now()) {

    internal constructor(action: Action, entityValue: EntityValue)
            : this(entityValue, mutableListOf(ArchivedEntityValue(entityValue, action)))

    internal constructor(entity: NlpEntity, value: Value) : this(EntityValue(entity, value))

    init {
        if (value != null) {
            _history.add(ArchivedEntityValue(value, null, _lastUpdate))
        }
    }

    internal fun changeValue(entity: NlpEntity, newValue: Value?, action: Action? = null): EntityStateValue {
        return changeValue(EntityValue(entity, newValue), action)
    }

    internal fun changeValue(newValue: EntityValue?, action: Action? = null): EntityStateValue {
        _lastUpdate = now()
        _value = newValue
        //do not change history if previous value is exactly the same
        if (_history.lastOrNull()?.entityValue != newValue) {
            _history.add(ArchivedEntityValue(newValue, action, _lastUpdate))
        }

        return this
    }

    /**
     * Current entity's value
     */
    val value: EntityValue? get() = _value?.copy()

    /**
     * Returns previous values for this entity.
     */
    val previousValues: List<ArchivedEntityValue>
        get() = _history.run { if (isEmpty()) emptyList() else subList(0, size - 1) }

    /**
     * Entity's all history. First is older. Last in current value.
     * Could be empty if there is no history and current value is null.
     */
    val history: List<ArchivedEntityValue> get() = _history.toList()

    /**
     * The last update date of the value.
     */
    val lastUpdate: Instant get() = _lastUpdate

}
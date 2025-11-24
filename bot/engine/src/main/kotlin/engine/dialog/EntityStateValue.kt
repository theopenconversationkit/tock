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

package ai.tock.bot.engine.dialog

import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.user.UserTimelineDAO
import ai.tock.nlp.api.client.model.Entity
import ai.tock.nlp.entity.Value
import ai.tock.shared.injector
import ai.tock.shared.provide
import kotlinx.coroutines.runBlocking
import org.litote.kmongo.Id
import java.time.Instant
import java.time.Instant.now

/**
 * EntityStateValue is the current value of an entity with its history.
 */
data class EntityStateValue(
    /**
     * The current value.
     */
    private var currentValue: EntityValue?,
    /**
     * The current history.
     */
    private val currentHistory: MutableList<ArchivedEntityValue> = mutableListOf(),
    /**
     * The initial update date of the state.
     */
    private val initialUpdate: Instant = now(),
    /**
     * State value id if any.
     */
    val stateValueId: Id<EntityStateValue>? = null,
    /**
     * Old action map in order to retrieve lazily the history.
     */
    private val oldActionsMap: Map<Id<Action>, Action> = emptyMap(),
) {
    private var updated: Instant = initialUpdate
    private var loaded: Boolean = stateValueId == null
    internal var multiRequestedValues: List<EntityValue>? = null

    constructor(action: Action, entityValue: EntityValue) :
        this(entityValue, mutableListOf(ArchivedEntityValue(entityValue, action)))

    constructor(entity: Entity, value: Value) : this(EntityValue(entity, value))

    init {
        currentHistory.add(ArchivedEntityValue(currentValue, null, updated))
    }

    internal fun changeValue(
        entity: Entity,
        newValue: Value?,
        action: Action? = null,
    ): EntityStateValue {
        return changeValue(EntityValue(entity, newValue), action)
    }

    internal fun changeValue(
        newValue: EntityValue?,
        action: Action? = null,
    ): EntityStateValue {
        updated = now()
        currentValue = newValue
        // do not change history if previous value is exactly the same
        if (currentHistory.lastOrNull()?.entityValue != newValue) {
            currentHistory.add(ArchivedEntityValue(newValue, action, updated))
        }

        return this
    }

    private fun checkLoadedValue() {
        if (!loaded) {
            loaded = true
            val old =
                stateValueId?.let {
                    runBlocking { injector.provide<UserTimelineDAO>().getArchivedEntityValues(stateValueId, oldActionsMap) }
                }
                    ?: emptyList()
            currentHistory.addAll(0, old)
            // remove duplicate if any
            if (old.isNotEmpty() &&
                currentHistory.size != old.size &&
                old.last().entityValue?.value == currentHistory[old.size].entityValue?.value
            ) {
                currentHistory.removeAt(old.size - 1)
            }
        }
    }

    override fun toString(): String {
        return "EntityStateValue(currentValue=$currentValue, currentHistory=$currentHistory, initialUpdate=$initialUpdate, stateValueId=$stateValueId, updated=$updated, loaded=$loaded)"
    }

    /**
     * Current entity value
     */
    val value: EntityValue? get() = currentValue?.copy()

    /**
     * Previous values for this entity.
     */
    val previousValues: List<ArchivedEntityValue>
        get() {
            checkLoadedValue()
            return currentHistory.run { if (isEmpty()) this else subList(0, size - 1) }
        }

    /**
     * Returns the penultimate value.
     */
    val penultimateValue: ArchivedEntityValue? get() = previousValues.lastOrNull()

    /**
     * Entity's all history. First is older. Last in current value.
     * Could be empty if there is no history and current value is null.
     */
    val history: List<ArchivedEntityValue>
        get() {
            checkLoadedValue()
            return currentHistory.toList()
        }

    /**
     * The last update date of the value.
     */
    val lastUpdate: Instant get() = updated

    /**
     * Is this state has been updated in current [ai.tock.bot.engine.BotBus]?
     */
    val hasBeanUpdatedInBus: Boolean get() = initialUpdate != updated

    /**
     *  All values for the current request when merge is not applicable.
     */
    val newValues: List<EntityValue> get() = multiRequestedValues ?: listOfNotNull(currentValue)
}

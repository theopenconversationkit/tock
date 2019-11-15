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

package ai.tock.bot.engine.dialog

import ai.tock.bot.definition.Intent
import ai.tock.bot.engine.user.UserLocation
import ai.tock.nlp.api.client.model.Entity
import ai.tock.nlp.entity.Value

/**
 * The [Dialog] state.
 */
data class DialogState(
    /**
     * The current [Intent] of the dialog, can be null.
     */
    var currentIntent: Intent? = null,
    /**
     * The current entity values (with their history).
     */
    val entityValues: MutableMap<String, EntityStateValue> = mutableMapOf(),
    /**
     * The context of the dialog, a versatile map.
     */
    val context: MutableMap<String, Any> = mutableMapOf(),
    /**
     * The current [UserLocation] if any.
     */
    var userLocation: UserLocation? = null,
    /**
     * The [NextUserActionState] if any.
     * If not null, it will be applied to the next user action, with NLP custom qualifiers.
     */
    var nextActionState: NextUserActionState? = null
) {

    companion object {
        /**
         * Init a new state from the specified state.
         */
        fun initFromDialogState(dialog: DialogState): DialogState {
            return DialogState(
                dialog.currentIntent,
                dialog.entityValues.map { it.key to EntityStateValue(it.value.value) }.toMap().toMutableMap(),
                dialog.context,
                dialog.userLocation,
                dialog.nextActionState
            )
        }
    }

    /**
     * Updates persistent context value.
     * Do not store Collection or Map in the context, only plain objects or typed arrays.
     */
    fun setContextValue(name: String, value: Any?) {
        if (value == null) {
            context.remove(name)
        } else {
            if (value is Collection<*> || value is Map<*, *>) {
                error("Storing collection or map is dialog context is unsupported, use plain objects or typed arrays")
            }
            context[name] = value
        }
    }


    /**
     * Set a new entity value. Remove previous entity values history.
     *
     * @role the role of the entity
     * @value the new entity value
     */
    fun setValue(role: String, value: EntityValue) {
        entityValues[role] = EntityStateValue(value)
    }

    /**
     * Set a new entity value. Remove previous entity values history.
     *
     * @entity the entity
     * @value the new entity value
     */
    fun setValue(entity: Entity, value: Value) {
        entityValues[entity.role] = EntityStateValue(entity, value)
    }

    /**
     * Change an entity value. Keep previous entity values history.
     *
     * @entity the entity
     * @newValue the new entity value
     */
    fun changeValue(entity: Entity, newValue: Value?) {
        entityValues[entity.role]?.changeValue(entity, newValue)
                ?: if (newValue != null) setValue(entity, newValue)
    }

    /**
     * Change an entity value. Keep previous entity values history.
     *
     * @newValue the new entity value
     */
    fun changeValue(newValue: EntityValue) = changeValue(newValue.entity.role, newValue)

    /**
     * Change an entity value. Keep previous entity values history.
     *
     * @role the role of the entity
     * @newValue the new entity value
     */
    fun changeValue(role: String, newValue: EntityValue?) {
        entityValues[role]?.changeValue(newValue)
                ?: if (newValue != null) setValue(role, newValue)
    }

    /**
     * Reset all entity values. Keep entity values history.
     */
    fun resetAllEntityValues() {
        entityValues.forEach {
            resetValue(it.key)
        }
    }

    /**
     * Reset the value of an entity. Keep entity values history.
     *
     * @role the role of the entity
     */
    fun resetValue(role: String) {
        changeValue(role, null)
    }

    /**
     * Same than [resetState] but remove also entity values history.
     */
    fun cleanupState() {
        entityValues.clear()
        context.clear()
        userLocation = null
        nextActionState = null
    }

    /**
     * Reset all entity values, context values, [userLocation] and [nextActionState]
     * but keep entity values history.
     */
    fun resetState() {
        resetAllEntityValues()
        context.clear()
        userLocation = null
        nextActionState = null
    }

}
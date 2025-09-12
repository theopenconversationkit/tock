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

import ai.tock.bot.definition.DialogContextKey
import ai.tock.bot.definition.Intent
import ai.tock.bot.engine.user.UserLocation
import ai.tock.nlp.api.client.model.Entity
import ai.tock.nlp.entity.Value
import ai.tock.shared.intProperty

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
        private const val SWITCH_STORY_BUS_KEY = "_tock_switch"
        private const val ASK_AGAIN_STORY_BUS_KEY = "_tock_ask_again"
        private const val ASK_AGAIN_STORY_ROUND_BUS_KEY = "_tock_ask_again_round"

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

    internal var hasCurrentSwitchStoryProcess: Boolean
        get() = context[SWITCH_STORY_BUS_KEY] == true
        set(v) {
            context[SWITCH_STORY_BUS_KEY] = v
        }

    internal var hasCurrentAskAgainProcess: Boolean
        get() = context[ASK_AGAIN_STORY_BUS_KEY] == true
        set(v) {
            context[ASK_AGAIN_STORY_BUS_KEY] = v
        }

    private val askAgainRoundDefault = intProperty("tock_ask_again_round", 1)

    //askAgain round with default value
    internal var askAgainRound: Int = askAgainRoundDefault
        get() {
            //retrieve default value
            val value = context[ASK_AGAIN_STORY_ROUND_BUS_KEY] as? Int
            return if (value == null) {
                context[ASK_AGAIN_STORY_ROUND_BUS_KEY] = field
                field
                //or retrieve current value
            } else {
                value
            }
        }
        set(v) {
            context[ASK_AGAIN_STORY_ROUND_BUS_KEY] = v
            field = v
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
     * Updates persistent context value.
     * Do not store generic objects like Collection or Map in the context, only plain objects or typed arrays.
     */
    fun <T : Any> setContextValue(key: DialogContextKey<T>, value: Any?) {
        require(key.type.typeParameters.isEmpty()) {
            "Generic type parameters cannot be safely preserved in a dialog context"
        }

        if (value == null) {
            context.remove(key.name)
        } else {
            context[key.name] = value
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
            ?: if (newValue != null) {
                setValue(entity, newValue)
            } else {
            }
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
            ?: if (newValue != null) {
                setValue(role, newValue)
            } else {
            }
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

    private fun currentEntityValues(): List<EntityValue> = entityValues.mapNotNull { it.value.value }

    fun hasEntity(role: String): Boolean {
        return hasSubEntity(currentEntityValues(), role)
    }

    /**
     * Does this event contains specified predefined value entity?
     */
    fun hasEntityPredefinedValue(role: String, value: String): Boolean {
        return hasEntityPredefinedValue(currentEntityValues(), role, value)
    }
}

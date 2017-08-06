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

import fr.vsct.tock.bot.definition.Intent
import fr.vsct.tock.bot.engine.user.UserLocation
import fr.vsct.tock.nlp.api.client.model.Entity
import fr.vsct.tock.nlp.entity.Value

/**
 *
 */
data class DialogState(
        var currentIntent: Intent? = null,
        val entityValues: MutableMap<String, EntityStateValue> = mutableMapOf(),
        val context: MutableMap<String, Any> = mutableMapOf(),
        var userLocation: UserLocation? = null,
        var nextActionState: NextUserActionState? = null) {

    fun setValue(role: String, value: ContextValue) {
        entityValues[role] = EntityStateValue(value)
    }

    fun setValue(entity: Entity, value: Value) {
        entityValues[entity.role] = EntityStateValue(entity, value)
    }

    fun changeValue(entity: Entity, newValue: Value?) {
        entityValues[entity.role]?.changeValue(entity, newValue)
                ?: if (newValue != null) setValue(entity, newValue)
    }

    fun changeValue(role: String, newValue: ContextValue?) {
        entityValues[role]?.changeValue(newValue)
                ?: if (newValue != null) setValue(role, newValue)
    }

    fun removeAllEntityValues() {
        entityValues.forEach {
            changeValue(it.key, null)
        }
    }

    fun removeValue(role: String) {
        changeValue(role, null)
    }

}
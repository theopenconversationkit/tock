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

/**
 *
 */
data class State(
        var currentIntent: Intent? = null,
        val entityValues: MutableMap<String, EntityStateValue> = mutableMapOf(),
        val context: MutableMap<String, Any> = mutableMapOf()) {

    companion object {
        const val userLocationFlag = "tock_user_location"
    }

    var userLocation: UserLocation?
        get() = context[userLocationFlag] as UserLocation?
        set(value) {
            if (value == null) context -= userLocationFlag
            else context[userLocationFlag] = value
        }

    fun setValue(role: String, value: ContextValue) {
        entityValues[role] = EntityStateValue(value)
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
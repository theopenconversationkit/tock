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

package fr.vsct.tock.bot.engine.user

import fr.vsct.tock.shared.longProperty
import java.time.Duration
import java.time.Instant
import java.time.Instant.now

/**
 * The user state.
 */
data class UserState(
        val creationDate: Instant = now(),
        val flags: MutableMap<String, TimeBoxedFlag> = mutableMapOf()) {

    companion object {
        private const val PROFILE_LOADED_FLAG = "tock_profile_loaded"
        private const val BOT_DISABLED_FLAG = "tock_bot_disabled"
    }

    /**
     * Cleanup the state.
     */
    fun cleanup() {
        flags.clear()
    }

    var profileLoaded: Boolean
        get() = getFlag(PROFILE_LOADED_FLAG)?.toBoolean() ?: false
        set(value) {
            if (value)
                setUnlimitedFlag(PROFILE_LOADED_FLAG, value.toString())
            else removeFlag(PROFILE_LOADED_FLAG)
        }

    var botDisabled: Boolean
        get() = getFlag(BOT_DISABLED_FLAG)?.toBoolean() ?: false
        set(value) {
            if (value)
                setFlag(
                        BOT_DISABLED_FLAG,
                        longProperty("tock_bot_disabled_duration_in_minutes", 60 * 24 * 5),
                        value.toString()
                )
            else removeFlag(BOT_DISABLED_FLAG)
        }

    fun getFlag(flag: String): String? {
        val f = flags[flag]
        return if (f?.isValid() ?: false) {
            f?.value
        } else {
            null
        }
    }

    fun hasFlag(flag: String): Boolean
            = getFlag(flag) != null

    fun setFlag(flag: String, timeoutInMinutes: Long, value: String) {
        setFlag(flag, Duration.ofMinutes(timeoutInMinutes), value)
    }

    fun setFlag(flag: String, timeoutDuration: Duration, value: String) {
        flags[flag] = TimeBoxedFlag(value, now().plus(timeoutDuration))
    }

    fun removeFlag(flag: String) {
        flags -= flag
    }

    fun setUnlimitedFlag(flag: String, value: String) {
        flags[flag] = TimeBoxedFlag(value, null)
    }


}
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

package ai.tock.bot.engine.user

import ai.tock.shared.longProperty
import java.time.Duration
import java.time.Instant
import java.time.Instant.now

/**
 * The user state.
 */
data class UserState(
    /** The user creation date. **/
    val creationDate: Instant = now(),
    /**
     * The flag for this user - useful to store basic information about this user.
     */
    val flags: MutableMap<String, TimeBoxedFlag> = mutableMapOf(),
) {
    companion object {
        private const val PROFILE_LOADED_FLAG = "tock_profile_loaded"
        private const val PROFILE_REFRESHED_FLAG = "tock_profile_refreshed"
        private const val BOT_DISABLED_FLAG = "tock_bot_disabled"

        /**
         * Default refresh profile duration.
         */
        private val refreshDuration: Long = longProperty("tock_bot_refresh_profil_duration_in_minutes", 60 * 24 * 5)
        private val disabledDuration: Long = longProperty("tock_bot_disabled_duration_in_minutes", 60 * 24 * 5)
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
            if (value) {
                setUnlimitedFlag(PROFILE_LOADED_FLAG, value.toString())
            } else {
                removeFlag(PROFILE_LOADED_FLAG)
            }
        }

    internal var profileRefreshed: Boolean
        get() = getFlag(PROFILE_REFRESHED_FLAG)?.toBoolean() ?: false
        set(value) {
            if (value) {
                setFlag(
                    PROFILE_REFRESHED_FLAG,
                    refreshDuration,
                    "true",
                )
            } else {
                removeFlag(PROFILE_REFRESHED_FLAG)
            }
        }

    var botDisabled: Boolean
        get() = getFlag(BOT_DISABLED_FLAG)?.toBoolean() ?: false
        set(value) {
            if (value) {
                setFlag(
                    BOT_DISABLED_FLAG,
                    disabledDuration,
                    "true",
                )
            } else {
                removeFlag(BOT_DISABLED_FLAG)
            }
        }

    fun getFlag(flag: String): String? {
        val f = flags[flag]
        return if (f?.isValid() == true) {
            f.value
        } else {
            null
        }
    }

    fun hasFlag(flag: String): Boolean = getFlag(flag) != null

    fun setFlag(
        flag: String,
        timeoutInMinutes: Long,
        value: String,
    ) {
        setFlag(flag, Duration.ofMinutes(timeoutInMinutes), value)
    }

    fun setFlag(
        flag: String,
        timeoutDuration: Duration,
        value: String,
    ) {
        flags[flag] = TimeBoxedFlag(value, now().plus(timeoutDuration))
    }

    fun removeFlag(flag: String) {
        flags -= flag
    }

    fun setUnlimitedFlag(
        flag: String,
        value: String,
    ) {
        flags[flag] = TimeBoxedFlag(value, null)
    }
}

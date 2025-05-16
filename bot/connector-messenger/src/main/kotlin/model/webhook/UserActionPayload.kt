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

package ai.tock.bot.connector.messenger.model.webhook

import java.util.regex.Pattern

private val PATTERN =
    Pattern.compile("[a-zA-Z0-9\\.\\_\\-]{1,256}\\@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+")

data class UserActionPayload(val payload: String?, val referral: Referral? = null) {
    fun hasEmailPayloadFromMessenger(): Boolean {
        val e = payload?.trim()
        if (e.isNullOrEmpty()) {
            return false
        }

        // Copy from ANDROID10 Patterns API. (because API8- does not support it)

        val matcher = PATTERN.matcher(e)
        val lastPointPosition = e.lastIndexOf('.')
        return e.trim { it <= ' ' }.isNotEmpty() && matcher.matches() && lastPointPosition != -1 && lastPointPosition < e.length - 2
    }
}

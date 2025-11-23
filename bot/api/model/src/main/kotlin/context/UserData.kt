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

package ai.tock.bot.api.model.context

import java.time.ZoneId
import java.util.Locale

data class UserData(
    /**
     * First name of the user.
     */
    var firstName: String? = null,
    /**
     * Last name of the user.
     */
    var lastName: String? = null,
    /**
     * Email of the user.
     */
    var email: String? = null,
    /**
     * Timezone of the user.
     */
    var timezone: ZoneId,
    /**
     * Locale of the user.
     */
    var locale: Locale,
    /**
     * Picture url of the user.
     */
    var picture: String? = null,
    /**
     * Gender of the user.
     */
    var gender: String? = null,
    /**
     * Is it a test user?
     */
    var test: Boolean = false,
)

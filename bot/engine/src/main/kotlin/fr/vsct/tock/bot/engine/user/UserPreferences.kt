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

import fr.vsct.tock.shared.defaultLocale
import fr.vsct.tock.shared.defaultZoneId
import java.time.ZoneId
import java.util.Locale

/**
 *
 */
data class UserPreferences(var firstName: String? = null,
                           var lastName: String? = null,
                           var email: String? = null,
                           var timezone: ZoneId = defaultZoneId,
                           var locale: Locale = defaultLocale,
                           var picture: String? = null,
                           var gender: String? = null) {

    fun copy(userPref: UserPreferences) {
        firstName = userPref.firstName
        lastName = userPref.lastName
        email = userPref.email
        timezone = userPref.timezone
        locale = userPref.locale
        picture = userPref.picture
        gender = userPref.gender
    }

}
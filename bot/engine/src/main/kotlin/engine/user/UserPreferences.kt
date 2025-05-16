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

import ai.tock.shared.defaultLocale
import ai.tock.shared.defaultZoneId
import java.time.ZoneId
import java.util.Locale

/**
 * User preferences.
 */
data class UserPreferences(
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
    var timezone: ZoneId = defaultZoneId,
    /**
     * Locale of the user.
     */
    var locale: Locale = defaultLocale,
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
    /**
     * The initial locale of the user
     */
    var initialLocale: Locale = locale,
    /**
     * Full name or screen name of the user in the application.
     */
    var username: String? = null,
    /**
     * Phone number of the user of application in E.164 format : +33612345678.
     */
    var phoneNumber: String? = null,

    ) {

    /**
     * Fill the current [UserPreferences] with the specified [UserPreferences].
     */
    fun fillWith(userPref: UserPreferences) {
        firstName = userPref.firstName
        lastName = userPref.lastName
        email = userPref.email
        timezone = userPref.timezone
        locale = userPref.locale
        picture = userPref.picture
        gender = userPref.gender
        test = userPref.test
        initialLocale = userPref.locale
        username = userPref.username
        phoneNumber = userPref.phoneNumber
    }

    /**
     * Refresh the current [UserPreferences] with the specified [UserPreferences].
     * Only not null values are taken into account.
     */
    fun refreshWith(userPref: UserPreferences) {
        userPref.firstName?.also { firstName = it }
        userPref.lastName?.also { lastName = it }
        userPref.email?.also { email = it }
        timezone = userPref.timezone
        locale = userPref.locale
        userPref.picture?.also { picture = it }
        userPref.gender?.also { gender = it }
        userPref.username?.also { username = it }
        userPref.phoneNumber?.also { phoneNumber = it }
    }
}

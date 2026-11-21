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

package ai.tock.bot.definition

import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.user.UserPreferences
import java.util.Locale

/**
 * Define values and behaviour for integration tests.
 */
interface TestBehaviour {
    /**
     * The default first name used in tests.
     */
    val firstName: String

    /**
     * The default last name used in tests.
     */
    val lastName: String

    /**
     * Setup [UserPreferences] from the bus.
     */
    fun setup(bus: BotBus) {
        bus.userTimeline.userState.cleanup()
        bus.userTimeline.userState.profileLoaded = true
        setup(bus.userPreferences, bus.targetConnectorType, bus.userLocale)
    }

    /**
     * Setup user preferences for test context.
     */
    fun setup(
        userPreferences: UserPreferences,
        connectorType: ConnectorType,
        locale: Locale,
    ) {
        userPreferences.test = true
        userPreferences.firstName = firstName
        userPreferences.lastName = lastName
        userPreferences.locale = locale
    }

    fun cleanup(bus: BotBus) {
        bus.userTimeline.userState.cleanup()
        bus.userPreferences.fillWith(UserPreferences())
    }
}

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

import fr.vsct.tock.bot.engine.BotBus
import fr.vsct.tock.bot.engine.user.UserPreferences
import fr.vsct.tock.shared.property

/**
 * Define values for test context.
 */
object TestContext {

    /**
     * The default first name used in tests.
     */
    val firstName = property("tock_bot_test_first_name", "Joe")
    /**
     * The default last name used in tests.
     */
    val lastName = property("tock_bot_test_last_name", "Hisaishi")

    /**
     * Setup the bus for test context.
     */
    fun setup(bus: BotBus) {
        bus.userTimeline.userState.cleanup()
        bus.userTimeline.userState.profileLoaded = true
        setup(bus.userPreferences)
    }

    /**
     * Setup user preferences for test context.
     */
    fun setup(userPreferences: UserPreferences) {
        with(userPreferences) {
            test = true
            firstName = TestContext.firstName
            lastName = TestContext.lastName
        }
    }

    fun cleanup(bus: BotBus) {
        bus.userTimeline.userState.cleanup()
        bus.userPreferences.fillWith(UserPreferences())
    }
}
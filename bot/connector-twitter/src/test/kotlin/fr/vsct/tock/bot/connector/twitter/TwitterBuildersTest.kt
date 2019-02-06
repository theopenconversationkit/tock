/*
 * Copyright (C) 2019 VSCT
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

package fr.vsct.tock.bot.connector.twitter

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import fr.vsct.tock.bot.engine.BotBus
import fr.vsct.tock.bot.engine.user.UserPreferences
import fr.vsct.tock.shared.sharedTestModule
import fr.vsct.tock.shared.tockInternalInjector
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach

class TwitterBuildersTest {

    val bus: BotBus = mockk(relaxed = true)

    @BeforeEach
    fun before() {
        tockInternalInjector = KodeinInjector().apply {
            inject(Kodein {
                import(sharedTestModule)
            })
        }

        every { bus.targetConnectorType } returns twitterConnectorType
        every { bus.applicationId } returns "appId"
        every { bus.userPreferences } returns UserPreferences()
        every { bus.translate(allAny()) } answers { firstArg() ?: "" }
    }


}
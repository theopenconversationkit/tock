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

package ai.tock.bot.connector.twitter

import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.UserPreferences
import ai.tock.shared.sharedTestModule
import ai.tock.shared.tockInternalInjector
import ai.tock.translator.raw
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TwitterBuildersTest {

    val bus: BotBus = mockk(relaxed = true)

    @BeforeEach
    fun before() {
        tockInternalInjector = KodeinInjector().apply {
            inject(
                Kodein {
                    import(sharedTestModule)
                }
            )
        }

        every { bus.targetConnectorType } returns twitterConnectorType
        every { bus.applicationId } returns "appId"
        every { bus.userId } returns PlayerId("userId")
        every { bus.botId } returns PlayerId("botId")
        every { bus.userPreferences } returns UserPreferences()
        every { bus.translate(allAny()) } answers { firstArg() ?: "".raw }
    }

    @Test
    fun `strip dots lesser for twitter`() {
        val givenStringLesserThan: CharSequence = "String Text"

        assertEquals("String Text", givenStringLesserThan.truncateIfLongerThan(12))
        assertEquals("", givenStringLesserThan.truncateIfLongerThan(0))
        assertEquals("S", givenStringLesserThan.truncateIfLongerThan(1))
        assertEquals("St", givenStringLesserThan.truncateIfLongerThan(2))
        assertEquals("Str", givenStringLesserThan.truncateIfLongerThan(3))
        assertEquals("S...", givenStringLesserThan.truncateIfLongerThan(4))
        assertEquals("String Text", givenStringLesserThan.truncateIfLongerThan(-1))
    }

    @Test
    fun `strip dots longer for twitter`() {
        val givenStringMoreThan: CharSequence = "String Text 1 String Text 1"

        assertEquals("String T...", givenStringMoreThan.truncateIfLongerThan(11))
    }

    @Test
    fun `strip dots for twitter empty`() {
        val givenStringEmpty: CharSequence = ""

        assertEquals("", givenStringEmpty.truncateIfLongerThan(11))
    }

    @Test
    fun `no strip dots for twitter equal`() {
        val givenStringEqual: CharSequence = "String Text"

        assertEquals("String Text", givenStringEqual.truncateIfLongerThan(11))
    }
}

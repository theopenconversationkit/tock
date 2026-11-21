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

import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.engine.feature.FeatureDAO
import ai.tock.bot.engine.feature.FeatureRuntimeDAO
import ai.tock.bot.engine.feature.FeatureType
import ai.tock.bot.engine.feature.asRuntimeDAO
import ai.tock.shared.tockInternalInjector
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import kotlin.test.assertSame
import kotlin.test.assertTrue

class RuntimeDAOTest {
    private val userTimelineDAO: UserTimelineDAO = mockk(relaxed = true)

    @AfterEach
    fun reset() {
        tockInternalInjector = KodeinInjector()
    }

    private fun inject(module: Kodein.Builder.() -> Unit) {
        tockInternalInjector = KodeinInjector().apply { inject(Kodein(init = module)) }
    }

    @Test
    fun `GIVEN only the complete DAO is bound WHEN the runtime DAO is used THEN it delegates to the complete DAO with its default values`() {
        inject { bind<UserTimelineDAO>() with provider { userTimelineDAO } }
        val timeline: UserTimeline = mockk()
        val botDefinition: BotDefinition = mockk()

        runBlocking { UserTimelineRuntimeDAO.fromInjector().save(timeline, botDefinition) }

        coVerify { userTimelineDAO.save(timeline, botDefinition, true) }
    }

    @Test
    fun `GIVEN a runtime DAO is bound WHEN it is looked up THEN it is preferred to the complete DAO`() {
        val runtimeDAO: UserTimelineRuntimeDAO = mockk()
        inject {
            bind<UserTimelineDAO>() with provider { userTimelineDAO }
            bind<UserTimelineRuntimeDAO>() with provider { runtimeDAO }
        }

        assertSame(runtimeDAO, UserTimelineRuntimeDAO.fromInjector())
    }

    @Test
    fun `GIVEN the overloads of isEnabled WHEN the adapter is called THEN each one reaches the same overload of the DAO`() {
        val featureDAO: FeatureDAO = mockk()
        coEvery { featureDAO.isEnabled(any(), any(), any<FeatureType>(), any<Boolean>(), any()) } returns true
        coEvery { featureDAO.isEnabled(any(), any(), any<String>(), any<String>(), any<Boolean>(), any()) } returns true
        coEvery { featureDAO.isEnabled(any(), any(), any<FeatureType>(), any<String>(), any(), any()) } returns true
        coEvery { featureDAO.isEnabled(any(), any(), any<String>(), any(), any<String>(), any(), any()) } returns true
        val adapter: FeatureRuntimeDAO = featureDAO.asRuntimeDAO()
        val type =
            object : FeatureType {
                override val category: String = "c"
                override val name: String = "n"
            }

        runBlocking {
            assertTrue(adapter.isEnabled("bot", "ns", type, default = true))
            assertTrue(adapter.isEnabled("bot", "ns", "c", "n", default = true))
            assertTrue(adapter.isEnabled("bot", "ns", type, "app"))
            assertTrue(adapter.isEnabled("bot", "ns", "c", "n", "app"))
        }

        coVerify { featureDAO.isEnabled("bot", "ns", type, true, null) }
        coVerify { featureDAO.isEnabled("bot", "ns", "c", "n", true, null) }
        coVerify { featureDAO.isEnabled("bot", "ns", type, "app", false, null) }
        coVerify { featureDAO.isEnabled("bot", "ns", "c", "n", "app", false, null) }
    }
}

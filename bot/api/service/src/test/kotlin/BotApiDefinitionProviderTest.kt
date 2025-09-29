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

import ai.tock.bot.admin.bot.BotConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.api.service.BotApiDefinitionProvider
import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.engine.user.UserTimelineDAO
import ai.tock.shared.Executor
import ai.tock.shared.tockInternalInjector
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import io.mockk.coJustRun
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.math.E
import kotlin.test.assertNotEquals

class BotApiDefinitionProviderTest {

    private val storyDefinitionDAO: StoryDefinitionConfigurationDAO = mockk {
        every { getStoryDefinitionByNamespaceAndBotIdAndStoryId(any(), any(), any()) } returns
                mockk {
                    every { findEnabledEndWithStoryId(any()) } returns "endWithStoryId"
                }
    }

    private val userTimelineDAO: UserTimelineDAO = mockk {
        coJustRun { save(any(), any() as BotDefinition) }
    }

    @Test
    fun `BotApiDefinitionProvider with same botId and configuration name but different namespaces are not equals`() {
        tockInternalInjector = KodeinInjector()
        tockInternalInjector.inject(
            Kodein.invoke {
                bind<Executor>() with provider {mockk(relaxed = true) }
                bind<StoryDefinitionConfigurationDAO>() with provider { storyDefinitionDAO }
                bind<UserTimelineDAO>() with provider { userTimelineDAO }
            }
        )
        val p1 = BotApiDefinitionProvider(BotConfiguration("name", "id", "namespace1", "name"))
        val p2 = BotApiDefinitionProvider(BotConfiguration("name", "id", "namespace2", "name"))
        assertNotEquals(p1, p2)
    }
}

/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
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

package ai.tock.bot.engine.config

import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationFeature
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.BotDefinitionTest
import ai.tock.bot.engine.StepTest
import ai.tock.bot.engine.TestStoryDefinition
import ai.tock.bot.engine.TestStoryDefinition.test
import ai.tock.bot.engine.dialog.Story
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class ConfiguredStoryHandlerTest {

    @Test
    fun `switchStory switch story and reset step if same story def`() {
        val feature: StoryDefinitionConfigurationFeature = mockk {
            every { switchToStoryId } returns test.id
        }
        val configuration: StoryDefinitionConfiguration = mockk {
            every { findEnabledFeature("appId") } returns feature
        }
        val st: Story = mockk {
            every { definition } returns test
        }
        val bus: BotBus = mockk {
            every { applicationId } returns "appId"
            every { botDefinition } returns BotDefinitionTest()
            every { step } returns StepTest.s1
            every { story } returns st
            every { step = null } returns Unit
            every { handleAndSwitchStory(TestStoryDefinition.test, TestStoryDefinition.test.mainIntent()) } returns Unit
        }

        val handler = ConfiguredStoryHandler(configuration)

        handler.handle(bus)

        verify { bus.step = null }
        verify { bus.handleAndSwitchStory(test, test.mainIntent()) }
    }
}
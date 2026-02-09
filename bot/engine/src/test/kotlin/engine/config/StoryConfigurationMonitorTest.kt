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

package ai.tock.bot.engine.config

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.definition.SimpleBotDefinition
import ai.tock.bot.definition.story
import ai.tock.bot.engine.Bot
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyAll
import org.junit.jupiter.api.Test

class StoryConfigurationMonitorTest {
    @Test
    fun `StoryConfigurationMonitor deletes obsolete builtin story configurations`() {
        val namespace = "test-namespace"
        val botId = "test-bot-id"
        val dao: StoryDefinitionConfigurationDAO = mockk(relaxed = true)
        val monitor = StoryConfigurationMonitor(dao)
        val botApplication =
            BotApplicationConfiguration(
                applicationId = "test-app",
                botId = botId,
                namespace = namespace,
                nlpModel = "",
                connectorType = ConnectorType.none,
            )
        val testStory = story("test-story") {}
        val definition =
            SimpleBotDefinition(
                botId = botId,
                namespace = namespace,
                stories = listOf(testStory),
            )
        val bot =
            mockk<Bot> {
                every { configuration } returns botApplication
                every { botDefinition } returns BotDefinitionWrapper(definition)
            }
        val builtinStoryConfiguration = StoryDefinitionConfiguration(definition, testStory, null)
        val unregisteredStoryConfiguration = builtinStoryConfiguration.copy(storyId = "unregistered-story-definition")
        every { dao.getStoryDefinitionsByNamespaceAndBotId(namespace, botId) } returns
            listOf(
                builtinStoryConfiguration,
                unregisteredStoryConfiguration,
            )
        monitor.monitor(bot)
        verifyAll {
            dao.listenChanges(any())
            dao.getStoryDefinitionsByNamespaceAndBotId(namespace, botId)
            dao.delete(unregisteredStoryConfiguration)
        }
    }
}

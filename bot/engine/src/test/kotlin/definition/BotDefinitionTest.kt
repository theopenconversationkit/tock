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

import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.engine.BotDefinitionTest
import ai.tock.bot.engine.StoryHandlerTest
import ai.tock.bot.engine.config.BotDefinitionWrapper
import ai.tock.bot.engine.config.ConfiguredStoryDefinition
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.DialogState
import ai.tock.bot.engine.dialog.Story
import ai.tock.translator.I18nLabelValue
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.litote.kmongo.newId
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 *
 */
class BotDefinitionTest {

    private val botDef = BotDefinitionTest()

    @Test
    fun `i18nTranslator() returns an I18nTranslator that use BotDefinition#i18nKeyFromLabel`() {
        val result = botDef.i18nTranslator(Locale.ENGLISH, ConnectorType.none).i18n("test")
        assertEquals(
            I18nLabelValue(
                "namespace_BotTest_test",
                "namespace",
                "bottest",
                "test"
            ),
            result
        )
    }

    @Test
    fun `GIVEN not notified action WHEN the intent is not a enable intent THEN the bot is not activated`() {
        val dialog = mockk<Dialog>()
        val state = DialogState()
        every { dialog.state } returns state
        val result = botDef.enableBot(mockk(), dialog, mockk())
        assertFalse(result)
    }

    @Test
    fun `GIVEN step only intent WHEN findIntent is called THEN the intent is found`() {
        val r = botDef.findIntent("s4_secondary", "appId")
        assertEquals(Intent("s4_secondary"), r)
    }

    @Test
    fun `GIVEN stories containing configured story with specific id WHEN findStoryDefinitionById THEN story is found`() {
        // Given
        val wrap = BotDefinitionWrapper(botDef)

        wrap.updateStories(
            listOf(
                StoryDefinitionConfiguration(
                    botDefinition = botDef,
                    storyDefinition = SimpleStoryDefinition(
                        id = "disable_bot",
                        storyHandler = StoryHandlerTest,
                        starterIntents = setOf(Intent("starter_intent"))
                    ),
                    configurationName = "toto"
                )
            )
        )

        // When
        val result = wrap.findStoryDefinitionById("disable_bot", "appId")

        // Then
        assertNotNull(result)
    }

    @Test
    fun `GIVEN stories containing simple story with specific id WHEN findStoryDefinitionById THEN story is found`() {
        // Given
        val storyConfiguration = SimpleStoryDefinition(
            id = "disable_bot",
            storyHandler = StoryHandlerTest,
            starterIntents = setOf(Intent("starter_intent"))
        )

        val botDef = BotDefinitionBase(
            botId = "test",
            namespace = "namespace",
            stories = listOf(storyConfiguration)
        )

        // When
        val result = botDef.findStoryDefinitionById("disable_bot", "appId")

        // Then
        assertNotNull(result)
    }

    @Test
    fun `GIVEN stories not containing story with specific id WHEN findStoryDefinitionById THEN story is not found`() {
        // Given
        val botDef = BotDefinitionBase(
            botId = "test",
            namespace = "namespace",
            stories = emptyList()
        )

        // When
        val result = botDef.findStoryDefinitionById("disable_bot", "appId")

        // Then
        assertEquals(botDef.unknownStory, result)
    }

    @Test
    fun `GIVEN list of stories WHEN findStoryDefinitionById with non existing id THEN story is not found`() {
        // Given
        val simpleStoryConfiguration = SimpleStoryDefinition(
            id = "disable_bot",
            storyHandler = StoryHandlerTest,
            starterIntents = setOf(Intent("starter_intent"))
        )

        val configuredStoryConfiguration = ConfiguredStoryDefinition(
            BotDefinitionWrapper(botDef),
            StoryDefinitionConfiguration(
                botDefinition = botDef,
                storyDefinition = SimpleStoryDefinition(
                    id = "enable_bot",
                    storyHandler = StoryHandlerTest,
                    starterIntents = setOf(Intent("starter_intent"))
                ),
                configurationName = "toto"
            )
        )

        val botDef = BotDefinitionBase(
            botId = "test",
            namespace = "namespace",
            stories = listOf(simpleStoryConfiguration, configuredStoryConfiguration)
        )

        // When
        val result = botDef.findStoryDefinitionById("should_not_find_id", "appId")

        // Then
        assertEquals(botDef.unknownStory, result)
    }

    @Test
    fun `GIVEN intent story with tag disabled WHEN hasDisableTagIntent THEN return true`() {
        // Given
        val intent = Intent("intent")

        val simpleTaggedStoryDefinition = SimpleStoryDefinition(
            id = "tagged_story",
            storyHandler = StoryHandlerTest,
            starterIntents = setOf(intent),
            tags = setOf(StoryTag.DISABLE)
        )

        val botDef = BotDefinitionBase(
            botId = "test",
            namespace = "namespace",
            stories = listOf(simpleTaggedStoryDefinition)
        )

        val wrapper = BotDefinitionWrapper(botDef)

        val dialog = Dialog(
            playerIds = emptySet(),
            id = newId(),
            state = DialogState(
                currentIntent = intent,
                entityValues = mutableMapOf(),
                context = mutableMapOf()
            ),
            stories = mutableListOf(
                Story(
                    definition = simpleTaggedStoryDefinition,
                    starterIntent = intent
                )
            )
        )

        // When
        val result = wrapper.hasDisableTagIntent(dialog)

        // Then
        assertTrue(result)
    }

    @Test
    fun `GIVEN intent story without tag disabled WHEN hasDisableTagIntent THEN return false`() {
        // Given
        val intent = Intent("intent")

        val simpleTaggedStoryDefinition = SimpleStoryDefinition(
            id = "not_tagged_story",
            storyHandler = StoryHandlerTest,
            starterIntents = setOf(intent),
            tags = emptySet()
        )

        val botDef = BotDefinitionBase(
            botId = "test",
            namespace = "namespace",
            stories = listOf(simpleTaggedStoryDefinition)
        )

        val wrapper = BotDefinitionWrapper(botDef)

        val dialog = Dialog(
            playerIds = emptySet(),
            id = newId(),
            state = DialogState(
                currentIntent = intent,
                entityValues = mutableMapOf(),
                context = mutableMapOf()
            ),
            stories = mutableListOf(
                Story(
                    definition = simpleTaggedStoryDefinition,
                    starterIntent = intent
                )
            )
        )

        // When
        val result = wrapper.hasDisableTagIntent(dialog)

        // Then
        assertFalse(result)
    }
}

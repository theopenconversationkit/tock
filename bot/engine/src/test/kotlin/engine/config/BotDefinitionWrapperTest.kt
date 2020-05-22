/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
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

package engine.config

import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationFeature
import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.bot.engine.BotDefinitionTest
import ai.tock.bot.engine.TestStoryDefinition.test
import ai.tock.bot.engine.config.BotDefinitionWrapper
import ai.tock.bot.engine.config.ConfiguredStoryDefinition
import io.mockk.mockk
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import kotlin.test.assertEquals

internal class BotDefinitionWrapperTest {

    val botDefinition: BotDefinition = BotDefinitionTest()

    val applicationId = "test_application"

    val inputStoryId = "input_story"

    private fun story(id: String, type: AnswerConfigurationType, vararg features: StoryDefinitionConfigurationFeature) =
            ConfiguredStoryDefinition(StoryDefinitionConfiguration(id, botDefinition.botId, IntentWithoutNamespace(id),
                    type, emptyList(), features = features.toList()))

    @Nested
    inner class NoRedirection {

        @Test
        fun `GIVEN story is null WHEN find story THEN return unknown story`() {
            val inputStory = BotDefinitionWrapper(botDefinition).findStoryDefinition(null, applicationId)
            assertEquals(botDefinition.unknownStory, inputStory)
        }

        @Test
        fun `GIVEN no story redirection WHEN find story THEN return story`() {
            val inputStory = BotDefinitionWrapper(botDefinition).findStoryDefinition(test.mainIntent().name, applicationId)
            assertEquals(test, inputStory)
        }
    }

    @Nested
    inner class SingleRedirection {

        @ParameterizedTest
        @EnumSource(AnswerConfigurationType::class)
        fun `GIVEN story redirection for all apps WHEN find story THEN return other story`(type: AnswerConfigurationType) {

            val inputStory = story(inputStoryId, type, StoryDefinitionConfigurationFeature(null, true, test.id))
            val wrapper = BotDefinitionWrapper(botDefinition)
            wrapper.updateStories(listOf(inputStory), applicationId)

            val outputStory = wrapper.findStoryDefinition(inputStoryId, applicationId)

            assertEquals(test, outputStory)
        }

        @Test
        fun `GIVEN story redirection from configured to configured WHEN find story THEN return other story`() {

            val inputStory = story(inputStoryId, AnswerConfigurationType.message, StoryDefinitionConfigurationFeature(null, true, "target"))
            val targetStory = story("target", AnswerConfigurationType.message)
            val wrapper = BotDefinitionWrapper(botDefinition)
            wrapper.updateStories(listOf(inputStory, targetStory), applicationId)

            val outputStory = wrapper.findStoryDefinition(inputStoryId, applicationId)

            assertEquals(targetStory, outputStory)
        }

        @Test
        fun `GIVEN story redirection from builtin to builtin WHEN find story THEN return other story`() {

            val inputStory = story(inputStoryId, AnswerConfigurationType.builtin, StoryDefinitionConfigurationFeature(null, true, "target"))
            val targetStory = story("target", AnswerConfigurationType.builtin)
            val wrapper = BotDefinitionWrapper(botDefinition)
            wrapper.updateStories(listOf(inputStory, targetStory), applicationId)

            val outputStory = wrapper.findStoryDefinition(inputStoryId, applicationId)

            assertEquals(targetStory, outputStory)
        }

        @Test
        fun `GIVEN story redirection from configured to builtin WHEN find story THEN return builtin story`() {

            val inputStory = story(inputStoryId, AnswerConfigurationType.message, StoryDefinitionConfigurationFeature(null, true, "target"))
            val targetStory = story("target", AnswerConfigurationType.builtin)
            val wrapper = BotDefinitionWrapper(botDefinition)
            wrapper.updateStories(listOf(inputStory, targetStory), applicationId)

            val outputStory = wrapper.findStoryDefinition(inputStoryId, applicationId)

            assertEquals(targetStory, outputStory)
        }

        @Test
        fun `GIVEN story redirection from builtin to configured WHEN find story THEN return configured story`() {

            val inputStory = story(inputStoryId, AnswerConfigurationType.builtin, StoryDefinitionConfigurationFeature(null, true, "target"))
            val targetStory = story("target", AnswerConfigurationType.message)
            val wrapper = BotDefinitionWrapper(botDefinition)
            wrapper.updateStories(listOf(inputStory, targetStory), applicationId)

            val outputStory = wrapper.findStoryDefinition(inputStoryId, applicationId)

            assertEquals(targetStory, outputStory)
        }

        @ParameterizedTest
        @EnumSource(AnswerConfigurationType::class)
        fun `GIVEN story redirection to disabled story WHEN find story THEN return story`(type: AnswerConfigurationType) {

            val inputStory = story(inputStoryId, type, StoryDefinitionConfigurationFeature(null, true, "target"))
            val targetStory = story("target", type, StoryDefinitionConfigurationFeature(null, false, null))
            val wrapper = BotDefinitionWrapper(botDefinition)
            wrapper.updateStories(listOf(inputStory, targetStory), applicationId)

            val outputStory = wrapper.findStoryDefinition(inputStoryId, applicationId)

            assertEquals(inputStory, outputStory)
        }

        @ParameterizedTest
        @EnumSource(AnswerConfigurationType::class)
        fun `GIVEN disabled story redirection WHEN find story THEN return story`(type: AnswerConfigurationType) {

            val inputStory = story(inputStoryId, type, StoryDefinitionConfigurationFeature(null, false, test.id))
            val wrapper = BotDefinitionWrapper(botDefinition)
            wrapper.updateStories(listOf(inputStory), applicationId)

            val outputStory = wrapper.findStoryDefinition(inputStoryId, applicationId)

            assertEquals(inputStory, outputStory)
        }

        @ParameterizedTest
        @EnumSource(AnswerConfigurationType::class)
        fun `GIVEN story redirection with missing target WHEN find story THEN return story`(type: AnswerConfigurationType) {

            val inputStory = story(inputStoryId, type, StoryDefinitionConfigurationFeature(null, true, null))
            val wrapper = BotDefinitionWrapper(botDefinition)
            wrapper.updateStories(listOf(inputStory), applicationId)

            val outputStory = wrapper.findStoryDefinition(inputStoryId, applicationId)

            assertEquals(inputStory, outputStory)
        }

        @ParameterizedTest
        @EnumSource(AnswerConfigurationType::class)
        fun `GIVEN story redirection for other app WHEN find story THEN return story`(type: AnswerConfigurationType) {

            val inputStory = story(inputStoryId, type, StoryDefinitionConfigurationFeature(mockk(), true, test.id))
            val wrapper = BotDefinitionWrapper(botDefinition)
            wrapper.updateStories(listOf(inputStory), applicationId)

            val outputStory = wrapper.findStoryDefinition(inputStoryId, applicationId)

            assertEquals(inputStory, outputStory)
        }
    }

    @Nested
    inner class MultipleRedirections {

        @ParameterizedTest
        @EnumSource(AnswerConfigurationType::class)
        fun `GIVEN chained redirections WHEN find story THEN return last story`(type: AnswerConfigurationType) {

            val inputStory = story(inputStoryId, type, StoryDefinitionConfigurationFeature(null, true, "story2"))
            val story2 = story("story2", type, StoryDefinitionConfigurationFeature(null, true, "story3"))
            val story3 = story("story3", type, StoryDefinitionConfigurationFeature(null, true, "story4"))
            val story4 = story("story4", type, StoryDefinitionConfigurationFeature(null, true, test.id))
            val wrapper = BotDefinitionWrapper(botDefinition)
            wrapper.updateStories(listOf(inputStory, story2, story3, story4), applicationId)

            val outputStory = wrapper.findStoryDefinition(inputStoryId, applicationId)

            assertEquals(test, outputStory)
        }

        @Test
        fun `GIVEN chained redirections with various types WHEN find story THEN return last story`() {

            val inputStory = story(inputStoryId, AnswerConfigurationType.message, StoryDefinitionConfigurationFeature(null, true, "story2"))
            val story2 = story("story2", AnswerConfigurationType.script, StoryDefinitionConfigurationFeature(null, true, "story3"))
            val story3 = story("story3", AnswerConfigurationType.simple, StoryDefinitionConfigurationFeature(null, true, "story4"))
            val story4 = story("story4", AnswerConfigurationType.builtin, StoryDefinitionConfigurationFeature(null, true, test.id))
            val wrapper = BotDefinitionWrapper(botDefinition)
            wrapper.updateStories(listOf(inputStory, story2, story3, story4), applicationId)

            val outputStory = wrapper.findStoryDefinition(inputStoryId, applicationId)

            assertEquals(test, outputStory)
        }

        @ParameterizedTest
        @EnumSource(AnswerConfigurationType::class)
        fun `GIVEN chained redirections with one disabled WHEN find story THEN return last redirection enabled`(type: AnswerConfigurationType) {

            val inputStory = story(inputStoryId, type, StoryDefinitionConfigurationFeature(null, true, "story2"))
            val story2 = story("story2", type, StoryDefinitionConfigurationFeature(null, true, "story3"))
            val story3 = story("story3", type, StoryDefinitionConfigurationFeature(null, false, "story4"))
            val story4 = story("story4", type, StoryDefinitionConfigurationFeature(null, true, test.id))
            val wrapper = BotDefinitionWrapper(botDefinition)
            wrapper.updateStories(listOf(inputStory, story2, story3, story4), applicationId)

            val outputStory = wrapper.findStoryDefinition(inputStoryId, applicationId)

            assertEquals(story3, outputStory)
        }

        @ParameterizedTest
        @EnumSource(AnswerConfigurationType::class)
        fun `GIVEN chained redirections with story disabled WHEN find story THEN return last story enabled`(type: AnswerConfigurationType) {

            val inputStory = story(inputStoryId, type, StoryDefinitionConfigurationFeature(null, true, "story2"))
            val story2 = story("story2", type, StoryDefinitionConfigurationFeature(null, true, "story3"))
            val story3 = story("story3", type, StoryDefinitionConfigurationFeature(null, false, null))
            val wrapper = BotDefinitionWrapper(botDefinition)
            wrapper.updateStories(listOf(inputStory, story2, story3), applicationId)

            val outputStory = wrapper.findStoryDefinition(inputStoryId, applicationId)

            assertEquals(story2, outputStory)
        }


        @ParameterizedTest
        @EnumSource(AnswerConfigurationType::class)
        fun `GIVEN cycling redirections WHEN find story THEN return story`(type: AnswerConfigurationType) {

            val inputStory = story(inputStoryId, type, StoryDefinitionConfigurationFeature(null, true, "story2"))
            val story2 = story("story2", type, StoryDefinitionConfigurationFeature(null, true, "story3"))
            val story3 = story("story3", type, StoryDefinitionConfigurationFeature(null, true, "story4"))
            val story4 = story("story4", type, StoryDefinitionConfigurationFeature(null, true, inputStoryId))
            val wrapper = BotDefinitionWrapper(botDefinition)
            wrapper.updateStories(listOf(inputStory, story2, story3, story4), applicationId)

            var outputStory = wrapper.findStoryDefinition(inputStoryId, applicationId)
            assertEquals(inputStory, outputStory)

            outputStory = wrapper.findStoryDefinition("story3", applicationId)
            assertEquals(story3, outputStory)
        }
    }
}
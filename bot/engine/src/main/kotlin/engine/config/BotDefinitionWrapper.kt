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

package ai.tock.bot.engine.config

import ai.tock.bot.admin.answer.AnswerConfigurationType.builtin
import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.Intent.Companion.unknown
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.definition.StoryDefinition
import mu.KotlinLogging

/**
 *
 */
internal class BotDefinitionWrapper(val botDefinition: BotDefinition) : BotDefinition by botDefinition {

    private val logger = KotlinLogging.logger {}

    @Volatile
    private var configuredStories: Map<String, List<ConfiguredStoryDefinition>> = emptyMap()

    @Volatile
    private var allStories: List<StoryDefinition> = botDefinition.stories

    fun updateStories(configuredStories: List<ConfiguredStoryDefinition>) {
        logger.debug { "refresh configured stories for ${botDefinition.botId}" }
        this.configuredStories = configuredStories.filter { it.answerType != builtin }.groupBy { it.id }
        //configured stories can override built-in
        allStories = (this.configuredStories + botDefinition.stories.groupBy { it.id }).values.flatten()
    }

    override val stories: List<StoryDefinition>
        get() = allStories

    override fun findIntent(intent: String): Intent {
        val i = super.findIntent(intent)
        return if (i == unknown) {
            val i2 = botDefinition.findIntent(intent)
            if (i2 == unknown) BotDefinition.findIntent(stories, intent) else i2
        } else i
    }

    override fun findStoryDefinition(intent: IntentAware?): StoryDefinition {
        return findStoryDefinition(intent?.wrappedIntent()?.name)
    }

    override fun findStoryDefinition(intent: String?, applicationId: String?): StoryDefinition =
        intent?.let { i ->
            configuredStories[i]
                ?.firstOrNull()
                //does not take if story is disabled
                ?.takeUnless { it.configuration.hasOnlyDisabledFeature(applicationId) }
        }
            ?: BotDefinition.findStoryDefinition(
                stories.filter {
                    when (it) {
                        is ConfiguredStoryDefinition -> !it.configuration.hasOnlyDisabledFeature(applicationId)
                        else -> true
                    }
                },
                intent,
                unknownStory,
                keywordStory
            )

    override fun toString(): String {
        return "Wrapper($botDefinition)"
    }

}
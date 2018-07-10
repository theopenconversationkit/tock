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

package fr.vsct.tock.bot.engine.config

import fr.vsct.tock.bot.admin.answer.AnswerConfigurationType.builtin
import fr.vsct.tock.bot.definition.BotDefinition
import fr.vsct.tock.bot.definition.Intent
import fr.vsct.tock.bot.definition.Intent.Companion.unknown
import fr.vsct.tock.bot.definition.IntentAware
import fr.vsct.tock.bot.definition.StoryDefinition

/**
 *
 */
internal class BotDefinitionWrapper(val botDefinition: BotDefinition) : BotDefinition by botDefinition {

    @Volatile
    private var allStories: List<StoryDefinition> = botDefinition.stories

    fun updateStories(configuredStories: List<ConfiguredStoryDefinition>) {
        //configured stories can override built-in
        allStories =
                (botDefinition.stories.groupBy { it.id }
                        + configuredStories.filter { it.answerType != builtin }.groupBy { it.id })
                    .values
                    .flatMap { it }
    }

    override val stories: List<StoryDefinition>
        get() = allStories

    override fun findIntent(intent: String): Intent {
        val i = super.findIntent(intent)
        return if (i == unknown) BotDefinition.findIntent(stories, intent) else i
    }

    override fun findStoryDefinition(intent: IntentAware?): StoryDefinition {
        return findStoryDefinition(intent?.wrappedIntent()?.name)
    }

    override fun findStoryDefinition(intent: String?): StoryDefinition {
        val s = super.findStoryDefinition(intent)
        return if (s == unknownStory) BotDefinition.findStoryDefinition(
            stories,
            intent,
            unknownStory,
            keywordStory
        ) else s
    }

    override fun toString(): String {
        return "Wrapper($botDefinition)"
    }


}
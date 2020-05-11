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

package ai.tock.bot.engine.config

import ai.tock.bot.admin.answer.AnswerConfigurationType.builtin
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.definition.*
import ai.tock.bot.definition.Intent.Companion.unknown
import ai.tock.shared.injector
import ai.tock.shared.provide
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

    private val dao: StoryDefinitionConfigurationDAO = injector.provide()

    @Volatile
    private var storyConfRedirections: Map<String, String?> = emptyMap()

    fun updateStories(configuredStories: List<ConfiguredStoryDefinition>, applicationId: String?) {
        this.configuredStories = configuredStories.filter { it.answerType != builtin }.groupBy { it.id }
        //configured stories can override built-in
        allStories = (this.configuredStories + botDefinition.stories.groupBy { it.id }).values.flatten()
        val storyConfigurations = with(botDefinition) {
            dao.getStoryDefinitionsByNamespaceAndBotId(namespace, botId).map { story -> story.storyId to story }.toMap()
        }
        storyConfRedirections = storyConfigurations.map { it.key to followStoryRedirects(applicationId, storyConfigurations, it.key, null) }.toMap()
        logger.info("Story rules for '${botDefinition.botId} ($applicationId)': $storyConfRedirections")
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

    fun followStoryRedirects(applicationId: String?, stories: Map<String, StoryDefinitionConfiguration>, requestStoryId: String, switchStoryId: String?): String? =
        if (switchStoryId == requestStoryId)
            requestStoryId
        else
            with(switchStoryId ?: requestStoryId) {
                val theStory = stories[this]
                if (theStory?.isDisabled(applicationId) == true) { return null }
                theStory?.findFeatures(applicationId)?.find { it.enabled && !it.switchToStoryId.isNullOrBlank() }?.let {
                    followStoryRedirects(applicationId, stories.filterKeys { it -> this != it }, requestStoryId, it.switchToStoryId)
                } ?: this
            }

    override fun findStoryDefinition(intent: String?, applicationId: String?): StoryDefinition {
        val redirectStoryId = storyConfRedirections[intent]
        if (redirectStoryId != null && redirectStoryId != intent) {
            return findStoryDefinition(redirectStoryId, applicationId)
        } else {
            return intent?.let { i ->
            configuredStories[i]
                    ?.firstOrNull()
                    ?.takeUnless { it.configuration.isDisabled(applicationId) }
            }
                ?: BotDefinition.findStoryDefinition(
                        stories.filter {
                            when (it) {
                                is ConfiguredStoryDefinition -> !it.configuration.isDisabled(applicationId)
                                else -> true
                            }
                        },
                        intent,
                        unknownStory,
                        keywordStory
                )
        }
    }

    override fun toString(): String {
        return "Wrapper($botDefinition)"
    }

}
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

    /**
     * Story redirections (resolved from story features).
     * Key: storyId
     * Value: storyId to redirect to | <code>null</code> when story is disabled
     */
    @Volatile
    private var storyRedirectionsMap: Map<String, String?> = emptyMap()

    fun updateStories(configuredStories: List<ConfiguredStoryDefinition>, connectorId: String) {
        this.configuredStories = configuredStories.groupBy { it.configuration.storyId }
        allStories = (this.configuredStories + botDefinition.stories.groupBy { it.id }).values.flatten()
        val storyConfigurations = configuredStories.map { it.configuration }.map { it.storyId to it }.toMap()
        storyRedirectionsMap = storyConfigurations.map { it.key to followStoryRedirects(connectorId, storyConfigurations, it.key, null) }.toMap()
        logger.debug { "Story rules for '${botDefinition.botId} ($connectorId)': $storyRedirectionsMap" }
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

    private fun followStoryRedirects(connectorId: String?, stories: Map<String, StoryDefinitionConfiguration>, requestStoryId: String, switchStoryId: String?): String? =
        if (switchStoryId == requestStoryId)
            requestStoryId
        else
            with(switchStoryId ?: requestStoryId) {
                val theStory = stories[this]
                if (theStory?.isDisabled(connectorId) == true) { return null }
                theStory?.findFeatures(connectorId)?.find { it.enabled && !it.switchToStoryId.isNullOrBlank() }?.let { feature ->
                    followStoryRedirects(connectorId, stories.filterKeys { this != it }, requestStoryId, feature.switchToStoryId)
                } ?: this
            }

    override fun findStoryDefinition(intent: String?, applicationId: String?): StoryDefinition {
        val targetStory = storyRedirectionsMap[intent]
        return if (storyRedirectionsMap.containsKey(intent) && targetStory == null) { // Story is disabled
            unknownStory
        } else {
            if (targetStory != null && targetStory != intent) { // Story is redirected
                findStoryDefinition(targetStory, applicationId)
            } else {
                intent?.let { i ->
                    val enabledConfiguredStories = configuredStories[i]
                            ?.filter { !it.configuration.isDisabled(applicationId) }
                    // Configured stories have priority over built-in
                    enabledConfiguredStories?.firstOrNull { it.answerType != builtin }
                            ?: enabledConfiguredStories?.firstOrNull()
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
    }

    override fun toString(): String {
        return "Wrapper($botDefinition)"
    }

}
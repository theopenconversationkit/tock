/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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
import ai.tock.bot.admin.answer.RagAnswerConfiguration
import ai.tock.bot.admin.bot.BotApplicationConfigurationKey
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.definition.*
import ai.tock.bot.definition.Intent.Companion.ragexcluded
import ai.tock.bot.definition.Intent.Companion.unknown
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.BotRepository
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.user.UserTimeline
import ai.tock.bot.llm.rag.core.client.RagClient
import ai.tock.bot.llm.rag.core.client.models.RagQuery
import ai.tock.shared.exception.rest.RestException
import ai.tock.shared.injector
import ai.tock.shared.property
import ai.tock.shared.provide
import mu.KotlinLogging
import java.net.ConnectException

/**
 *
 */
internal class BotDefinitionWrapper(val botDefinition: BotDefinition) : BotDefinition by botDefinition {

    private val logger = KotlinLogging.logger {}
    private val defaultUnknownRagAnswer = property("tock_rag_default_unknown_answer", "Pardon ! je ne sais pas.").replace("\"", "")

    // stories with configuration (including built-in)
    @Volatile
    private var configuredStories: Map<String, List<ConfiguredStoryDefinition>> = emptyMap()

    @Volatile
    private var allStoriesById: Map<String, StoryDefinition> = botDefinition.stories.associateBy { it.id }

    // all stories
    @Volatile
    private var allStories: List<StoryDefinition> = botDefinition.stories

    override fun disableBot(timeline: UserTimeline, dialog: Dialog, action: Action): Boolean =
            super.disableBot(timeline, dialog, action)

    override fun enableBot(timeline: UserTimeline, dialog: Dialog, action: Action): Boolean =
            super.enableBot(timeline, dialog, action)

    override fun hasDisableTagIntent(dialog: Dialog): Boolean =
            super.hasDisableTagIntent(dialog)

    private fun findStoryDefinitionByTag(tag: StoryTag): List<StoryDefinition> =
            stories.filter { it.tags.contains(tag) }

    override val botDisabledStories: List<StoryDefinition>
        get() = findStoryDefinitionByTag(StoryTag.DISABLE)

    override val botEnabledStories: List<StoryDefinition>
        get() = findStoryDefinitionByTag(StoryTag.ENABLE)

    // only built-in
    private val builtInStoriesMap: Map<String, StoryDefinition> = botDefinition.stories.associateBy { it.id }

    fun updateStories(configuredStories: List<StoryDefinitionConfiguration>) {
        val activatedModules = findActivatedModules(configuredStories.map { it.storyId })
        val botStoryHandlers = activatedModules.flatMap { it.storiesById.entries }.associateBy({ it.key }) { it.value }

        this.configuredStories =
                configuredStories
                        .map {
                            ConfiguredStoryDefinition(
                                    definition = this,
                                    configuration = it,
                                    configurationStoryHandler = botStoryHandlers[it.storyId]
                            )
                        }
                        .groupBy { it.storyId }

        allStories = (
                this.configuredStories +
                        // in order to handle built-in not yet configured...
                        botDefinition
                                .stories
                                .asSequence()
                                .filterNot { this.configuredStories.containsKey(it.id) }
                                .groupBy { it.id }
                )
                .values.flatten()

        this.allStoriesById = allStories.associateBy { it.id }
    }

    override val stories: List<StoryDefinition>
        get() = allStories

    override fun findIntent(intent: String, applicationId: String): Intent {
        val i = super.findIntent(intent, applicationId)
        return if (i == ragexcluded) {
            val i2 = botDefinition.findIntent(intent, applicationId)
            if (i2 == ragexcluded) BotDefinition.findIntent(stories, intent) else i2
        } else if (i == unknown) {
            val i2 = botDefinition.findIntent(intent, applicationId)
            if (i2 == unknown) BotDefinition.findIntent(stories, intent) else i2
        } else i
    }

    override fun findStoryDefinition(intent: IntentAware?, applicationId: String): StoryDefinition {
        return findStoryDefinition(intent?.wrappedIntent()?.name, applicationId)
    }

    private fun findStory(intent: String?, applicationId: String): StoryDefinition =
            BotDefinition.findStoryDefinition(
                    stories
                            .asSequence()
                            .filter {
                                when (it) {
                                    is ConfiguredStoryDefinition -> !it.isDisabled(applicationId)
                                    else -> true
                                }
                            }
                            .map { it.checkApplicationId(applicationId) }
                            .toList(),
                    intent,
                    unknownStory,
                    keywordStory,
                    if (ragConfigurationEnabled) ragExcludedStory else null
            )

    internal fun builtInStory(storyId: String): StoryDefinition =
            builtInStoriesMap[storyId] ?: returnsUnknownStory(storyId)

    /**
     * Build the rag story with its configuration
     * @param story
     * @param botDefinition
     */
    internal fun builtRagStory(
            storyConfig: StoryDefinitionConfiguration,
            botDefinition: BotDefinitionWrapper
    ): RagStoryDefinition? {

        if (botDefinition.botDefinition.ragConfigurationEnabled) {
            val ragStory = ragStoryDefinitionHandler(storyConfig.answers.first() as RagAnswerConfiguration)
            //configuration générale pour prise en compte de la story dans la botdefinition
            botDefinition.botDefinition.ragUnknownStory = ragStory
        } else {
            botDefinition.botDefinition.ragUnknownStory = null
        }

        return botDefinition.botDefinition.ragUnknownStory
    }

    /**
     * Creates the [RagStoryDefinition] with its handler and according to its configuration [RagAnswerConfiguration]
     */
    private fun ragStoryDefinitionHandler(conf: RagAnswerConfiguration): RagStoryDefinition {
        val ragClient: RagClient = injector.provide()
        return RagStoryDefinition(
                object : SimpleStoryHandlerBase() {
                    override fun action(bus: BotBus) {
                        bus.markAsUnknown()
                        with(bus) {
                            try {
                                logger.debug { "Rag config : $conf" }
                                val response =
                                        ragClient.ask(RagQuery(userText.toString(), applicationId, userId.id))
                                //handle rag redirection in case answer is not known
                                if (response?.answer == defaultUnknownRagAnswer && conf.noAnswerRedirection != null && allStories.firstOrNull { it.id == conf.noAnswerRedirection.toString() } != null) {
                                    allStories.firstOrNull { it.id == conf.noAnswerRedirection.toString() }!!.storyHandler.handle(this)
                                } else {
                                    //handle rag response
                                    response?.answer?.let {
//                                        bus.underlyingConnector.notify()
                                        if (it != defaultUnknownRagAnswer) {
                                            //TODO to format per connector or other ?
                                            end("$it " +
                                                    "${response.sourceDocuments}")
                                        } else {
                                            end(it)
                                        }

                                    } ?: botDefinition.unknownStory.storyHandler.handle(this)
                                }
                            } catch (conn: ConnectException) {
                                logger.error { "failed to connect to ${conn.message}" }
                                botDefinition.unknownStory.storyHandler.handle(this)
                            } catch (e: RestException) {
                                logger.error { "error during rag call ${e.message}" }
                                botDefinition.unknownStory.storyHandler.handle(this)
                            }
                        }
                    }
                })
    }


    private fun returnsUnknownStory(storyId: String): StoryDefinition =
            unknownStory.also {
                logger.warn { "unknown story: $storyId" }
            }

    private fun findStoryDefinition(intent: String?, applicationId: String, initialIntent: String?): StoryDefinition {
        val story = findStory(intent, applicationId)

        return (story as? ConfiguredStoryDefinition)?.let {
            val switchId = it.findEnabledStorySwitchId(applicationId)
            if (switchId != null) {
                (configuredStories[switchId] ?: listOfNotNull(builtInStoriesMap[switchId]))
                        .let { stories ->
                            val targetStory = stories
                                    .asSequence()
                                    .filterIsInstance<ConfiguredStoryDefinition>()
                                    .filterNot { c -> c.isDisabled(applicationId) }
                                    .run {
                                        firstOrNull { c -> c.answerType != builtin } ?: firstOrNull()
                                    }
                                    ?: stories.firstOrNull { c -> c !is ConfiguredStoryDefinition }

                            targetStory
                                    ?.let { toStory ->
                                        val storyMainIntent = toStory.mainIntent().name
                                        if (storyMainIntent == initialIntent) {
                                            toStory.checkApplicationId(applicationId)
                                        } else {
                                            findStoryDefinition(storyMainIntent, applicationId, initialIntent)
                                        }
                                    }
                        }
                        ?: story
            } else {
                it
            }
        } ?: story
    }

    override fun findStoryDefinition(intent: String?, applicationId: String): StoryDefinition =
            findStoryDefinition(intent, applicationId, intent).let {
                if (it is ConfiguredStoryDefinition && it.answerType == builtin) {
                    builtInStory(it.storyId)
                } else {
                    it
                }
            }

    override fun findStoryDefinitionById(storyId: String, applicationId: String): StoryDefinition =
            // first search into built-in then in configured, fallback to search by intent
            builtInStoriesMap[storyId] ?: allStoriesById[storyId]?.checkApplicationId(applicationId)
            ?: findStoryDefinition(
                    storyId,
                    applicationId
            )

    override fun findStoryByStoryHandler(storyHandler: StoryHandler, applicationId: String): StoryDefinition? =
            (
                    botDefinition.stories.find { it.storyHandler == storyHandler }
                            ?: stories.find { it.storyHandler == storyHandler }
                    )
                    ?.checkApplicationId(applicationId)

    private fun StoryDefinition.checkApplicationId(applicationId: String): StoryDefinition =
            if (this is ConfiguredStoryDefinition &&
                    configuration.configuredSteps.isNotEmpty() &&
                    answerType != builtin
            ) {
                ConfiguredStoryDefinition(
                        this@BotDefinitionWrapper,
                        configuration,
                        BotApplicationConfigurationKey(applicationId, this@BotDefinitionWrapper)
                )
            } else {
                this
            }

    override fun toString(): String {
        return "Wrapper($botDefinition)"
    }
}

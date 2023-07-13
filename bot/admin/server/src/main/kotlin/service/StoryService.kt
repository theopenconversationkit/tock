/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

package ai.tock.bot.admin.service

import ai.tock.bot.admin.BotAdminService
import ai.tock.bot.admin.answer.AnswerConfiguration
import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.answer.TickAnswerConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.admin.story.StoryDefinitionConfigurationFeature
import ai.tock.bot.bean.TickStoryQuery
import ai.tock.bot.bean.TickStorySettings
import ai.tock.bot.validation.TickStoryValidation
import ai.tock.bot.bean.unknown.TickUnknownConfiguration
import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.nlp.front.service.storage.ApplicationDefinitionDAO
import ai.tock.shared.exception.rest.BadRequestException
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.vertx.WebVerticle
import ai.tock.shared.withoutNamespace
import com.mongodb.MongoWriteException
import mu.KLogger
import mu.KotlinLogging
import org.litote.kmongo.toId

/**
 * Service that manage the scenario functionality
 */
object StoryService {

    private const val TICK = "tick"

    private val logger: KLogger = KotlinLogging.logger {}
    private val storyDefinitionDAO: StoryDefinitionConfigurationDAO get() = injector.provide()
    private val applicationDefinitionDAO: ApplicationDefinitionDAO get() = injector.provide()

    init {
        /* On scenarioSettings changes, all TickStoryConfiguration must be updated */
        ScenarioSettingsService.listenChanges { settings ->
            storyDefinitionDAO.getStoryDefinitionByCategory(TICK)
                .forEach { storyDefinition ->
                    val answers: List<AnswerConfiguration> = storyDefinition.answers.map { answer ->
                        when (answer) {
                            is TickAnswerConfiguration -> answer.copy(
                                storySettings = TickStorySettings(
                                    settings.actionRepetitionNumber,
                                    settings.redirectStoryId ?: TickStorySettings.default.redirectStory,
                                    answer.storySettings?.unknownAnswerId
                                )
                            )
                            else -> answer
                        }
                    }

                    storyDefinitionDAO.save(
                        storyDefinition.copy(
                            answers = answers
                        )
                    )
                }
        }

        ScenarioGroupService.listenChanges { scenarioGroup ->
            storyDefinitionDAO.getStoryDefinitionByCategoryAndStoryId(TICK, scenarioGroup._id.toString())?.apply {

                val answers: List<AnswerConfiguration> = answers.map { answer ->
                    when (answer) {
                        is TickAnswerConfiguration -> answer.copy(
                            storySettings =  TickStorySettings(
                                answer.storySettings?.repetitionNb ?: 2,
                                answer.storySettings?.redirectStory ?: TickStorySettings.default.redirectStory,
                                scenarioGroup.unknownAnswerId
                            )
                        )
                        else -> answer
                    }
                }

                storyDefinitionDAO.save(copy(answers = answers))
            }

        }
    }

    /**
     * Get a [StoryDefinitionConfiguration]
     * @param namespace : the namespace
     * @param botId : the id of the bot
     * @param storyId : functional id of story to delete
     */
    fun getStoryByNamespaceAndBotIdAndStoryId(
        namespace: String,
        botId: String,
        storyId: String
    ): StoryDefinitionConfiguration? = storyDefinitionDAO
        .getStoryDefinitionByNamespaceAndBotIdAndStoryId(namespace, botId, storyId)

    /**
     * Create a new tick story
     * @param namespace : the namespace
     * @param tickStoryQuery : the tick story to create
     * @throws [BadRequestException] if a tick story is invalid
     */
    fun createTickStory(
        namespace: String,
        tickStoryQuery: TickStoryQuery
    ) {
        val errors = TickStoryValidation.validateTickStory(tickStoryQuery){
            storyDefinitionDAO.getStoryDefinitionByNamespaceAndBotIdAndStoryId(
                namespace,
                tickStoryQuery.botId,
                it
            ) != null
        }

        if (errors.isEmpty()) {
            saveTickStory(namespace, tickStoryQuery)
        } else {
            throw BadRequestException(errors)
        }
    }

    /**
     * Update the activation feature of story
     * @param namespace : the namespace
     * @param botId : the id of the bot
     * @param feature : feature to add to the story
     */
    fun updateActivateStoryFeatureByNamespaceAndBotIdAndStoryId(
        namespace: String,
        botId: String,
        storyId: String,
        feature: StoryDefinitionConfigurationFeature
    ): Boolean {
        val story = storyDefinitionDAO.getStoryDefinitionByNamespaceAndBotIdAndStoryId(namespace, botId, storyId)
        if (story != null) {
            val botConf = BotAdminService.getBotConfigurationsByNamespaceAndBotId(namespace, story.botId).firstOrNull()
            if (botConf != null) {
                storyDefinitionDAO.save(
                    story.copy(features = story.features.filterNot { it.isStoryActivationFeature() } + feature))
            }
        }
        return false
    }

    /**
     * Delete a [StoryDefinitionConfiguration]
     * @param namespace : the namespace
     * @param storyDefinitionConfigurationId : technical id of story to delete
     */
    fun deleteStoryByNamespaceAndStoryDefinitionConfigurationId(
        namespace: String,
        storyDefinitionConfigurationId: String
    ): Boolean {
        val story = storyDefinitionDAO.getStoryDefinitionById(storyDefinitionConfigurationId.toId())
        if (story != null) {
            val botConf = BotAdminService.getBotConfigurationsByNamespaceAndBotId(namespace, story.botId).firstOrNull()
            if (botConf != null) {
                storyDefinitionDAO.delete(story)
            }
        }
        return false
    }

    /**
     * Delete a [StoryDefinitionConfiguration]
     * @param namespace : the namespace
     * @param botId : the id of the bot
     * @param storyId : functional id of story to delete
     */
    fun deleteStoryByNamespaceAndBotIdAndStoryId(
        namespace: String,
        botId: String,
        storyId: String
    ): Boolean {
        val story = storyDefinitionDAO.getStoryDefinitionByNamespaceAndBotIdAndStoryId(namespace, botId, storyId)
        if (story != null) {
            val botConf = BotAdminService.getBotConfigurationsByNamespaceAndBotId(namespace, story.botId).firstOrNull()
            if (botConf != null) {
                storyDefinitionDAO.delete(story)
            }
        }
        return false
    }

    private fun saveTickStory(
        namespace: String,
        story: TickStoryQuery,
    ) {

        val botConf = BotAdminService.getBotConfigurationsByNamespaceAndBotId(namespace, story.botId).firstOrNull()
        botConf
            ?: WebVerticle.badRequest("No bot configuration is defined yet [namespace: $namespace, botId = ${story.botId}]")

        val application = applicationDefinitionDAO.getApplicationByNamespaceAndName(namespace, botConf.name)
            ?: WebVerticle.badRequest("No application is defined yet [namespace: $namespace, name = ${botConf.name}]")

        val newStory =
            StoryDefinitionConfiguration(
                storyId = story.storyId,
                botId = story.botId,
                intent = IntentWithoutNamespace(story.mainIntent.withoutNamespace()),
                currentType = AnswerConfigurationType.tick,
                answers = listOf(
                    TickAnswerConfiguration(
                        story.stateMachine,
                        story.primaryIntents,
                        story.secondaryIntents
                            .union(
                                listOf(
                                    // Add the unknown intent
                                    Intent.unknown.intentWithoutNamespace().name
                                )
                            ),
                        story.triggers,
                        story.contexts,
                        story.actions,
                        story.intentsContexts,
                        TickUnknownConfiguration(story.unknownAnswerConfigs),
                        storySettings = (
                                ScenarioSettingsService.getScenarioSettingsByBotId(application.name) to
                                        ScenarioGroupService.findOneById(story.storyId)
                                ).let { (settings, group) ->
                                (settings
                                    ?.let { TickStorySettings(it.actionRepetitionNumber, it.redirectStoryId ?: TickStorySettings.default.redirectStory) }
                                    ?: TickStorySettings.default).copy(unknownAnswerId = group.unknownAnswerId)
                            }
                    )
                ),
                namespace = namespace,
                name = story.name,
                category = TICK,
                description = story.description,
            )

        try {
            // Delete the tick story
            storyDefinitionDAO.deleteStoryDefinitionByNamespaceAndBotIdAndStoryId(namespace, story.botId, story.storyId)
            logger.info { "Deleting of the tick story <storyId:${story.storyId}>" }

            storyDefinitionDAO.save(newStory)
            logger.info { "Creation of a new tick story <storyId:${story.storyId}>" }
        } catch (e: MongoWriteException) {
            throw BadRequestException(e.message ?: "Tick Story: registration failed ")
        }
    }

    // FIXME : Migrate all story methods here
}
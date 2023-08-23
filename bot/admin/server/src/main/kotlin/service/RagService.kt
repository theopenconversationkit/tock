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

package ai.tock.bot.admin.service

import ai.tock.bot.admin.BotAdminService
import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.answer.RagAnswerConfiguration
import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.bot.BotRAGConfiguration
import ai.tock.bot.admin.bot.BotRAGConfigurationDAO
import ai.tock.bot.admin.model.BotRAGConfigurationDTO
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.admin.story.StoryDefinitionConfigurationFeature
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.bot.definition.RagStoryDefinition
import ai.tock.shared.exception.rest.BadRequestException
import ai.tock.shared.exception.rest.RestException
import ai.tock.shared.injector
import ai.tock.shared.property
import ai.tock.shared.provide
import ai.tock.shared.vertx.WebVerticle
import mu.KLogger
import mu.KotlinLogging
import org.litote.kmongo.Id

/**
 * Service that manage the retrieval augmented generation (RAG) with Large Language Model (LLM) functionality
 */
object RagService {

    private const val RAG = "rag"
    private const val UNKNOWN_INTENT ="unknown"

    private val logger: KLogger = KotlinLogging.logger {}
    private val storyDefinitionDAO: StoryDefinitionConfigurationDAO get() = injector.provide()
    private val ragConfigurationDAO: BotRAGConfigurationDAO get() = injector.provide()

    /**
     * unknown backup intent if unknown story exists
     */
    private val unknownStoryBackupIntentName = property("tock_rag_unknown_backup_intent_name", "unknownBackup")

    /**
     * Save Rag configuration and filter errors
     * @param ragConfig : the rag story to create
     * @throws [BadRequestException] if a rag story is invalid
     * @return [BotRAGConfiguration]
     */
    fun saveRag(
            ragConfig: BotRAGConfigurationDTO
    ): BotRAGConfiguration {
            val botConf =
                    BotAdminService.getBotConfigurationsByNamespaceAndBotId(ragConfig.namespace, ragConfig.botId)
                            .firstOrNull()
            botConf
                    ?: WebVerticle.badRequest("No bot configuration is defined yet [namespace: ${ragConfig.namespace}, botId = ${ragConfig.botId}]")
            return saveRagConfigurationStory(ragConfig, botConf._id)
    }

    fun getRAGConfiguration(namespace: String, botId: String): BotRAGConfiguration? {
        return ragConfigurationDAO.findByNamespaceAndBotId(namespace, botId)
    }

    /**
     * Save the Rag configuration and its story declaration
     * @param ragConfig [BotRAGConfigurationDTO]
     */
    private fun saveRagConfigurationStory(
            ragConfig: BotRAGConfigurationDTO,
            botConfId: Id<BotApplicationConfiguration>
    ): BotRAGConfiguration {
        try {

            val savedRagConfig: BotRAGConfiguration = managePreviousStoryWithUnknownIntents(ragConfig)
                    ?: ragConfig.toBotRAGConfiguration()
            ragConfigurationDAO.save(savedRagConfig)
            if (ragConfig.enabled) {
                // save rag new Story
                val newRagStory = prepareRagStory(ragConfig, botConfId)
                logger.info { "Saving ${newRagStory.storyId}" }
                storyDefinitionDAO.save(newRagStory)
            }
            return savedRagConfig
        } catch (e: Exception) {
            throw BadRequestException(e.message ?: "Rag Story: registration failed ")
        }
    }

    /**
     * Manage unknown Stories associated with unknown intents
     * @param ragConfig [BotRAGConfiguration]
     * @return null or [BotRAGConfiguration]
     */
    private fun managePreviousStoryWithUnknownIntents(ragConfig: BotRAGConfigurationDTO): BotRAGConfiguration? {
        var savedRagConfig: BotRAGConfiguration? = null
        storyDefinitionDAO.getConfiguredStoryDefinitionByNamespaceAndBotIdAndIntent(
                ragConfig.namespace,
                ragConfig.botId,
                UNKNOWN_INTENT
                //manage existing unknownStory
        )?.let {currentUnknown ->
            // existing unknown story is not rag and enabled
            if (!currentUnknown.isRagAnswerType() && ragConfig.enabled) {
                logger.debug { "Found other type ${currentUnknown.currentType} story with same namespace ${currentUnknown.namespace}, and intent: ${currentUnknown.intent}" }
                        .apply { logger.info { "\"${currentUnknown.name}\" was saved in the ragConfiguration in case of disabling" } }
                storyDefinitionDAO.save(currentUnknown.copy(intent = IntentWithoutNamespace(unknownStoryBackupIntentName)))
                        .apply { logger.info { "\"${currentUnknown.name}\" changed its intent to $unknownStoryBackupIntentName to be allowed create the ragStory" } }
                val ragWithUnknown = ragConfig.copy(unknownStoryBackupId = currentUnknown._id)
                savedRagConfig = ragWithUnknown.toBotRAGConfiguration()
                // existing unknown story is rag
            } else if (currentUnknown.isRagAnswerType()) {
                //remove rag story if it exists
                storyDefinitionDAO.getAndDeleteRagStoryDefinitionByNamespaceAndBotId(
                        ragConfig.namespace,
                        ragConfig.botId,
                )

                // put back unknown story backup if it exists when ragConfig is disabled
                if(!ragConfig.enabled) {
                    ragConfigurationDAO.findByNamespaceAndBotId(ragConfig.namespace, ragConfig.botId)?.let { currentRagConfig ->
                        currentRagConfig.unknownStoryBackupId?.let { unknownStoryBackupStoryId ->
                            storyDefinitionDAO.getStoryDefinitionById(unknownStoryBackupStoryId)?.let { unknownStoryBackup ->
                                logger.debug { "put back old replaced unknown story \"${unknownStoryBackup.storyId}\"" }
                                storyDefinitionDAO.save(unknownStoryBackup.copy(intent = IntentWithoutNamespace(UNKNOWN_INTENT)))
                            }
                        }
                    }
                }
                savedRagConfig = ragConfig.toBotRAGConfiguration()
            }
        }
        return savedRagConfig
    }

    /**
     * Save the rag story as [StoryDefinitionConfiguration]
     * @param ragConfig
     * @param botConfId
     * @return [StoryDefinitionConfiguration]
     */
    private fun prepareRagStory(
            ragConfig: BotRAGConfigurationDTO,
            botConfId: Id<BotApplicationConfiguration>?
    ): StoryDefinitionConfiguration {
        return StoryDefinitionConfiguration(
                //story id devrait être généré // update du précédent
                storyId = RagStoryDefinition.RAG_STORY_NAME,
                botId = ragConfig.botId,
                intent = IntentWithoutNamespace(RagStoryDefinition.OVERRIDDEN_UNKNOWN_INTENT),
                currentType = AnswerConfigurationType.rag,
                answers = listOf(
                        RagAnswerConfiguration(
                                ragConfig.enabled,
                                ragConfig.engine,
                                ragConfig.embeddingEngine,
                                ragConfig.temperature,
                                ragConfig.prompt,
                                ragConfig.params,
                                ragConfig.noAnswerRedirection
                        )

                ),
                namespace = ragConfig.namespace,
                //category RAG to be sure to not see it in frontend ?
                category = RAG,
                description = "the retrieval augmented generation story which overrides unknown",
                //botConf not needed ?
                features = listOf(
                        StoryDefinitionConfigurationFeature(
                                botConfId,
                                ragConfig.enabled,
                                null,
                                ragConfig.noAnswerRedirection
                        )
                )
        )
    }
}

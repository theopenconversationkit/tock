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
import ai.tock.shared.provide
import ai.tock.shared.security.UserLogin
import ai.tock.shared.vertx.WebVerticle
import mu.KLogger
import mu.KotlinLogging
import org.litote.kmongo.Id

/**
 * Service that manage the retrieval augmented generation (RAG) with Large Language Model (LLM) functionality
 */
object RagService {

    private const val RAG = "rag"

    private val logger: KLogger = KotlinLogging.logger {}
    private val storyDefinitionDAO: StoryDefinitionConfigurationDAO get() = injector.provide()
    private val ragConfigurationDAO: BotRAGConfigurationDAO get() = injector.provide()

    /**
     * Save Rag configuration and filter errors
     * @param ragConfig : the rag story to create
     * @throws [BadRequestException] if a rag story is invalid
     * @return [BotRAGConfiguration]
     */
    fun saveRag(
        ragConfig: BotRAGConfigurationDTO, userLogin: UserLogin
    ): BotRAGConfiguration {
        try {
            return saveRagConfigurationStory(ragConfig, userLogin)
        } catch (e: BadRequestException) {
            throw RestException(e.httpResponseBody, e.httpResponseStatus)
        }
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
        userLogin: UserLogin
    ): BotRAGConfiguration {
        val botConf =
            BotAdminService.getBotConfigurationsByNamespaceAndBotId(ragConfig.namespace, ragConfig.botId)
                .firstOrNull()
        botConf
            ?: WebVerticle.badRequest("No bot configuration is defined yet [namespace: ${ragConfig.namespace}, botId = ${ragConfig.botId}]")

        var savedRagConfig: BotRAGConfiguration?
        try {
                //save rag new Story
                savedRagConfig = manageUnknownIntents(ragConfig) ?: ragConfig.toBotRAGConfiguration()
                if(ragConfig.enabled) {
                    //remove rag story if it exists
                    storyDefinitionDAO.getAndDeleteRagStoryDefinitionByNamespaceAndBotId(
                        ragConfig.namespace,
                        ragConfig.botId,
                    )
                    val newRagStory = prepareRagStory(ragConfig, botConf._id)
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
    private fun manageUnknownIntents(ragConfig: BotRAGConfigurationDTO): BotRAGConfiguration? {
        var savedRagConfig: BotRAGConfiguration? = null
        storyDefinitionDAO.getConfiguredStoryDefinitionByNamespaceAndBotIdAndIntent(
            ragConfig.namespace,
            ragConfig.botId,
            RagStoryDefinition.OVERRIDDEN_UNKNOWN_INTENT
            //manage existing unknownStory
        )?.let {
            val currentRagConfig =
                ragConfigurationDAO.findByNamespaceAndBotId(ragConfig.namespace, ragConfig.botId)

            //TODO : what is happening with other type than simple ?
            if (!it.isRagAnswerType() && ragConfig.enabled) {
                logger.debug { "Found other type ${it.currentType} story with same namespace ${it.namespace}, and intent: ${it.intent}" }
                val ragWithUnknown = ragConfig.copy(backupUnknownStory = it)
                    .apply { logger.info { "\"${it.name}\" was saved in the ragConfiguration in case of disabling" } }
                storyDefinitionDAO.delete(it)
                    .apply { logger.info { "\"${it.name}\" was deleted to create the ragStory" } }
                savedRagConfig = ragWithUnknown.toBotRAGConfiguration()
                ragConfigurationDAO.save(savedRagConfig as BotRAGConfiguration)
            } else if (currentRagConfig?.backupUnknownStory != null && it.isRagAnswerType()) {
                logger.debug { "Deletion of previous ${it.storyId}" }
                //remove rag story if it exists
                storyDefinitionDAO.getAndDeleteRagStoryDefinitionByNamespaceAndBotId(
                    ragConfig.namespace,
                    ragConfig.botId,
                )
                logger.debug { "put back old replaced unknown story \"${ragConfig.backupUnknownStory!!.storyId}\"" }
                storyDefinitionDAO.save(currentRagConfig.backupUnknownStory!!)
                savedRagConfig = ragConfig.copy(backupUnknownStory = null).toBotRAGConfiguration()
            } else {
                savedRagConfig = null
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
                    //TODO null or empty ?
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

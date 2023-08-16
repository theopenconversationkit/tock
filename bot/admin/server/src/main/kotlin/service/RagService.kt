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
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.vertx.WebVerticle
import com.mongodb.MongoException
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
        ragConfig: BotRAGConfigurationDTO
    ): BotRAGConfiguration {
        try {
            return saveRagConfigurationStory(ragConfig)
        } catch (e: MongoException) {
            throw BadRequestException(e.message ?: "an error occurred when saving Rag")
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
        ragConfig: BotRAGConfigurationDTO
    ): BotRAGConfiguration {
        val botConf =
            BotAdminService.getBotConfigurationsByNamespaceAndBotId(ragConfig.namespace, ragConfig.botId)
                .firstOrNull()
        botConf
            ?: WebVerticle.badRequest("No bot configuration is defined yet [namespace: ${ragConfig.namespace}, botId = ${ragConfig.botId}]")

        val newStory = saveRagStory(ragConfig,botConf._id)

        try {
           ragConfigurationDAO.findByNamespaceAndBotId(ragConfig.namespace, ragConfig.botId)?.let { previousRagConf ->
                storyDefinitionDAO.deleteRagStoryDefinitionByNamespaceAndBotId(
                    ragConfig.namespace,
                    previousRagConf.botId
                ).apply {
                    logger.debug {"Delete and recreate a new rag story <storyId:${newStory.storyId}>"}
                }
            } ?: logger.debug { "Creation of a new rag story <storyId:${newStory.storyId}>" }
            //update actual configuration
            storyDefinitionDAO.save(newStory)
            return ragConfigurationDAO.save(ragConfig.toBotRAGConfiguration())
        } catch (e: MongoException) {
            throw BadRequestException(e.message ?: "Rag Story: registration failed ")
        }
    }

    /**
     * Save the rag story as [StoryDefinitionConfiguration]
     * @param ragConfig
     * @param botConfId
     * @return [StoryDefinitionConfiguration]
     */
    private fun saveRagStory(ragConfig: BotRAGConfigurationDTO,botConfId: Id<BotApplicationConfiguration>?) : StoryDefinitionConfiguration {
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

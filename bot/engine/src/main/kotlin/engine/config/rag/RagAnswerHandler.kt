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

package ai.tock.bot.engine.config.rag

import ai.tock.bot.admin.answer.RagAnswerConfiguration
import ai.tock.bot.admin.bot.BotRAGConfigurationDAO
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.engine.BotBus
import ai.tock.bot.llm.rag.core.client.RagClient
import ai.tock.bot.llm.rag.core.client.models.RagQuery
import ai.tock.shared.exception.rest.RestException
import ai.tock.shared.injector
import ai.tock.shared.property
import ai.tock.shared.provide
import mu.KotlinLogging
import java.net.ConnectException

/**
 * Handler of a rag story answer
 */
object RagAnswerHandler {

    private val logger = KotlinLogging.logger {}
    private val ragClient: RagClient = injector.provide()
    private val ragConfigurationDAO: BotRAGConfigurationDAO = injector.provide()
    private val storyDefinitionConfigurationDAO: StoryDefinitionConfigurationDAO = injector.provide()
    private val defaultUnknownRagAnswer =
        property("tock_rag_default_unknown_answer", "Pardon ! je ne sais pas.").replace("\"", "")

    internal fun handle(
        botBus: BotBus,
        configuration: RagAnswerConfiguration,
    ) {
        with(botBus) {
            // check api is UP via healthcheck
            // check qu'il est up sinon unknown
            // Appel api ici !
            //TODO
//            val currentRagConfig = ragConfigurationDAO.findByNamespaceAndBotId(this.botDefinition.namespace, this.botDefinition.botId)
            if (configuration.activation == true) {
                //TODO: careful if connector notify needed
                markAsUnknown()
                try {
                    logger.debug { "Rag config : $configuration" }
                    val response =
                        ragClient.ask(RagQuery(userText.toString(), applicationId, userId.id))

                    //handle rag response
                    response?.answer?.let {
//                  bus.underlyingConnector.notify()
                        if (it != defaultUnknownRagAnswer) {
                            //TODO to format per connector or other ?
                            end(
                                "$it " +
                                        "${response.sourceDocuments}"
                            )
                        } else {
                            logger.debug { "no answer found in documents" }
                            if (configuration.noAnswerRedirection != null) {
                                manageNoAnswerRedirection(botBus, configuration)
                            } else {
                                end(it)
                            }
                        }
                    } ?: manageNoAnswerRedirection(botBus, configuration)
                } catch (conn: ConnectException) {
                    logger.error { "failed to connect to ${conn.message}" }
                } catch (e: RestException) {
                    logger.error { "error during rag call ${e.message}" }
                } finally {
                    manageNoAnswerRedirection(botBus, configuration)
                }
            } else {
                manageNoAnswerRedirection(botBus, configuration)
            }
        }
    }

    /**
     * Manage story redirection when no answer redirection is filled
     * Use the handler of the configured story otherwise launch default unknown
     * @param botBus
     * @param configuration
     */
    private fun manageNoAnswerRedirection(botBus: BotBus, configuration: RagAnswerConfiguration) {
        with(botBus) {
            //handle rag redirection in case answer is not known
            if (configuration.noAnswerRedirection != null) {
                val redirectStory = storyDefinitionConfigurationDAO.getStoryDefinitionByNamespaceAndBotIdAndStoryId(
                        botBus.botDefinition.namespace,
                        botBus.botDefinition.botId,
                        configuration.noAnswerRedirection
                )

                val noAnswerRedirectionStory = botBus.botDefinition.stories.firstOrNull { it.id == redirectStory?._id.toString() }
                noAnswerRedirectionStory?.storyHandler?.handle(this)
                        ?: botDefinition.unknownStory.storyHandler.handle(this)
            } else {
                botDefinition.unknownStory.storyHandler.handle(this)
            }
        }
    }

}

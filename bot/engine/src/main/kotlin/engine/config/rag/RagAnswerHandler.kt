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
import ai.tock.bot.admin.story.StoryDefinitionAnswersContainer
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.engine.BotBus
import ai.tock.bot.llm.rag.core.client.RagClient
import ai.tock.bot.llm.rag.core.client.models.RagQuery
import ai.tock.shared.exception.rest.RestException
import ai.tock.shared.injector
import ai.tock.shared.property
import ai.tock.shared.provide
import java.net.ConnectException
import mu.KotlinLogging

/**
 * Handler of a rag story answer
 */
object RagAnswerHandler {

    private val logger = KotlinLogging.logger {}
    private val ragClient: RagClient = injector.provide()
//    private val ragConfigurationDAO: BotRAGConfigurationDAO = injector.provide()
    private val storyDefinitionConfigurationDAO: StoryDefinitionConfigurationDAO = injector.provide()
    private val defaultUnknownRagAnswer = property("tock_rag_default_unknown_answer", "Pardon ! je ne sais pas.").replace("\"", "")

    internal fun handle(
            botBus: BotBus,
            container: StoryDefinitionAnswersContainer?,
            configuration: RagAnswerConfiguration?,
            redirectFn : (String) -> Unit
    ) {
        with(botBus) {
            // check api is UP via healthcheck
            // check qu'il est up sinon unknown
            // Appel api ici !
            //TODO
//            val currentRagConfig = ragConfigurationDAO.findByNamespaceAndBotId(this.botDefinition.namespace, this.botDefinition.botId)
            val storyConfig = container?.answers?.firstOrNull() as RagAnswerConfiguration?
            if(storyConfig?.activation == true) {
                //TODO: careful if connector notify needed
                markAsUnknown()
                try {
                    logger.debug { "Rag config : $configuration" }
                    val response =
                            ragClient.ask(RagQuery(userText.toString(), applicationId, userId.id))
                    //handle rag redirection in case answer is not known
//                    if (response?.answer == defaultUnknownRagAnswer && conf.noAnswerRedirection != null && allStories.firstOrNull { it.id == conf.noAnswerRedirection.toString() } != null) {
//                        allStories.firstOrNull { it.id == conf.noAnswerRedirection.toString() }!!.storyHandler.handle(this)
//                    } else {
                    //handle rag response
                    response?.answer?.let {
//                  bus.underlyingConnector.notify()
                        if (it != defaultUnknownRagAnswer) {
                            //TODO to format per connector or other ?
                            end("$it " +
                                    "${response.sourceDocuments}")
                        } else {
                            end(it)
                        }

                    } ?: botDefinition.unknownStory.storyHandler.handle(this)
                } catch (conn: ConnectException) {
                    logger.error { "failed to connect to ${conn.message}" }
                } catch (e: RestException) {
                    logger.error { "error during rag call ${e.message}" }
                } finally {
                    botDefinition.unknownStory.storyHandler.handle(this)
                }
            } else{
                botDefinition.unknownStory.storyHandler.handle(this)
            }
        }
    }

}

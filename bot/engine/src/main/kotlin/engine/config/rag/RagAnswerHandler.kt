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

import ai.tock.bot.engine.BotBus
import ai.tock.bot.llm.rag.core.client.RagClient
import ai.tock.bot.llm.rag.core.client.models.RagQuery
import ai.tock.shared.exception.rest.RestException
import ai.tock.shared.injector
import ai.tock.shared.provide
import mu.KotlinLogging
import java.net.ConnectException

// TODO MASS : A finaliser dans le ticket qui ajout la feature notify au bus
/**
 * Handler of a rag story answer
 */
object RagAnswerHandler {

    private val logger = KotlinLogging.logger {}
    private val ragClient: RagClient = injector.provide()


    internal fun handle(
            botBus: BotBus
    ) {
        with(botBus) {
                try {
                    logger.debug { "Rag config : ${botDefinition.ragConfiguration}" }
                    val response =
                            ragClient.ask(RagQuery(userText.toString(), applicationId, userId.id))

                    //handle rag response
                    response?.answer?.let {
                        if (!it.contains(botDefinition.ragConfiguration!!.noAnswerSentence)) {
                            //TODO to format per connector or other ?
                            end(
                                    "$it " +
                                            "${response.sourceDocuments}"
                            )
                            // TODO MASS : get langchain debug data
                        } else {
                            logger.debug { "no answer found in documents" }
                            if (botDefinition.ragConfiguration?.noAnswerStoryId != null) {
                                manageNoAnswerRedirection(this)
                            } else {
                                end(it)
                            }
                        }
                    } ?: manageNoAnswerRedirection(this)
                    // TODO MASS
                    // Comment définir le fait que le RAG n'a pas pu trouver une réponse :
                    // - Pas de documents sources retournés
                    // - Avoir "noAnswerSentence" dans la réponse (est paramètré dans le RAG setting)
                    // - Ko technique lors de l'appel de la stack python
                } catch (conn: ConnectException) {
                    logger.error { "failed to connect to ${conn.message}" }
                    manageNoAnswerRedirection(this)
                } catch (e: RestException) {
                    logger.error { "error during rag call ${e.message}" }
                    manageNoAnswerRedirection(this)
                }
        }
    }

    /**
     * Manage story redirection when no answer redirection is filled
     * Use the handler of the configured story otherwise launch default unknown
     * @param botBus
     * @param configuration
     */
    private fun manageNoAnswerRedirection(botBus: BotBus) {
        with(botBus) {
            // handle rag redirection in case answer is not known
            val noAnswerStory = botDefinition.ragConfiguration?.noAnswerStoryId?.let { noAnswerStoryId ->
                botBus.botDefinition.stories.firstOrNull { it.id == noAnswerStoryId.toString() }
            }
                ?: botDefinition.unknownStory

            noAnswerStory.storyHandler.handle(this)
        }
    }

}

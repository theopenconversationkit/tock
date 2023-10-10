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

import ai.tock.bot.connector.ConnectorFeature
import ai.tock.bot.definition.Parameters
import ai.tock.bot.definition.notify
import ai.tock.bot.engine.BotBus
import ai.tock.bot.llm.rag.core.client.RagClient
import ai.tock.bot.llm.rag.core.client.models.RagQuery
import ai.tock.shared.exception.error.ErrorMessageWrapper
import ai.tock.shared.exception.rest.RestException
import ai.tock.shared.injector
import ai.tock.shared.provide
import io.netty.handler.codec.http.HttpResponseStatus
import mu.KotlinLogging
import java.net.ConnectException

/**
 * Handler of a rag story answer
 */
// TODO MASS : to be finalised once python stack (RAG Agent) is ready
object RagAnswerHandler {

//    private val logger = KotlinLogging.logger {}
//    private val ragClient: RagClient = injector.provide()
//
//    internal fun handle(
//            botBus: BotBus
//    ) {
//        with(botBus) {
//            try {
//                if (this.underlyingConnector.hasFeature(ConnectorFeature.NOTIFY_SUPPORTED, targetConnectorType)) {
//                    // default end
//                    end()
//                    val parameters = Parameters(
//                            botBus.connectorData.metadata.toMap()
//                    )
//
//                    notify(
//                            applicationId = applicationId,
//                            namespace = botBus.botDefinition.namespace,
//                            botId = botBus.botDefinition.botId,
//                            recipientId = botBus.userId,
//                            intent = botBus.currentIntent!!,
//                            parameters = parameters,
//                            ragResult = callLLM(botBus),
//                            // TODO : error listener managing Throwable Exceptions : seems to not work as expected
//                            errorListener = {
//                                logger.info { "passing by error listener" }
//                                manageNoAnswerRedirection(botBus)
//                            }
//                    )
//                } else {
//                    end(botBus.underlyingConnector.formatNotifyRagMessage(callLLM(botBus)))
//                }
//                //   TODO : check if error Listener is doing its job : seems NOT so lets keep the following below
//            } catch (conn: ConnectException) {
//                logger.error { "failed to connect to ${conn.message}" }
//                manageNoAnswerRedirection(this)
//            } catch (e: RestException) {
//                if (e.httpResponseStatus.code() / 100 != 2) {
//                    logger.error { "error during rag call ${e.message}" }
//                }
//                manageNoAnswerRedirection(this)
//            }
//        }
//    }
//callLLM(botBus)
//    /**
//     * Call the LLM
//     * @param botBus
//     * @return [RagResult]
//     * How define that RAG could not find an answer :
//     * - No sources documents found
//     * - "noAnswerSentence" presents in answer (parameterized in RAG setting)
//     * - Technical error calling ragClient
//     */
//    private fun callLLM(botBus: BotBus): RagResult {
//        with(botBus) {
//            logger.debug { "Rag config : ${botBus.botDefinition.ragConfiguration}" }
//            val response = ragClient.ask(RagQuery(userText.toString(), applicationId, userId.id))
//
//            return if (response?.answer != null && !(response.answer.contains(botDefinition.ragConfiguration!!.noAnswerSentence))) {
//                response
//            } else {
//                if (response?.answer == null) {
//                    throw RagUnavailableException()
//                } else {
//                    throw RagNotFoundAnswerException()
//                }
//            }
//        }
//    }
//
//    /**
//     * Manage story redirection when no answer redirection is filled
//     * Use the handler of the configured story otherwise launch default unknown
//     * @param botBus
//     */
//    private fun manageNoAnswerRedirection(botBus: BotBus) {
//        with(botBus) {
//            val noAnswerStory = botDefinition.ragConfiguration?.noAnswerStoryId?.let { noAnswerStoryId ->
//                botBus.botDefinition.stories.firstOrNull { it.id == noAnswerStoryId.toString() }
//            }
//                    ?: botDefinition.unknownStory
//
//            noAnswerStory.storyHandler.handle(this)
//        }
//    }
}

/**
 * Unique Exception that throws a 204 code because RAG found no content
 * TODO : enhance the behavior
 */
class RagNotFoundAnswerException :
        RestException(ErrorMessageWrapper("No answer found in the documents"), HttpResponseStatus.NO_CONTENT)

class RagUnavailableException : RestException(
        ErrorMessageWrapper("An error seems to occurs : No answer from the service"),
        HttpResponseStatus.SERVICE_UNAVAILABLE
)

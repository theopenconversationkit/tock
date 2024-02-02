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

import ai.tock.bot.admin.bot.llm.BotRagConfiguration
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.action.Footnote
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.action.SendSentenceWithFootnotes
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.user.PlayerType
import ai.tock.llm.orchestrator.client.requests.*
import ai.tock.llm.orchestrator.client.responses.RAGResponse
import ai.tock.llm.orchestrator.client.responses.TextWithFootnotes
import ai.tock.llm.orchestrator.client.services.RAGService
import ai.tock.llm.orchestrator.core.utils.OpenSearchUtils
import ai.tock.shared.*
import engine.config.AbstractProactiveAnswerHandler
import mu.KotlinLogging

private val k_neighborsDocuments = intProperty(name="tock_gen_ai_orchestrator_document_number_neighbors", defaultValue = 4)
private val n_lastMessages = intProperty(name="tock_gen_ai_orchestrator_dialog_number_messages", defaultValue = 10)
private val technicalErrorMessage = property("tock_gen_ai_orchestrator_technical_error",
    defaultValue = property("tock_technical_error", "Technical error :( sorry!"))

object RAGAnswerHandler : AbstractProactiveAnswerHandler {

    private val logger = KotlinLogging.logger {}
    private val ragService: RAGService get() = injector.provide()

    override fun handleProactiveAnswer(botBus: BotBus) {
        with(botBus) {
            // Call RAG Api - Gen AI Orchestrator
            val response = rag(this)

            logger.info { "Send RAG API response" }
            send(
                SendSentenceWithFootnotes(
                    botId,
                    applicationId,
                    userId,
                    text = response.answer.text,
                    footnotes = response.answer.footnotes.map {
                        Footnote(
                            it.identifier,
                            it.title,
                            it.url
                        )
                        }.toMutableList()
                )
            )

            if(connectorData.metadata["debugEnabled"].toBoolean()) {
                response.debug?.let { sendDebugData("RAG", it) }
            }
        }
    }

    /**
     * Manage story redirection when no answer redirection is filled
     * Use the handler of the configured story otherwise launch default unknown story
     * @param botBus
     */
    private fun ragResponseHandler(botBus: BotBus, response: RAGResponse?) {

        with(botBus) {
            val ragConfig = botDefinition.ragConfiguration
            if (response?.answer?.text.equals(ragConfig?.noAnswerSentence, ignoreCase = true)) {
                logger.info { "The RAG API response is equal to the configured no-answer sentence." }
                val noAnswerStoryId = ragConfig?.noAnswerStoryId?.toString()
                if (!noAnswerStoryId.isNullOrBlank()) {
                    logger.info { "A no-answer story $noAnswerStoryId is configured, so run it." }
                    var noAnswerStory = botDefinition.stories.firstOrNull { it.id == noAnswerStoryId }

                    if(noAnswerStory == null) {
                        logger.warn { "The no-answer story $noAnswerStoryId was not found. So run the predefined unknown story."  }
                        noAnswerStory = botDefinition.unknownStory
                    }

                    logger.info { "Run the story intent=${noAnswerStory.mainIntent()}" }
                    noAnswerStory.storyHandler.handle(this)
                }
            }
        }
    }

    /**
     * Call RAG API
     * @param botBus
     */
    private fun rag(botBus: BotBus): RAGResponse {
        logger.info { "Call Generative AI Orchestrator - RAG API" }
        with(botBus) {

            val ragConfiguration = botDefinition.ragConfiguration!!

            try {
                val response = ragService.rag(
                    query = RAGQuery(
                        history = getDialogHistory(dialog),
                        questionAnsweringLlmSetting = ragConfiguration.llmSetting,
                        questionAnsweringPromptInputs = mapOf(
                            "question" to action.toString(),
                            "locale" to userPreferences.locale.displayLanguage,
                            "no_answer" to ragConfiguration.noAnswerSentence
                        ),
                        embeddingQuestionEmSetting = ragConfiguration.emSetting,
                        documentIndexName = OpenSearchUtils.normalizeDocumentIndexName(
                            ragConfiguration.namespace,
                            ragConfiguration.botId
                        ),
                        documentSearchParams = OpenSearchParams(
                            // The number of neighbors to return for each query_embedding.
                            k = k_neighborsDocuments,
                            filter = listOf(
                                Term(term = mapOf("metadata.index_session_id.keyword" to ragConfiguration.indexSessionId!!))
                            )
                        ),
                    ), debug = connectorData.metadata["debugEnabled"].toBoolean()
                )

                // Handle RAG response
                ragResponseHandler(this, response)

                return response!!
            }catch (exc: Exception){
                logger.error { exc }
                return RAGResponse(answer = TextWithFootnotes(text = technicalErrorMessage), debug = exc)
            }
        }
    }

    /**
     * Create a dialog history (Human and Bot message)
     * @param dialog
     */
    private fun getDialogHistory(dialog: Dialog): List<ChatMessage> =
        dialog.stories
            .flatMap { it.actions }
            .mapNotNull {
                when (it) {
                    is SendSentence -> if (it.text == null)
                        null
                    else ChatMessage(
                        text = it.text.toString(),
                        type = if (PlayerType.user == it.playerId.type) ChatMessageType.HUMAN
                        else ChatMessageType.AI
                    )

                    is SendSentenceWithFootnotes -> ChatMessage(
                        text = it.text.toString(),
                        type = ChatMessageType.AI
                    )

                    // Other types of action are not considered part of history.
                    else -> null
                }
            }
            // drop the last message, because it corresponds to the user's current question
            .dropLast(n=1)
            // take last 10 messages
            .takeLast(n=n_lastMessages)

}
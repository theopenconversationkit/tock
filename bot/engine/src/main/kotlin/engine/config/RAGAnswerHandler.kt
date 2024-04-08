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

import ai.tock.bot.admin.bot.rag.BotRAGConfiguration
import ai.tock.bot.definition.StoryDefinition
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.action.Footnote
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.action.SendSentenceWithFootnotes
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.user.PlayerType
import ai.tock.genai.orchestratorclient.requests.*
import ai.tock.genai.orchestratorclient.responses.RAGResponse
import ai.tock.genai.orchestratorclient.responses.TextWithFootnotes
import ai.tock.genai.orchestratorclient.retrofit.GenAIOrchestratorBusinessError
import ai.tock.genai.orchestratorclient.services.RAGService
import ai.tock.genai.orchestratorcore.utils.OpenSearchUtils
import ai.tock.shared.*
import engine.config.AbstractProactiveAnswerHandler
import mu.KotlinLogging

private val kNeighborsDocuments =
    intProperty(name = "tock_gen_ai_orchestrator_document_number_neighbors", defaultValue = 1)
private val nLastMessages = intProperty(name = "tock_gen_ai_orchestrator_dialog_number_messages", defaultValue = 10)
private val technicalErrorMessage = property(
    "tock_gen_ai_orchestrator_technical_error",
    defaultValue = property("tock_technical_error", "Technical error :( sorry!")
)
private val ragDebugEnabled = booleanProperty(name = "tock_gen_ai_orchestrator_rag_debug_enabled", defaultValue = false)

object RAGAnswerHandler : AbstractProactiveAnswerHandler {

    private val logger = KotlinLogging.logger {}
    private val ragService: RAGService get() = injector.provide()

    override fun handleProactiveAnswer(botBus: BotBus) {
        with(botBus) {
            // Call RAG Api - Gen AI Orchestrator
            val (answer, debug) = rag(this)

            if (answer != null) {
                logger.info { "Send RAG API response" }
                send(
                    SendSentenceWithFootnotes(
                        botId,
                        applicationId,
                        userId,
                        text = answer.text,
                        footnotes = answer.footnotes.map {
                            Footnote(
                                it.identifier,
                                it.title,
                                it.url
                            )
                        }.toMutableList()
                    )
                )
            } else {
                logger.info { "No RAG response to send!" }
            }

            if (connectorData.metadata["debugEnabled"].toBoolean() || ragDebugEnabled) {
                debug?.let { sendDebugData("RAG", it) }
            }
        }
    }

    /**
     * Manage story redirection when no answer redirection is filled
     * Use the handler of the configured story otherwise launch default unknown story
     * @param botBus the bot Bus
     * @param response the RAG response
     */
    private fun ragStoryRedirection(botBus: BotBus, response: RAGResponse?): Boolean {
        with(botBus) {
            val ragConfig = botDefinition.ragConfiguration
            if (response?.answer?.text.equals(ragConfig?.noAnswerSentence, ignoreCase = true)) {
                logger.info { "The RAG API response is equal to the configured no-answer sentence." }
                switch(ragConfig)
                return true
            }
        }
        return false
    }

    /**
     * Switch to the configured no-answer story if exists.
     * Switch to the default unknown story otherwise.
     * @param ragConfig: The RAG configuration
     */
    private fun BotBus.switch(
        ragConfig: BotRAGConfiguration?
    ) {
        val noAnswerStory: StoryDefinition
        val noAnswerStoryId = ragConfig?.noAnswerStoryId
        if (!noAnswerStoryId.isNullOrBlank()) {
            logger.info { "A no-answer story $noAnswerStoryId is configured, so run it." }
            noAnswerStory = botDefinition.findStoryDefinitionById(noAnswerStoryId, applicationId)
        } else {
            logger.info { "No no-answer story is configured, so run the default unknown story." }
            noAnswerStory = botDefinition.unknownStory
        }

        logger.info { "Run the story intent=${noAnswerStory.mainIntent()}, id=${noAnswerStory.id}" }
        handleAndSwitchStory(noAnswerStory, noAnswerStory.mainIntent())
    }

    /**
     * Call RAG API
     * @param botBus
     *
     * @return RAG response if it needs to be handled, null otherwise (already handled by a switch for instance in case of no response)
     */
    private fun rag(botBus: BotBus): Pair<TextWithFootnotes?, Any?> {
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
                            k = kNeighborsDocuments,
                            filter = listOf(
                                Term(term = mapOf("metadata.index_session_id.keyword" to ragConfiguration.indexSessionId!!))
                            )
                        ),
                    ), debug = connectorData.metadata["debugEnabled"].toBoolean() || ragDebugEnabled
                )

                // Handle RAG response
                return if (!ragStoryRedirection(this, response)) {
                    Pair(response?.answer, response?.debug)
                } else {
                    // Do not return a response when the RAG story has been switched to the no RAG answer story
                    Pair(null, response?.debug)
                }
            } catch (exc: Exception) {
                logger.error { exc }
                return if (exc is GenAIOrchestratorBusinessError && exc.error.info.error == "APITimeoutError") {
                    switch(ragConfiguration)
                    // Do not return a response when the RAG story has been switched to the no RAG answer story
                    Pair(null, null)
                }else Pair(TextWithFootnotes(text = technicalErrorMessage), exc)
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
            .dropLast(n = 1)
            // take last 10 messages
            .takeLast(n = nLastMessages)

}
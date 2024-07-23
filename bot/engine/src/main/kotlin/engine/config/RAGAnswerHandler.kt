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
import ai.tock.bot.admin.indicators.IndicatorValues
import ai.tock.bot.admin.indicators.Indicators
import ai.tock.bot.admin.indicators.metric.MetricType
import ai.tock.bot.definition.RAGStoryDefinition
import ai.tock.bot.definition.StoryDefinition
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.BotRepository
import ai.tock.bot.engine.action.ActionMetadata
import ai.tock.bot.engine.action.Footnote
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.action.SendSentenceWithFootnotes
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.user.PlayerType
import ai.tock.genai.orchestratorclient.requests.ChatMessage
import ai.tock.genai.orchestratorclient.requests.ChatMessageType
import ai.tock.genai.orchestratorclient.requests.DialogDetails
import ai.tock.genai.orchestratorclient.requests.RAGQuery
import ai.tock.genai.orchestratorclient.responses.ObservabilityInfo
import ai.tock.genai.orchestratorclient.responses.RAGResponse
import ai.tock.genai.orchestratorclient.responses.TextWithFootnotes
import ai.tock.genai.orchestratorclient.retrofit.GenAIOrchestratorBusinessError
import ai.tock.genai.orchestratorclient.retrofit.GenAIOrchestratorValidationError
import ai.tock.genai.orchestratorclient.services.RAGService
import ai.tock.genai.orchestratorcore.utils.VectorStoreUtils
import ai.tock.shared.*
import engine.config.AbstractProactiveAnswerHandler
import mu.KotlinLogging

private val nLastMessages = intProperty(
    name = "tock_gen_ai_orchestrator_dialog_number_messages",
    defaultValue = 5)
private val technicalErrorMessage = property(
    name = "tock_gen_ai_orchestrator_technical_error",
    defaultValue = "Technical error :( sorry!")
private val ragDebugEnabled = booleanProperty(
    name = "tock_gen_ai_orchestrator_rag_debug_enabled",
    defaultValue = false)


object RAGAnswerHandler : AbstractProactiveAnswerHandler {

    private val logger = KotlinLogging.logger {}
    private val ragService: RAGService get() = injector.provide()


    override fun handleProactiveAnswer(botBus: BotBus): StoryDefinition? {
        return with(botBus) {
            // Save story handled metric
            BotRepository.saveMetric(createMetric(MetricType.STORY_HANDLED))

            // Call RAG Api - Gen AI Orchestrator
            val (answer, debug, noAnswerStory, observabilityInfo) = rag(this)

            // Add debug data if available and if debugging is enabled
            if (debug != null && (action.metadata.debugEnabled || ragDebugEnabled)) {
                logger.info { "Send RAG debug data." }
                sendDebugData("RAG", debug)
            }

            // Handle the RAG answer
            if (noAnswerStory == null && answer != null) {
                logger.info { "Send RAG answer." }
                send(
                    SendSentenceWithFootnotes(
                        botId, connectorId, userId, text = answer.text, footnotes = answer.footnotes.map {
                            Footnote(
                                it.identifier, it.title, it.url,
                                if(action.metadata.sourceWithContent) it.content else null,
                                it.score
                            )
                        }.toMutableList(),
                        metadata = ActionMetadata(isGenAiRagAnswer = true, observabilityInfo = observabilityInfo)
                    )
                )
            } else {
                logger.info { "No RAG answer to send, because a noAnswerStory is returned." }
            }

            noAnswerStory
        }
    }

    /**
     * Manage story redirection when no answer redirection is filled
     * Use the handler of the configured story otherwise launch default unknown story
     * @param botBus the bot Bus
     * @param response the RAG response
     */
    private fun ragStoryRedirection(botBus: BotBus, response: RAGResponse?): StoryDefinition? {
        return with(botBus) {
            botDefinition.ragConfiguration?.let { ragConfig ->
                if (response?.answer?.text.equals(ragConfig.noAnswerSentence, ignoreCase = true)) {
                    // Save no answer metric
                    saveRagMetric(IndicatorValues.NO_ANSWER)

                    // Switch to no answer story if configured
                    if (!ragConfig.noAnswerStoryId.isNullOrBlank()) {
                        logger.info { "The RAG response is equal to the configured no-answer sentence, so switch to the no-answer story." }
                        getNoAnswerRAGStory(ragConfig)
                    } else null
                } else {
                    // Save success metric
                    saveRagMetric(IndicatorValues.SUCCESS)
                    null
                }
            }
        }
    }

    /**
     * Switch to the configured no-answer story if exists.
     * Switch to the default unknown story otherwise.
     * @param ragConfig: The RAG configuration
     */
    private fun BotBus.getNoAnswerRAGStory(
        ragConfig: BotRAGConfiguration
    ): StoryDefinition {
        val noAnswerStory: StoryDefinition
        val noAnswerStoryId = ragConfig.noAnswerStoryId
        if (!noAnswerStoryId.isNullOrBlank()) {
            logger.info { "A no-answer story $noAnswerStoryId is configured, so run it." }
            noAnswerStory = botDefinition.findStoryDefinitionById(noAnswerStoryId, connectorId).let {
                // Prevent infinite loop when the noAnswerStory is removed or disabled
                if (it.id == RAGStoryDefinition.RAG_STORY_NAME) {
                    logger.info { "The no-answer story is removed or disabled, so run the default unknown story." }
                    botDefinition.unknownStory
                } else it
            }
        } else {
            logger.info { "No no-answer story is configured, so run the default unknown story." }
            noAnswerStory = botDefinition.unknownStory
        }

        return noAnswerStory
    }

    /**
     * Call RAG API
     * @param botBus
     *
     * @return RAGResult. The answer is given if it needs to be handled, null otherwise (already handled by a switch for instance in case of no response)
     */
    private fun rag(botBus: BotBus): RAGResult {
        logger.info { "Call Generative AI Orchestrator - RAG API" }
        with(botBus) {
            // The RAG Story is only handled when RAG and will use Vector Store settings if defined,
            // otherwise it will not send any vector store setting in the RAG Query which will default to
            // Gen AI Orchestrator environment variable vector settings
            val ragConfiguration = botDefinition.ragConfiguration!!
            val vectorStoreConfiguration = botDefinition.vectorStoreConfiguration
            val vectorStoreSetting = vectorStoreConfiguration?.takeIf { it.enabled }?.setting

            val (documentSearchParams, indexName) = VectorStoreUtils.getVectorStoreElements(
                ragConfiguration.namespace,
                ragConfiguration.botId,
                // The indexSessionId is mandatory to enable RAG Story
                ragConfiguration.indexSessionId!!,
                vectorStoreSetting
            )

            try {
                val response = ragService.rag(
                    query = RAGQuery(
                        dialog = DialogDetails(
                            dialogId = dialog.id.toString(),
                            userId = dialog.playerIds.firstOrNull { PlayerType.user == it.type }?.id,
                            history = getDialogHistory(dialog),
                            tags = listOf(
                                "connector:${underlyingConnector.connectorType.id}"
                            )
                        ),
                        questionAnsweringLlmSetting = ragConfiguration.llmSetting,
                        questionAnsweringPromptInputs = mapOf(
                            "question" to action.toString(),
                            "locale" to userPreferences.locale.displayLanguage,
                            "no_answer" to ragConfiguration.noAnswerSentence
                        ),
                        embeddingQuestionEmSetting = ragConfiguration.emSetting,
                        documentIndexName = indexName,
                        documentSearchParams = documentSearchParams,
                        compressorSetting = botDefinition.documentCompressorConfiguration?.setting,
                        vectorStoreSetting = vectorStoreSetting,
                        observabilitySetting = botDefinition.observabilityConfiguration?.setting
                    ), debug = action.metadata.debugEnabled || ragDebugEnabled
                )

                // Handle RAG response
                return RAGResult(response?.answer, response?.debug, ragStoryRedirection(this, response), response?.observabilityInfo)
            } catch (exc: Exception) {
                logger.error { exc }
                // Save failure metric
                saveRagMetric(IndicatorValues.FAILURE)

                return if (exc is GenAIOrchestratorBusinessError && exc.error.info.error == "APITimeoutError") {
                    logger.info { "The APITimeoutError is raised, so switch to the no-answer story." }
                    RAGResult(noAnswerStory = getNoAnswerRAGStory(ragConfiguration))
                }
                else RAGResult(
                    answer = TextWithFootnotes(text = technicalErrorMessage),
                    debug = when(exc) {
                        is GenAIOrchestratorBusinessError -> RAGError(exc.message, exc.error)
                        is GenAIOrchestratorValidationError -> RAGError(exc.message, exc.detail)
                        else -> RAGError(errorMessage = exc.message)
                    }
                )
            }
        }
    }

    /**
     * Create a dialog history (Human and Bot message)
     * @param dialog
     */
    private fun getDialogHistory(dialog: Dialog): List<ChatMessage> = dialog.stories.flatMap { it.actions }.mapNotNull {
        when (it) {
            is SendSentence -> if (it.text == null) null
            else ChatMessage(
                text = it.text.toString(), type = if (PlayerType.user == it.playerId.type) ChatMessageType.HUMAN
                else ChatMessageType.AI
            )

            is SendSentenceWithFootnotes -> ChatMessage(
                text = it.text.toString(), type = ChatMessageType.AI
            )

            // Other types of action are not considered part of history.
            else -> null
        }
    }
        // drop the last message, because it corresponds to the user's current question
        .dropLast(n = 1)
        // take last 10 messages
        .takeLast(n = nLastMessages)

    private fun BotBus.saveRagMetric(indicator: IndicatorValues) {
        BotRepository.saveMetric(
            createMetric(
                MetricType.QUESTION_REPLIED, Indicators.RAG.value.name, indicator.value.name
            )
        )
    }
}

/**
 * The RAG result.
 * Aggregation of RAG answer, debug and the no answer Story.
 */
data class RAGResult(
    val answer: TextWithFootnotes? = null,
    val debug: Any? = null,
    val noAnswerStory: StoryDefinition? = null,
    val observabilityInfo: ObservabilityInfo? = null,
)

/**
 * The RAG error.
 */
data class RAGError(
    val errorMessage: String?,
    val errorDetail: Any? = null,
)
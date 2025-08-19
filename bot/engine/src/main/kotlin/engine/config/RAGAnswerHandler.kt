/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
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

import ai.tock.bot.admin.indicators.IndicatorValues
import ai.tock.bot.admin.indicators.Indicators
import ai.tock.bot.admin.indicators.metric.MetricType
import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.definition.RAGStoryDefinition.Companion.RAG_STORY_NAME
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
import ai.tock.genai.orchestratorclient.requests.RAGRequest
import ai.tock.genai.orchestratorclient.responses.LLMAnswer
import ai.tock.genai.orchestratorclient.responses.ObservabilityInfo
import ai.tock.genai.orchestratorclient.responses.RAGResponse
import ai.tock.genai.orchestratorclient.retrofit.GenAIOrchestratorBusinessError
import ai.tock.genai.orchestratorclient.retrofit.GenAIOrchestratorParsingError
import ai.tock.genai.orchestratorclient.retrofit.GenAIOrchestratorValidationError
import ai.tock.genai.orchestratorclient.services.RAGService
import ai.tock.genai.orchestratorcore.models.observability.LangfuseObservabilitySetting
import ai.tock.genai.orchestratorcore.utils.VectorStoreUtils
import ai.tock.shared.injector
import ai.tock.shared.property
import ai.tock.shared.provide
import engine.config.AbstractProactiveAnswerHandler
import mu.KotlinLogging

private val technicalErrorMessage =
    property(
        name = "tock_gen_ai_orchestrator_technical_error",
        defaultValue = "Technical error :( sorry!",
    )

private const val UNKNOWN_INTENT = "unknown"

object RAGAnswerHandler : AbstractProactiveAnswerHandler {
    private val logger = KotlinLogging.logger {}
    private val ragService: RAGService get() = injector.provide()

    override fun handleProactiveAnswer(botBus: BotBus): StoryDefinition? {
        return with(botBus) {
            // Save story handled metric
            BotRepository.saveMetric(createMetric(MetricType.STORY_HANDLED))

            // Call RAG Api - Gen AI Orchestrator
            val (answer, footnotes, debug, redirectStory, observabilityInfo) = rag(this)

            // Add debug data if available and if debugging is enabled
            if (debug != null) {
                logger.info { "Send RAG debug data." }
                sendDebugData("RAG", debug)
            }

            val modifiedObservabilityInfo = observabilityInfo?.let { updateObservabilityInfo(this, it) }

            // Footnotes building
            val preparedFootnotes =
                footnotes?.map {
                    Footnote(
                        it.identifier,
                        it.title,
                        it.url,
                        if (action.metadata.sourceWithContent) it.content else null,
                        it.score,
                    )
                }?.toMutableList() ?: mutableListOf()

            // Identifying text to be sent
            val textToSend = if (answer?.displayAnswer == true) answer.answer.orEmpty() else ""

            // Send SendSentenceWithFootnotes
            logger.info { "Send RAG answer." }
            send(
                action =
                    SendSentenceWithFootnotes(
                        playerId = botId,
                        applicationId = connectorId,
                        recipientId = userId,
                        text = textToSend,
                        footnotes = preparedFootnotes,
                        metadata =
                            ActionMetadata(
                                isGenAiRagAnswer = true,
                                observabilityInfo = modifiedObservabilityInfo,
                            ),
                    ),
            )

            redirectStory
        }
    }

    private fun updateObservabilityInfo(
        botBus: BotBus,
        info: ObservabilityInfo,
    ): ObservabilityInfo {
        val config = botBus.botDefinition.observabilityConfiguration
        if (config?.enabled == true && config.setting is LangfuseObservabilitySetting<*>) {
            val setting = config.setting as LangfuseObservabilitySetting<*>
            val publicUrl = setting.publicUrl
            if (!publicUrl.isNullOrBlank()) {
                return info.copy(traceUrl = info.traceUrl.replace(setting.url, publicUrl))
            }
        }
        return info
    }

    /**
     * Manage story redirection
     * Use the handler of the configured story otherwise launch default unknown story
     * @param botDefinition the bot definition
     * @param response the RAG response
     */
    private fun ragStoryRedirection(
        botDefinition: BotDefinition,
        response: RAGResponse?,
    ): StoryDefinition? {
        return response?.answer?.redirectionIntent?.let {
            if (UNKNOWN_INTENT == it) {
                throw GenAIOrchestratorParsingError(
                    message = "RAG - Story redirection failed",
                    detail = "Unknown story cannot be used.",
                )
            }

            val targetStory = botDefinition.findStoryDefinition(it, "")

            if (RAG_STORY_NAME == targetStory.id) {
                throw GenAIOrchestratorParsingError(
                    message = "RAG - Story redirection failed",
                    detail = "No story found for intent=$it",
                )
            }

            targetStory
        }
    }

    /**
     * Call RAG API
     * @param botBus
     *
     * @return RAGResult. The answer is given if it needs to be handled, null otherwise
     * (already handled by a switch for instance in case of no response)
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

            val (documentSearchParams, indexName) =
                VectorStoreUtils.getVectorStoreElements(
                    ragConfiguration.namespace,
                    ragConfiguration.botId,
                    // The indexSessionId is mandatory to enable RAG Story
                    ragConfiguration.indexSessionId!!,
                    ragConfiguration.maxDocumentsRetrieved,
                    vectorStoreSetting,
                )

            val questionAnsweringPrompt =
                ragConfiguration.questionAnsweringPrompt
                    ?: ragConfiguration.initQuestionAnsweringPrompt()

            var debug: Any? = null
            try {
                val response =
                    ragService.rag(
                        query =
                            RAGRequest(
                                dialog =
                                    DialogDetails(
                                        dialogId = dialog.id.toString(),
                                        userId = dialog.playerIds.firstOrNull { PlayerType.user == it.type }?.id,
                                        history = getDialogHistory(dialog, ragConfiguration.maxMessagesFromHistory),
                                        tags =
                                            listOf(
                                                "connector:${underlyingConnector.connectorType.id}",
                                            ),
                                    ),
                                questionCondensingLlmSetting = ragConfiguration.questionCondensingLlmSetting,
                                questionCondensingPrompt = ragConfiguration.questionCondensingPrompt,
                                questionAnsweringLlmSetting = ragConfiguration.getQuestionAnsweringLLMSetting(),
                                questionAnsweringPrompt =
                                    questionAnsweringPrompt.copy(
                                        inputs =
                                            mapOf(
                                                "question" to action.toString(),
                                                "locale" to userPreferences.locale.displayLanguage,
                                            ),
                                    ),
                                embeddingQuestionEmSetting = ragConfiguration.emSetting,
                                documentIndexName = indexName,
                                documentSearchParams = documentSearchParams,
                                compressorSetting = botDefinition.documentCompressorConfiguration?.setting,
                                vectorStoreSetting = vectorStoreSetting,
                                observabilitySetting = botDefinition.observabilityConfiguration?.setting,
                                documentsRequired = ragConfiguration.documentsRequired,
                            ),
                        debug = action.metadata.debugEnabled || ragConfiguration.debugEnabled,
                    )

                debug = response?.debug
                if (response?.answer?.status.equals("not_found_in_context", ignoreCase = true)) {
                    // Save no answer metric
                    saveRagMetric(IndicatorValues.NO_ANSWER)
                } else {
                    // Save success metric
                    saveRagMetric(IndicatorValues.SUCCESS)
                }

                // Handle RAG response
                return RAGResult(
                    answer = response?.answer,
                    footnotes = response?.footnotes,
                    debug = debug,
                    redirectStory = ragStoryRedirection(this.botDefinition, response),
                    observabilityInfo = response?.observabilityInfo,
                )
            } catch (exc: Exception) {
                logger.error { exc }
                // Save failure metric
                saveRagMetric(IndicatorValues.FAILURE)

                val ragError =
                    when (exc) {
                        is GenAIOrchestratorBusinessError -> RAGError(exc.message, exc.error)
                        is GenAIOrchestratorValidationError -> RAGError(exc.message, exc.detail)
                        is GenAIOrchestratorParsingError -> RAGError(exc.message, exc.detail)
                        else -> RAGError(errorMessage = exc.message)
                    }

                (debug as MutableMap<String, Any?>)["error"] = ragError

                return RAGResult(
                    answer = LLMAnswer(status = "technical_error", answer = technicalErrorMessage),
                    debug = debug,
                )
            }
        }
    }

    /**
     * Create a dialog history (Human and Bot message)
     * @param dialog
     */
    private fun getDialogHistory(
        dialog: Dialog,
        nLastMessages: Int,
    ): List<ChatMessage> =
        dialog.stories.flatMap { it.actions }.mapNotNull {
            when (it) {
                is SendSentence ->
                    if (it.text == null) {
                        null
                    } else {
                        ChatMessage(
                            text = it.text.toString(),
                            type =
                                if (PlayerType.user == it.playerId.type) {
                                    ChatMessageType.HUMAN
                                } else {
                                    ChatMessageType.AI
                                },
                        )
                    }

                is SendSentenceWithFootnotes ->
                    ChatMessage(
                        text = it.text.toString(),
                        type = ChatMessageType.AI,
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
                MetricType.QUESTION_REPLIED,
                Indicators.RAG.value.name,
                indicator.value.name,
            ),
        )
    }
}

/**
 * The RAG result.
 * Aggregation of RAG answer, debug and the no answer Story.
 */
data class RAGResult(
    val answer: LLMAnswer? = null,
    val footnotes: List<ai.tock.genai.orchestratorclient.responses.Footnote>? = null,
    val debug: Any? = null,
    val redirectStory: StoryDefinition? = null,
    val observabilityInfo: ObservabilityInfo? = null,
    val error: Any? = null,
)

/**
 * The RAG error.
 */
data class RAGError(
    val errorMessage: String?,
    val errorDetail: Any? = null,
)

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

package ai.tock.bot.admin.service

import ai.tock.bot.admin.dataset.DatasetDAO
import ai.tock.bot.admin.dataset.DatasetQuestion
import ai.tock.bot.admin.dataset.DatasetRun
import ai.tock.bot.admin.dataset.DatasetRunDAO
import ai.tock.bot.admin.dataset.DatasetRunQuestionResult
import ai.tock.bot.admin.dataset.DatasetRunQuestionResultState
import ai.tock.bot.admin.dataset.DatasetRunState
import ai.tock.bot.admin.test.TestTalkService
import ai.tock.bot.admin.test.model.BotDialogResponse
import ai.tock.bot.connector.rest.client.model.ClientDebug
import ai.tock.bot.engine.message.Sentence
import ai.tock.shared.Executor
import ai.tock.shared.injector
import ai.tock.shared.intProperty
import ai.tock.shared.longProperty
import ai.tock.shared.provide
import mu.KotlinLogging
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean

private const val DATASET_RUN_WORKER_POLL_INTERVAL_PROPERTY = "tock_dataset_run_worker_poll_interval_ms"
private const val DATASET_RUN_WORKER_MAX_RETRIES_PROPERTY = "tock_dataset_run_worker_max_retries"
private const val DEFAULT_DATASET_RUN_WORKER_POLL_INTERVAL_MS = 2000L
private const val DEFAULT_DATASET_RUN_WORKER_MAX_RETRIES = 0
private const val RAG_DEBUG_MESSAGE_TEXT = "RAG"
private const val RAG_TECHNICAL_ERROR_STATUS = "technical_error"
private val MIN_TIMER_DELAY: Duration = Duration.ofMillis(1)

fun interface DatasetQuestionExecutor {
    fun execute(
        run: DatasetRun,
        questionResult: DatasetRunQuestionResult,
        question: DatasetQuestion,
    ): BotDialogResponse
}

object DefaultDatasetQuestionExecutor : DatasetQuestionExecutor {
    override fun execute(
        run: DatasetRun,
        questionResult: DatasetRunQuestionResult,
        question: DatasetQuestion,
    ): BotDialogResponse =
        TestTalkService.talk(
            botApplicationConfigurationId =
                run.botApplicationConfigurationId
                    ?: error("Missing botApplicationConfigurationId for dataset run ${run._id}"),
            namespace = run.namespace,
            message = Sentence(question.question),
            language = run.language,
            userIdModifier = buildUserIdModifier(questionResult),
            debugEnabled = true,
            sourceWithContent = true,
        )

    private fun buildUserIdModifier(questionResult: DatasetRunQuestionResult): String =
        if (questionResult.retryCount == 0) {
            questionResult.userIdModifier
        } else {
            "${questionResult.userIdModifier}_retry${questionResult.retryCount}"
        }
}

class DatasetRunProcessor(
    private val datasetDAO: DatasetDAO = injector.provide(),
    private val datasetRunDAO: DatasetRunDAO = injector.provide(),
    private val questionExecutor: DatasetQuestionExecutor = DefaultDatasetQuestionExecutor,
    private val maxRetries: Int = intProperty(DATASET_RUN_WORKER_MAX_RETRIES_PROPERTY, DEFAULT_DATASET_RUN_WORKER_MAX_RETRIES),
    private val clock: Clock = Clock.systemUTC(),
) {
    private val logger = KotlinLogging.logger {}

    fun processNextQueuedRun(): Boolean {
        val run = datasetRunDAO.claimNextQueuedRun() ?: return false
        processRun(run)
        return true
    }

    internal fun processRun(run: DatasetRun) {
        val dataset = datasetDAO.getDatasetById(run.datasetId)
        if (dataset == null) {
            logger.warn { "Dataset ${run.datasetId} not found for run ${run._id}. Marking run as cancelled." }
            datasetRunDAO.saveRun(run.copy(state = DatasetRunState.CANCELLED, endTime = now()))
            return
        }

        val questionResultsById =
            datasetRunDAO.getQuestionResultsByRunId(run._id)
                .associateBy { it.questionId }
                .toMutableMap()

        dataset.questions.forEach { question ->
            val refreshedRun = datasetRunDAO.getRunById(run._id) ?: return
            if (refreshedRun.state == DatasetRunState.CANCELLED) {
                return
            }

            val questionResult = questionResultsById[question.id] ?: return@forEach
            if (questionResult.state.isTerminal()) {
                return@forEach
            }

            questionResultsById[question.id] = executeQuestion(refreshedRun, question, questionResult)
        }

        val finalRun = datasetRunDAO.getRunById(run._id) ?: return
        if (finalRun.state != DatasetRunState.CANCELLED) {
            datasetRunDAO.saveRun(finalRun.copy(state = DatasetRunState.COMPLETED, endTime = now()))
        }
    }

    private fun executeQuestion(
        run: DatasetRun,
        question: DatasetQuestion,
        questionResult: DatasetRunQuestionResult,
    ): DatasetRunQuestionResult {
        var currentResult =
            datasetRunDAO.saveQuestionResult(
                questionResult.copy(
                    state = DatasetRunQuestionResultState.RUNNING,
                    startedAt = questionResult.startedAt ?: now(),
                    endedAt = null,
                    error = null,
                ),
            )

        while (true) {
            val outcome =
                runCatching {
                    questionExecutor.execute(run, currentResult, question)
                }

            val response = outcome.getOrNull()
            val retryableFailureMessage = response?.toRetryableFailureMessage()
            if (response?.userActionId != null && retryableFailureMessage == null) {
                return datasetRunDAO.saveQuestionResult(
                    currentResult.copy(
                        state = DatasetRunQuestionResultState.COMPLETED,
                        endedAt = now(),
                        userActionId = response.userActionId,
                        error = null,
                    ),
                )
            }

            val errorMessage = outcome.exceptionOrNull()?.message ?: retryableFailureMessage ?: response.toFailureMessage()
            if (currentResult.retryCount >= maxRetries) {
                return datasetRunDAO.saveQuestionResult(
                    currentResult.copy(
                        state = DatasetRunQuestionResultState.FAILED,
                        endedAt = now(),
                        userActionId = response?.userActionId ?: currentResult.userActionId,
                        error = errorMessage,
                    ),
                )
            }

            currentResult =
                datasetRunDAO.saveQuestionResult(
                    currentResult.copy(
                        retryCount = currentResult.retryCount + 1,
                        userActionId = response?.userActionId ?: currentResult.userActionId,
                        error = errorMessage,
                    ),
                )
        }
    }

    private fun DatasetRunQuestionResultState.isTerminal(): Boolean =
        this == DatasetRunQuestionResultState.COMPLETED ||
            this == DatasetRunQuestionResultState.FAILED ||
            this == DatasetRunQuestionResultState.CANCELLED

    private fun BotDialogResponse?.toFailureMessage(): String =
        this?.messages?.takeIf { it.isNotEmpty() }?.joinToString(separator = " | ") { it.toString() }
            ?: "Talk execution did not return a userActionId"

    private fun BotDialogResponse.toRetryableFailureMessage(): String? =
        messages
            .asSequence()
            .filterIsInstance<ClientDebug>()
            .mapNotNull { it.toRAGTechnicalFailure() }
            .firstOrNull()

    private fun ClientDebug.toRAGTechnicalFailure(): String? {
        if (text != RAG_DEBUG_MESSAGE_TEXT) {
            return null
        }

        val dataMap = data.asMap() ?: return null
        val answerMap = dataMap["answer"].asMap()
        val status = answerMap?.stringValue("status")
        val errorMessage = dataMap["error"].asMap()?.stringValue("errorMessage")

        if (status != RAG_TECHNICAL_ERROR_STATUS && errorMessage == null) {
            return null
        }

        val answerMessage = answerMap?.stringValue("answer")
        return "RAG execution failed: ${errorMessage ?: answerMessage ?: status ?: "unknown error"}"
    }

    private fun Any?.asMap(): Map<*, *>? = this as? Map<*, *>

    private fun Map<*, *>.stringValue(key: String): String? = this[key] as? String

    private fun now(): Instant = Instant.now(clock)
}

object DatasetRunWorker {
    private val logger = KotlinLogging.logger {}
    private val started = AtomicBoolean(false)
    private val processing = AtomicBoolean(false)
    private val pollInterval =
        Duration.ofMillis(
            longProperty(
                DATASET_RUN_WORKER_POLL_INTERVAL_PROPERTY,
                DEFAULT_DATASET_RUN_WORKER_POLL_INTERVAL_MS,
            ),
        )

    private val executor: Executor by lazy { injector.provide() }

    fun start() {
        if (!started.compareAndSet(false, true)) {
            return
        }

        executor.setPeriodic(MIN_TIMER_DELAY, pollInterval) {
            triggerProcessing()
        }
    }

    private fun triggerProcessing() {
        if (!processing.compareAndSet(false, true)) {
            return
        }

        executor.executeBlocking {
            try {
                val processor = DatasetRunProcessor()
                while (processor.processNextQueuedRun()) {
                    // Drain the queue while we own the worker slot.
                }
            } catch (e: Exception) {
                logger.error(e) { "Dataset run worker execution failed." }
            } finally {
                processing.set(false)
            }
        }
    }
}

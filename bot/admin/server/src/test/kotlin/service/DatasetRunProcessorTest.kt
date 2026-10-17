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

import ai.tock.bot.admin.dataset.Dataset
import ai.tock.bot.admin.dataset.DatasetDAO
import ai.tock.bot.admin.dataset.DatasetQuestion
import ai.tock.bot.admin.dataset.DatasetRun
import ai.tock.bot.admin.dataset.DatasetRunDAO
import ai.tock.bot.admin.dataset.DatasetRunQuestionResult
import ai.tock.bot.admin.dataset.DatasetRunQuestionResultState
import ai.tock.bot.admin.dataset.DatasetRunState
import ai.tock.bot.admin.test.model.BotDialogResponse
import ai.tock.bot.connector.rest.client.model.ClientDebug
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.litote.kmongo.newId
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class DatasetRunProcessorTest {
    private val datasetDAO: DatasetDAO = mockk(relaxed = false)
    private val datasetRunDAO: DatasetRunDAO = mockk(relaxed = false)
    private val fixedClock = Clock.fixed(Instant.parse("2026-03-23T10:00:00Z"), ZoneOffset.UTC)

    @Test
    fun `processNextQueuedRun completes the run when all questions succeed`() {
        val dataset = newDataset()
        val run = newRun(dataset)
        val firstQuestion = dataset.questions.first()
        val secondQuestion = dataset.questions.last()
        val firstResult = newQuestionResult(run, dataset, firstQuestion.id)
        val secondResult = newQuestionResult(run, dataset, secondQuestion.id)
        val savedQuestionResults = mutableListOf<DatasetRunQuestionResult>()
        val savedRuns = mutableListOf<DatasetRun>()

        every { datasetRunDAO.claimNextQueuedRun() } returns run
        every { datasetDAO.getDatasetById(any()) } returns dataset
        every { datasetRunDAO.getQuestionResultsByRunId(any()) } returns listOf(firstResult, secondResult)
        every { datasetRunDAO.getRunById(any()) } returns run.copy(state = DatasetRunState.RUNNING)
        every { datasetRunDAO.updateActiveQuestionResult(any()) } answers {
            firstArg<DatasetRunQuestionResult>().also { savedQuestionResults.add(it) }
        }
        every { datasetRunDAO.finishRun(any(), any(), any()) } answers {
            run.copy(state = secondArg(), endTime = thirdArg()).also { savedRuns.add(it) }
        }

        val processor =
            DatasetRunProcessor(
                datasetDAO = datasetDAO,
                datasetRunDAO = datasetRunDAO,
                questionExecutor =
                    DatasetQuestionExecutor { _, questionResult, _ ->
                        BotDialogResponse(emptyList(), userActionId = "user-action-${questionResult.questionId}")
                    },
                clock = fixedClock,
            )

        val processed = processor.processNextQueuedRun()

        assertTrue(processed)
        assertEquals(DatasetRunState.COMPLETED, savedRuns.last().state)
        assertEquals(fixedClock.instant(), savedRuns.last().endTime)

        val completedResults =
            savedQuestionResults
                .filter { it.state == DatasetRunQuestionResultState.COMPLETED }
                .associateBy { it.questionId }

        assertEquals(2, completedResults.size)
        assertEquals("user-action-${firstQuestion.id}", completedResults[firstQuestion.id]?.userActionId)
        assertEquals("user-action-${secondQuestion.id}", completedResults[secondQuestion.id]?.userActionId)
    }

    @Test
    fun `processNextQueuedRun retries RAG technical errors even when a user action id is returned`() {
        val dataset = newDataset(questionCount = 1)
        val run = newRun(dataset)
        val question = dataset.questions.first()
        val questionResult = newQuestionResult(run, dataset, question.id)
        val savedQuestionResults = mutableListOf<DatasetRunQuestionResult>()
        val savedRuns = mutableListOf<DatasetRun>()

        every { datasetRunDAO.claimNextQueuedRun() } returns run
        every { datasetDAO.getDatasetById(any()) } returns dataset
        every { datasetRunDAO.getQuestionResultsByRunId(any()) } returns listOf(questionResult)
        every { datasetRunDAO.getRunById(any()) } returns run.copy(state = DatasetRunState.RUNNING)
        every { datasetRunDAO.updateActiveQuestionResult(any()) } answers {
            firstArg<DatasetRunQuestionResult>().also { savedQuestionResults.add(it) }
        }
        every { datasetRunDAO.finishRun(any(), any(), any()) } answers {
            run.copy(state = secondArg(), endTime = thirdArg()).also { savedRuns.add(it) }
        }

        val processor =
            DatasetRunProcessor(
                datasetDAO = datasetDAO,
                datasetRunDAO = datasetRunDAO,
                questionExecutor =
                    DatasetQuestionExecutor { _, result, _ ->
                        if (result.retryCount == 0) {
                            BotDialogResponse(
                                listOf(ragTechnicalError("timeout")),
                                userActionId = "failed-user-action",
                            )
                        } else {
                            BotDialogResponse(emptyList(), userActionId = "successful-user-action")
                        }
                    },
                maxRetries = 1,
                clock = fixedClock,
            )

        val processed = processor.processNextQueuedRun()

        assertTrue(processed)
        assertEquals(DatasetRunState.COMPLETED, savedRuns.last().state)

        val completedResult = savedQuestionResults.last()
        assertEquals(DatasetRunQuestionResultState.COMPLETED, completedResult.state)
        assertEquals(1, completedResult.retryCount)
        assertEquals("successful-user-action", completedResult.userActionId)
        assertEquals(null, completedResult.error)
    }

    @Test
    fun `processNextQueuedRun marks question as failed after configured retries and completes the run`() {
        val dataset = newDataset(questionCount = 1)
        val run = newRun(dataset)
        val question = dataset.questions.first()
        val questionResult = newQuestionResult(run, dataset, question.id)
        val savedQuestionResults = mutableListOf<DatasetRunQuestionResult>()
        val savedRuns = mutableListOf<DatasetRun>()

        every { datasetRunDAO.claimNextQueuedRun() } returns run
        every { datasetDAO.getDatasetById(any()) } returns dataset
        every { datasetRunDAO.getQuestionResultsByRunId(any()) } returns listOf(questionResult)
        every { datasetRunDAO.getRunById(any()) } returns run.copy(state = DatasetRunState.RUNNING)
        every { datasetRunDAO.updateActiveQuestionResult(any()) } answers {
            firstArg<DatasetRunQuestionResult>().also { savedQuestionResults.add(it) }
        }
        every { datasetRunDAO.finishRun(any(), any(), any()) } answers {
            run.copy(state = secondArg(), endTime = thirdArg()).also { savedRuns.add(it) }
        }

        val processor =
            DatasetRunProcessor(
                datasetDAO = datasetDAO,
                datasetRunDAO = datasetRunDAO,
                questionExecutor =
                    DatasetQuestionExecutor { _, _, _ ->
                        BotDialogResponse(emptyList())
                    },
                maxRetries = 1,
                clock = fixedClock,
            )

        val processed = processor.processNextQueuedRun()

        assertTrue(processed)
        assertEquals(DatasetRunState.COMPLETED, savedRuns.last().state)

        val failedResult = savedQuestionResults.last()
        assertEquals(DatasetRunQuestionResultState.FAILED, failedResult.state)
        assertEquals(1, failedResult.retryCount)
        assertEquals("Talk execution did not return a userActionId", failedResult.error)
    }

    @Test
    fun `processNextQueuedRun marks missing question results as failed instead of skipping them`() {
        val dataset = newDataset()
        val run = newRun(dataset)
        val firstQuestion = dataset.questions.first()
        val secondQuestion = dataset.questions.last()
        val firstResult = newQuestionResult(run, dataset, firstQuestion.id)
        val savedQuestionResults = mutableListOf<DatasetRunQuestionResult>()
        val savedRuns = mutableListOf<DatasetRun>()

        every { datasetRunDAO.claimNextQueuedRun() } returns run
        every { datasetDAO.getDatasetById(any()) } returns dataset
        every { datasetRunDAO.getQuestionResultsByRunId(any()) } returns listOf(firstResult)
        every { datasetRunDAO.getRunById(any()) } returns run.copy(state = DatasetRunState.RUNNING)
        every { datasetRunDAO.saveQuestionResult(any()) } answers {
            firstArg<DatasetRunQuestionResult>().also { savedQuestionResults.add(it) }
        }
        every { datasetRunDAO.updateActiveQuestionResult(any()) } answers {
            firstArg<DatasetRunQuestionResult>().also { savedQuestionResults.add(it) }
        }
        every { datasetRunDAO.finishRun(any(), any(), any()) } answers {
            run.copy(state = secondArg(), endTime = thirdArg()).also { savedRuns.add(it) }
        }

        val processor =
            DatasetRunProcessor(
                datasetDAO = datasetDAO,
                datasetRunDAO = datasetRunDAO,
                questionExecutor =
                    DatasetQuestionExecutor { _, questionResult, _ ->
                        BotDialogResponse(emptyList(), userActionId = "user-action-${questionResult.questionId}")
                    },
                clock = fixedClock,
            )

        val processed = processor.processNextQueuedRun()

        assertTrue(processed)
        assertEquals(DatasetRunState.COMPLETED, savedRuns.last().state)

        val failedMissingResult = savedQuestionResults.first { it.questionId == secondQuestion.id }
        assertEquals(DatasetRunQuestionResultState.FAILED, failedMissingResult.state)
        assertEquals(
            "Dataset question result was missing before execution; the question was not sent to the bot.",
            failedMissingResult.error,
        )

        val completedResult = savedQuestionResults.last { it.questionId == firstQuestion.id }
        assertEquals(DatasetRunQuestionResultState.COMPLETED, completedResult.state)
        assertEquals("user-action-${firstQuestion.id}", completedResult.userActionId)
    }

    @Test
    fun `processNextQueuedRun stops immediately when the claimed run is already cancelled`() {
        val dataset = newDataset(questionCount = 1)
        val run = newRun(dataset)
        val questionResult = newQuestionResult(run, dataset, dataset.questions.first().id)

        every { datasetRunDAO.claimNextQueuedRun() } returns run
        every { datasetDAO.getDatasetById(any()) } returns dataset
        every { datasetRunDAO.getQuestionResultsByRunId(any()) } returns listOf(questionResult)
        every { datasetRunDAO.getRunById(any()) } returns run.copy(state = DatasetRunState.CANCELLED)

        val processor =
            DatasetRunProcessor(
                datasetDAO = datasetDAO,
                datasetRunDAO = datasetRunDAO,
                questionExecutor =
                    DatasetQuestionExecutor { _, _, _ ->
                        BotDialogResponse(emptyList(), userActionId = "should-not-run")
                    },
                clock = fixedClock,
            )

        val processed = processor.processNextQueuedRun()

        assertTrue(processed)
        verify(exactly = 0) { datasetRunDAO.updateActiveQuestionResult(any()) }
        verify(exactly = 0) { datasetRunDAO.finishRun(any(), any(), any()) }
    }

    @Test
    fun `late answer does not complete a cancelled question or run`() {
        val dataset = newDataset(questionCount = 1)
        val run = newRun(dataset)
        val result = newQuestionResult(run, dataset, dataset.questions.single().id)
        every { datasetRunDAO.claimNextQueuedRun() } returns run
        every { datasetDAO.getDatasetById(any()) } returns dataset
        every { datasetRunDAO.getQuestionResultsByRunId(any()) } returns listOf(result)
        every { datasetRunDAO.getRunById(any()) } returns run
        every { datasetRunDAO.updateActiveQuestionResult(any()) } answers {
            firstArg<DatasetRunQuestionResult>().takeIf { it.state == DatasetRunQuestionResultState.RUNNING }
        }
        val processor =
            DatasetRunProcessor(
                datasetDAO,
                datasetRunDAO,
                DatasetQuestionExecutor { _, _, _ ->
                    BotDialogResponse(emptyList(), userActionId = "late-answer")
                },
                clock = fixedClock,
            )

        assertTrue(processor.processNextQueuedRun())

        verify(exactly = 0) { datasetRunDAO.finishRun(any(), any(), any()) }
        verify(exactly = 0) { datasetRunDAO.saveQuestionResult(any()) }
    }

    @Test
    fun `cancellation during a failed call prevents another attempt`() {
        val dataset = newDataset(questionCount = 1)
        val run = newRun(dataset)
        val result = newQuestionResult(run, dataset, dataset.questions.single().id)
        every { datasetRunDAO.claimNextQueuedRun() } returns run
        every { datasetDAO.getDatasetById(any()) } returns dataset
        every { datasetRunDAO.getQuestionResultsByRunId(any()) } returns listOf(result)
        every { datasetRunDAO.getRunById(any()) } returnsMany listOf(run, run.copy(state = DatasetRunState.CANCELLED))
        every { datasetRunDAO.updateActiveQuestionResult(any()) } answers { firstArg() }
        var calls = 0
        val processor =
            DatasetRunProcessor(
                datasetDAO,
                datasetRunDAO,
                DatasetQuestionExecutor { _, _, _ ->
                    calls++
                    BotDialogResponse(emptyList())
                },
                maxRetries = 2,
                clock = fixedClock,
            )

        processor.processNextQueuedRun()

        assertEquals(1, calls)
        verify(exactly = 0) { datasetRunDAO.finishRun(any(), any(), any()) }
    }

    @Test
    fun `storage failure marks unfinished questions failed instead of leaving the run running`() {
        val dataset = newDataset(questionCount = 1)
        val run = newRun(dataset)
        every { datasetRunDAO.claimNextQueuedRun() } returns run
        every { datasetDAO.getDatasetById(any()) } throws IllegalStateException("storage unavailable")
        every { datasetRunDAO.finishActiveQuestionResults(any(), any(), any(), any()) } returns Unit
        every { datasetRunDAO.finishRun(any(), any(), any()) } returns run.copy(state = DatasetRunState.COMPLETED)
        val processor = DatasetRunProcessor(datasetDAO, datasetRunDAO, DatasetQuestionExecutor { _, _, _ -> error("must not execute") }, clock = fixedClock)

        processor.processNextQueuedRun()

        verify { datasetRunDAO.finishActiveQuestionResults(run._id, DatasetRunQuestionResultState.FAILED, fixedClock.instant(), "storage unavailable") }
        verify { datasetRunDAO.finishRun(run._id, DatasetRunState.COMPLETED, fixedClock.instant()) }
    }

    @Test
    fun `successful answer persists its RAG status`() {
        val dataset = newDataset(questionCount = 1)
        val run = newRun(dataset)
        val result = newQuestionResult(run, dataset, dataset.questions.single().id)
        val saved = mutableListOf<DatasetRunQuestionResult>()
        every { datasetRunDAO.claimNextQueuedRun() } returns run
        every { datasetDAO.getDatasetById(any()) } returns dataset
        every { datasetRunDAO.getQuestionResultsByRunId(any()) } returns listOf(result)
        every { datasetRunDAO.getRunById(any()) } returns run
        every { datasetRunDAO.updateActiveQuestionResult(any()) } answers { firstArg<DatasetRunQuestionResult>().also { saved.add(it) } }
        every { datasetRunDAO.finishRun(any(), any(), any()) } returns run.copy(state = DatasetRunState.COMPLETED)
        val processor =
            DatasetRunProcessor(
                datasetDAO,
                datasetRunDAO,
                DatasetQuestionExecutor { _, _, _ ->
                    BotDialogResponse(listOf(ClientDebug("RAG", mapOf("answer" to mapOf("status" to "found_in_context")))), userActionId = "answer")
                },
                clock = fixedClock,
            )

        processor.processNextQueuedRun()

        assertEquals(true, saved.last().isRagAnswer)
        assertEquals("found_in_context", saved.last().ragAnswerStatus)
    }

    private fun newDataset(questionCount: Int = 2): Dataset =
        Dataset(
            namespace = "testNamespace",
            botId = "testBotId",
            name = "dataset-name",
            description = "dataset-description",
            questions = (1..questionCount).map { index -> DatasetQuestion(question = "question-$index") },
            createdAt = fixedClock.instant(),
            createdBy = "dataset-user",
        )

    private fun newRun(dataset: Dataset): DatasetRun =
        DatasetRun(
            namespace = dataset.namespace,
            botId = dataset.botId,
            datasetId = dataset._id,
            state = DatasetRunState.RUNNING,
            startTime = fixedClock.instant(),
            startedBy = "dataset-user",
            botApplicationConfigurationId = newId(),
        )

    private fun newQuestionResult(
        run: DatasetRun,
        dataset: Dataset,
        questionId: String,
    ): DatasetRunQuestionResult =
        DatasetRunQuestionResult(
            namespace = dataset.namespace,
            botId = dataset.botId,
            datasetId = dataset._id,
            runId = run._id,
            questionId = questionId,
            userIdModifier = "dataset_${run._id}_$questionId",
        )

    private fun ragTechnicalError(errorMessage: String): ClientDebug =
        ClientDebug(
            text = "RAG",
            data =
                mapOf(
                    "error" to mapOf("errorMessage" to errorMessage),
                    "answer" to
                        mapOf(
                            "status" to "technical_error",
                            "answer" to "Technical error :( sorry!",
                        ),
                ),
        )
}

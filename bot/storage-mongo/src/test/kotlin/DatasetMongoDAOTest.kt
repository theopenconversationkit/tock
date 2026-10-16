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

package ai.tock.bot.mongo

import ai.tock.bot.admin.dataset.Dataset
import ai.tock.bot.admin.dataset.DatasetQuestion
import ai.tock.bot.admin.dataset.DatasetRun
import ai.tock.bot.admin.dataset.DatasetRunQuestionResult
import ai.tock.bot.admin.dataset.DatasetRunQuestionResultState
import ai.tock.bot.admin.dataset.DatasetRunState
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.concurrent.CompletableFuture
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DatasetMongoDAOTest : AbstractTest() {
    private val namespace = "dataset-dao-test"
    private val botId = "bot"

    @AfterEach
    fun cleanup() {
        DatasetMongoDAO.deleteByNamespaceAndBotId(namespace, botId)
    }

    @Test
    fun `two workers cannot claim the same queued run`() {
        val run = run(DatasetRunState.QUEUED)
        val claims = (1..2).map { CompletableFuture.supplyAsync { DatasetMongoDAO.claimNextQueuedRun() } }.map { it.get() }
        assertEquals(listOf(run._id), claims.mapNotNull { it?._id })
        assertEquals(DatasetRunState.RUNNING, DatasetMongoDAO.getRunById(run._id)?.state)
    }

    @Test
    fun `completion cannot overwrite cancellation or recreate a deleted run`() {
        val run = run()
        DatasetMongoDAO.finishRun(run._id, DatasetRunState.CANCELLED, Instant.now())
        assertNull(DatasetMongoDAO.finishRun(run._id, DatasetRunState.COMPLETED, Instant.now()))
        assertEquals(DatasetRunState.CANCELLED, DatasetMongoDAO.getRunById(run._id)?.state)
        DatasetMongoDAO.deleteRun(run._id)
        assertNull(DatasetMongoDAO.finishRun(run._id, DatasetRunState.COMPLETED, Instant.now()))
        assertNull(DatasetMongoDAO.getRunById(run._id))
    }

    @Test
    fun `notes survive claiming and concurrent completion and can be cleared`() {
        val run = run(DatasetRunState.QUEUED)
        DatasetMongoDAO.updateRunNote(run._id, "Compressor OFF")
        assertEquals("Compressor OFF", DatasetMongoDAO.claimNextQueuedRun()?.note)
        val endTime = Instant.parse("2026-10-16T12:00:00Z")
        val finish = CompletableFuture.runAsync { DatasetMongoDAO.finishRun(run._id, DatasetRunState.COMPLETED, endTime) }
        val edit = CompletableFuture.runAsync { DatasetMongoDAO.updateRunNote(run._id, "Compressor ON") }
        CompletableFuture.allOf(finish, edit).get()

        val completed = DatasetMongoDAO.getRunById(run._id)!!
        assertEquals("Compressor ON", completed.note)
        assertEquals(DatasetRunState.COMPLETED, completed.state)
        assertEquals(endTime, completed.endTime)
        assertEquals(completed.copy(note = null), DatasetMongoDAO.updateRunNote(run._id, null))
    }

    @Test
    fun `editing a note cannot recreate a deleted run`() {
        val run = run()
        DatasetMongoDAO.deleteRun(run._id)
        assertNull(DatasetMongoDAO.updateRunNote(run._id, "Compressor OFF"))
        assertNull(DatasetMongoDAO.getRunById(run._id))
    }

    @Test
    fun `late answer cannot overwrite cancelled results or recreate deleted results`() {
        val run = run()
        val result = result(run)
        DatasetMongoDAO.saveQuestionResults(listOf(result))
        DatasetMongoDAO.finishActiveQuestionResults(run._id, DatasetRunQuestionResultState.CANCELLED, Instant.now())
        val lateAnswer = result.copy(state = DatasetRunQuestionResultState.COMPLETED, userActionId = "late-answer")
        assertNull(DatasetMongoDAO.updateActiveQuestionResult(lateAnswer))
        assertEquals(DatasetRunQuestionResultState.CANCELLED, DatasetMongoDAO.getQuestionResultsByRunId(run._id).single().state)
        DatasetMongoDAO.deleteQuestionResultsByRunId(run._id)
        assertNull(DatasetMongoDAO.updateActiveQuestionResult(lateAnswer))
        DatasetMongoDAO.updateQuestionResultAnswer(lateAnswer)
        assertEquals(emptyList(), DatasetMongoDAO.getQuestionResultsByRunId(run._id))
    }

    @Test
    fun `bulk cancellation preserves completed results and persisted answer statistics`() {
        val run = run()
        val completed = result(run).copy(state = DatasetRunQuestionResultState.COMPLETED, isRagAnswer = true, ragAnswerStatus = "found_in_context")
        val pending = result(run).copy(questionId = "pending")
        DatasetMongoDAO.saveQuestionResults(listOf(completed, pending))
        DatasetMongoDAO.finishActiveQuestionResults(run._id, DatasetRunQuestionResultState.CANCELLED, Instant.now())
        val results = DatasetMongoDAO.getQuestionResultsByRunId(run._id).associateBy { it._id }
        assertEquals(completed, results[completed._id])
        assertEquals(DatasetRunQuestionResultState.CANCELLED, results[pending._id]?.state)
    }

    @Test
    fun `dataset deletion also removes question results without a parent run`() {
        val run = run()
        DatasetMongoDAO.saveQuestionResults(listOf(result(run)))
        // Simulate an interrupted creation with results referencing an unpublished run.
        val orphan = result(run).copy(runId = org.litote.kmongo.newId(), questionId = "orphan")
        DatasetMongoDAO.saveQuestionResults(listOf(orphan))
        DatasetMongoDAO.delete(run.datasetId)
        assertEquals(emptyList(), DatasetMongoDAO.getQuestionResultsByRunId(orphan.runId))
        assertEquals(emptyList(), DatasetMongoDAO.getQuestionResultsByRunId(run._id))
    }

    private fun run(state: DatasetRunState = DatasetRunState.RUNNING): DatasetRun {
        val dataset =
            Dataset(namespace = namespace, botId = botId, name = "dataset", description = "", questions = listOf(DatasetQuestion(question = "question")), createdAt = Instant.now(), createdBy = "test")
        DatasetMongoDAO.save(dataset)
        return DatasetMongoDAO.saveRun(DatasetRun(namespace = namespace, botId = botId, datasetId = dataset._id, state = state, startTime = Instant.now(), startedBy = "test"))
    }

    private fun result(run: DatasetRun) = DatasetRunQuestionResult(namespace = namespace, botId = botId, datasetId = run.datasetId, runId = run._id, questionId = "question", userIdModifier = "test")
}

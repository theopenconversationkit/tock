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

import ai.tock.bot.admin.AbstractTest
import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.bot.rag.BotRAGConfigurationDAO
import ai.tock.bot.admin.dataset.Dataset
import ai.tock.bot.admin.dataset.DatasetDAO
import ai.tock.bot.admin.dataset.DatasetQuestion
import ai.tock.bot.admin.dataset.DatasetRun
import ai.tock.bot.admin.dataset.DatasetRunDAO
import ai.tock.bot.admin.dataset.DatasetRunQuestionResult
import ai.tock.bot.admin.dataset.DatasetRunQuestionResultState
import ai.tock.bot.admin.dataset.DatasetRunState
import ai.tock.bot.admin.dialog.ActionReport
import ai.tock.bot.admin.dialog.DialogReport
import ai.tock.bot.admin.dialog.DialogReportDAO
import ai.tock.bot.admin.dialog.DialogReportQuery
import ai.tock.bot.admin.dialog.DialogReportQueryResult
import ai.tock.bot.admin.model.Valid
import ai.tock.bot.admin.model.ValidationError
import ai.tock.bot.admin.model.dataset.DatasetCreateRequest
import ai.tock.bot.admin.model.dataset.DatasetRunActionState
import ai.tock.bot.admin.model.dataset.DatasetRunCreateRequest
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.engine.action.ActionMetadata
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.message.DebugMessage
import ai.tock.bot.engine.message.Sentence
import ai.tock.bot.engine.message.SentenceWithFootnotes
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.shared.tockInternalInjector
import ai.tock.translator.UserInterfaceType
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.singleton
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.litote.kmongo.newId
import org.litote.kmongo.toId
import java.time.Instant
import java.util.Locale

class DatasetServiceTest : AbstractTest() {
    companion object {
        private const val NAMESPACE = "testNamespace"
        private const val BOT_ID = "testBotId"
        private const val USER = "dataset-user"

        private val datasetDAO: DatasetDAO = mockk(relaxed = false)
        private val datasetRunDAO: DatasetRunDAO = mockk(relaxed = false)
        private val ragConfigurationDAO: BotRAGConfigurationDAO = mockk(relaxed = true)
        private val dialogReportDAO: DialogReportDAO = mockk(relaxed = false)

        init {
            tockInternalInjector = KodeinInjector()
            val module =
                Kodein.Module(allowSilentOverride = true) {
                    bind<DatasetDAO>() with singleton { datasetDAO }
                    bind<DatasetRunDAO>() with singleton { datasetRunDAO }
                    bind<BotRAGConfigurationDAO>() with singleton { ragConfigurationDAO }
                    bind<DialogReportDAO>() with singleton { dialogReportDAO }
                }
            tockInternalInjector.inject(
                Kodein {
                    import(AbstractTest.defaultModulesBinding())
                    import(module, allowOverride = true)
                },
            )
        }
    }

    @AfterEach
    fun tearDown() {
        clearMocks(datasetDAO, datasetRunDAO, ragConfigurationDAO, dialogReportDAO, AbstractTest.applicationConfigurationDAO)
    }

    @Test
    fun `createRun creates a queued run and pre-creates question results`() {
        val dataset = newDataset()
        val restConfiguration = newRestConfiguration()
        val runSlot = slot<DatasetRun>()
        val questionResultsSlot = slot<List<DatasetRunQuestionResult>>()

        every { datasetDAO.getDatasetById(any()) } returns dataset
        every { datasetRunDAO.getActiveRunsByDatasetId(dataset._id) } returns emptyList()
        every { AbstractTest.applicationConfigurationDAO.getConfigurationsByNamespaceAndBotId(NAMESPACE, BOT_ID) } returns listOf(restConfiguration)
        every { ragConfigurationDAO.findByNamespaceAndBotId(NAMESPACE, BOT_ID) } returns null
        every { datasetRunDAO.saveRun(capture(runSlot)) } answers { runSlot.captured }
        every { datasetRunDAO.saveQuestionResults(capture(questionResultsSlot)) } returns Unit
        every { datasetRunDAO.getQuestionResultsByRunId(any()) } answers { questionResultsSlot.captured }

        val result =
            DatasetService.createRun(
                namespace = NAMESPACE,
                botId = BOT_ID,
                datasetId = dataset._id.toString(),
                request = DatasetRunCreateRequest(language = "fr"),
                userLogin = USER,
            )

        assertEquals(DatasetRunState.QUEUED, result.state)
        assertEquals(USER, result.startedBy)
        assertNull(result.settingsSnapshot)
        assertEquals(2, result.stats.totalQuestions)
        assertEquals(0, result.stats.completedQuestions)
        assertEquals(0, result.stats.failedQuestions)

        val savedRun = runSlot.captured
        assertEquals(dataset._id, savedRun.datasetId)
        assertEquals(DatasetRunState.QUEUED, savedRun.state)
        assertEquals(restConfiguration._id, savedRun.botApplicationConfigurationId)
        assertEquals(Locale.forLanguageTag("fr"), savedRun.language)
        assertEquals(USER, savedRun.startedBy)
        assertTrue(savedRun.settingsSnapshot.isEmpty())

        val savedQuestionResults = questionResultsSlot.captured
        assertEquals(dataset.questions.size, savedQuestionResults.size)
        savedQuestionResults.forEachIndexed { index, resultEntry ->
            assertEquals(dataset.questions[index].id, resultEntry.questionId)
            assertEquals(DatasetRunQuestionResultState.PENDING, resultEntry.state)
            assertEquals(savedRun._id, resultEntry.runId)
            assertEquals("dataset_${savedRun._id}_${dataset.questions[index].id}", resultEntry.userIdModifier)
        }
    }

    @Test
    fun `createRun throws when several REST configurations are available`() {
        val dataset = newDataset()

        every { datasetDAO.getDatasetById(any()) } returns dataset
        every { datasetRunDAO.getActiveRunsByDatasetId(dataset._id) } returns emptyList()
        every { AbstractTest.applicationConfigurationDAO.getConfigurationsByNamespaceAndBotId(NAMESPACE, BOT_ID) } returns
            listOf(
                newRestConfiguration("test-app-a"),
                newRestConfiguration("test-app-b"),
            )

        val exception =
            assertThrows<DatasetError.InvalidRequest> {
                DatasetService.createRun(
                    namespace = NAMESPACE,
                    botId = BOT_ID,
                    datasetId = dataset._id.toString(),
                    request = DatasetRunCreateRequest(language = "fr"),
                    userLogin = USER,
                )
            }

        assertEquals("Multiple REST test configurations found for bot $BOT_ID", exception.message)
    }

    @Test
    fun `getRun returns the run without settings snapshot`() {
        val dataset = newDataset()
        val run =
            DatasetRun(
                namespace = NAMESPACE,
                botId = BOT_ID,
                datasetId = dataset._id,
                state = DatasetRunState.QUEUED,
                startTime = Instant.now(),
                startedBy = USER,
                settingsSnapshot = mapOf("indexSessionId" to "session-id"),
            )

        every { datasetDAO.getDatasetById(any()) } returns dataset
        every { datasetRunDAO.getRunById(any()) } returns run
        every {
            datasetRunDAO.getQuestionResultsByRunId(run._id)
        } returns
            listOf(
                DatasetRunQuestionResult(
                    namespace = NAMESPACE,
                    botId = BOT_ID,
                    datasetId = dataset._id,
                    runId = run._id,
                    questionId = dataset.questions.first().id,
                    state = DatasetRunQuestionResultState.COMPLETED,
                    userIdModifier = "dataset_${run._id}_${dataset.questions.first().id}",
                ),
                DatasetRunQuestionResult(
                    namespace = NAMESPACE,
                    botId = BOT_ID,
                    datasetId = dataset._id,
                    runId = run._id,
                    questionId = dataset.questions.last().id,
                    state = DatasetRunQuestionResultState.FAILED,
                    userIdModifier = "dataset_${run._id}_${dataset.questions.last().id}",
                ),
            )

        val result =
            DatasetService.getRun(
                namespace = NAMESPACE,
                botId = BOT_ID,
                datasetId = dataset._id.toString(),
                runId = run._id.toString(),
            )

        assertEquals(run._id.toString(), result.id)
        assertEquals(DatasetRunState.QUEUED, result.state)
        assertNull(result.settingsSnapshot)
        assertEquals(2, result.stats.totalQuestions)
        assertEquals(1, result.stats.completedQuestions)
        assertEquals(1, result.stats.failedQuestions)
    }

    @Test
    fun `cancelRun cancels active run and updates pending question results`() {
        val dataset = newDataset()
        val run =
            DatasetRun(
                namespace = NAMESPACE,
                botId = BOT_ID,
                datasetId = dataset._id,
                state = DatasetRunState.RUNNING,
                startTime = Instant.now(),
                startedBy = USER,
            )
        val pendingResult =
            DatasetRunQuestionResult(
                namespace = NAMESPACE,
                botId = BOT_ID,
                datasetId = dataset._id,
                runId = run._id,
                questionId = dataset.questions.first().id,
                userIdModifier = "dataset_${run._id}_${dataset.questions.first().id}",
            )
        val completedResult =
            DatasetRunQuestionResult(
                namespace = NAMESPACE,
                botId = BOT_ID,
                datasetId = dataset._id,
                runId = run._id,
                questionId = dataset.questions.last().id,
                state = DatasetRunQuestionResultState.COMPLETED,
                userIdModifier = "dataset_${run._id}_${dataset.questions.last().id}",
            )
        val savedRunSlot = slot<DatasetRun>()
        val updatedResultsSlot = slot<List<DatasetRunQuestionResult>>()

        every { datasetDAO.getDatasetById(any()) } returns dataset
        every { datasetRunDAO.getRunById(any()) } returns run
        every { datasetRunDAO.saveRun(capture(savedRunSlot)) } answers { savedRunSlot.captured }
        every { datasetRunDAO.getQuestionResultsByRunId(any()) } returns listOf(pendingResult, completedResult)
        every { datasetRunDAO.saveQuestionResults(capture(updatedResultsSlot)) } returns Unit

        val result =
            DatasetService.cancelRun(
                namespace = NAMESPACE,
                botId = BOT_ID,
                datasetId = dataset._id.toString(),
                runId = run._id.toString(),
            )

        assertEquals(DatasetRunState.CANCELLED, result.state)
        assertNotNull(result.endTime)
        assertEquals(2, result.stats.totalQuestions)
        assertEquals(1, result.stats.completedQuestions)
        assertEquals(0, result.stats.failedQuestions)

        val cancelledRun = savedRunSlot.captured
        assertEquals(DatasetRunState.CANCELLED, cancelledRun.state)
        assertNotNull(cancelledRun.endTime)

        val updatedResults = updatedResultsSlot.captured
        assertEquals(DatasetRunQuestionResultState.CANCELLED, updatedResults.first().state)
        assertEquals(DatasetRunQuestionResultState.COMPLETED, updatedResults.last().state)

        verify(exactly = 1) { datasetRunDAO.saveQuestionResults(any()) }
    }

    @Test
    fun `dataset create request rejects blank name`() {
        val exception =
            assertThrows<ValidationError> {
                Valid(
                    DatasetCreateRequest(
                        name = "   ",
                        description = "desc",
                        questions = listOf(ai.tock.bot.admin.model.dataset.DatasetQuestionRequest(question = "question")),
                    ),
                )
            }

        assertEquals("Dataset name is required", exception.message)
    }

    @Test
    fun `getRunActions returns completed action and caches resolved references`() {
        val dataset = newDataset()
        val run = newFinishedRun(dataset, DatasetRunState.COMPLETED)
        val questionResult =
            DatasetRunQuestionResult(
                namespace = NAMESPACE,
                botId = BOT_ID,
                datasetId = dataset._id,
                runId = run._id,
                questionId = dataset.questions.first().id,
                state = DatasetRunQuestionResultState.COMPLETED,
                userIdModifier = "dataset_${run._id}_${dataset.questions.first().id}",
                userActionId = "user-action-id",
            )
        val answerAction = botSentenceWithFootnotesAction("answer-action-id")
        val dialog =
            DialogReport(
                actions =
                    listOf(
                        userSentenceAction("user-action-id"),
                        botDebugAction("debug-action-id"),
                        answerAction,
                    ),
                userInterface = UserInterfaceType.textChat,
                id = newId<Dialog>(),
            )
        val updatedQuestionResultSlot = slot<DatasetRunQuestionResult>()
        val querySlot = slot<DialogReportQuery>()

        every { datasetDAO.getDatasetById(any()) } returns dataset
        every { datasetRunDAO.getRunById(any()) } returns run
        every { datasetRunDAO.getQuestionResultsByRunId(run._id) } returns listOf(questionResult)
        every { dialogReportDAO.findByDialogByIds(emptySet()) } returns emptySet()
        every { dialogReportDAO.search(capture(querySlot)) } returns DialogReportQueryResult(total = 1, dialogs = listOf(dialog))
        every { datasetRunDAO.saveQuestionResult(capture(updatedQuestionResultSlot)) } answers { updatedQuestionResultSlot.captured }

        val result =
            DatasetService.getRunActions(
                namespace = NAMESPACE,
                botId = BOT_ID,
                datasetId = dataset._id.toString(),
                runId = run._id.toString(),
            )

        assertEquals(1, result.size)
        assertEquals(DatasetRunActionState.COMPLETED, result.first().state)
        assertEquals(answerAction.id.toString(), result.first().action?.id.toString())
        assertEquals(dialog.id, updatedQuestionResultSlot.captured.dialogId)
        assertEquals(answerAction.id, updatedQuestionResultSlot.captured.answerActionId)
        assertEquals("test_${run.botApplicationConfigurationId}_${run.language}_${questionResult.userIdModifier}", querySlot.captured.playerId?.id)
    }

    @Test
    fun `getRunActions returns completed with null action when completed dialog has been purged`() {
        val dataset = newDataset()
        val run = newFinishedRun(dataset, DatasetRunState.COMPLETED)
        val questionResult =
            DatasetRunQuestionResult(
                namespace = NAMESPACE,
                botId = BOT_ID,
                datasetId = dataset._id,
                runId = run._id,
                questionId = dataset.questions.first().id,
                state = DatasetRunQuestionResultState.COMPLETED,
                userIdModifier = "dataset_${run._id}_${dataset.questions.first().id}",
                userActionId = "user-action-id",
            )

        every { datasetDAO.getDatasetById(any()) } returns dataset
        every { datasetRunDAO.getRunById(any()) } returns run
        every { datasetRunDAO.getQuestionResultsByRunId(run._id) } returns listOf(questionResult)
        every { dialogReportDAO.findByDialogByIds(emptySet()) } returns emptySet()
        every { dialogReportDAO.search(any()) } returns DialogReportQueryResult(total = 0, dialogs = emptyList())

        val result =
            DatasetService.getRunActions(
                namespace = NAMESPACE,
                botId = BOT_ID,
                datasetId = dataset._id.toString(),
                runId = run._id.toString(),
            )

        assertEquals(1, result.size)
        assertEquals(DatasetRunActionState.COMPLETED, result.first().state)
        assertNull(result.first().action)
        verify(exactly = 0) { datasetRunDAO.saveQuestionResult(any()) }
    }

    @Test
    fun `getRunActions maps failed and cancelled question results to failed actions`() {
        val dataset = newDataset()
        val run = newFinishedRun(dataset, DatasetRunState.CANCELLED)
        val failedQuestionResult =
            DatasetRunQuestionResult(
                namespace = NAMESPACE,
                botId = BOT_ID,
                datasetId = dataset._id,
                runId = run._id,
                questionId = dataset.questions.first().id,
                state = DatasetRunQuestionResultState.FAILED,
                userIdModifier = "dataset_${run._id}_${dataset.questions.first().id}",
                retryCount = 2,
            )
        val cancelledQuestionResult =
            DatasetRunQuestionResult(
                namespace = NAMESPACE,
                botId = BOT_ID,
                datasetId = dataset._id,
                runId = run._id,
                questionId = dataset.questions.last().id,
                state = DatasetRunQuestionResultState.CANCELLED,
                userIdModifier = "dataset_${run._id}_${dataset.questions.last().id}",
            )

        every { datasetDAO.getDatasetById(any()) } returns dataset
        every { datasetRunDAO.getRunById(any()) } returns run
        every { datasetRunDAO.getQuestionResultsByRunId(run._id) } returns listOf(failedQuestionResult, cancelledQuestionResult)
        every { dialogReportDAO.findByDialogByIds(emptySet()) } returns emptySet()

        val result =
            DatasetService.getRunActions(
                namespace = NAMESPACE,
                botId = BOT_ID,
                datasetId = dataset._id.toString(),
                runId = run._id.toString(),
            )

        assertEquals(2, result.size)
        assertTrue(result.all { it.state == DatasetRunActionState.FAILED && it.action == null })
        assertEquals(2, result.first().retryCount)
        verify(exactly = 0) { dialogReportDAO.search(any()) }
    }

    private fun newDataset(): Dataset =
        Dataset(
            namespace = NAMESPACE,
            botId = BOT_ID,
            name = "dataset-name",
            description = "dataset-description",
            questions =
                listOf(
                    DatasetQuestion(question = "question-1"),
                    DatasetQuestion(question = "question-2"),
                ),
            createdAt = Instant.now(),
            createdBy = USER,
        )

    private fun newFinishedRun(
        dataset: Dataset,
        state: DatasetRunState,
    ): DatasetRun =
        DatasetRun(
            namespace = NAMESPACE,
            botId = BOT_ID,
            datasetId = dataset._id,
            state = state,
            startTime = Instant.now(),
            endTime = Instant.now(),
            startedBy = USER,
            language = Locale.FRENCH,
            botApplicationConfigurationId = newId(),
        )

    private fun newRestConfiguration(applicationId: String = "test-app"): BotApplicationConfiguration =
        BotApplicationConfiguration(
            applicationId = applicationId,
            botId = BOT_ID,
            namespace = NAMESPACE,
            nlpModel = "test-model",
            connectorType = ConnectorType.rest,
            name = applicationId,
            targetConfigurationId = newId(),
        )

    private fun userSentenceAction(id: String): ActionReport =
        ActionReport(
            playerId = PlayerId("user", PlayerType.user),
            recipientId = PlayerId(BOT_ID, PlayerType.bot),
            date = Instant.now(),
            message = Sentence("question"),
            connectorType = null,
            userInterfaceType = UserInterfaceType.textChat,
            id = id.toId(),
            intent = null,
            applicationId = null,
            metadata = ActionMetadata(),
        )

    private fun botDebugAction(id: String): ActionReport =
        ActionReport(
            playerId = PlayerId(BOT_ID, PlayerType.bot),
            recipientId = PlayerId("user", PlayerType.user),
            date = Instant.now(),
            message = DebugMessage("debug", null),
            connectorType = null,
            userInterfaceType = UserInterfaceType.textChat,
            id = id.toId(),
            intent = null,
            applicationId = null,
            metadata = ActionMetadata(),
        )

    private fun botSentenceWithFootnotesAction(id: String): ActionReport =
        ActionReport(
            playerId = PlayerId(BOT_ID, PlayerType.bot),
            recipientId = PlayerId("user", PlayerType.user),
            date = Instant.now(),
            message = SentenceWithFootnotes("answer"),
            connectorType = null,
            userInterfaceType = UserInterfaceType.textChat,
            id = id.toId(),
            intent = null,
            applicationId = null,
            metadata = ActionMetadata(isGenAiRagAnswer = true),
        )
}

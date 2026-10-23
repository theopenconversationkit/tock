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
import ai.tock.bot.admin.bot.compressor.BotDocumentCompressorConfiguration
import ai.tock.bot.admin.bot.compressor.BotDocumentCompressorConfigurationDAO
import ai.tock.bot.admin.bot.rag.BotRAGConfiguration
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
import ai.tock.bot.admin.evaluation.Evaluation
import ai.tock.bot.admin.evaluation.EvaluationDAO
import ai.tock.bot.admin.evaluation.EvaluationSample
import ai.tock.bot.admin.evaluation.EvaluationSampleDAO
import ai.tock.bot.admin.model.Valid
import ai.tock.bot.admin.model.ValidationError
import ai.tock.bot.admin.model.dataset.DatasetCreateRequest
import ai.tock.bot.admin.model.dataset.DatasetQuestionRequest
import ai.tock.bot.admin.model.dataset.DatasetRunActionState
import ai.tock.bot.admin.model.dataset.DatasetRunCreateRequest
import ai.tock.bot.admin.model.dataset.DatasetRunNoteRequest
import ai.tock.bot.admin.model.evaluation.CreateEvaluationSampleRequest
import ai.tock.bot.admin.model.evaluation.DatasetRunInfo
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.engine.action.ActionMetadata
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.message.DebugMessage
import ai.tock.bot.engine.message.Sentence
import ai.tock.bot.engine.message.SentenceWithFootnotes
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.genai.orchestratorclient.requests.PromptTemplate
import ai.tock.genai.orchestratorcore.models.compressor.BloomzDocumentCompressorSetting
import ai.tock.genai.orchestratorcore.models.em.OllamaEMSetting
import ai.tock.genai.orchestratorcore.models.llm.OllamaLLMSetting
import ai.tock.shared.exception.rest.UnprocessableEntityException
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
import io.mockk.verifyOrder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
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
        private val documentCompressorConfigurationDAO: BotDocumentCompressorConfigurationDAO = mockk()
        private val dialogReportDAO: DialogReportDAO = mockk(relaxed = false)
        private val evaluationSampleDAO: EvaluationSampleDAO = mockk(relaxed = false)
        private val evaluationDAO: EvaluationDAO = mockk(relaxed = false)

        init {
            tockInternalInjector = KodeinInjector()
            val module =
                Kodein.Module(allowSilentOverride = true) {
                    bind<DatasetDAO>() with singleton { datasetDAO }
                    bind<DatasetRunDAO>() with singleton { datasetRunDAO }
                    bind<BotRAGConfigurationDAO>() with singleton { ragConfigurationDAO }
                    bind<BotDocumentCompressorConfigurationDAO>() with singleton { documentCompressorConfigurationDAO }
                    bind<DialogReportDAO>() with singleton { dialogReportDAO }
                    bind<EvaluationSampleDAO>() with singleton { evaluationSampleDAO }
                    bind<EvaluationDAO>() with singleton { evaluationDAO }
                }
            tockInternalInjector.inject(
                Kodein {
                    import(AbstractTest.defaultModulesBinding())
                    import(module, allowOverride = true)
                },
            )
        }
    }

    @BeforeEach
    fun prepareAnswerCache() {
        every { datasetRunDAO.updateQuestionResultAnswer(any()) } returns Unit
        every { documentCompressorConfigurationDAO.findByNamespaceAndBotId(NAMESPACE, BOT_ID) } returns null
    }

    @AfterEach
    fun tearDown() {
        clearMocks(
            datasetDAO,
            datasetRunDAO,
            ragConfigurationDAO,
            documentCompressorConfigurationDAO,
            dialogReportDAO,
            evaluationSampleDAO,
            evaluationDAO,
            AbstractTest.applicationConfigurationDAO,
        )
    }

    @ParameterizedTest
    @EnumSource(DatasetRunState::class)
    fun `notes can be edited and cleared in every run state without loading answers`(state: DatasetRunState) {
        val dataset = newDataset()
        val run = newFinishedRun(dataset, state)
        every { datasetDAO.getDatasetById(match { it.toString() == dataset._id.toString() }) } returns dataset
        every { datasetRunDAO.getRunById(match { it.toString() == run._id.toString() }) } returns run
        every { datasetRunDAO.updateRunNote(run._id, any()) } answers { run.copy(note = secondArg()) }

        val result = DatasetService.updateRunNote(NAMESPACE, BOT_ID, dataset._id.toString(), run._id.toString(), DatasetRunNoteRequest("  Compressor OFF  "))
        assertEquals("Compressor OFF", result.note)
        val cleared = DatasetService.updateRunNote(NAMESPACE, BOT_ID, dataset._id.toString(), run._id.toString(), DatasetRunNoteRequest("   "))
        assertNull(cleared.note)
        verify(exactly = 0) { datasetRunDAO.saveRun(any()) }
        verify(exactly = 0) { datasetRunDAO.getQuestionResultsByRunId(any()) }
        verify(exactly = 0) { dialogReportDAO.findByDialogByIds(any()) }
    }

    @Test
    fun `note length is validated before writing`() {
        assertDoesNotThrow { Valid(DatasetRunNoteRequest("x".repeat(200))) }
        assertThrows<ValidationError> {
            DatasetService.updateRunNote(NAMESPACE, BOT_ID, "dataset", "run", DatasetRunNoteRequest("x".repeat(201)))
        }
        verify(exactly = 0) { datasetRunDAO.updateRunNote(any(), any()) }
    }

    @Test
    fun `note update rejects runs outside the requested scope`() {
        val dataset = newDataset()
        val run = newFinishedRun(dataset, DatasetRunState.COMPLETED)
        every { datasetDAO.getDatasetById(match { it.toString() == dataset._id.toString() }) } returns dataset
        listOf(run.copy(namespace = "other"), run.copy(botId = "other"), run.copy(datasetId = newId())).forEach { foreignRun ->
            every { datasetRunDAO.getRunById(match { it.toString() == run._id.toString() }) } returns foreignRun
            assertThrows<DatasetError.RunNotFound> {
                DatasetService.updateRunNote(NAMESPACE, BOT_ID, dataset._id.toString(), run._id.toString(), DatasetRunNoteRequest("note"))
            }
        }
        verify(exactly = 0) { datasetRunDAO.updateRunNote(any(), any()) }
    }

    @Test
    fun `note update reports a run deleted between reading and writing`() {
        val dataset = newDataset()
        val run = newFinishedRun(dataset, DatasetRunState.COMPLETED)
        every { datasetDAO.getDatasetById(match { it.toString() == dataset._id.toString() }) } returns dataset
        every { datasetRunDAO.getRunById(match { it.toString() == run._id.toString() }) } returns run
        every { datasetRunDAO.updateRunNote(run._id, any()) } returns null

        assertThrows<DatasetError.RunNotFound> {
            DatasetService.updateRunNote(NAMESPACE, BOT_ID, dataset._id.toString(), run._id.toString(), DatasetRunNoteRequest("note"))
        }
    }

    @Test
    fun `createRun creates a queued run and pre-creates question results`() {
        val dataset = newDataset()
        val restConfiguration = newRestConfiguration()
        val runSlot = slot<DatasetRun>()
        val questionResultsSlot = slot<List<DatasetRunQuestionResult>>()

        every { datasetDAO.getDatasetById(any()) } returns dataset
        every { datasetRunDAO.getActiveRunsByDatasetId(dataset._id) } returns emptyList()
        every { applicationConfigurationDAO.getConfigurationsByNamespaceAndBotId(NAMESPACE, BOT_ID) } returns listOf(restConfiguration)
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
        verifyOrder {
            datasetRunDAO.saveQuestionResults(any())
            datasetRunDAO.saveRun(any())
        }

        val savedRun = runSlot.captured
        assertEquals(dataset._id, savedRun.datasetId)
        assertEquals(DatasetRunState.QUEUED, savedRun.state)
        assertEquals(restConfiguration._id, savedRun.botApplicationConfigurationId)
        assertEquals(Locale.forLanguageTag("fr"), savedRun.language)
        assertEquals(USER, savedRun.startedBy)
        assertEquals(mapOf("documentCompressor" to mapOf("enabled" to false)), savedRun.settingsSnapshot)

        val savedQuestionResults = questionResultsSlot.captured
        assertEquals(dataset.questions.size, savedQuestionResults.size)
        savedQuestionResults.forEachIndexed { index, resultEntry ->
            assertEquals(dataset.questions[index].id, resultEntry.questionId)
            assertEquals(DatasetRunQuestionResultState.PENDING, resultEntry.state)
            assertEquals(savedRun._id, resultEntry.runId)
            assertEquals("dataset_${savedRun._id}_${dataset.questions[index].id}", resultEntry.userIdModifier)
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `createRun snapshots all compressor settings even when disabled and retains them after configuration changes`(enabled: Boolean) {
        val dataset = newDataset()
        val compressorSetting =
            BloomzDocumentCompressorSetting(
                maxDocuments = 4,
                minScore = 0.5f,
                fillToMaxDocuments = true,
                label = "entailment",
                endpoint = "https://compressor.example.test",
            )
        val compressor = BotDocumentCompressorConfiguration(newId(), NAMESPACE, BOT_ID, enabled, compressorSetting)
        val rag =
            BotRAGConfiguration(
                _id = newId(),
                namespace = NAMESPACE,
                botId = BOT_ID,
                enabled = true,
                questionCondensingLlmSetting = OllamaLLMSetting(temperature = "0", model = "condensing", baseUrl = "http://localhost"),
                questionCondensingPrompt = PromptTemplate(template = "Condense the question"),
                questionAnsweringLlmSetting = OllamaLLMSetting(temperature = "0", model = "answering", baseUrl = "http://localhost"),
                questionAnsweringPrompt = PromptTemplate(template = "Answer the question"),
                emSetting = OllamaEMSetting(model = "embedding", baseUrl = "http://localhost"),
            )
        val savedRun = slot<DatasetRun>()
        val savedResults = slot<List<DatasetRunQuestionResult>>()
        every { datasetDAO.getDatasetById(any()) } returns dataset
        every { datasetRunDAO.getActiveRunsByDatasetId(dataset._id) } returns emptyList()
        every { applicationConfigurationDAO.getConfigurationsByNamespaceAndBotId(NAMESPACE, BOT_ID) } returns listOf(newRestConfiguration())
        every { ragConfigurationDAO.findByNamespaceAndBotId(NAMESPACE, BOT_ID) } returns rag
        every { documentCompressorConfigurationDAO.findByNamespaceAndBotId(NAMESPACE, BOT_ID) } returns compressor
        every { datasetRunDAO.saveRun(capture(savedRun)) } answers { savedRun.captured }
        every { datasetRunDAO.saveQuestionResults(capture(savedResults)) } returns Unit
        every { datasetRunDAO.getQuestionResultsByRunId(any()) } answers { savedResults.captured }

        DatasetService.createRun(NAMESPACE, BOT_ID, dataset._id.toString(), DatasetRunCreateRequest(language = "fr"), USER)

        val snapshot = savedRun.captured.settingsSnapshot
        val compression = snapshot["documentCompressor"] as Map<*, *>
        assertEquals(enabled, compression["enabled"])
        assertEquals(
            mapOf(
                "provider" to "BloomzRerank",
                "maxDocuments" to 4,
                "minScore" to 0.5f,
                "fillToMaxDocuments" to true,
                "label" to "entailment",
                "endpoint" to compressorSetting.endpoint,
            ),
            compression["setting"],
        )
        assertEquals("Answer the question", (snapshot["questionAnsweringPrompt"] as Map<*, *>)["template"])
        assertTrue(!(snapshot["questionAnsweringLlmSetting"] as Map<*, *>).containsKey("apiKey"))

        every { documentCompressorConfigurationDAO.findByNamespaceAndBotId(NAMESPACE, BOT_ID) } returns
            compressor.copy(setting = compressorSetting.copy(minScore = 0.9f))
        every { datasetRunDAO.getRunsByDatasetId(dataset._id) } returns listOf(savedRun.captured)
        val reloaded = DatasetService.getDataset(NAMESPACE, BOT_ID, dataset._id.toString())

        assertEquals(snapshot, reloaded.runs.single().settingsSnapshot)
        verify(exactly = 1) { documentCompressorConfigurationDAO.findByNamespaceAndBotId(NAMESPACE, BOT_ID) }
    }

    @Test
    fun `createRun does not throws when several REST configurations are available`() {
        val dataset = newDataset()
        val runSlot = slot<DatasetRun>()
        val questionResultsSlot = slot<List<DatasetRunQuestionResult>>()

        every { datasetDAO.getDatasetById(any()) } returns dataset
        every { datasetRunDAO.getActiveRunsByDatasetId(dataset._id) } returns emptyList()
        every { applicationConfigurationDAO.getConfigurationsByNamespaceAndBotId(NAMESPACE, BOT_ID) } returns
            listOf(
                newRestConfiguration("test-app-a"),
                newRestConfiguration("test-app-b"),
            )
        every { ragConfigurationDAO.findByNamespaceAndBotId(NAMESPACE, BOT_ID) } returns null
        every { datasetRunDAO.saveRun(capture(runSlot)) } answers { runSlot.captured }
        every { datasetRunDAO.saveQuestionResults(capture(questionResultsSlot)) } returns Unit
        every { datasetRunDAO.getQuestionResultsByRunId(any()) } answers { questionResultsSlot.captured }

        assertDoesNotThrow {
            DatasetService.createRun(
                namespace = NAMESPACE,
                botId = BOT_ID,
                datasetId = dataset._id.toString(),
                request = DatasetRunCreateRequest(language = "fr"),
                userLogin = USER,
            )
        }
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
    fun `getRun returns rag status and non rag answer stats for completed run`() {
        val dataset = newDataset()
        val run = newFinishedRun(dataset, DatasetRunState.COMPLETED)
        val dialogId = newId<Dialog>()
        val ragAnswer =
            botSentenceWithFootnotesAction(
                id = "rag-answer-action-id",
                ragDebug = mapOf("answer" to mapOf("status" to "found_in_context")),
            )
        val nonRagAnswer = botSentenceAction("non-rag-answer-action-id")
        val questionResults =
            listOf(
                DatasetRunQuestionResult(
                    namespace = NAMESPACE,
                    botId = BOT_ID,
                    datasetId = dataset._id,
                    runId = run._id,
                    questionId = dataset.questions.first().id,
                    state = DatasetRunQuestionResultState.COMPLETED,
                    userIdModifier = "dataset_${run._id}_${dataset.questions.first().id}",
                    userActionId = "user-action-id-1",
                    dialogId = dialogId,
                    answerActionId = ragAnswer.id,
                ),
                DatasetRunQuestionResult(
                    namespace = NAMESPACE,
                    botId = BOT_ID,
                    datasetId = dataset._id,
                    runId = run._id,
                    questionId = dataset.questions.last().id,
                    state = DatasetRunQuestionResultState.COMPLETED,
                    userIdModifier = "dataset_${run._id}_${dataset.questions.last().id}",
                    userActionId = "user-action-id-2",
                    dialogId = dialogId,
                    answerActionId = nonRagAnswer.id,
                ),
            )
        val dialog =
            DialogReport(
                actions =
                    listOf(
                        userSentenceAction("user-action-id-1"),
                        ragAnswer,
                        userSentenceAction("user-action-id-2"),
                        nonRagAnswer,
                    ),
                userInterface = UserInterfaceType.textChat,
                id = dialogId,
            )

        every { datasetDAO.getDatasetById(any()) } returns dataset
        every { datasetRunDAO.getRunById(any()) } returns run
        every { datasetRunDAO.getQuestionResultsByRunId(run._id) } returns questionResults
        every { dialogReportDAO.findByDialogByIds(setOf(dialogId)) } returns setOf(dialog)

        val result =
            DatasetService.getRun(
                namespace = NAMESPACE,
                botId = BOT_ID,
                datasetId = dataset._id.toString(),
                runId = run._id.toString(),
            )

        assertEquals(2, result.stats.totalQuestions)
        assertEquals(2, result.stats.completedQuestions)
        assertEquals(1, result.stats.ragAnswerStatusCounts["found_in_context"])
        assertEquals(0, result.stats.ragAnswerStatusCounts["technical_error"])
        assertEquals(1, result.stats.nonRagAnswers)
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
        val endedAt = Instant.now()
        every { datasetDAO.getDatasetById(any()) } returns dataset
        every { datasetRunDAO.getRunById(any()) } returns run
        every { datasetRunDAO.finishRun(run._id, DatasetRunState.CANCELLED, any()) } returns run.copy(state = DatasetRunState.CANCELLED, endTime = endedAt)
        every { datasetRunDAO.finishActiveQuestionResults(run._id, DatasetRunQuestionResultState.CANCELLED, any(), isNull()) } returns Unit
        every { datasetRunDAO.getQuestionResultsByRunId(run._id) } returns listOf(pendingResult.copy(state = DatasetRunQuestionResultState.CANCELLED), completedResult)

        val result = DatasetService.cancelRun(NAMESPACE, BOT_ID, dataset._id.toString(), run._id.toString())

        assertEquals(DatasetRunState.CANCELLED, result.state)
        assertEquals(endedAt, result.endTime)
        assertEquals(2, result.stats.totalQuestions)
        assertEquals(1, result.stats.completedQuestions)
        verify(exactly = 1) { datasetRunDAO.finishActiveQuestionResults(run._id, DatasetRunQuestionResultState.CANCELLED, any(), isNull()) }
        verify(exactly = 0) { datasetRunDAO.saveQuestionResults(any()) }
    }

    @Test
    fun `deleteRun deletes completed run`() {
        val dataset = newDataset()
        val run = newFinishedRun(dataset, DatasetRunState.COMPLETED)

        every { datasetDAO.getDatasetById(any()) } returns dataset
        every { datasetRunDAO.getRunById(any()) } returns run
        every { datasetRunDAO.deleteRun(run._id) } returns Unit

        DatasetService.deleteRun(
            namespace = NAMESPACE,
            botId = BOT_ID,
            datasetId = dataset._id.toString(),
            runId = run._id.toString(),
        )

        verify(exactly = 1) { datasetRunDAO.deleteRun(run._id) }
    }

    @Test
    fun `deleteRun deletes cancelled run`() {
        val dataset = newDataset()
        val run = newFinishedRun(dataset, DatasetRunState.CANCELLED)

        every { datasetDAO.getDatasetById(any()) } returns dataset
        every { datasetRunDAO.getRunById(any()) } returns run
        every { datasetRunDAO.deleteRun(run._id) } returns Unit

        DatasetService.deleteRun(
            namespace = NAMESPACE,
            botId = BOT_ID,
            datasetId = dataset._id.toString(),
            runId = run._id.toString(),
        )

        verify(exactly = 1) { datasetRunDAO.deleteRun(run._id) }
    }

    @Test
    fun `deleteRun rejects active run`() {
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

        every { datasetDAO.getDatasetById(any()) } returns dataset
        every { datasetRunDAO.getRunById(any()) } returns run

        assertThrows<DatasetError.RunNotFinished> {
            DatasetService.deleteRun(
                namespace = NAMESPACE,
                botId = BOT_ID,
                datasetId = dataset._id.toString(),
                runId = run._id.toString(),
            )
        }

        verify(exactly = 0) { datasetRunDAO.deleteRun(any()) }
    }

    @Test
    fun `createEvaluationSample creates sample from valid run actions`() {
        val dataset = newDataset()
        val run = newFinishedRun(dataset, DatasetRunState.COMPLETED)
        val answerAction = botSentenceWithFootnotesAction("answer-action-id")
        val dialog =
            DialogReport(
                actions = listOf(answerAction),
                userInterface = UserInterfaceType.textChat,
                id = newId<Dialog>(),
            )
        val questionResult =
            DatasetRunQuestionResult(
                namespace = NAMESPACE,
                botId = BOT_ID,
                datasetId = dataset._id,
                runId = run._id,
                questionId = dataset.questions.first().id,
                state = DatasetRunQuestionResultState.COMPLETED,
                userIdModifier = "dataset_${run._id}_${dataset.questions.first().id}",
                dialogId = dialog.id,
                answerActionId = answerAction.id,
            )
        val sampleSlot = slot<EvaluationSample>()
        val evaluationsSlot = slot<List<Evaluation>>()

        every { datasetDAO.getDatasetById(any()) } returns dataset
        every { datasetRunDAO.getRunById(any()) } returns run
        every { datasetRunDAO.getQuestionResultsByRunId(run._id) } returns listOf(questionResult)
        every { dialogReportDAO.findByDialogByIds(setOf(dialog.id)) } returns setOf(dialog)
        every { evaluationSampleDAO.save(capture(sampleSlot)) } answers { sampleSlot.captured }
        every { evaluationDAO.createAll(capture(evaluationsSlot)) } returns Unit

        val result =
            EvaluationService.createEvaluationSample(
                namespace = NAMESPACE,
                botId = BOT_ID,
                request =
                    CreateEvaluationSampleRequest(
                        name = " Evaluation from run ",
                        description = " description ",
                        dialogInfo = null,
                        datasetRunInfo = DatasetRunInfo(runId = run._id.toString()),
                    ),
                createdBy = USER,
            )

        assertEquals("Evaluation from run", result.name)
        assertEquals("description", result.description)
        assertEquals(run._id.toString(), result.createdFromRun)
        assertEquals(run.startTime, result.dialogActivityFrom)
        assertEquals(run.endTime, result.dialogActivityTo)
        assertEquals(1, result.requestedDialogCount)
        assertEquals(1, result.dialogsCount)
        assertEquals(1, result.totalDialogCount)
        assertEquals(1, result.botActionCount)
        assertTrue(result.allowTestDialogs)

        val savedSample = sampleSlot.captured
        assertEquals(run._id, savedSample.createdFromRun)
        assertEquals(listOf(dialog.id), savedSample.actionRefs.map { it.dialogId })
        assertEquals(listOf(answerAction.id), savedSample.actionRefs.map { it.actionId })

        val createdEvaluations = evaluationsSlot.captured
        assertEquals(1, createdEvaluations.size)
        assertEquals(dialog.id, createdEvaluations.first().dialogId)
        assertEquals(answerAction.id, createdEvaluations.first().actionId)
    }

    @Test
    fun `createEvaluationSample rejects active run`() {
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

        every { datasetDAO.getDatasetById(any()) } returns dataset
        every { datasetRunDAO.getRunById(any()) } returns run

        assertThrows<DatasetError.RunNotFinished> {
            EvaluationService.createEvaluationSample(
                namespace = NAMESPACE,
                botId = BOT_ID,
                request =
                    CreateEvaluationSampleRequest(
                        name = "Evaluation",
                        description = null,
                        dialogInfo = null,
                        datasetRunInfo = DatasetRunInfo(runId = run._id.toString()),
                    ),
                createdBy = USER,
            )
        }

        verify(exactly = 0) { evaluationSampleDAO.save(any()) }
        verify(exactly = 0) { evaluationDAO.createAll(any()) }
    }

    @Test
    fun `createEvaluationSample rejects cancelled run`() {
        val dataset = newDataset()
        val run = newFinishedRun(dataset, DatasetRunState.CANCELLED)

        every { datasetRunDAO.getRunById(any()) } returns run

        assertThrows<DatasetError.RunNotFinished> {
            EvaluationService.createEvaluationSample(
                namespace = NAMESPACE,
                botId = BOT_ID,
                request =
                    CreateEvaluationSampleRequest(
                        name = "Evaluation",
                        description = null,
                        dialogInfo = null,
                        datasetRunInfo = DatasetRunInfo(runId = run._id.toString()),
                    ),
                createdBy = USER,
            )
        }

        verify(exactly = 0) { evaluationSampleDAO.save(any()) }
        verify(exactly = 0) { evaluationDAO.createAll(any()) }
    }

    @Test
    fun `createEvaluationSample rejects run without valid dialog`() {
        val dataset = newDataset()
        val run = newFinishedRun(dataset, DatasetRunState.COMPLETED)
        val questionResult =
            DatasetRunQuestionResult(
                namespace = NAMESPACE,
                botId = BOT_ID,
                datasetId = dataset._id,
                runId = run._id,
                questionId = dataset.questions.first().id,
                state = DatasetRunQuestionResultState.FAILED,
                userIdModifier = "dataset_${run._id}_${dataset.questions.first().id}",
            )

        every { datasetDAO.getDatasetById(any()) } returns dataset
        every { datasetRunDAO.getRunById(any()) } returns run
        every { datasetRunDAO.getQuestionResultsByRunId(run._id) } returns listOf(questionResult)
        every { dialogReportDAO.findByDialogByIds(emptySet()) } returns emptySet()

        assertThrows<UnprocessableEntityException> {
            EvaluationService.createEvaluationSample(
                namespace = NAMESPACE,
                botId = BOT_ID,
                request =
                    CreateEvaluationSampleRequest(
                        name = "Evaluation",
                        description = null,
                        dialogInfo = null,
                        datasetRunInfo = DatasetRunInfo(runId = run._id.toString()),
                    ),
                createdBy = USER,
            )
        }

        verify(exactly = 0) { evaluationSampleDAO.save(any()) }
        verify(exactly = 0) { evaluationDAO.createAll(any()) }
    }

    @Test
    fun `dataset create request rejects blank name`() {
        val exception =
            assertThrows<ValidationError> {
                Valid(
                    DatasetCreateRequest(
                        name = "   ",
                        description = "desc",
                        questions =
                            listOf(
                                ai.tock.bot.admin.model.dataset
                                    .DatasetQuestionRequest(question = "question"),
                            ),
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
        val answerAction = botSentenceWithFootnotesAction("answer-action-id", ragDebug = mapOf("duration" to 3.25, "answer" to mapOf("status" to "found_in_context")))
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
        every { datasetRunDAO.updateQuestionResultAnswer(capture(updatedQuestionResultSlot)) } returns Unit

        val result =
            DatasetService.getRunActions(
                namespace = NAMESPACE,
                botId = BOT_ID,
                datasetId = dataset._id.toString(),
                runId = run._id.toString(),
            )

        assertEquals(1, result.size)
        assertEquals(DatasetRunActionState.COMPLETED, result.first().state)
        assertEquals(
            answerAction.id.toString(),
            result
                .first()
                .action
                ?.id
                .toString(),
        )
        assertEquals(dialog.id, updatedQuestionResultSlot.captured.dialogId)
        assertEquals(answerAction.id, updatedQuestionResultSlot.captured.answerActionId)
        assertEquals(3.25, updatedQuestionResultSlot.captured.ragDurationSeconds)
        assertEquals("test_${run.botApplicationConfigurationId}_${run.language}_${questionResult.userIdModifier}", querySlot.captured.playerId?.id)

        every { datasetRunDAO.getQuestionResultsByRunId(run._id) } returns listOf(updatedQuestionResultSlot.captured)
        val reloaded = DatasetService.getRun(NAMESPACE, BOT_ID, dataset._id.toString(), run._id.toString())
        assertEquals(3.25, reloaded.stats.averageRagDurationSeconds)
        assertEquals(1, reloaded.stats.ragDurationCount)
        verify(exactly = 1) { dialogReportDAO.search(any()) }
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
        verify(exactly = 0) { datasetRunDAO.updateQuestionResultAnswer(any()) }
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
                error = "RAG execution failed: timeout",
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
        assertEquals("RAG execution failed: timeout", result.first().error)
        verify(exactly = 0) { dialogReportDAO.search(any()) }
    }

    @Test
    fun `completed run statistics do not load dialogs when answer statuses are persisted`() {
        val dataset = newDataset()
        val run = newFinishedRun(dataset, DatasetRunState.COMPLETED)
        every { datasetDAO.getDatasetById(any()) } returns dataset
        every { datasetRunDAO.getRunById(any()) } returns run
        val result =
            DatasetRunQuestionResult(
                namespace = NAMESPACE,
                botId = BOT_ID,
                datasetId = dataset._id,
                runId = run._id,
                questionId = "rag",
                userIdModifier = "test",
                state = DatasetRunQuestionResultState.COMPLETED,
                isRagAnswer = true,
                ragAnswerStatus = "found_in_context",
            )
        every { datasetRunDAO.getQuestionResultsByRunId(run._id) } returns
            listOf(
                result,
                result.copy(questionId = "intent", isRagAnswer = false, ragAnswerStatus = null),
            )

        val response = DatasetService.getRun(NAMESPACE, BOT_ID, dataset._id.toString(), run._id.toString())

        assertEquals(1, response.stats.ragAnswerStatusCounts["found_in_context"])
        assertEquals(1, response.stats.nonRagAnswers)
        assertEquals(2, response.stats.completedQuestions)
        assertNull(response.stats.averageRagDurationSeconds)
        assertEquals(0, response.stats.ragDurationCount)
        verify(exactly = 0) { dialogReportDAO.findByDialogByIds(any()) }
        verify(exactly = 0) { dialogReportDAO.search(any()) }
    }

    @ParameterizedTest
    @EnumSource(value = DatasetRunState::class, names = ["RUNNING", "COMPLETED", "CANCELLED"])
    fun `RAG duration averages only measured successful RAG answers without loading dialogs`(state: DatasetRunState) {
        val dataset = newDataset()
        val run = newFinishedRun(dataset, state)
        val measured =
            DatasetRunQuestionResult(
                namespace = NAMESPACE,
                botId = BOT_ID,
                datasetId = dataset._id,
                runId = run._id,
                questionId = "rag",
                userIdModifier = "test",
                state = DatasetRunQuestionResultState.COMPLETED,
                isRagAnswer = true,
                ragAnswerStatus = "found_in_context",
                ragDurationSeconds = 4.0,
            )
        every { datasetDAO.getDatasetById(any()) } returns dataset
        every { datasetRunDAO.getRunById(any()) } returns run
        every { datasetRunDAO.getQuestionResultsByRunId(run._id) } returns
            listOf(
                measured,
                measured.copy(ragAnswerStatus = "not_found_in_context", ragDurationSeconds = 2.0, retryCount = 1),
                measured.copy(ragDurationSeconds = 0.0),
                measured.copy(ragDurationSeconds = null),
                measured.copy(isRagAnswer = false),
                measured.copy(state = DatasetRunQuestionResultState.FAILED),
                measured.copy(state = DatasetRunQuestionResultState.CANCELLED),
                measured.copy(state = DatasetRunQuestionResultState.RUNNING),
                measured.copy(ragAnswerStatus = "technical_error"),
                measured.copy(ragDurationSeconds = -1.0),
                measured.copy(ragDurationSeconds = Double.NaN),
                measured.copy(ragDurationSeconds = Double.POSITIVE_INFINITY),
            )

        val response = DatasetService.getRun(NAMESPACE, BOT_ID, dataset._id.toString(), run._id.toString())

        assertEquals(2.0, response.stats.averageRagDurationSeconds)
        assertEquals(3, response.stats.ragDurationCount)
        verify(exactly = 0) { dialogReportDAO.findByDialogByIds(any()) }
        verify(exactly = 0) { dialogReportDAO.search(any()) }
    }

    @Test
    fun `dataset request rejects duplicate question IDs after trimming`() {
        val request =
            DatasetCreateRequest(
                name = "Dataset",
                questions = listOf(DatasetQuestionRequest(id = "same", question = "First"), DatasetQuestionRequest(id = " same ", question = "Second")),
            )
        assertTrue(request.validate().contains("Question IDs must be unique"))
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

    private fun botSentenceAction(id: String): ActionReport =
        ActionReport(
            playerId = PlayerId(BOT_ID, PlayerType.bot),
            recipientId = PlayerId("user", PlayerType.user),
            date = Instant.now(),
            message = Sentence("answer"),
            connectorType = null,
            userInterfaceType = UserInterfaceType.textChat,
            id = id.toId(),
            intent = null,
            applicationId = null,
            metadata = ActionMetadata(),
        )

    private fun botSentenceWithFootnotesAction(
        id: String,
        ragDebug: Any? = null,
    ): ActionReport =
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
            ragDebug = ragDebug,
        )
}

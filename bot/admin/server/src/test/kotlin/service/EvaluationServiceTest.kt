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
import ai.tock.bot.admin.dialog.ActionReport
import ai.tock.bot.admin.dialog.DialogReport
import ai.tock.bot.admin.dialog.DialogReportDAO
import ai.tock.bot.admin.dialog.DialogReportQuery
import ai.tock.bot.admin.dialog.DialogReportQueryResult
import ai.tock.bot.admin.evaluation.ActionRef
import ai.tock.bot.admin.evaluation.Evaluation
import ai.tock.bot.admin.evaluation.EvaluationDAO
import ai.tock.bot.admin.evaluation.EvaluationReason
import ai.tock.bot.admin.evaluation.EvaluationSample
import ai.tock.bot.admin.evaluation.EvaluationSampleDAO
import ai.tock.bot.admin.evaluation.EvaluationSampleStatus
import ai.tock.bot.admin.evaluation.EvaluationStatus
import ai.tock.bot.admin.model.evaluation.CreateEvaluationSampleRequest
import ai.tock.bot.engine.action.ActionMetadata
import ai.tock.bot.engine.message.Sentence
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.shared.exception.rest.BadRequestException
import ai.tock.shared.tockInternalInjector
import ai.tock.translator.UserInterfaceType
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import org.litote.kmongo.toId
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EvaluationServiceTest : AbstractTest() {
    companion object {
        private const val NAMESPACE = "testNamespace"
        private const val BOT_ID = "testBotId"
        private const val USER = "testUser"

        val evaluationSampleDAO: EvaluationSampleDAO = mockk()
        val evaluationDAO: EvaluationDAO = mockk()
        val dialogReportDAO: DialogReportDAO = mockk()

        init {
            tockInternalInjector = KodeinInjector()
            val module =
                Kodein.Module(allowSilentOverride = true) {
                    bind<EvaluationSampleDAO>() with provider { evaluationSampleDAO }
                    bind<EvaluationDAO>() with provider { evaluationDAO }
                    bind<DialogReportDAO>() with provider { dialogReportDAO }
                }
            tockInternalInjector.inject(
                Kodein {
                    import(defaultModulesBinding())
                    import(module, allowOverride = true)
                },
            )
        }
    }

    @BeforeEach
    fun setup() {
        clearMocks(evaluationSampleDAO, evaluationDAO, dialogReportDAO)
    }

    private fun createDialogReport(
        dialogId: String,
        botActions: Int = 2,
    ): DialogReport {
        val actions = mutableListOf<ActionReport>()
        for (i in 0 until botActions * 2) {
            val isBot = i % 2 == 1
            actions.add(
                ActionReport(
                    playerId = PlayerId(if (isBot) BOT_ID else "user", if (isBot) PlayerType.bot else PlayerType.user),
                    recipientId = PlayerId(if (isBot) "user" else BOT_ID, if (isBot) PlayerType.user else PlayerType.bot),
                    date = Instant.now(),
                    message = Sentence(if (isBot) "Bot response $i" else "User message $i"),
                    connectorType = null,
                    userInterfaceType = UserInterfaceType.textChat,
                    intent = null,
                    applicationId = null,
                    metadata = ActionMetadata(),
                ),
            )
        }
        return DialogReport(
            actions = actions,
            id = dialogId.toId(),
            userInterface = UserInterfaceType.textChat,
        )
    }

    @Test
    fun `findSamplesByBotId returns samples with computed evaluationsResult`() {
        val sampleId = newId<EvaluationSample>()
        val sample =
            EvaluationSample(
                _id = sampleId,
                botId = BOT_ID,
                namespace = NAMESPACE,
                name = "Test Sample",
                description = null,
                dialogActivityFrom = Instant.now().minusSeconds(86400),
                dialogActivityTo = Instant.now(),
                requestedDialogCount = 10,
                dialogsCount = 5,
                totalDialogCount = 100,
                botActionCount = 15,
                allowTestDialogs = false,
                actionRefs = emptyList(),
                status = EvaluationSampleStatus.IN_PROGRESS,
                createdBy = USER,
                statusChangedBy = USER,
            )

        every { evaluationSampleDAO.findByNamespaceAndBotId(NAMESPACE, BOT_ID, null) } returns listOf(sample)
        every { evaluationDAO.countByStatus(sampleId) } returns
            mapOf(
                EvaluationStatus.UNSET to 10L,
                EvaluationStatus.UP to 3L,
                EvaluationStatus.DOWN to 2L,
            )

        val result = EvaluationService.findSamplesByBotId(NAMESPACE, BOT_ID)

        assertEquals(1, result.size)
        val dto = result[0]
        assertEquals(15, dto.evaluationsResult.total)
        assertEquals(5, dto.evaluationsResult.evaluated)
        assertEquals(10, dto.evaluationsResult.remaining)
        assertEquals(3, dto.evaluationsResult.positiveCount)
        assertEquals(2, dto.evaluationsResult.negativeCount)
    }

    @Test
    fun `createEvaluationSample creates sample and evaluations`() {
        val dialog1 = createDialogReport("dialog1", 2)
        val dialog2 = createDialogReport("dialog2", 3)

        every { dialogReportDAO.search(any<DialogReportQuery>()) } returns
            DialogReportQueryResult(
                total = 2,
                dialogs = listOf(dialog1, dialog2),
            )

        val sampleSlot = slot<EvaluationSample>()
        every { evaluationSampleDAO.save(capture(sampleSlot)) } answers { sampleSlot.captured }

        val evaluationsSlot = slot<List<Evaluation>>()
        every { evaluationDAO.createAll(capture(evaluationsSlot)) } returns Unit

        val request =
            CreateEvaluationSampleRequest(
                name = "Test Sample",
                description = "Test description",
                dialogActivityFrom = Instant.now().minusSeconds(86400),
                dialogActivityTo = Instant.now(),
                requestedDialogCount = 10,
                allowTestDialogs = false,
            )

        val result = EvaluationService.createEvaluationSample(NAMESPACE, BOT_ID, request, USER)

        assertNotNull(result)
        assertEquals("Test Sample", result.name)
        assertEquals(BOT_ID, result.botId)
        assertEquals(NAMESPACE, result.namespace)
        assertEquals(2, result.dialogsCount)
        assertTrue(result.botActionCount > 0)
        assertEquals(EvaluationSampleStatus.IN_PROGRESS, result.status)

        verify { evaluationSampleDAO.save(any()) }
        verify { evaluationDAO.createAll(any()) }

        val createdEvaluations = evaluationsSlot.captured
        assertTrue(createdEvaluations.isNotEmpty())
        assertTrue(createdEvaluations.all { it.status == EvaluationStatus.UNSET })
    }

    @Test
    fun `evaluate updates evaluation with UP status`() {
        val sampleId = newId<EvaluationSample>()
        val evaluationId = newId<Evaluation>()

        val sample =
            EvaluationSample(
                _id = sampleId,
                botId = BOT_ID,
                namespace = NAMESPACE,
                name = "Test",
                description = null,
                dialogActivityFrom = Instant.now(),
                dialogActivityTo = Instant.now(),
                requestedDialogCount = 10,
                dialogsCount = 5,
                totalDialogCount = 100,
                botActionCount = 15,
                allowTestDialogs = false,
                actionRefs = listOf(ActionRef("dialog1", "action1")),
                status = EvaluationSampleStatus.IN_PROGRESS,
                createdBy = USER,
                statusChangedBy = USER,
            )

        val evaluation =
            Evaluation(
                _id = evaluationId,
                evaluationSampleId = sampleId,
                dialogId = "dialog1",
                actionId = "action1",
                status = EvaluationStatus.UNSET,
            )

        every { evaluationSampleDAO.findById(any<Id<EvaluationSample>>()) } returns sample
        every { evaluationDAO.findById(any<Id<Evaluation>>()) } returns evaluation
        every { evaluationDAO.update(any()) } answers { firstArg() }

        val result =
            EvaluationService.evaluate(
                sampleId = sampleId.toString(),
                evaluationId = evaluationId.toString(),
                status = EvaluationStatus.UP,
                reason = null,
                evaluatorId = USER,
            )

        assertNotNull(result)
        assertEquals(EvaluationStatus.UP, result.status)
        assertNull(result.reason)
        assertNotNull(result.evaluator)
        assertEquals(USER, result.evaluator?.id)
    }

    @Test
    fun `evaluate updates evaluation with DOWN status and reason`() {
        val sampleId = newId<EvaluationSample>()
        val evaluationId = newId<Evaluation>()

        val sample =
            EvaluationSample(
                _id = sampleId,
                botId = BOT_ID,
                namespace = NAMESPACE,
                name = "Test",
                description = null,
                dialogActivityFrom = Instant.now(),
                dialogActivityTo = Instant.now(),
                requestedDialogCount = 10,
                dialogsCount = 5,
                totalDialogCount = 100,
                botActionCount = 15,
                allowTestDialogs = false,
                actionRefs = listOf(ActionRef("dialog1", "action1")),
                status = EvaluationSampleStatus.IN_PROGRESS,
                createdBy = USER,
                statusChangedBy = USER,
            )

        val evaluation =
            Evaluation(
                _id = evaluationId,
                evaluationSampleId = sampleId,
                dialogId = "dialog1",
                actionId = "action1",
                status = EvaluationStatus.UNSET,
            )

        every { evaluationSampleDAO.findById(any<Id<EvaluationSample>>()) } returns sample
        every { evaluationDAO.findById(any<Id<Evaluation>>()) } returns evaluation
        every { evaluationDAO.update(any()) } answers { firstArg() }

        val result =
            EvaluationService.evaluate(
                sampleId = sampleId.toString(),
                evaluationId = evaluationId.toString(),
                status = EvaluationStatus.DOWN,
                reason = EvaluationReason.HALLUCINATION,
                evaluatorId = USER,
            )

        assertNotNull(result)
        assertEquals(EvaluationStatus.DOWN, result.status)
        assertEquals(EvaluationReason.HALLUCINATION, result.reason)
    }

    @Test
    fun `evaluate throws when sample is VALIDATED`() {
        val sampleId = newId<EvaluationSample>()
        val evaluationId = newId<Evaluation>()

        val sample =
            EvaluationSample(
                _id = sampleId,
                botId = BOT_ID,
                namespace = NAMESPACE,
                name = "Test",
                description = null,
                dialogActivityFrom = Instant.now(),
                dialogActivityTo = Instant.now(),
                requestedDialogCount = 10,
                dialogsCount = 5,
                totalDialogCount = 100,
                botActionCount = 15,
                allowTestDialogs = false,
                actionRefs = emptyList(),
                status = EvaluationSampleStatus.VALIDATED,
                createdBy = USER,
                statusChangedBy = USER,
            )

        every { evaluationSampleDAO.findById(any<Id<EvaluationSample>>()) } returns sample

        assertThrows<BadRequestException> {
            EvaluationService.evaluate(
                sampleId = sampleId.toString(),
                evaluationId = evaluationId.toString(),
                status = EvaluationStatus.UP,
                reason = null,
                evaluatorId = USER,
            )
        }
    }

    @Test
    fun `changeStatus validates sample when all evaluations are done`() {
        val sampleId = newId<EvaluationSample>()

        val sample =
            EvaluationSample(
                _id = sampleId,
                botId = BOT_ID,
                namespace = NAMESPACE,
                name = "Test",
                description = null,
                dialogActivityFrom = Instant.now(),
                dialogActivityTo = Instant.now(),
                requestedDialogCount = 10,
                dialogsCount = 5,
                totalDialogCount = 100,
                botActionCount = 15,
                allowTestDialogs = false,
                actionRefs = emptyList(),
                status = EvaluationSampleStatus.IN_PROGRESS,
                createdBy = USER,
                statusChangedBy = USER,
            )

        val updatedSample = sample.copy(status = EvaluationSampleStatus.VALIDATED)

        every { evaluationSampleDAO.findById(any<Id<EvaluationSample>>()) } returns sample
        every { evaluationDAO.countByStatus(any<Id<EvaluationSample>>()) } returns
            mapOf(
                EvaluationStatus.UNSET to 0L,
                EvaluationStatus.UP to 10L,
                EvaluationStatus.DOWN to 5L,
            )
        every { evaluationSampleDAO.updateStatus(any(), any(), any(), any()) } returns updatedSample

        val result =
            EvaluationService.changeStatus(
                sampleId = sampleId.toString(),
                targetStatus = EvaluationSampleStatus.VALIDATED,
                changedBy = USER,
                comment = "Validation complete",
            )

        assertNotNull(result)
        assertEquals(EvaluationSampleStatus.VALIDATED, result.status)
    }

    @Test
    fun `changeStatus allows cancellation`() {
        val sampleId = newId<EvaluationSample>()

        val sample =
            EvaluationSample(
                _id = sampleId,
                botId = BOT_ID,
                namespace = NAMESPACE,
                name = "Test",
                description = null,
                dialogActivityFrom = Instant.now(),
                dialogActivityTo = Instant.now(),
                requestedDialogCount = 10,
                dialogsCount = 5,
                totalDialogCount = 100,
                botActionCount = 15,
                allowTestDialogs = false,
                actionRefs = emptyList(),
                status = EvaluationSampleStatus.IN_PROGRESS,
                createdBy = USER,
                statusChangedBy = USER,
            )

        val updatedSample = sample.copy(status = EvaluationSampleStatus.CANCELLED)

        every { evaluationSampleDAO.findById(any<Id<EvaluationSample>>()) } returns sample
        every { evaluationSampleDAO.updateStatus(any(), any(), any(), any()) } returns updatedSample
        every { evaluationDAO.countByStatus(any<Id<EvaluationSample>>()) } returns
            mapOf(
                EvaluationStatus.UNSET to 5L,
                EvaluationStatus.UP to 10L,
                EvaluationStatus.DOWN to 0L,
            )

        val result =
            EvaluationService.changeStatus(
                sampleId = sampleId.toString(),
                targetStatus = EvaluationSampleStatus.CANCELLED,
                changedBy = USER,
                comment = "Cancelled",
            )

        assertNotNull(result)
        assertEquals(EvaluationSampleStatus.CANCELLED, result.status)
    }

    @Test
    fun `deleteSample deletes evaluations and sample`() {
        val sampleId = "507f1f77bcf86cd799439011"

        every { evaluationDAO.deleteByEvaluationSampleId(any()) } returns 10L
        every { evaluationSampleDAO.delete(any()) } returns true

        val result = EvaluationService.deleteSample(sampleId)

        assertTrue(result)
        verify { evaluationDAO.deleteByEvaluationSampleId(any()) }
        verify { evaluationSampleDAO.delete(any()) }
    }
}

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

import ai.tock.bot.admin.evaluation.Evaluation
import ai.tock.bot.admin.evaluation.EvaluationReason
import ai.tock.bot.admin.evaluation.EvaluationSample
import ai.tock.bot.admin.evaluation.EvaluationStatus
import ai.tock.bot.admin.evaluation.Evaluator
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.dialog.Dialog
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import org.litote.kmongo.toId
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EvaluationMongoDAOTest : AbstractTest() {
    @BeforeEach
    fun setup() {
        EvaluationMongoDAO.col.drop()
    }

    private fun createEvaluation(
        sampleId: Id<EvaluationSample>,
        dialogId: String,
        actionId: String,
        status: EvaluationStatus = EvaluationStatus.UNSET,
    ): Evaluation {
        return Evaluation(
            evaluationSampleId = sampleId,
            dialogId = dialogId.toId<Dialog>(),
            actionId = actionId.toId<Action>(),
            status = status,
        )
    }

    @Test
    fun `should save and retrieve evaluations`() {
        val sampleId = newId<EvaluationSample>()
        val evaluation1 = createEvaluation(sampleId, "dialog1", "action1")
        val evaluation2 = createEvaluation(sampleId, "dialog2", "action2")

        EvaluationMongoDAO.createAll(listOf(evaluation1, evaluation2))

        val foundEvaluations = EvaluationMongoDAO.findByEvaluationSampleId(sampleId)
        assertEquals(2, foundEvaluations.size)
    }

    @Test
    fun `should count evaluations by sample id`() {
        val sampleId = newId<EvaluationSample>()
        EvaluationMongoDAO.createAll(
            listOf(
                createEvaluation(sampleId, "dialog1", "action1"),
                createEvaluation(sampleId, "dialog2", "action2"),
                createEvaluation(sampleId, "dialog3", "action3"),
            ),
        )

        val count = EvaluationMongoDAO.countByEvaluationSampleId(sampleId)
        assertEquals(3, count)
    }

    @Test
    fun `should paginate evaluations`() {
        val sampleId = newId<EvaluationSample>()
        val evaluations = (0 until 10).map { createEvaluation(sampleId, "dialog$it", "action$it") }
        EvaluationMongoDAO.createAll(evaluations)

        val firstPage = EvaluationMongoDAO.findByEvaluationSampleId(sampleId, 0, 5)
        assertEquals(5, firstPage.size)

        val secondPage = EvaluationMongoDAO.findByEvaluationSampleId(sampleId, 5, 5)
        assertEquals(5, secondPage.size)
    }

    @Test
    fun `should find evaluations by sample id and dialog ids`() {
        val sampleId = newId<EvaluationSample>()
        EvaluationMongoDAO.createAll(
            listOf(
                createEvaluation(sampleId, "d1", "a1"),
                createEvaluation(sampleId, "d2", "a2"),
                createEvaluation(sampleId, "d3", "a3"),
            ),
        )

        val result =
            EvaluationMongoDAO.findByEvaluationSampleIdAndDialogIds(
                sampleId,
                listOf("d1".toId<Dialog>(), "d3".toId<Dialog>()),
            )
        assertEquals(2, result.size)
        assertEquals(setOf("d1".toId<Dialog>(), "d3".toId<Dialog>()), result.map { it.dialogId }.toSet())
    }

    @Test
    fun `should return empty list when findByEvaluationSampleIdAndDialogIds with empty dialog ids`() {
        val sampleId = newId<EvaluationSample>()
        EvaluationMongoDAO.createAll(listOf(createEvaluation(sampleId, "d1", "a1")))

        val result = EvaluationMongoDAO.findByEvaluationSampleIdAndDialogIds(sampleId, emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should return grouped evaluations paginated and sorted by dialogId`() {
        val sampleId = newId<EvaluationSample>()

        // Insert in non-sorted order to validate ordering.
        EvaluationMongoDAO.createAll(
            listOf(
                createEvaluation(sampleId, "d2", "a2"),
                createEvaluation(sampleId, "d1", "a2"),
                createEvaluation(sampleId, "d1", "a1"),
                createEvaluation(sampleId, "d3", "a1"),
                createEvaluation(sampleId, "d2", "a1"),
            ),
        )

        val result = EvaluationMongoDAO.findGroupedEvaluationsBySampleId(sampleId, start = 0, size = 2)

        assertEquals(2, result.size)
        assertEquals(listOf("d1".toId<Dialog>(), "d2".toId<Dialog>()), result.map { it._id })

        val d1 = result.first { it._id == "d1".toId<Dialog>() }.evaluations
        assertEquals(listOf("a1".toId<Action>(), "a2".toId<Action>()), d1.map { it.actionId })

        val d2 = result.first { it._id == "d2".toId<Dialog>() }.evaluations
        assertEquals(listOf("a1".toId<Action>(), "a2".toId<Action>()), d2.map { it.actionId })
    }

    @Test
    fun `should update evaluation`() {
        val sampleId = newId<EvaluationSample>()
        val evaluation = createEvaluation(sampleId, "dialog1", "action1")
        EvaluationMongoDAO.createAll(listOf(evaluation))

        val updated =
            evaluation.copy(
                status = EvaluationStatus.DOWN,
                reason = EvaluationReason.HALLUCINATION,
                evaluator = Evaluator("user123"),
                evaluationDate = Instant.now(),
            )
        val result = EvaluationMongoDAO.update(updated)

        assertNotNull(result)
        assertEquals(EvaluationStatus.DOWN, result.status)
        assertEquals(EvaluationReason.HALLUCINATION, result.reason)
        assertNotNull(result.evaluator)
        assertEquals("user123", result.evaluator?.id)
    }

    @Test
    fun `should delete evaluations by sample id`() {
        val sampleId1 = newId<EvaluationSample>()
        val sampleId2 = newId<EvaluationSample>()
        EvaluationMongoDAO.createAll(
            listOf(
                createEvaluation(sampleId1, "d1", "a1"),
                createEvaluation(sampleId1, "d2", "a2"),
                createEvaluation(sampleId2, "d3", "a3"),
            ),
        )

        val deletedCount = EvaluationMongoDAO.deleteByEvaluationSampleId(sampleId1)
        assertEquals(2, deletedCount)

        val remaining = EvaluationMongoDAO.findByEvaluationSampleId(sampleId1)
        assertTrue(remaining.isEmpty())

        val sample2Evaluations = EvaluationMongoDAO.findByEvaluationSampleId(sampleId2)
        assertEquals(1, sample2Evaluations.size)
    }

    @Test
    fun `should count by status`() {
        val sampleId = newId<EvaluationSample>()
        EvaluationMongoDAO.createAll(
            listOf(
                createEvaluation(sampleId, "d1", "a1", EvaluationStatus.UNSET),
                createEvaluation(sampleId, "d2", "a2", EvaluationStatus.UNSET),
                createEvaluation(sampleId, "d3", "a3", EvaluationStatus.UP),
                createEvaluation(sampleId, "d4", "a4", EvaluationStatus.UP),
                createEvaluation(sampleId, "d5", "a5", EvaluationStatus.UP),
                createEvaluation(sampleId, "d6", "a6", EvaluationStatus.DOWN),
            ),
        )

        val counts = EvaluationMongoDAO.countByStatus(sampleId)
        assertEquals(2L, counts[EvaluationStatus.UNSET])
        assertEquals(3L, counts[EvaluationStatus.UP])
        assertEquals(1L, counts[EvaluationStatus.DOWN])
    }

    @Test
    fun `should find evaluation by id`() {
        val sampleId = newId<EvaluationSample>()
        val evaluation = createEvaluation(sampleId, "dialog1", "action1")
        EvaluationMongoDAO.createAll(listOf(evaluation))

        val found = EvaluationMongoDAO.findById(evaluation._id)
        assertNotNull(found)
        assertEquals(evaluation._id, found._id)
    }

    @Test
    fun `should return null for non-existent evaluation`() {
        val found = EvaluationMongoDAO.findById(newId())
        assertNull(found)
    }

    @Test
    fun `should return empty list for non-existent sample`() {
        val found = EvaluationMongoDAO.findByEvaluationSampleId(newId())
        assertTrue(found.isEmpty())
    }

    @Test
    fun `should return zero counts for non-existent sample`() {
        val counts = EvaluationMongoDAO.countByStatus(newId())
        assertEquals(0L, counts[EvaluationStatus.UNSET])
        assertEquals(0L, counts[EvaluationStatus.UP])
        assertEquals(0L, counts[EvaluationStatus.DOWN])
    }
}

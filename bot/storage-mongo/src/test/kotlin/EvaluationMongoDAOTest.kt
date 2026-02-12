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
import ai.tock.bot.admin.evaluation.EvaluationDAO
import ai.tock.bot.admin.evaluation.EvaluationReason
import ai.tock.bot.admin.evaluation.EvaluationSample
import ai.tock.bot.admin.evaluation.EvaluationStatus
import ai.tock.bot.admin.evaluation.Evaluator
import ai.tock.shared.tockInternalInjector
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EvaluationMongoDAOTest {
    companion object {
        private val evaluationDAO: EvaluationDAO get() = EvaluationMongoDAO

        init {
            tockInternalInjector = KodeinInjector()
            tockInternalInjector.inject(
                Kodein {
                    import(botMongoModule)
                },
            )
        }
    }

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
            dialogId = dialogId,
            actionId = actionId,
            status = status,
        )
    }

    @Test
    fun `should save and retrieve evaluations`() {
        val sampleId = newId<EvaluationSample>()
        val evaluation1 = createEvaluation(sampleId, "dialog1", "action1")
        val evaluation2 = createEvaluation(sampleId, "dialog2", "action2")

        evaluationDAO.createAll(listOf(evaluation1, evaluation2))

        val foundEvaluations = evaluationDAO.findByEvaluationSampleId(sampleId)
        assertEquals(2, foundEvaluations.size)
    }

    @Test
    fun `should count evaluations by sample id`() {
        val sampleId = newId<EvaluationSample>()
        evaluationDAO.createAll(
            listOf(
                createEvaluation(sampleId, "dialog1", "action1"),
                createEvaluation(sampleId, "dialog2", "action2"),
                createEvaluation(sampleId, "dialog3", "action3"),
            ),
        )

        val count = evaluationDAO.countByEvaluationSampleId(sampleId)
        assertEquals(3, count)
    }

    @Test
    fun `should paginate evaluations`() {
        val sampleId = newId<EvaluationSample>()
        val evaluations = (0 until 10).map { createEvaluation(sampleId, "dialog$it", "action$it") }
        evaluationDAO.createAll(evaluations)

        val firstPage = evaluationDAO.findByEvaluationSampleId(sampleId, 0, 5)
        assertEquals(5, firstPage.size)

        val secondPage = evaluationDAO.findByEvaluationSampleId(sampleId, 5, 5)
        assertEquals(5, secondPage.size)
    }

    @Test
    fun `should filter evaluations by status`() {
        val sampleId = newId<EvaluationSample>()
        evaluationDAO.createAll(
            listOf(
                createEvaluation(sampleId, "d1", "a1", EvaluationStatus.UNSET),
                createEvaluation(sampleId, "d2", "a2", EvaluationStatus.UP),
                createEvaluation(sampleId, "d3", "a3", EvaluationStatus.DOWN),
            ),
        )

        val unsetEvaluations = evaluationDAO.findByEvaluationSampleId(sampleId, 0, 20, EvaluationStatus.UNSET)
        assertEquals(1, unsetEvaluations.size)
        assertEquals("d1", unsetEvaluations[0].dialogId)

        val upEvaluations = evaluationDAO.findByEvaluationSampleId(sampleId, 0, 20, EvaluationStatus.UP)
        assertEquals(1, upEvaluations.size)
        assertEquals("d2", upEvaluations[0].dialogId)
    }

    @Test
    fun `should update evaluation`() {
        val sampleId = newId<EvaluationSample>()
        val evaluation = createEvaluation(sampleId, "dialog1", "action1")
        evaluationDAO.createAll(listOf(evaluation))

        val updated =
            evaluation.copy(
                status = EvaluationStatus.DOWN,
                reason = EvaluationReason.HALLUCINATION,
                evaluator = Evaluator("user123"),
                evaluationDate = Instant.now(),
            )
        val result = evaluationDAO.update(updated)

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
        evaluationDAO.createAll(
            listOf(
                createEvaluation(sampleId1, "d1", "a1"),
                createEvaluation(sampleId1, "d2", "a2"),
                createEvaluation(sampleId2, "d3", "a3"),
            ),
        )

        val deletedCount = evaluationDAO.deleteByEvaluationSampleId(sampleId1)
        assertEquals(2, deletedCount)

        val remaining = evaluationDAO.findByEvaluationSampleId(sampleId1)
        assertTrue(remaining.isEmpty())

        val sample2Evaluations = evaluationDAO.findByEvaluationSampleId(sampleId2)
        assertEquals(1, sample2Evaluations.size)
    }

    @Test
    fun `should count by status`() {
        val sampleId = newId<EvaluationSample>()
        evaluationDAO.createAll(
            listOf(
                createEvaluation(sampleId, "d1", "a1", EvaluationStatus.UNSET),
                createEvaluation(sampleId, "d2", "a2", EvaluationStatus.UNSET),
                createEvaluation(sampleId, "d3", "a3", EvaluationStatus.UP),
                createEvaluation(sampleId, "d4", "a4", EvaluationStatus.UP),
                createEvaluation(sampleId, "d5", "a5", EvaluationStatus.UP),
                createEvaluation(sampleId, "d6", "a6", EvaluationStatus.DOWN),
            ),
        )

        val counts = evaluationDAO.countByStatus(sampleId)
        assertEquals(2L, counts[EvaluationStatus.UNSET])
        assertEquals(3L, counts[EvaluationStatus.UP])
        assertEquals(1L, counts[EvaluationStatus.DOWN])
    }

    @Test
    fun `should find evaluation by id`() {
        val sampleId = newId<EvaluationSample>()
        val evaluation = createEvaluation(sampleId, "dialog1", "action1")
        evaluationDAO.createAll(listOf(evaluation))

        val found = evaluationDAO.findById(evaluation._id)
        assertNotNull(found)
        assertEquals(evaluation._id, found._id)
    }

    @Test
    fun `should return null for non-existent evaluation`() {
        val found = evaluationDAO.findById(newId())
        assertNull(found)
    }

    @Test
    fun `should return empty list for non-existent sample`() {
        val found = evaluationDAO.findByEvaluationSampleId(newId())
        assertTrue(found.isEmpty())
    }

    @Test
    fun `should return zero counts for non-existent sample`() {
        val counts = evaluationDAO.countByStatus(newId())
        assertEquals(0L, counts[EvaluationStatus.UNSET])
        assertEquals(0L, counts[EvaluationStatus.UP])
        assertEquals(0L, counts[EvaluationStatus.DOWN])
    }
}

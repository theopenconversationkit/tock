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

import ai.tock.bot.admin.evaluation.ActionRef
import ai.tock.bot.admin.evaluation.EvaluationSample
import ai.tock.bot.admin.evaluation.EvaluationSampleStatus
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.shared.tockInternalInjector
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.newId
import org.litote.kmongo.toId
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EvaluationSampleMongoDAOTest : AbstractTest() {
    companion object {
        private const val NAMESPACE = "testNamespace"
        private const val BOT_ID = "testBotId"
        private const val USER = "testUser"

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
        EvaluationSampleMongoDAO.col.drop()
    }

    private fun createSample(
        name: String,
        botId: String = BOT_ID,
        namespace: String = NAMESPACE,
        status: EvaluationSampleStatus = EvaluationSampleStatus.IN_PROGRESS,
    ): EvaluationSample {
        val now = Instant.now()
        return EvaluationSample(
            botId = botId,
            namespace = namespace,
            name = name,
            description = "Test description",
            dialogActivityFrom = now.minusSeconds(86400),
            dialogActivityTo = now,
            requestedDialogCount = 10,
            dialogsCount = 5,
            totalDialogCount = 100,
            botActionCount = 15,
            allowTestDialogs = false,
            actionRefs =
                listOf(
                    ActionRef("dialog1".toId<Dialog>(), "action1".toId<Action>()),
                    ActionRef("dialog2".toId<Dialog>(), "action2".toId<Action>()),
                ),
            status = status,
            createdBy = USER,
            statusChangedBy = USER,
            creationDate = now,
            statusChangeDate = now,
            lastUpdateDate = now,
        )
    }

    @Test
    fun `should save and retrieve evaluation sample`() {
        val sample = createSample("Test Sample")
        val savedSample = EvaluationSampleMongoDAO.save(sample)

        assertNotNull(savedSample._id)
        assertEquals(sample.name, savedSample.name)

        val foundSample = EvaluationSampleMongoDAO.findById(savedSample._id)
        assertNotNull(foundSample)
        assertEquals(savedSample._id, foundSample._id)
        assertEquals("Test Sample", foundSample.name)
        assertEquals(2, foundSample.actionRefs.size)
    }

    @Test
    fun `should find samples by namespace and botId`() {
        EvaluationSampleMongoDAO.save(createSample("Sample 1"))
        EvaluationSampleMongoDAO.save(createSample("Sample 2"))
        EvaluationSampleMongoDAO.save(createSample("Sample 3", botId = "otherBot"))

        val foundSamples = EvaluationSampleMongoDAO.findByNamespaceAndBotId(NAMESPACE, BOT_ID)
        assertEquals(2, foundSamples.size)
        assertTrue(foundSamples.any { it.name == "Sample 1" })
        assertTrue(foundSamples.any { it.name == "Sample 2" })
    }

    @Test
    fun `should find samples by status`() {
        EvaluationSampleMongoDAO.save(createSample("Sample IN_PROGRESS", status = EvaluationSampleStatus.IN_PROGRESS))
        EvaluationSampleMongoDAO.save(createSample("Sample VALIDATED", status = EvaluationSampleStatus.VALIDATED))
        EvaluationSampleMongoDAO.save(createSample("Sample CANCELLED", status = EvaluationSampleStatus.CANCELLED))

        val inProgressSamples = EvaluationSampleMongoDAO.findByNamespaceAndBotId(NAMESPACE, BOT_ID, EvaluationSampleStatus.IN_PROGRESS)
        assertEquals(1, inProgressSamples.size)
        assertEquals("Sample IN_PROGRESS", inProgressSamples[0].name)

        val validatedSamples = EvaluationSampleMongoDAO.findByNamespaceAndBotId(NAMESPACE, BOT_ID, EvaluationSampleStatus.VALIDATED)
        assertEquals(1, validatedSamples.size)
        assertEquals("Sample VALIDATED", validatedSamples[0].name)
    }

    @Test
    fun `should update sample status`() {
        val sample = createSample("Test Sample")
        val savedSample = EvaluationSampleMongoDAO.save(sample)

        val updatedSample =
            EvaluationSampleMongoDAO.updateStatus(
                savedSample._id,
                EvaluationSampleStatus.VALIDATED,
                "validator",
                "Validation complete",
            )

        assertNotNull(updatedSample)
        assertEquals(EvaluationSampleStatus.VALIDATED, updatedSample.status)
        assertEquals("validator", updatedSample.statusChangedBy)
        assertEquals("Validation complete", updatedSample.statusComment)
    }

    @Test
    fun `should delete sample`() {
        val sample = createSample("Test Sample")
        val savedSample = EvaluationSampleMongoDAO.save(sample)

        val deleted = EvaluationSampleMongoDAO.delete(savedSample._id)
        assertTrue(deleted)

        val foundSample = EvaluationSampleMongoDAO.findById(savedSample._id)
        assertNull(foundSample)
    }

    @Test
    fun `should return null for non-existent id`() {
        val foundSample = EvaluationSampleMongoDAO.findById(newId())
        assertNull(foundSample)
    }

    @Test
    fun `should return empty list for non-existent botId`() {
        EvaluationSampleMongoDAO.save(createSample("Test Sample"))

        val foundSamples = EvaluationSampleMongoDAO.findByNamespaceAndBotId(NAMESPACE, "nonExistentBot")
        assertTrue(foundSamples.isEmpty())
    }

    @Test
    fun `should return samples sorted by creation date descending`() {
        val sample1 = createSample("Sample 1").copy(creationDate = Instant.now().minusSeconds(100))
        val sample2 = createSample("Sample 2").copy(creationDate = Instant.now().minusSeconds(50))
        val sample3 = createSample("Sample 3").copy(creationDate = Instant.now())

        EvaluationSampleMongoDAO.save(sample1)
        EvaluationSampleMongoDAO.save(sample2)
        EvaluationSampleMongoDAO.save(sample3)

        val foundSamples = EvaluationSampleMongoDAO.findByNamespaceAndBotId(NAMESPACE, BOT_ID)
        assertEquals(3, foundSamples.size)
        assertEquals("Sample 3", foundSamples[0].name)
        assertEquals("Sample 2", foundSamples[1].name)
        assertEquals("Sample 1", foundSamples[2].name)
    }
}

/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.nlp.model.service.storage.s3

import ai.tock.aws.s3.S3Bucket
import ai.tock.nlp.core.EntityType
import ai.tock.nlp.core.NlpEngineType
import ai.tock.nlp.core.configuration.NlpApplicationConfiguration
import ai.tock.nlp.model.EntityContextKey
import ai.tock.nlp.model.IntentContext.IntentContextKey
import ai.tock.nlp.model.service.storage.NlpApplicationConfigurationDAO
import ai.tock.shared.changes.ChangeChecks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.InputStream
import java.time.Duration
import java.time.Instant
import java.util.Locale
import java.util.Random
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class S3NlpEngineModelDAOTest {
    private val configuration = NlpApplicationConfiguration()
    private val configurationDAO: NlpApplicationConfigurationDAO = mockk()
    private val bucket = S3Bucket(S3TestServer.BUCKET, "test/models", S3TestServer.client)
    private var scheduled: Duration? = null
    private val dao = S3NlpEngineModelDAO(bucket, Duration.ofSeconds(42), { configurationDAO }, { interval, _ -> scheduled = interval })

    private val intentKey = IntentContextKey("app:test", Locale.FRENCH, NlpEngineType.opennlp)
    private val otherIntentKey = IntentContextKey("app:other", Locale.ENGLISH, NlpEngineType.opennlp)
    private val entityKey = EntityContextKey("app:test", null, Locale.FRENCH, NlpEngineType.opennlp, EntityType("app:city é/ü"), true)

    private fun InputStream.text(): String = use { String(it.readBytes()) }

    @BeforeEach
    fun cleanup() {
        dao.deleteIntentModelsNotIn(emptyList())
        dao.deleteEntityModelsNotIn(emptyList())
        every { configurationDAO.loadLastConfiguration(any(), any(), any()) } returns configuration
    }

    @Test
    fun `a saved model is read with its update date and the configuration of this date`() {
        val before = Instant.now()
        dao.saveIntentModel(intentKey, "model".byteInputStream())

        val model = assertNotNull(dao.getIntentModelInputStream(intentKey))

        assertEquals("model", model.inputStream.text())
        // the millisecond precision of the date is kept, unlike the last modified date of S3
        assertTrue(!model.updatedDate.isBefore(before.truncatedTo(java.time.temporal.ChronoUnit.MILLIS)) && !model.updatedDate.isAfter(Instant.now()))
        assertEquals(model.updatedDate, dao.getIntentModelLastUpdate(intentKey))
        assertEquals(configuration, model.configuration)
        io.mockk.verify { configurationDAO.loadLastConfiguration("app:test", NlpEngineType.opennlp, model.updatedDate) }
    }

    @Test
    fun `the last saved version of a model is returned`() {
        dao.saveIntentModel(intentKey, "previous".byteInputStream())
        dao.saveIntentModel(intentKey, "last".byteInputStream())

        assertEquals("last", dao.getIntentModelInputStream(intentKey)?.inputStream?.text())
    }

    @Test
    fun `a missing model has no stream and no update date`() {
        assertNull(dao.getIntentModelInputStream(intentKey))
        assertNull(dao.getIntentModelLastUpdate(intentKey))
        assertNull(dao.getEntityModelInputStream(entityKey))
        assertNull(dao.getEntityModelLastUpdate(entityKey))
    }

    @Test
    fun `a large model of unknown size is stored`() {
        val bytes = ByteArray(12 * 1024 * 1024).also { Random(1).nextBytes(it) }
        // a stream of unknown size, like the piped stream of the model builders
        dao.saveEntityModel(
            entityKey,
            object : InputStream() {
                private val delegate = bytes.inputStream()

                override fun read(): Int = delegate.read()

                override fun read(
                    b: ByteArray,
                    off: Int,
                    len: Int,
                ): Int = delegate.read(b, off, len)
            },
        )

        assertContentEquals(bytes, dao.getEntityModelInputStream(entityKey)?.inputStream?.use { it.readBytes() })
    }

    @Test
    fun `the intent and entity models of the same application are separated`() {
        val sameIdEntityKey = EntityContextKey("app:test", null, Locale.FRENCH, NlpEngineType.opennlp)
        dao.saveIntentModel(intentKey, "intent".byteInputStream())
        dao.saveEntityModel(sameIdEntityKey, "entity".byteInputStream())

        dao.deleteIntentModel(intentKey)

        assertNull(dao.getIntentModelInputStream(intentKey))
        assertEquals("entity", dao.getEntityModelInputStream(sameIdEntityKey)?.inputStream?.text())
    }

    @Test
    fun `deleteIntentModelsNotIn keeps only the given models`() {
        dao.saveIntentModel(intentKey, "kept".byteInputStream())
        dao.saveIntentModel(otherIntentKey, "removed".byteInputStream())
        dao.saveEntityModel(entityKey, "entity".byteInputStream())

        dao.deleteIntentModelsNotIn(listOf(intentKey))

        assertEquals("kept", dao.getIntentModelInputStream(intentKey)?.inputStream?.text())
        assertNull(dao.getIntentModelInputStream(otherIntentKey))
        assertEquals("entity", dao.getEntityModelInputStream(entityKey)?.inputStream?.text())
    }

    @Test
    fun `the listeners are notified of the models added or updated after their registration`() {
        dao.saveIntentModel(intentKey, "v1".byteInputStream())
        dao.saveEntityModel(entityKey, "v1".byteInputStream())
        val intentChanges = mutableListOf<String>()
        val entityChanges = mutableListOf<String>()

        dao.listenIntentModelChanges { intentChanges += it }
        dao.listenEntityModelChanges { entityChanges += it }
        assertEquals(Duration.ofSeconds(42), scheduled)
        dao.pollChanges()
        assertEquals(emptyList(), intentChanges)
        assertEquals(emptyList(), entityChanges)

        dao.saveIntentModel(intentKey, "v2".byteInputStream())
        dao.saveIntentModel(otherIntentKey, "v1".byteInputStream())
        dao.pollChanges()
        assertEquals(setOf(intentKey.id(), otherIntentKey.id()), intentChanges.toSet())
        assertEquals(emptyList(), entityChanges)

        intentChanges.clear()
        dao.pollChanges()
        assertEquals(emptyList(), intentChanges)

        dao.saveEntityModel(entityKey, "v2".byteInputStream())
        dao.pollChanges()
        assertEquals(listOf(entityKey.id()), entityChanges)
        assertEquals(emptyList(), intentChanges)
    }

    @Test
    fun `GIVEN a process frozen between its requests WHEN a model changes THEN the next request start notifies the listeners`() {
        // the periodic task never runs: only the checks at the start of the requests
        val frozen = S3NlpEngineModelDAO(bucket, Duration.ZERO, { configurationDAO }, { _, _ -> })
        frozen.saveIntentModel(intentKey, "v1".byteInputStream())
        val intentChanges = CopyOnWriteArrayList<String>()
        frozen.listenIntentModelChanges { intentChanges += it }

        frozen.saveIntentModel(intentKey, "v2".byteInputStream())
        try {
            ChangeChecks.runAll()
            assertEquals(listOf(intentKey.id()), intentChanges)
        } finally {
            ChangeChecks.unregister("S3 NLP models")
        }
    }

    @Test
    fun `the keys are stored under the prefix`() {
        dao.saveIntentModel(intentKey, "model".byteInputStream())

        assertEquals(listOf("test/models/intent/${intentKey.id()}"), bucket.list().map { it.key() }.toList())
    }
}

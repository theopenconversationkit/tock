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

package ai.tock.nlp.model.service.storage.mongo

import ai.tock.nlp.core.NlpEngineType
import ai.tock.nlp.model.IntentContext.IntentContextKey
import ai.tock.shared.getAsyncDatabase
import ai.tock.shared.getDatabase
import ai.tock.shared.sharedTestModule
import ai.tock.shared.tockInternalInjector
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import com.mongodb.client.MongoDatabase
import com.mongodb.client.gridfs.GridFSBuckets
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Locale
import java.util.UUID
import kotlin.test.assertEquals

internal class NlpEngineModelMongoDAOTest {
    private val key = IntentContextKey("app:test-${UUID.randomUUID()}", Locale.ENGLISH, NlpEngineType.opennlp)

    @BeforeEach
    fun before() {
        tockInternalInjector = KodeinInjector()
        tockInternalInjector.inject(
            Kodein {
                import(sharedTestModule)
                bind<MongoDatabase>(MONGO_DATABASE) with provider { getDatabase(MONGO_DATABASE) }
                bind<com.mongodb.reactivestreams.client.MongoDatabase>(MONGO_DATABASE) with
                    provider { getAsyncDatabase(MONGO_DATABASE) }
            },
        )
    }

    @AfterEach
    fun cleanup() {
        NlpEngineModelMongoDAO.deleteIntentModelsNotIn(emptyList())
    }

    @Test
    fun `getIntentModelInputStream returns the last uploaded model while the previous version is not removed yet`() {
        // saveModel uploads the new version, then removes the previous ones:
        // in between, the change stream listeners already reload the model
        val bucket = GridFSBuckets.create(MongoModelConfiguration.database, "fs_intent")
        bucket.uploadFromStream(key.id(), "previous".byteInputStream())
        Thread.sleep(10)
        bucket.uploadFromStream(key.id(), "last".byteInputStream())

        val model = NlpEngineModelMongoDAO.getIntentModelInputStream(key)

        assertEquals("last", model?.inputStream?.use { String(it.readBytes()) })
    }

    @Test
    fun `saveIntentModel keeps only the last version`() {
        NlpEngineModelMongoDAO.saveIntentModel(key, "previous".byteInputStream())
        NlpEngineModelMongoDAO.saveIntentModel(key, "last".byteInputStream())

        val model = NlpEngineModelMongoDAO.getIntentModelInputStream(key)

        assertEquals("last", model?.inputStream?.use { String(it.readBytes()) })
    }
}

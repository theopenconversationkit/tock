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

package ai.tock.nlp.front.storage.dynamodb

import ai.tock.nlp.core.DictionaryData
import ai.tock.nlp.core.Intent.Companion.UNKNOWN_INTENT_NAME
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.Classification
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus.inbox
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus.model
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus.validated
import ai.tock.nlp.front.shared.config.EntityTypeDefinition
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.nlp.front.shared.config.SentencesQuery
import ai.tock.nlp.front.shared.monitoring.ParseRequestLog
import ai.tock.nlp.front.shared.parser.ParseQuery
import ai.tock.nlp.front.shared.parser.QueryContext
import ai.tock.storage.dynamodb.DOC
import ai.tock.storage.dynamodb.DocumentCodec
import ai.tock.storage.dynamodb.DynamoStore
import ai.tock.storage.dynamodb.bytes
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.litote.kmongo.newId
import org.litote.kmongo.toId
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.time.Instant
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/**
 * The runtime DAOs of the NLP on DynamoDB Local, on a classpath without MongoDB, Vert.x nor tock-shared.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NlpDynamoDAOsTest {
    private val dynamo = GenericContainer("amazon/dynamodb-local:2.6.1").withExposedPorts(8000).waitingFor(Wait.forListeningPort())

    @BeforeAll
    fun start() {
        dynamo.start()
        System.setProperty("tock_dynamodb_endpoint", "http://${dynamo.host}:${dynamo.getMappedPort(8000)}")
        System.setProperty("aws.accessKeyId", "x")
        System.setProperty("aws.secretAccessKey", "x")
    }

    private fun application(normalizeText: Boolean = false) =
        ApplicationDefinition("app_${newId<ApplicationDefinition>()}", namespace = "ns", supportedLocales = setOf(Locale.FRENCH), normalizeText = normalizeText)

    private fun sentence(
        app: ApplicationDefinition,
        text: String,
        status: ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus,
        intent: String = "greetings",
    ) = ClassifiedSentence(
        text = text,
        language = Locale.FRENCH,
        applicationId = app._id,
        creationDate = Instant.now(),
        updateDate = Instant.now(),
        status = status,
        classification = Classification(intent.toId(), emptyList()),
        lastIntentProbability = 1.0,
        lastEntityProbability = 1.0,
    )

    private val runtime = DynamoApplicationRuntimeConfiguration

    @Test
    fun `the storage runs without MongoDB, Vert_x nor tock-shared`() {
        assertFailsWith<ClassNotFoundException> { Class.forName("com.mongodb.client.MongoClient") }
        assertFailsWith<ClassNotFoundException> { Class.forName("io.vertx.core.Vertx") }
        assertFailsWith<ClassNotFoundException> { Class.forName("ai.tock.shared.vertx.WebVerticle") }
    }

    @Test
    fun `GIVEN published applications and intents WHEN the runtime reads them THEN it finds them by name and qualified name`() {
        val app = application()
        val other = application()
        ApplicationDefinitionDynamoDAO.documents.save(app)
        ApplicationDefinitionDynamoDAO.documents.save(other)
        val intent = IntentDefinition("greetings_${app.name}", "ns", setOf(app._id), emptySet())
        IntentDefinitionDynamoDAO.documents.save(intent)
        IntentDefinitionDynamoDAO.documents.save(IntentDefinition("other_${app.name}", "ns", setOf(other._id), emptySet()))

        assertEquals(app, runtime.getApplicationByNamespaceAndName("ns", app.name))
        assertEquals(listOf(intent), runtime.getIntentsByApplicationId(app._id))
        assertEquals(intent, runtime.getIntentById(intent._id))
        assertEquals(intent._id, runtime.getIntentIdByQualifiedName("ns:${intent.name}"))
        assertNull(runtime.getIntentIdByQualifiedName("other:${intent.name}"))
        assertEquals(UNKNOWN_INTENT_NAME, runtime.getIntentIdByQualifiedName(UNKNOWN_INTENT_NAME).toString())
    }

    @Test
    fun `GIVEN entity types and dictionaries WHEN they are saved THEN they are read back`() {
        val entityType = EntityTypeDefinition("ns:city_${newId<Any>()}", "a city")
        EntityTypeDefinitionDynamoDAO.save(entityType)
        val dictionary = DictionaryData("ns", "city", onlyValues = true)
        EntityTypeDefinitionDynamoDAO.save(dictionary)

        assertEquals(entityType, EntityTypeDefinitionDynamoDAO.getEntityTypes().single { it.name == entityType.name })
        assertEquals(dictionary, EntityTypeDefinitionDynamoDAO.getAllDictionaryData().single { it.entityName == "city" })
    }

    private fun exact(
        app: ApplicationDefinition,
        text: String,
    ) = runtime
        .search(
            SentencesQuery(app._id, Locale.FRENCH, search = text, status = setOf(validated, model), onlyExactMatch = true, normalizeText = app.normalizeText),
        ).sentences
        .firstOrNull()

    @Test
    fun `GIVEN validated and inbox sentences WHEN the parser searches a text THEN only the validated ones are found`() {
        val app = application()
        val validatedSentence = sentence(app, "Bonjour !", validated)
        runtime.save(validatedSentence, "admin")
        runtime.save(sentence(app, "salut", inbox), null)

        assertEquals(validatedSentence.copy(qualifier = "admin"), exact(app, "Bonjour !"))
        assertNull(exact(app, "bonjour"), "the text is not normalized by this application")
        assertNull(exact(app, "salut"), "an inbox sentence is not a validated one")
    }

    @Test
    fun `GIVEN an application that normalizes the texts WHEN the parser searches a text THEN its normalized form is found`() {
        val app = application(normalizeText = true)
        runtime.save(sentence(app, "Bonjour !", validated), null)

        assertEquals("Bonjour !", exact(app, "bonjour")?.text)
        assertEquals("Bonjour !", exact(app, "BONJOUR")?.text)
    }

    @Test
    fun `GIVEN the search of the Studio WHEN it runs on DynamoDB THEN it is refused`() {
        assertFailsWith<UnsupportedOperationException> { runtime.search(SentencesQuery(application()._id, Locale.FRENCH, search = "bonjour")) }
    }

    @Test
    fun `GIVEN unknown sentences WHEN they are counted THEN only the existing sentences are counted`() {
        val app = application()
        runtime.save(sentence(app, "quoi ?", inbox, intent = "unknown"), null)
        repeat(3) { ClassifiedSentenceDynamoDAO.incrementUnknownStat(app._id, Locale.FRENCH, "quoi ?") }
        ClassifiedSentenceDynamoDAO.incrementUnknownStat(app._id, Locale.FRENCH, "absent")

        val item = DynamoStore.getItem(DynamoStore.sentenceTable, "SENT#${app._id}#fr", "quoi ?")
        assertEquals("3", item?.get(ClassifiedSentenceDynamoDAO.UNKNOWN_COUNT)?.n())
        assertNull(DynamoStore.getItem(DynamoStore.sentenceTable, "SENT#${app._id}#fr", "absent"))
    }

    @Test
    fun `GIVEN a parse request WHEN it is logged THEN the log is stored`() {
        val app = application()
        val log = ParseRequestLog(app._id, ParseQuery(listOf("bonjour"), "ns", app.name, QueryContext(Locale.FRENCH)), null, 12)
        ParseRequestLogDynamoDAO.save(log)

        val stored = DynamoStore.queryPartition(DynamoStore.runtimeTable, "PARSELOG#${app._id}", consistentRead = true)
        assertEquals(listOf(log), stored.map { DocumentCodec.decode<ParseRequestLog>(it.bytes(DOC)) })
    }
}

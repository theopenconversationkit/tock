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

package ai.tock.serverless.nlp

import ai.tock.duckling.client.ducklingModule
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.Classification
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.nlp.front.storage.dynamodb.ApplicationDefinitionDynamoDAO
import ai.tock.nlp.front.storage.dynamodb.ClassifiedSentenceDynamoDAO
import ai.tock.nlp.front.storage.dynamodb.EntityTypeDefinitionDynamoDAO
import ai.tock.nlp.front.storage.dynamodb.IntentDefinitionDynamoDAO
import ai.tock.nlp.front.storage.dynamodb.nlpDynamoModule
import ai.tock.serverless.ServerlessHttpServer
import ai.tock.shared.jackson.mapper
import ai.tock.storage.dynamodb.DynamoChanges
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.time.Instant
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Runs the NLP API in a serverless process, on DynamoDB Local, without Vert.x, MongoDB nor tock-shared on the
 * classpath: the parser answers from the configuration and the validated sentences published by the admin.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServerlessNlpTest {
    private val dynamo = GenericContainer("amazon/dynamodb-local:2.6.1").withExposedPorts(8000).waitingFor(Wait.forListeningPort())
    private lateinit var server: ServerlessHttpServer
    private val client = HttpClient.newHttpClient()

    private val greetings = IntentDefinition("greetings", "app", emptySet(), emptySet())
    private val application = ApplicationDefinition("bot", namespace = "app", supportedLocales = setOf(Locale.FRENCH), intents = setOf(greetings._id))

    private fun validated(
        text: String,
        intent: IntentDefinition,
    ) = ClassifiedSentence(
        text = text,
        language = Locale.FRENCH,
        applicationId = application._id,
        creationDate = Instant.now(),
        updateDate = Instant.now(),
        status = ClassifiedSentenceStatus.validated,
        classification = Classification(intent._id, emptyList()),
        lastIntentProbability = 1.0,
        lastEntityProbability = 1.0,
    )

    @BeforeAll
    fun start() {
        // the publications of the admin are checked at each request
        System.setProperty("tock_dynamodb_change_check_interval_ms", "0")
        dynamo.start()
        System.setProperty("tock_dynamodb_endpoint", "http://${dynamo.host}:${dynamo.getMappedPort(8000)}")
        System.setProperty("aws.accessKeyId", "x")
        System.setProperty("aws.secretAccessKey", "x")
        // the configuration published by the admin
        IntentDefinitionDynamoDAO.documents.save(greetings.copy(applications = setOf(application._id)))
        ApplicationDefinitionDynamoDAO.documents.save(application)
        ClassifiedSentenceDynamoDAO.save(validated("bonjour", greetings))

        // the built-in entities of Duckling: its client loads without Vert.x (the server is not called for a validated sentence without entity)
        ServerlessNlpLauncher.setup(nlpDynamoModule, listOf(ducklingModule))
        server = ServerlessNlpLauncher.start(port = 0)
    }

    @AfterAll
    fun stop() {
        server.stop()
        dynamo.stop()
    }

    private fun parseResponse(text: String) =
        client.send(
            HttpRequest
                .newBuilder(URI("http://localhost:${server.port}/rest/nlp/parse"))
                .POST(
                    HttpRequest.BodyPublishers.ofString(
                        mapper.writeValueAsString(
                            mapOf("queries" to listOf(text), "namespace" to "app", "applicationName" to "bot", "context" to mapOf("language" to "fr")),
                        ),
                    ),
                ).build(),
            BodyHandlers.ofString(),
        )

    private fun parse(text: String): Map<String, Any?> {
        val response = parseResponse(text)
        assertEquals(200, response.statusCode(), response.body())
        return mapper.readValue(response.body())
    }

    @Test
    fun `the process runs without Vert_x, MongoDB nor tock-shared`() {
        assertFailsWith<ClassNotFoundException> { Class.forName("io.vertx.core.Vertx") }
        assertFailsWith<ClassNotFoundException> { Class.forName("com.mongodb.client.MongoClient") }
        assertFailsWith<ClassNotFoundException> { Class.forName("ai.tock.shared.vertx.WebVerticle") }
    }

    @Test
    fun `GIVEN the Duckling client WHEN the configuration is loaded THEN its built-in entity types are known`() {
        assertTrue(EntityTypeDefinitionDynamoDAO.getEntityTypes().any { it.name == "duckling:datetime" }, EntityTypeDefinitionDynamoDAO.getEntityTypes().toString())
    }

    @Test
    fun `GIVEN a validated sentence WHEN it is parsed THEN the intent of the sentence is answered`() {
        assertEquals("greetings", parse("bonjour")["intent"])
    }

    @Test
    fun `GIVEN an application deleted by the admin WHEN a sentence is parsed THEN the next request does not know it anymore`() {
        assertEquals(200, parseResponse("bonjour").statusCode())
        ApplicationDefinitionDynamoDAO.documents.delete(application._id.toString())
        DynamoChanges.changed(listOf(DynamoChanges.NLP_APPLICATION))
        try {
            // the parser reads the applications from its cache: without the notification, it would still know it
            assertEquals(400, parseResponse("bonjour").statusCode())
        } finally {
            ApplicationDefinitionDynamoDAO.documents.save(application)
            DynamoChanges.changed(listOf(DynamoChanges.NLP_APPLICATION))
        }
    }
}

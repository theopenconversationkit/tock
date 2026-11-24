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

package ai.tock.serverless

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.connector.whatsapp.cloud.whatsAppCloudConnectorType
import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.definition.BotProvider
import ai.tock.bot.definition.StoryDefinition
import ai.tock.bot.definition.bot
import ai.tock.bot.definition.story
import ai.tock.bot.engine.BotRepository
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.dialog.ArchivedEntityValue
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.EntityStateValue
import ai.tock.bot.engine.dialog.Snapshot
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.UserTimeline
import ai.tock.nlp.api.client.NlpClient
import ai.tock.nlp.api.client.model.NlpResult
import ai.tock.serverless.queue.BackgroundRequestQueue
import ai.tock.serverless.queue.RequestQueueing
import ai.tock.storage.dynamodb.BotApplicationConfigurationDynamoDAO
import ai.tock.storage.dynamodb.DynamoChanges
import ai.tock.storage.dynamodb.I18nDynamoDAO
import ai.tock.storage.dynamodb.UserTimelineDynamoDAO
import ai.tock.storage.dynamodb.botDynamoModule
import ai.tock.translator.Translator
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import com.sun.net.httpserver.HttpServer
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.net.InetSocketAddress
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Runs a WhatsApp Cloud bot in a serverless process, from the webhook to the answer sent to the Graph API,
 * without Vert.x, MongoDB, tock-shared nor tock-bot-engine on the classpath.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServerlessWhatsAppBotTest {
    /** The requests received by the test double of the Graph API. */
    private val graphApiRequests = CopyOnWriteArrayList<String>()
    private lateinit var graphApi: HttpServer
    private lateinit var server: ServerlessHttpServer

    private val botDefinition: BotDefinition =
        bot(
            "slice",
            stories = listOf<StoryDefinition>(story("greetings") { end("world") }),
            namespace = "app",
        )

    private val configuration =
        BotApplicationConfiguration(
            applicationId = "whatsapp",
            botId = "slice",
            namespace = "app",
            nlpModel = "slice",
            connectorType = whatsAppCloudConnectorType,
            path = "/whatsapp",
            parameters =
                mapOf(
                    "whatsAppPhoneNumberId" to "phone",
                    "whatsAppBusinessAccountId" to "account",
                    "token" to "token",
                    "verifyToken" to "verify",
                ),
        )

    private val storage =
        Kodein.Module(allowSilentOverride = true) {
            // everything is stored in DynamoDB (DynamoDB Local): only the NLP is mocked
            import(botDynamoModule, allowOverride = true)
            bind<NlpClient>() with
                provider {
                    mockk<NlpClient>(relaxed = true).apply {
                        every { parse(any()) } returns
                            NlpResult(intent = "greetings", intentNamespace = "app", language = Locale.FRENCH, retainedQuery = "hello")
                    }
                }
        }

    private val dynamo = GenericContainer("amazon/dynamodb-local:2.6.1").withExposedPorts(8000).waitingFor(Wait.forListeningPort())

    @BeforeAll
    fun start() {
        // the publications of the admin are checked at each request
        System.setProperty("tock_dynamodb_change_check_interval_ms", "0")
        dynamo.start()
        System.setProperty("tock_dynamodb_endpoint", "http://${dynamo.host}:${dynamo.getMappedPort(8000)}")
        System.setProperty("aws.accessKeyId", "x")
        System.setProperty("aws.secretAccessKey", "x")
        BotApplicationConfigurationDynamoDAO.save(configuration)
        // the labels of the answers are read and created in DynamoDB
        Translator.enabled = true

        graphApi =
            HttpServer.create(InetSocketAddress(0), 0).apply {
                createContext("/") { exchange ->
                    val body = exchange.requestBody.use { String(it.readAllBytes()) }
                    if (exchange.requestURI.path.endsWith("/messages")) {
                        // a slow Graph API: the webhook must still wait for the answer to be sent
                        Thread.sleep(500)
                    }
                    graphApiRequests += "${exchange.requestMethod} ${exchange.requestURI.path} $body"
                    val answer = """{"messaging_product":"whatsapp","contacts":[],"messages":[{"id":"wamid.out"}],"success":true}"""
                    exchange.responseHeaders.add("Content-Type", "application/json")
                    exchange.sendResponseHeaders(200, answer.length.toLong())
                    exchange.responseBody.use { it.write(answer.toByteArray()) }
                }
                start()
            }
        System.setProperty("tock_whatsappcloud_api_base_url", "http://localhost:${graphApi.address.port}")

        ServerlessBotLauncher.setup(storage)
        BotRepository.registerBotProvider(
            object : BotProvider {
                override fun botDefinition(): BotDefinition = botDefinition
            },
        )
        server = ServerlessBotLauncher.start(port = 0)
    }

    @AfterAll
    fun stop() {
        server.stop()
        graphApi.stop(0)
        dynamo.stop()
        Translator.enabled = false
    }

    private val client = HttpClient.newHttpClient()

    @Test
    fun `the process runs without Vert_x, tock-shared nor tock-bot-engine`() {
        assertFailsWith<ClassNotFoundException> { Class.forName("io.vertx.core.Vertx") }
        assertFailsWith<ClassNotFoundException> { Class.forName("ai.tock.shared.vertx.WebVerticle") }
        assertFailsWith<ClassNotFoundException> { Class.forName("ai.tock.bot.engine.VertxBotServer") }
    }

    @Test
    fun `GIVEN the WhatsApp Cloud connector WHEN Meta verifies the webhook THEN the challenge is answered`() {
        val response =
            client.send(
                HttpRequest
                    .newBuilder(URI("http://localhost:${server.port}/whatsapp?hub.mode=subscribe&hub.verify_token=verify&hub.challenge=1234"))
                    .build(),
                BodyHandlers.ofString(),
            )

        assertEquals(200, response.statusCode())
        assertEquals("1234", response.body())
    }

    @Test
    fun `GIVEN a message WHEN it is posted to the webhook THEN the answer is sent to the Graph API before the webhook answers`() {
        val webhook =
            """
            {"object":"whatsapp_business_account","entry":[{"id":"account","changes":[{"field":"messages","value":{
            "messaging_product":"whatsapp","metadata":{"display_phone_number":"33100000000","phone_number_id":"phone"},
            "contacts":[{"profile":{"name":"Julien"},"wa_id":"33600000000","user_id":"bsuid1"}],
            "messages":[{"from":"33600000000","from_user_id":"bsuid1","id":"wamid.in","timestamp":"1700000000","type":"text","text":{"body":"hello"}}]}}]}]}
            """.trimIndent()

        val response =
            client.send(
                HttpRequest
                    .newBuilder(URI("http://localhost:${server.port}/whatsapp"))
                    .POST(HttpRequest.BodyPublishers.ofString(webhook))
                    .build(),
                BodyHandlers.ofString(),
            )

        assertEquals(200, response.statusCode())
        // the webhook answered after the work it started: the answer is already sent
        assertTrue(graphApiRequests.any { it.contains("/phone/messages") && it.contains("world") }, graphApiRequests.joinToString("\n"))
        // the dialog is stored in DynamoDB
        val timeline = runBlocking { UserTimelineDynamoDAO.loadWithLastValidDialog("app", PlayerId("bsuid1")) { botDefinition.stories.first() } }
        assertEquals(listOf("hello", "world"), timeline.currentDialog?.allActions()?.map { (it as? SendSentence)?.stringText })
        // the label of the answer is created in DynamoDB
        assertTrue(I18nDynamoDAO.getLabels("app").any { it.defaultLabel == "world" }, I18nDynamoDAO.getLabels("app").toString())
    }

    @Test
    fun `GIVEN queued webhooks WHEN a message is posted twice THEN the webhook is acknowledged at once and the answer is sent once`() {
        val queueing =
            RequestQueueing(
                queue = BackgroundRequestQueue { ServerlessRequestHandler.queueing!! },
                queuedPaths = setOf("/whatsapp"),
                secret = "the secret of the test queue",
            )
        ServerlessRequestHandler.configure(queueing)
        graphApiRequests.clear()
        try {
            val webhook =
                """
                {"object":"whatsapp_business_account","entry":[{"id":"account","changes":[{"field":"messages","value":{
                "messaging_product":"whatsapp","metadata":{"display_phone_number":"33100000000","phone_number_id":"phone"},
                "contacts":[{"profile":{"name":"Julien"},"wa_id":"33600000000","user_id":"bsuid1"}],
                "messages":[{"from":"33600000000","from_user_id":"bsuid1","id":"wamid.queued","timestamp":"1700000001","type":"text","text":{"body":"hello"}}]}}]}]}
                """.trimIndent()
            val post = { HttpRequest.newBuilder(URI("http://localhost:${server.port}/whatsapp")).POST(HttpRequest.BodyPublishers.ofString(webhook)).build() }

            val start = System.currentTimeMillis()
            val response = client.send(post(), BodyHandlers.ofString())
            val elapsed = System.currentTimeMillis() - start
            // Meta retries a webhook that is not acknowledged quickly
            client.send(post(), BodyHandlers.ofString())

            assertEquals(200, response.statusCode())
            assertTrue(elapsed < 500, "acknowledged in $elapsed ms")
            assertTrue(graphApiRequests.none { it.contains("world") }, "the answer is sent after the acknowledgment")

            val deadline = System.currentTimeMillis() + 10_000
            while (graphApiRequests.none { it.contains("world") } && System.currentTimeMillis() < deadline) {
                Thread.sleep(50)
            }
            // let a duplicate processing, if any, send its answer
            Thread.sleep(1_500)
            assertEquals(1, graphApiRequests.count { it.contains("/phone/messages") && it.contains("world") }, graphApiRequests.joinToString("\n"))
        } finally {
            ServerlessRequestHandler.configure(null)
        }
    }

    @Test
    fun `GIVEN a label in the cache of the translator WHEN the admin publishes a new text THEN the next answer has the new text`() {
        fun post(messageId: String) =
            client.send(
                HttpRequest
                    .newBuilder(URI("http://localhost:${server.port}/whatsapp"))
                    .POST(
                        HttpRequest.BodyPublishers.ofString(
                            """
                            {"object":"whatsapp_business_account","entry":[{"id":"account","changes":[{"field":"messages","value":{
                            "messaging_product":"whatsapp","metadata":{"display_phone_number":"33100000000","phone_number_id":"phone"},
                            "contacts":[{"profile":{"name":"Julien"},"wa_id":"33600000001","user_id":"bsuid2"}],
                            "messages":[{"from":"33600000001","from_user_id":"bsuid2","id":"$messageId","timestamp":"1700000002","type":"text","text":{"body":"hello"}}]}}]}]}
                            """.trimIndent(),
                        ),
                    ).build(),
                BodyHandlers.ofString(),
            )
        // the first answer creates the label; a new instance would load it in the cache of the translator at startup
        post("wamid.label1")
        Translator.initTranslator()
        val label = I18nDynamoDAO.getLabels("app").first { it.defaultLabel == "world" }

        // the admin publishes a new text
        I18nDynamoDAO.publish(
            label.copy(
                defaultLabel = "le monde",
                i18n = label.i18n.map { it.copy(label = "le monde", validated = true) }.toCollection(LinkedHashSet()),
            ),
        )
        DynamoChanges.changed(listOf(DynamoChanges.LABEL))
        graphApiRequests.clear()
        try {
            post("wamid.label2")

            assertTrue(graphApiRequests.any { it.contains("/phone/messages") && it.contains("le monde") }, graphApiRequests.joinToString("\n"))
        } finally {
            // the other tests expect the first text
            I18nDynamoDAO.publish(label)
            DynamoChanges.changed(listOf(DynamoChanges.LABEL))
        }
    }
}

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

package ai.tock.serverless.sqs

import ai.tock.serverless.queue.QueuedRequest
import ai.tock.serverless.queue.RequestQueueing
import ai.tock.shared.http.HttpMethod
import ai.tock.shared.http.HttpRequest
import ai.tock.shared.http.HttpResponse
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.QueueAttributeName
import java.net.URI
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SqsRequestQueueTest {
    private val elasticMq = GenericContainer("softwaremill/elasticmq-native:1.6.11").withExposedPorts(9324).waitingFor(Wait.forListeningPort())
    private lateinit var client: SqsClient
    private val processed = CopyOnWriteArrayList<HttpRequest>()

    // a consumer per test: the deduplication of one test must not see the webhooks of the other
    private var queueing = newQueueing()

    private fun newQueueing() =
        RequestQueueing(
            queue = { error("not used") },
            queuedPaths = setOf("/whatsapp"),
            secret = "the secret of the test queue",
            process = {
                processed += it
                HttpResponse.status()
            },
        )

    @BeforeAll
    fun start() {
        elasticMq.start()
        client =
            SqsClient
                .builder()
                .httpClient(UrlConnectionHttpClient.create())
                .endpointOverride(URI("http://${elasticMq.host}:${elasticMq.getMappedPort(9324)}"))
                .region(Region.EU_WEST_3)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("x", "x")))
                .build()
    }

    @AfterAll
    fun stop() {
        elasticMq.stop()
    }

    private fun webhook(body: String) = HttpRequest(HttpMethod.POST, "/whatsapp", body = body.toByteArray(), remoteAddress = "10.0.0.1")

    private fun roundTrip(queueUrl: String): List<String> {
        val queue = SqsRequestQueue(queueUrl, { queueing }, client)
        queue.send(QueuedRequest.of(webhook("""{"entry":[1]}""")))
        queue.send(QueuedRequest.of(webhook("""{"entry":[1]}""")))
        queue.send(QueuedRequest.of(webhook("""{"entry":[2]}""")))
        val bodies = mutableListOf<String>()
        repeat(5) {
            client.receiveMessage { it.queueUrl(queueUrl).maxNumberOfMessages(10).waitTimeSeconds(1) }.messages().forEach { message ->
                bodies += message.body()
                client.deleteMessage { it.queueUrl(queueUrl).receiptHandle(message.receiptHandle()) }
            }
        }
        return bodies
    }

    @Test
    fun `GIVEN a standard queue WHEN webhooks are sent THEN the consumer processes each webhook once`() {
        processed.clear()
        queueing = newQueueing()
        val queueUrl = client.createQueue { it.queueName("webhooks") }.queueUrl()

        val bodies = roundTrip(queueUrl)
        bodies.forEach { queueing.processMessage(it) }

        assertEquals(3, bodies.size)
        assertEquals(listOf("""{"entry":[1]}""", """{"entry":[2]}"""), processed.map { it.bodyAsString }.sorted())
    }

    @Test
    fun `GIVEN a FIFO queue WHEN a webhook is sent twice THEN SQS drops the duplicate and keeps the order`() {
        processed.clear()
        queueing = newQueueing()
        val queueUrl =
            client
                .createQueue {
                    it.queueName("webhooks.fifo").attributes(mapOf(QueueAttributeName.FIFO_QUEUE to "true"))
                }.queueUrl()

        val bodies = roundTrip(queueUrl)
        bodies.forEach { queueing.processMessage(it) }

        assertEquals(2, bodies.size)
        assertEquals(listOf("""{"entry":[1]}""", """{"entry":[2]}"""), processed.map { it.bodyAsString })
    }
}

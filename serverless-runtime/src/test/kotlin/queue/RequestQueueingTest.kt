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

package ai.tock.serverless.queue

import ai.tock.shared.http.HttpMethod
import ai.tock.shared.http.HttpRequest
import ai.tock.shared.http.HttpResponse
import ai.tock.shared.jackson.mapper
import java.util.Base64
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RequestQueueingTest {
    private val sent = CopyOnWriteArrayList<QueuedRequest>()
    private val processed = CopyOnWriteArrayList<HttpRequest>()
    private var status = 200
    private val queueing =
        RequestQueueing(
            queue = { sent += it },
            queuedPaths = setOf("/whatsapp"),
            secret = "a secret of the queue",
            process = {
                processed += it
                HttpResponse.status(status)
            },
        )

    private fun webhook(body: String = """{"entry":[]}""") =
        HttpRequest(
            HttpMethod.POST,
            "/whatsapp",
            headers = mapOf("X-Hub-Signature-256" to listOf("sha256=abc")),
            body = body.toByteArray(),
            remoteAddress = "10.0.0.1",
        )

    @Test
    fun `GIVEN a webhook of a queued path WHEN it is received THEN it is queued and acknowledged`() {
        assertTrue(queueing.isQueued(webhook()))
        assertFalse(queueing.isQueued(HttpRequest(HttpMethod.GET, "/whatsapp")))
        assertFalse(queueing.isQueued(HttpRequest(HttpMethod.POST, "/messenger")))

        assertEquals(200, queueing.acknowledge(webhook()).status)
        assertEquals(1, sent.size)
        assertEquals(0, processed.size)
    }

    @Test
    fun `GIVEN a queue that fails WHEN a webhook is received THEN the answer is a 500, so that the sender retries`() {
        val failing = RequestQueueing({ error("unavailable") }, setOf("/whatsapp"), "a secret of the queue")

        assertEquals(500, failing.acknowledge(webhook()).status)
    }

    @Test
    fun `GIVEN a queued message WHEN it is delivered twice THEN the request is replayed once, as it was received`() {
        val message = queueing.message(QueuedRequest.of(webhook()))

        assertTrue(queueing.processMessage(message))
        assertTrue(queueing.processMessage(message))

        val replayed = processed.single()
        assertEquals("""{"entry":[]}""", replayed.bodyAsString)
        assertEquals("sha256=abc", replayed.header("x-hub-signature-256"))
        assertEquals("10.0.0.1", replayed.remoteAddress)
    }

    @Test
    fun `GIVEN the same webhook sent twice by its sender WHEN both are delivered THEN it is replayed once`() {
        assertTrue(queueing.processMessage(queueing.message(QueuedRequest.of(webhook()))))
        assertTrue(queueing.processMessage(queueing.message(QueuedRequest.of(webhook()))))

        assertEquals(1, processed.size)
    }

    @Test
    fun `GIVEN a forged or tampered message WHEN it is delivered THEN it is ignored`() {
        val forged = RequestQueueing({}, setOf("/whatsapp"), "another secret of the queue").message(QueuedRequest.of(webhook()))
        val genuine = SignedQueuedRequest.fromJson(queueing.message(QueuedRequest.of(webhook())))
        val tampered = genuine.copy(request = genuine.request.replace("10.0.0.1", "10.0.0.2")).toJson()

        assertTrue(queueing.processMessage(forged))
        assertTrue(queueing.processMessage(tampered))
        assertTrue(queueing.processMessage("not json"))
        assertEquals(0, processed.size)
    }

    @Test
    fun `GIVEN a replay that fails WHEN the message is delivered again THEN it is replayed again`() {
        val message = queueing.message(QueuedRequest.of(webhook()))
        status = 503

        assertFalse(queueing.processMessage(message))

        status = 200
        assertTrue(queueing.processMessage(message))
        assertEquals(2, processed.size)
    }

    @Test
    fun `GIVEN an SQS event WHEN it is processed THEN the failed messages are reported`() {
        val ok = queueing.message(QueuedRequest.of(webhook("""{"entry":[1]}""")))
        val event =
            mapper.writeValueAsString(
                mapOf("Records" to listOf(mapOf("messageId" to "m1", "body" to ok, "eventSource" to "aws:sqs"))),
            )
        assertEquals("""{"batchItemFailures":[]}""", String(queueing.processSqsEvent(HttpRequest(HttpMethod.POST, "/_queue/sqs", body = event.toByteArray())).body))

        status = 500
        val failing = queueing.message(QueuedRequest.of(webhook("""{"entry":[2]}""")))
        val failingEvent = mapper.writeValueAsString(mapOf("Records" to listOf(mapOf("messageId" to "m2", "body" to failing))))
        assertEquals(
            """{"batchItemFailures":[{"itemIdentifier":"m2"}]}""",
            String(queueing.processSqsEvent(HttpRequest(HttpMethod.POST, "/_queue/sqs", body = failingEvent.toByteArray())).body),
        )
    }

    @Test
    fun `GIVEN a Pub_Sub push WHEN it is processed THEN 2xx acknowledges it and 5xx lets Pub_Sub retry`() {
        fun push(body: String) =
            HttpRequest(
                HttpMethod.POST,
                "/_queue/pubsub",
                body =
                    mapper.writeValueAsBytes(
                        mapOf(
                            "message" to mapOf("data" to Base64.getEncoder().encodeToString(queueing.message(QueuedRequest.of(webhook(body))).toByteArray()), "messageId" to "1"),
                            "subscription" to "projects/p/subscriptions/s",
                        ),
                    ),
            )

        assertEquals(204, queueing.processPubSubPush(push("""{"entry":[3]}""")).status)
        status = 500
        assertEquals(503, queueing.processPubSubPush(push("""{"entry":[4]}""")).status)
    }

    @Test
    fun `GIVEN a group per sender WHEN webhooks are queued THEN their group is the sender`() {
        val grouped =
            RequestQueueing({ sent += it }, setOf("/whatsapp"), "a secret of the queue", groupId = { it.header("X-Sender") ?: it.path })

        grouped.acknowledge(webhook().let { HttpRequest(it.method, it.path, mapOf("X-Sender" to listOf("user1")), body = it.body) })
        grouped.acknowledge(webhook())

        assertEquals(listOf("user1", "/whatsapp"), sent.map { it.groupId })
    }
}

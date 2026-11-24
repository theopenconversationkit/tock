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

package ai.tock.serverless.dynamodb

import ai.tock.serverless.queue.QueuedRequest
import ai.tock.serverless.queue.RequestQueueing
import ai.tock.shared.http.HttpMethod
import ai.tock.shared.http.HttpRequest
import ai.tock.shared.http.HttpResponse
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.time.Duration
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DynamoDeduplicationStoreTest {
    private val dynamo = GenericContainer("amazon/dynamodb-local:2.6.1").withExposedPorts(8000).waitingFor(Wait.forListeningPort())
    private val store by lazy { DynamoDeduplicationStore() }

    @BeforeAll
    fun start() {
        dynamo.start()
        System.setProperty("tock_dynamodb_endpoint", "http://${dynamo.host}:${dynamo.getMappedPort(8000)}")
        System.setProperty("aws.accessKeyId", "x")
        System.setProperty("aws.secretAccessKey", "x")
        System.setProperty("tock_dynamodb_table_prefix", "test_")
    }

    private fun newKey() = UUID.randomUUID().toString()

    @Test
    fun `GIVEN a key WHEN it is marked twice THEN the second mark fails until it is unmarked`() {
        val key = newKey()
        assertTrue(store.markIfAbsent(key, Duration.ofHours(1)))
        assertFalse(store.markIfAbsent(key, Duration.ofHours(1)))
        store.unmark(key)
        assertTrue(store.markIfAbsent(key, Duration.ofHours(1)))
    }

    @Test
    fun `GIVEN an expired mark not yet removed by the TTL WHEN the key is marked THEN the mark succeeds`() {
        val key = newKey()
        assertTrue(store.markIfAbsent(key, Duration.ofSeconds(1)))
        Thread.sleep(2100)
        assertTrue(store.markIfAbsent(key, Duration.ofHours(1)))
    }

    @Test
    fun `GIVEN two instances WHEN they receive the same message at the same time THEN only one processes it`() {
        val processed = CopyOnWriteArrayList<HttpRequest>()

        // two consumers, as two instances: they share only DynamoDB
        fun instance() =
            RequestQueueing(
                queue = { error("not used") },
                queuedPaths = setOf("/whatsapp"),
                secret = "the secret of the test queue",
                deduplication = DynamoDeduplicationStore(),
                process = {
                    processed += it
                    HttpResponse.status()
                },
            )
        val instances = listOf(instance(), instance())
        val message =
            instances[0].message(
                QueuedRequest.of(HttpRequest(HttpMethod.POST, "/whatsapp", body = "{\"id\":\"${newKey()}\"}".toByteArray()), "/whatsapp"),
            )
        val pool = Executors.newFixedThreadPool(8)
        try {
            val results = (1..8).map { i -> pool.submit<Boolean> { instances[i % 2].processMessage(message) } }.map { it.get() }
            assertTrue(results.all { it }, "every delivery is acknowledged")
            assertEquals(1, processed.size)
        } finally {
            pool.shutdown()
            pool.awaitTermination(5, TimeUnit.SECONDS)
        }
    }
}

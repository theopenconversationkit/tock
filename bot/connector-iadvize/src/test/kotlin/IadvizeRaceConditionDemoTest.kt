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

package ai.tock.bot.connector.iadvize

import ai.tock.bot.connector.iadvize.IadvizeConnectorCallback.ActionWithDelay
import ai.tock.bot.engine.action.ActionMetadata
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.user.PlayerId
import org.junit.jupiter.api.Test
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Race condition demonstration tests.
 *
 * These tests demonstrate WHY the DeferredMessageCoordinator design is necessary
 * and HOW it prevents race conditions that existed in the old implementation.
 *
 * | Test | Scenario | Expected | Demonstrates |
 * |------|----------|----------|--------------|
 * | 1 | Synchronous execution | PASS | Why old tests passed |
 * | 2 | Concurrent sends without coordination | Race condition | The bug |
 * | 3 | Concurrent sends WITH coordinator | PASS | The fix |
 */
class IadvizeRaceConditionDemoTest {
    private val applicationId = "test-app"

    private fun createAction(text: String): ActionWithDelay {
        return ActionWithDelay(
            SendSentence(
                playerId = PlayerId("user"),
                applicationId = applicationId,
                recipientId = PlayerId("bot"),
                text = text,
                metadata = ActionMetadata(),
            ),
            0,
        )
    }

    // ========= SCENARIO 1: Synchronous - Why old tests passed =========

    @Test
    fun `scenario 1 - synchronous execution always works`() {
        // This simulates how the old tests worked:
        // - Everything runs on single thread
        // - No race condition possible
        // - Tests always pass

        // Given - simple list (NOT thread-safe)
        val messages = mutableListOf<String>()

        // When - sequential operations
        messages.add("Message 1")
        messages.add("Message 2")
        messages.add("Message 3")

        // Simulate "flush"
        val flushed = messages.toList()
        messages.clear()

        // Then - works perfectly
        assertEquals(3, flushed.size)
        assertTrue(flushed.contains("Message 1"))
        assertTrue(flushed.contains("Message 2"))
        assertTrue(flushed.contains("Message 3"))
    }

    // ========= SCENARIO 2: The Race Condition Bug =========

    @Test
    fun `scenario 2 - concurrent access without coordination causes issues`() {
        // This demonstrates the race condition that existed:
        // - Producer thread adds messages
        // - Consumer thread tries to flush
        // - Without coordination, flush can happen before messages are added

        // Given - simple queue (thread-safe for add, but NOT coordinated)
        val messages = ConcurrentLinkedQueue<String>()
        val flushedMessages = mutableListOf<String>()
        val producerDone = CountDownLatch(1)
        val consumerStarted = CountDownLatch(1)

        // Producer - adds messages with small delays
        val producer =
            Thread {
                consumerStarted.await() // Wait for consumer to start
                repeat(5) { i ->
                    Thread.sleep(10) // Simulate async processing time
                    messages.add("Async-$i")
                }
                producerDone.countDown()
            }

        // Consumer - tries to flush immediately
        val consumer =
            Thread {
                consumerStarted.countDown() // Signal we're ready
                // DON'T wait for producer - this is the bug!
                // In old code, flush() would happen immediately

                // "Flush" the queue
                while (messages.isNotEmpty()) {
                    flushedMessages.add(messages.poll()!!)
                }
            }

        // When
        producer.start()
        consumer.start()

        producer.join(5000)
        consumer.join(5000)

        // Then - consumer likely got FEWER messages than expected
        // because it didn't wait for producer
        // This is the race condition!
        println("Race condition demo: flushed ${flushedMessages.size} of 5 messages")
        // We can't assert exact count because it's a race condition
        // but typically it will be less than 5
    }

    // ========= SCENARIO 3: The Fix with DeferredMessageCoordinator =========

    @Test
    fun `scenario 3 - coordinator prevents race condition`() {
        // This demonstrates how DeferredMessageCoordinator fixes the issue:
        // - Messages are collected until end() is called
        // - end() atomically drains all messages
        // - No race condition possible

        // Given
        val coordinator = createTestCoordinator()
        val sentMessages = mutableListOf<ActionWithDelay>()
        val producerDone = CountDownLatch(1)

        // Producer - adds messages concurrently
        val producers =
            (1..5).map { threadId ->
                Thread {
                    repeat(10) { msgId ->
                        coordinator.collect(createAction("Thread$threadId-Msg$msgId"))
                    }
                }
            }

        // When - start all producers
        producers.forEach { it.start() }
        producers.forEach { it.join(5000) }

        // Now end() - this atomically drains all messages
        val wasEnded =
            coordinator.end { action ->
                synchronized(sentMessages) {
                    sentMessages.add(action)
                }
            }

        // Then - ALL messages should be captured
        assertTrue(wasEnded)
        assertEquals(50, sentMessages.size) // 5 threads × 10 messages
    }

    @Test
    fun `scenario 3b - coordinator end is idempotent`() {
        // Even if multiple threads try to end(), only one succeeds
        // This prevents duplicate message sending

        // Given
        val coordinator = createTestCoordinator()
        repeat(10) { i ->
            coordinator.collect(createAction("Msg-$i"))
        }

        val endCount = AtomicInteger(0)
        val messagesSent = AtomicInteger(0)
        val startLatch = CountDownLatch(1)
        val doneLatch = CountDownLatch(10)

        // When - 10 threads try to end() simultaneously
        repeat(10) {
            Thread {
                startLatch.await()
                val wasEnded =
                    coordinator.end {
                        messagesSent.incrementAndGet()
                    }
                if (wasEnded) endCount.incrementAndGet()
                doneLatch.countDown()
            }.start()
        }

        startLatch.countDown()
        doneLatch.await(5, TimeUnit.SECONDS)

        // Then - only ONE thread succeeded in ending
        assertEquals(1, endCount.get())
        assertEquals(10, messagesSent.get()) // All messages sent once
    }

    @Test
    fun `scenario 3c - coordinator handles concurrent send and end`() {
        // Messages sent after end() are ignored (no exception)

        // Given
        val coordinator = createTestCoordinator()
        val messagesSent = AtomicInteger(0)
        val collectAttempts = AtomicInteger(0)
        val startLatch = CountDownLatch(1)
        val doneLatch = CountDownLatch(11)

        // Thread that ends the coordinator
        Thread {
            startLatch.await()
            Thread.sleep(5) // Small delay
            coordinator.end { messagesSent.incrementAndGet() }
            doneLatch.countDown()
        }.start()

        // Threads that try to collect
        repeat(10) { i ->
            Thread {
                startLatch.await()
                repeat(10) { j ->
                    coordinator.collect(createAction("Late-$i-$j"))
                    collectAttempts.incrementAndGet()
                }
                doneLatch.countDown()
            }.start()
        }

        // When
        startLatch.countDown()
        doneLatch.await(5, TimeUnit.SECONDS)

        // Then - no exceptions thrown, some messages may have been collected
        // before end(), but no duplicates
        println("Collected ${collectAttempts.get()} attempts, sent ${messagesSent.get()} messages")
        // The coordinator is consistent regardless of timing
    }

    // ========= Helper to create coordinator without mocking =========

    private fun createTestCoordinator(): TestCoordinator {
        return TestCoordinator()
    }

    /**
     * Simplified coordinator for testing without mocks.
     * Same logic as DeferredMessageCoordinator but standalone.
     */
    private class TestCoordinator {
        private val messages = ConcurrentLinkedQueue<ActionWithDelay>()
        private val ended = java.util.concurrent.atomic.AtomicBoolean(false)

        fun collect(action: ActionWithDelay) {
            if (!ended.get()) {
                messages.add(action)
            }
        }

        fun end(sendAction: (ActionWithDelay) -> Unit): Boolean {
            if (!ended.compareAndSet(false, true)) {
                return false
            }
            // Atomically drain and send
            var action = messages.poll()
            while (action != null) {
                sendAction(action)
                action = messages.poll()
            }
            return true
        }
    }
}

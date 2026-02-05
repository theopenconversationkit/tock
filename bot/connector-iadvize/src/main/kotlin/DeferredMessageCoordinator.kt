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
import mu.KotlinLogging
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Coordinates deferred message handling for a single iAdvize conversation.
 *
 * This class solves the race condition problem by:
 * 1. Being stored in the callback (per conversation, not shared)
 * 2. Using thread-safe collections (ConcurrentLinkedQueue)
 * 3. Using atomic operations for state management
 *
 * Lifecycle:
 * 1. Created in handleRequest() - sends HTTP 200 immediately via start()
 * 2. Messages collected via collect() during handler execution
 * 3. Flushed via end() when lastAnswer=true is detected
 * 4. Or force-flushed via forceEnd() on timeout/error
 */
class DeferredMessageCoordinator(
    private val callback: IadvizeConnectorCallback,
    private val parameters: Map<String, String>,
) {
    private val logger = KotlinLogging.logger {}

    // Thread-safe queue for collected messages
    private val messages = ConcurrentLinkedQueue<ActionWithDelay>()

    // Atomic flags for state management
    private val started = AtomicBoolean(false)
    private val ended = AtomicBoolean(false)

    /**
     * Start the deferred session.
     * Sends the HTTP response immediately to acknowledge the request.
     * Can only be called once (idempotent).
     */
    fun start() {
        if (started.compareAndSet(false, true)) {
            callback.answerWithResponse()
            logger.info { "Deferred session started" }
        } else {
            logger.warn { "Deferred session already started - ignoring" }
        }
    }

    /**
     * Collect a message for later sending.
     * Thread-safe - can be called from multiple threads.
     *
     * @param action The action with delay to collect
     */
    fun collect(action: ActionWithDelay) {
        if (!ended.get()) {
            messages.add(action)
            logger.debug { "Deferred message collected. Queue size: ${messages.size}" }
        } else {
            logger.warn { "Cannot collect message - deferred session already ended" }
        }
    }

    /**
     * End the deferred session and flush all collected messages.
     * Thread-safe - only the first caller will execute the flush.
     *
     * @param sendAction Function to send each collected action
     * @return true if this call performed the end, false if already ended
     */
    fun end(sendAction: (ActionWithDelay) -> Unit): Boolean {
        if (!ended.compareAndSet(false, true)) {
            logger.debug { "Deferred session already ended - ignoring duplicate end()" }
            return false
        }

        logger.info { "Ending deferred session. Flushing ${messages.size} message(s)" }

        // Atomically drain and send all messages
        var action = messages.poll()
        while (action != null) {
            sendAction(action)
            action = messages.poll()
        }

        logger.debug { "Deferred session ended" }
        return true
    }

    /**
     * Force end the deferred session (for timeout/error scenarios).
     * Flushes collected messages and optionally sends an error message.
     *
     * @param sendAction Function to send each collected action
     * @param errorAction Optional error action to send after collected messages
     * @param logMessage Optional message to log for debugging
     * @return true if this call performed the end, false if already ended
     */
    fun forceEnd(
        sendAction: (ActionWithDelay) -> Unit,
        errorAction: ActionWithDelay? = null,
        logMessage: String? = null,
    ): Boolean {
        if (!ended.compareAndSet(false, true)) {
            logger.debug { "Deferred session already ended - ignoring forceEnd()" }
            return false
        }

        logger.warn { "Force ending deferred session (timeout/error): ${logMessage ?: "no details"}. Flushing ${messages.size} collected message(s)" }

        // Drain and send all collected messages
        var action = messages.poll()
        while (action != null) {
            sendAction(action)
            action = messages.poll()
        }

        // Send error message if provided
        errorAction?.let {
            logger.info { "Sending error message to user" }
            sendAction(it)
        }

        logger.debug { "Deferred session force-ended" }
        return true
    }

    /**
     * Check if the session has started.
     */
    fun hasStarted(): Boolean = started.get()

    /**
     * Check if the session has ended.
     */
    fun hasEnded(): Boolean = ended.get()

    /**
     * Get the number of messages currently in the queue.
     */
    fun messageCount(): Int = messages.size

    /**
     * Get the parameters associated with this coordinator.
     */
    fun getParameters(): Map<String, String> = parameters
}

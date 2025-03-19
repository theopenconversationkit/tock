/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.bot.connector

import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.user.PlayerId
import ai.tock.shared.Executor
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import java.time.Duration
import java.time.InstantSource
import java.util.Queue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

/**
 * A Queue to ensure the calls from the same user id are sent sequentially.
 */
class ConnectorQueue(private val executor: Executor, private val clock: InstantSource = InstantSource.system()) {

    private class ScheduledAction<T>(
        private val baseAction: Action,
        private val processedAction: CompletableFuture<T?>,
        private val send: (action: T) -> Unit,
        val delay: Duration,
    ) {
        val lastInAnswer get() = baseAction.metadata.lastAnswer
        fun joinAndSend() = processedAction.join()?.let(send)

        override fun toString(): String {
            return baseAction.toString()
        }
    }

    private inner class UserQueue : Queue<ScheduledAction<*>> by ConcurrentLinkedQueue() {
        private var answerInProgress = false
        private var lastScheduledTime = clock.instant()
        private var lastSentTime = clock.instant()

        @Synchronized
        fun getElapsedTime(): Duration = Duration.between(
            if (answerInProgress) lastSentTime else lastScheduledTime,
            clock.instant()
        )

        @Synchronized
        fun <T> enqueueMessage(actionWrapper: ScheduledAction<T>): Boolean {
            lastScheduledTime = clock.instant()
            val existingAction = peek()
            offer(actionWrapper)
            return existingAction != null
        }

        @Synchronized
        fun dequeueMessage(): ScheduledAction<*>? {
            // remove the current one
            val popped: ScheduledAction<*>? = poll()
            lastSentTime = clock.instant()
            answerInProgress = popped != null && !popped.lastInAnswer
            // schedule the next one
            return peek()
        }
    }

    private val messagesByRecipientMap: Cache<String, UserQueue> =
        CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .build()

    /**
     * Adds an action to send to a queue by recipient.
     *
     * This method is thread safe.
     *
     * @param action the action to send
     * @param delayInMs the optional delay
     * @param send the send function
     */
    fun add(action: Action, delayInMs: Long, send: (action: Action) -> Unit) {
        val actionWrapper = ScheduledAction(
            action,
            CompletableFuture.completedFuture(action),
            send,
            Duration.ofMillis(delayInMs),
        )

        add0(action.recipientId, actionWrapper)
    }

    /**
     * Adds an action to send to a queue by recipient, with a specific [prepare] step
     * beforehand.
     *
     * [prepare] is run asynchronously as soon as this method is called.
     * [send] is run once the following preconditions are met:
     * - [prepare] returned a result. If [prepare] returns `null`, [send] is not called.
     * - [delayInMs] has passed
     * - the previous message has been sent
     *
     * This method is thread safe.
     *
     * @param action the action to send
     * @param delayInMs the optional delay
     * @param send the send function
     */
    fun <T> add(
        action: Action,
        delayInMs: Long,
        prepare: (action: Action) -> T?,
        send: (action: T) -> Unit
    ) {
        val actionWrapper = ScheduledAction(
            action,
            executor.executeBlockingTask { prepare(action) },
            send,
            Duration.ofMillis(delayInMs),
        )

        add0(action.recipientId, actionWrapper)
    }

    private fun <T> add0(
        recipient: PlayerId,
        actionWrapper: ScheduledAction<T>,
    ) {
        val queue = messagesByRecipientMap
            .get(recipient.id) { UserQueue() }
            .apply {
                if (enqueueMessage(actionWrapper)) {
                    return
                }
            }
        sendNextAction(actionWrapper, queue)
    }

    private fun <T> sendNextAction(
        action: ScheduledAction<T>,
        queue: UserQueue,
    ) {
        val timeToWait = action.delay - queue.getElapsedTime()
        executor.executeBlocking(timeToWait) {
            try {
                action.joinAndSend()
            } finally {
                queue.dequeueMessage()?.also { a ->
                    sendNextAction(a, queue)
                }
            }
        }
    }
}

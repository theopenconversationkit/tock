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
import ai.tock.shared.Executor
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import java.time.Duration
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

/**
 * A Queue to ensure the calls from the same user id are sent sequentially.
 */
class ConnectorQueue(private val executor: Executor) {

    private data class ActionWithTimestamp(val action: Action, val timestamp: Long)

    private val messagesByRecipientMap: Cache<String, ConcurrentLinkedQueue<ActionWithTimestamp>> =
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
        val actionWrapper = ActionWithTimestamp(action, System.currentTimeMillis() + delayInMs)

        val queue = messagesByRecipientMap
            .get(action.recipientId.id) { ConcurrentLinkedQueue() }
            .apply {
                synchronized(this) {
                    peek().also { existingAction ->
                        offer(actionWrapper)
                        if (existingAction != null) {
                            return
                        }
                    }
                }
            }
        executor.executeBlocking(Duration.ofMillis(delayInMs)) {
            sendActionFromConnector(actionWrapper, queue, send)
        }
    }

    private fun sendActionFromConnector(
        action: ActionWithTimestamp,
        queue: ConcurrentLinkedQueue<ActionWithTimestamp>,
        send: (action: Action) -> Unit
    ) {
        try {
            val timeToWait = action.timestamp - System.currentTimeMillis()
            if (timeToWait > 0) {
                Thread.sleep(timeToWait)
            }
            send(action.action)
        } finally {
            synchronized(queue) {
                // remove the current one
                queue.poll()
                queue.peek()
            }?.also { a ->
                sendActionFromConnector(a, queue, send)
            }
        }
    }
}

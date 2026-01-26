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

import ai.tock.bot.connector.Connector
import ai.tock.bot.connector.ConnectorCallback

/**
 * Interface for connectors that support deferred messaging mode.
 *
 * Deferred mode allows sending an immediate acknowledgement (HTTP 200) to the client,
 * while processing the request asynchronously and sending responses via a different
 * channel (e.g., GraphQL API for iAdvize).
 *
 * ## Lifecycle
 *
 * 1. `acknowledge(callback)` - Sends immediate HTTP 200 response
 * 2. `beginDeferred(callback)` - Initializes the coordinator for the conversation
 * 3. Messages are collected during handler execution via standard `send()`
 * 4. `endDeferred(callback)` - Flushes all collected messages
 *
 * ## Thread Safety
 *
 * The deferred coordinator is stored in the callback (per conversation),
 * ensuring thread safety for concurrent conversations.
 *
 * ## Usage
 *
 * Connectors implementing this interface will automatically handle deferred mode
 * when appropriate. The standard `send()` method detects `lastAnswer=true` to
 * trigger the flush.
 *
 * ```kotlin
 * class MyConnector : ConnectorBase(...), DeferredConnector {
 *
 *     override fun handleRequest(...) {
 *         val callback = createCallback(...)
 *
 *         if (canUseDeferred) {
 *             acknowledge(callback)
 *             beginDeferred(callback, metadata)
 *
 *             executor.executeBlocking {
 *                 controller.handle(event, ConnectorData(callback, metadata))
 *             }
 *         } else {
 *             // Standard synchronous handling
 *             controller.handle(event, ConnectorData(callback, metadata))
 *         }
 *     }
 *
 *     override fun send(event, callback, delayInMs) {
 *         if (isDeferredMode(callback)) {
 *             collectDeferred(callback, event, delayInMs)
 *             if (event.metadata.lastAnswer) {
 *                 endDeferred(callback)
 *             }
 *         } else {
 *             // Standard send
 *         }
 *     }
 * }
 * ```
 */
interface DeferredConnector : Connector {
    /**
     * Check if the callback is in deferred mode.
     *
     * @param callback The connector callback
     * @return true if deferred mode is active
     */
    fun isDeferredMode(callback: ConnectorCallback): Boolean

    /**
     * Send an immediate acknowledgement (HTTP 200) to the client.
     * This should be called before starting async processing.
     *
     * @param callback The connector callback containing the HTTP response
     */
    fun acknowledge(callback: ConnectorCallback)

    /**
     * Initialize deferred mode for this conversation.
     * Creates a coordinator and stores it in the callback.
     *
     * @param callback The connector callback to store the coordinator
     * @param parameters Metadata for the conversation (conversation ID, etc.)
     */
    fun beginDeferred(
        callback: ConnectorCallback,
        parameters: Map<String, String>,
    )

    /**
     * Flush all collected messages and end deferred mode.
     * This is typically called when `lastAnswer=true` is detected,
     * or when force-flushing due to timeout/error.
     *
     * @param callback The connector callback containing the coordinator
     */
    fun endDeferred(callback: ConnectorCallback)

    /**
     * Force end deferred mode with an optional error message.
     * Used for timeout/error scenarios.
     *
     * @param callback The connector callback containing the coordinator
     * @param reason Reason for force ending (for logging)
     * @param sendErrorMessage Whether to send a default error message to the user
     */
    fun forceEndDeferred(
        callback: ConnectorCallback,
        reason: String,
        sendErrorMessage: Boolean = true,
    )
}

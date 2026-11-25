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

package ai.tock.shared.http

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select
import java.nio.charset.StandardCharsets
import java.time.Duration

/**
 * The response of a route whose answer is sent later, by a callback of the bot: [complete] answers at once,
 * [startStream] opens a streamed response that [write] feeds and [close] ends. The route returns [await].
 *
 * The first call wins: a [complete] after [startStream] writes its body as the last chunk, as a Vert.x response
 * ended after its headers were sent, and a [fail] after [startStream] aborts the stream.
 */
class DeferredHttpResponse {
    private val response = CompletableDeferred<HttpResponse>()
    private val chunks = Channel<ByteArray>(Channel.UNLIMITED)

    @Volatile
    private var streaming = false

    /** Answers [completed], or ends the stream with its body. */
    @Synchronized
    fun complete(completed: HttpResponse) {
        if (streaming) {
            if (completed.body.isNotEmpty()) chunks.trySend(completed.body)
            close()
        } else {
            response.complete(completed)
        }
    }

    /**
     * Starts a streamed response: [prelude] is its first chunk, [keepAlive] is written when nothing has been written
     * for [keepAliveDelay]. Ignored if the response is already started or completed.
     */
    @Synchronized
    fun startStream(
        status: Int = 200,
        headers: Map<String, List<String>> = emptyMap(),
        prelude: String? = null,
        keepAlive: String? = null,
        keepAliveDelay: Duration = Duration.ofSeconds(10),
    ) {
        if (streaming || response.isCompleted) return
        streaming = true
        val keepAliveChunk = keepAlive?.toByteArray(StandardCharsets.UTF_8)
        response.complete(
            HttpResponse(status, headers) { writer ->
                prelude?.let { writer.write(it.toByteArray(StandardCharsets.UTF_8)) }
                while (true) {
                    val result =
                        if (keepAliveChunk == null) {
                            chunks.receiveCatching()
                        } else {
                            receiveOrKeepAlive(keepAliveDelay) ?: run {
                                writer.write(keepAliveChunk)
                                null
                            }
                        } ?: continue
                    if (result.isClosed) {
                        // a stream ended by fail: the server aborts the response
                        result.exceptionOrNull()?.let { throw it }
                        break
                    }
                    writer.write(result.getOrThrow())
                }
            },
        )
    }

    /**
     * Waits for the next chunk, or for [delay] without chunk (null). Unlike a `receive` cancelled by a timeout,
     * the select never drops a chunk that arrives as the delay expires.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun receiveOrKeepAlive(delay: Duration): ChannelResult<ByteArray>? =
        select {
            chunks.onReceiveCatching { it }
            onTimeout(delay.toMillis()) { null }
        }

    /** True once [startStream] has been called. */
    val isStreaming: Boolean get() = streaming

    /** Writes a chunk of the stream (ignored if it is not started). */
    fun write(chunk: String) {
        if (streaming) chunks.trySend(chunk.toByteArray(StandardCharsets.UTF_8))
    }

    /** Ends the stream. */
    fun close() {
        chunks.close()
    }

    /**
     * Fails the response: answers [failure] if nothing has been sent yet, or aborts the stream with [cause]
     * (the client then sees an interrupted response rather than a normal end).
     */
    @Synchronized
    fun fail(
        cause: Throwable,
        failure: HttpResponse = HttpResponse.text("Internal Server Error", 500),
    ) {
        if (streaming) {
            chunks.close(cause)
        } else {
            response.complete(failure)
        }
    }

    suspend fun await(): HttpResponse = response.await()
}

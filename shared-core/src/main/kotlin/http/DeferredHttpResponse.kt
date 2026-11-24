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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.withTimeoutOrNull
import java.nio.charset.StandardCharsets
import java.time.Duration

/**
 * The response of a route whose answer is sent later, by a callback of the bot: [complete] answers at once,
 * [startStream] opens a streamed response that [write] feeds and [close] ends. The route returns [await].
 *
 * The first call wins: a [complete] after [startStream] writes its body as the last chunk, as a Vert.x response
 * ended after its headers were sent.
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
        response.complete(
            HttpResponse(status, headers) { writer: HttpBodyWriter ->
                prelude?.let { writer.write(it.toByteArray(StandardCharsets.UTF_8)) }
                while (true) {
                    val chunk =
                        try {
                            if (keepAlive == null) {
                                chunks.receive()
                            } else {
                                withTimeoutOrNull(keepAliveDelay.toMillis()) { chunks.receive() }
                                    ?: keepAlive.toByteArray(StandardCharsets.UTF_8)
                            }
                        } catch (_: ClosedReceiveChannelException) {
                            break
                        }
                    writer.write(chunk)
                }
            },
        )
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

    suspend fun await(): HttpResponse = response.await()
}

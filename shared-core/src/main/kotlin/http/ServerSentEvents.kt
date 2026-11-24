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

import ai.tock.shared.longProperty
import java.time.Duration

/**
 * The server-sent events of the server independent routes, as tock-shared writes them with Vert.x (setupSSE,
 * sendSseMessage, sendSsePing).
 */
object ServerSentEvents {
    /** The headers of a server-sent events response. */
    val headers: Map<String, List<String>> =
        mapOf(
            "Content-Type" to listOf("text/event-stream;charset=UTF-8"),
            "Connection" to listOf("keep-alive"),
            "Cache-Control" to listOf("no-cache"),
            "X-Accel-Buffering" to listOf("no"),
        )

    /** The ping sent when the stream starts, and to keep it alive. */
    const val PING: String = "event: ping\ndata: 1\n\n"

    /** The delay of the keep alive pings: `tock_web_sse_keepalive_delay` seconds. */
    val keepAliveDelay: Duration = Duration.ofSeconds(longProperty("tock_web_sse_keepalive_delay", 10))

    /** A message event. */
    fun message(data: String): String = "event: message\ndata: $data\n\n"

    /** Starts the server-sent events of [response]: the ping, then a ping when nothing is sent for a while ([keepAlive]). */
    fun start(
        response: DeferredHttpResponse,
        keepAlive: Boolean,
    ) {
        response.startStream(headers = headers, prelude = PING, keepAlive = if (keepAlive) PING else null, keepAliveDelay = keepAliveDelay)
    }
}

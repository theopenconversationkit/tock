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

package ai.tock.bot.connector.web

import ai.tock.bot.connector.ConnectorCallbackBase
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.event.MetadataEvent
import ai.tock.bot.engine.event.MetadataEvent.Companion.STREAM_RESPONSE_METADATA
import ai.tock.bot.engine.event.hasStreamMetadata
import ai.tock.shared.booleanProperty
import ai.tock.shared.vertx.sendSseMessage
import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.web.RoutingContext
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Metadata of a partial frame that only carries the end of the rendered text (instead of the whole text so far):
 * the client keeps the first TOCK_STREAM_KEEP characters of the current message and appends the frame text.
 * The rendered text is the same as in the default mode, only the common prefix is not sent again.
 */
const val STREAM_MODE_METADATA = "TOCK_STREAM_MODE"
const val STREAM_MODE_DELTA = "delta"
const val STREAM_KEEP_METADATA = "TOCK_STREAM_KEEP"

private val mergeStreamResponse: Boolean =
    booleanProperty("tock_web_connector_merge_stream_response", true)

internal class WebConnectorCallback(
    applicationId: String,
    errorListener: ((Throwable) -> Unit)?,
    val locale: Locale,
    private val context: RoutingContext?,
    private val actions: MutableList<Action> = CopyOnWriteArrayList(),
    private val metadata: MutableMap<String, String> = ConcurrentHashMap(),
    private val webMapper: ObjectMapper,
    private val eventId: String,
    private val messageProcessor: WebMessageProcessor,
    internal val streamedResponse: Boolean,
    private val sseTokenSender: ((WebConnectorResponse) -> Unit)? = null,
    private val streamDelta: Boolean = false,
) : ConnectorCallbackBase(applicationId, webConnectorType, errorListener) {
    private val streamBuffer = StringBuilder()

    @Volatile
    private var sseStreamStarted = false

    // Guarded by streamBuffer: once the final response is queued, late partial frames are dropped
    private var sseStreamFinished = false

    // Rendered text of the previous partial frame, to send only what changed in delta mode
    private var lastRenderedText = ""

    // One fixed Vert.x context so that partial responses are written in order
    private val streamContext by lazy { context?.vertx()?.getOrCreateContext() }

    fun addAction(action: Action) {
        actions.add(action)
    }

    fun addMetadata(metadata: MetadataEvent) {
        this.metadata[metadata.type] = metadata.value
        if (metadata.isStreamMetadata()) {
            WebRequestInfosByEvent.get(eventId)?.clearStreamedResponse()
        }
    }

    fun createResponse(actions: List<Action>): WebConnectorResponse {
        val messages =
            actions.mapNotNull { a ->
                val action =
                    if (a is SendSentence && metadata.hasStreamMetadata() && mergeStreamResponse) {
                        WebRequestInfosByEvent.getOrPut(eventId).addStreamedResponse(a, webConnectorType)
                    } else {
                        a
                    }
                messageProcessor.process(action)
            }

        // The stream flag is kept out of `metadata` so that final messages are not merged as stream chunks
        val responseMetadata =
            if (sseStreamStarted) metadata + (STREAM_RESPONSE_METADATA to "true") else metadata.toMap()
        return WebConnectorResponse(messages, responseMetadata)
    }

    fun sendResponse() {
        WebRequestInfosByEvent.invalidate(eventId)
        context
            ?.response()
            ?.putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            ?.end(webMapper.writeValueAsString(createResponse(actions)))
    }

    fun sendStreamedResponse(action: Action) {
        context
            ?.response()
            ?.sendSseMessage(webMapper.writeValueAsString(createResponse(listOf(action))))
    }

    /** Forwards a RAG token to the client over the standard SSE channel; `/sse/direct` does not receive tokens. */
    override fun sendStreamedToken(content: String) {
        if (content.isEmpty() || streamedResponse) return
        sendStreamedTokenOverSse(content)
    }

    /**
     * Standard streaming: the accumulated text is pushed on the regular SSE channel as a message
     * flagged TOCK_STREAM_RESPONSE with the RESPONSE_ID, so that clients replace the previous version.
     */
    private fun sendStreamedTokenOverSse(content: String) {
        val sender = sseTokenSender ?: return
        synchronized(streamBuffer) {
            if (sseStreamFinished) return
            streamBuffer.append(content)
            sseStreamStarted = true
            val rendered = messageProcessor.processText(streamBuffer.toString())
            val response =
                if (streamDelta) {
                    val previous = lastRenderedText
                    val renderedText = rendered.text.orEmpty()
                    val keep = commonPrefixLength(previous, renderedText)
                    lastRenderedText = renderedText
                    if (keep == renderedText.length && keep == previous.length) return
                    WebConnectorResponse(
                        listOf(WebMessage(text = renderedText.substring(keep))),
                        metadata +
                            (STREAM_RESPONSE_METADATA to "true") +
                            (STREAM_MODE_METADATA to STREAM_MODE_DELTA) +
                            (STREAM_KEEP_METADATA to keep.toString()),
                    )
                } else {
                    WebConnectorResponse(listOf(rendered), metadata + (STREAM_RESPONSE_METADATA to "true"))
                }
            val streamContext = this.streamContext
            if (streamContext != null) {
                streamContext.runOnContext { sender(response) }
            } else {
                sender(response)
            }
        }
    }

    /**
     * Sends the final response on the SSE channel. When partial frames were queued, it goes through the same
     * Vert.x context so that it is always written after them and TRK/TVK end up with the complete message.
     */
    fun sendFinalOverSse(send: () -> Unit) {
        val streamContext = this.streamContext
        val queued =
            synchronized(streamBuffer) {
                sseStreamFinished = true
                sseStreamStarted && streamContext != null
            }
        if (queued) streamContext?.runOnContext { send() } else send()
    }

    override fun eventSkipped(event: Event) {
        super.eventSkipped(event)
        sendResponse()
    }
}

private fun commonPrefixLength(
    a: String,
    b: String,
): Int {
    val max = minOf(a.length, b.length)
    var i = 0
    while (i < max && a[i] == b[i]) i++
    return i
}

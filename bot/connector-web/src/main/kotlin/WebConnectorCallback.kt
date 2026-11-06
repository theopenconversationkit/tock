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
) : ConnectorCallbackBase(applicationId, webConnectorType, errorListener) {
    private val streamBuffer = StringBuilder()

    @Volatile
    private var sseStreamStarted = false

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
        val text =
            synchronized(streamBuffer) {
                streamBuffer.append(content)
                streamBuffer.toString()
            }
        sseStreamStarted = true
        val response =
            WebConnectorResponse(
                listOf(messageProcessor.processText(text)),
                metadata + (STREAM_RESPONSE_METADATA to "true"),
            )
        val streamContext = this.streamContext
        if (streamContext != null) {
            streamContext.runOnContext { sender(response) }
        } else {
            sender(response)
        }
    }

    override fun eventSkipped(event: Event) {
        super.eventSkipped(event)
        sendResponse()
    }
}

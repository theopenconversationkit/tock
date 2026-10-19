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
import ai.tock.bot.engine.event.hasStreamMetadata
import ai.tock.shared.booleanProperty
import ai.tock.shared.vertx.sendSseEvent
import ai.tock.shared.vertx.sendSseMessage
import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

private val mergeStreamResponse: Boolean =
    booleanProperty("tock_web_connector_merge_stream_response", true)

private val logger = KotlinLogging.logger {}

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
) : ConnectorCallbackBase(applicationId, webConnectorType, errorListener) {
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

        return WebConnectorResponse(messages, metadata.toMap()) // clone the metadata
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

    /**
     * Forward a partial/progressive text token to the client as an SSE `token` event
     * while the answer is still being generated (RAG streaming mode).
     *
     * Token callbacks run on a foreign thread (the orchestrator SSE background thread);
     * writing the Vert.x response from a non-event-loop thread does not flush until the
     * next event-loop activity, which batches all tokens together. Marshalling every write
     * onto the Vert.x context serializes them in order and flushes each chunk immediately.
     */
    override fun sendStreamedToken(content: String) {
        if (!streamedResponse || content.isEmpty()) return
        val ctx = context ?: return
        try {
            val data = webMapper.writeValueAsString(mapOf("content" to content))
            ctx.vertx().runOnContext {
                val response = ctx.response()
                if (!response.ended()) {
                    response.sendSseEvent("token", data)
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Error writing streamed token" }
        }
    }

    override fun eventSkipped(event: Event) {
        super.eventSkipped(event)
        sendResponse()
    }
}

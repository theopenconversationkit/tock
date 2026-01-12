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
import ai.tock.bot.connector.web.WebConnector.Companion.sendSseResponse
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.event.MetadataEvent
import ai.tock.bot.engine.event.hasStreamMetadata
import ai.tock.shared.booleanProperty
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
    val locale: Locale,
    private val context: RoutingContext?,
    private val actions: MutableList<Action> = CopyOnWriteArrayList(),
    private val metadata: MutableMap<String, String> = ConcurrentHashMap(),
    private val webMapper: ObjectMapper,
    private val eventId: String,
    private val messageProcessor: WebMessageProcessor,
    internal val streamedResponse: Boolean,
) : ConnectorCallbackBase(applicationId, webConnectorType) {
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
        context?.response()
            ?.putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            ?.end(webMapper.writeValueAsString(createResponse(actions)))
    }

    fun sendStreamedResponse(action: Action) {
        context?.response()
            ?.sendSseResponse(createResponse(listOf(action)))
    }
}

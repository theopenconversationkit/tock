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

package ai.tock.bot.connector.web

import ai.tock.bot.connector.ConnectorCallbackBase
import ai.tock.bot.connector.web.WebConnector.Companion.sendSseResponse
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.event.MetadataEvent
import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.web.RoutingContext
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

internal class WebConnectorCallback(
    applicationId: String,
    val locale: Locale,
    private val context: RoutingContext?,
    private val actions: MutableList<Action> = CopyOnWriteArrayList(),
    private val metadata: MutableMap<String, String> = mutableMapOf(),
    private val webMapper: ObjectMapper,
    private val eventId: String,
    private val messageProcessor: WebMessageProcessor,
    internal val streamedResponse:Boolean,
) : ConnectorCallbackBase(applicationId, webConnectorType) {

    fun addAction(action: Action) {
        actions.add(action)
    }

    fun addMetadata(metadata: MetadataEvent) {
        this.metadata[metadata.type] = metadata.value
    }

    fun createResponse(actions: List<Action>): WebConnectorResponse {
        val messages = actions.mapNotNull(messageProcessor::process)
        return WebConnectorResponse(messages, metadata)
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

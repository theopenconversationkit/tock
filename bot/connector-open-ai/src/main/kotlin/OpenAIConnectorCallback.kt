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

package ai.tock.bot.connector.openai

import ai.tock.bot.connector.ConnectorCallbackBase
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.event.MetadataEvent
import ai.tock.bot.engine.event.hasStreamMetadata
import ai.tock.shared.serialization.writeJson
import ai.tock.shared.vertx.sendSseMessage
import io.vertx.core.CompositeFuture
import io.vertx.core.Future
import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
class OpenAIConnectorCallback(
    applicationId: String,
    val locale: Locale,
    private val context: RoutingContext?,
    private val actions: MutableList<Action> = CopyOnWriteArrayList(),
    private val metadata: MutableMap<String, String> = ConcurrentHashMap(),
    private val eventId: String,
    internal val streamedResponse: Boolean,
) : ConnectorCallbackBase(applicationId, openAIConnectorType) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    // to manage \n
    private val endStreaming = AtomicBoolean(false)

    fun addAction(action: Action) {
        actions.add(action)
    }

    fun addMetadata(metadata: MetadataEvent) {
        this.metadata[metadata.type] = metadata.value
        endStreaming.store(metadata.isEndStreamMetadata())
    }

    fun createResponse(actions: List<Action>): OpenAIConnectorResponse {
        val messages =
            actions.mapNotNull {
                toOpenAI(it)
            }
        return OpenAIConnectorResponse(messages)
    }

    private fun toOpenAI(action: Action): OpenAIConnectorMessage? {
        return when (action) {
            is SendSentence -> {
                val message = action.message(openAIConnectorType) as? OpenAIConnectorMessage
                if (message != null) {
                    message
                } else {
                    val stringText =
                        if (metadata.hasStreamMetadata()) {
                            action.stringText
                        } else {
                            "${if (endStreaming.load()) "\n" else ""}${action.stringText}\n"
                        }
                    endStreaming.store(false)

                    if (stringText != null) {
                        OpenAIConnectorMessage(stringText)
                    } else {
                        OpenAIConnectorMessage("[unsupported message]")
                    }
                }
            }

            else -> {
                OpenAIConnectorMessage("[unsupported message]")
            }
        }
    }

    fun sendResponse() {
        val messages = actions.mapNotNull { toOpenAI(it) }
        context?.response()
            ?.putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            ?.end(writeJson(OpenAIConnectorResponse(messages).toOpenAIResponse()))
    }

    fun sendStreamedResponse(action: Action) {
        context?.response()?.apply {
            sendSseResponse(createResponse(listOf(action)))
            if (action.metadata.lastAnswer) {
                sendSseEnd()
            }
        }
    }

    private fun HttpServerResponse.sendSseEnd(): CompositeFuture =
        Future.all(
            write("data: [DONE]\n\n"),
            end(),
        )

    private fun HttpServerResponse.sendSseResponse(webConnectorResponse: OpenAIConnectorResponse) =
        webConnectorResponse.messages.forEach { message ->
            sendSseMessage(
                writeJson(message.toOpenAIChunk()).apply {
                    logger.debug { "send to client: $this" }
                },
            )
        }
}

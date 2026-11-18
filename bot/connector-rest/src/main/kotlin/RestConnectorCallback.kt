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

package ai.tock.bot.connector.rest

import ai.tock.bot.connector.ConnectorCallbackBase
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.rest.RestConnector.Companion.checkNlpStats
import ai.tock.bot.connector.rest.model.MessageResponse
import ai.tock.bot.definition.TestBehaviour
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendDebug
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.action.SendSentenceWithFootnotes
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.message.DebugMessage
import ai.tock.bot.engine.message.Message
import ai.tock.shared.jackson.mapper
import ai.tock.shared.vertx.sendSseEvent
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

/**
 * [ConnectorCallback] for [RestConnector].
 */
internal class RestConnectorCallback(
    applicationId: String,
    connectorType: ConnectorType,
    val context: RoutingContext,
    val testContext: TestBehaviour?,
    val locale: Locale,
    private val userAction: Action,
    val actions: MutableList<Action> = CopyOnWriteArrayList(),
    val streaming: Boolean = false,
) : ConnectorCallbackBase(applicationId, connectorType) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * Writes a raw SSE chunk on the Vert.x event loop.
     *
     * Token callbacks run on a foreign thread (the orchestrator SSE background thread);
     * writing a Vert.x response from a non-event-loop thread does not get flushed until the
     * next event-loop activity, which batches all tokens together. Marshalling every SSE
     * write onto the context serializes them in order and flushes each chunk immediately.
     */
    private fun writeSseEvent(
        event: String,
        data: String,
    ) {
        context.vertx().runOnContext {
            if (!context.response().ended()) {
                context.response().sendSseEvent(event, data)
            }
        }
    }

    private fun endSse() {
        context.vertx().runOnContext {
            if (!context.response().ended()) {
                context.response().end()
            }
        }
    }

    override fun sendStreamedToken(content: String) {
        if (!streaming) return
        try {
            val data = mapper.writeValueAsString(mapOf("content" to content))
            writeSseEvent("token", data)
        } catch (e: Exception) {
            logger.error(e) { "Error writing streamed token" }
        }
    }

    override fun eventSkipped(event: Event) {
        super.eventSkipped(event)
        sendAnswer()
    }

    override fun eventAnswered(event: Event) {
        super.eventAnswered(event)
        sendAnswer()
    }

    override fun exceptionThrown(
        event: Event,
        throwable: Throwable,
    ) {
        super.exceptionThrown(event, throwable)
        sendAnswer(throwable)
    }

    private fun sendAnswer(throwable: Throwable? = null) {
        if (streaming) {
            sendStreamingAnswer(throwable)
        } else {
            sendClassicAnswer(throwable)
        }
    }

    private fun sendClassicAnswer(throwable: Throwable?) {
        if (actions.isEmpty()) {
            context.fail(throwable ?: IllegalStateException("no response available"))
        } else {
            val nlpStats = if (checkNlpStats) (userAction as? SendSentence)?.nlpStats else null
            val ragDebug = actions.filterIsInstance<SendSentenceWithFootnotes>().lastOrNull()?.ragDebug
            val r =
                mapper.writeValueAsString(
                    MessageResponse(
                        actions.flatMap { it.toRestMessages(userAction.metadata.debugEnabled) },
                        applicationId,
                        userAction.id.toString(),
                        nlpStats?.locale ?: locale,
                        userAction is SendSentence,
                        ragDebug,
                    ),
                )
            logger.debug { "response : $r" }
            context.response().end(r)
        }
    }

    private fun sendStreamingAnswer(throwable: Throwable?) {
        if (throwable != null) {
            try {
                logger.error(throwable) { "Error in streaming response" }
                val errorJson = mapper.writeValueAsString(mapOf("error" to (throwable.message ?: "Unknown error")))
                writeSseEvent("error", errorJson)
                endSse()
            } catch (e: Exception) {
                logger.error(e) { "Error writing streaming error" }
            }
        } else if (actions.isEmpty()) {
            try {
                val errorJson = mapper.writeValueAsString(mapOf("error" to "no response available"))
                writeSseEvent("error", errorJson)
                endSse()
            } catch (e: Exception) {
                logger.error(e) { "Error writing streaming error" }
            }
        } else {
            try {
                val nlpStats = if (checkNlpStats) (userAction as? SendSentence)?.nlpStats else null
                val ragDebug = actions.filterIsInstance<SendSentenceWithFootnotes>().lastOrNull()?.ragDebug
                val response =
                    actions
                        .filter {
                            if (!userAction.metadata.debugEnabled) it !is SendDebug else true
                        }.map { it.toMessage() }
                val jsonData =
                    mapper.writeValueAsString(
                        MessageResponse(
                            response,
                            applicationId,
                            userAction.id.toString(),
                            nlpStats?.locale ?: locale,
                            userAction is SendSentence,
                            ragDebug,
                        ),
                    )
                writeSseEvent("answer", jsonData)
                endSse()
            } catch (e: Exception) {
                logger.error(e) { "Error writing streaming answer" }
            }
        }
    }
}

internal fun Action.toRestMessages(debugEnabled: Boolean): List<Message> =
    when {
        this is SendDebug && !debugEnabled -> {
            emptyList()
        }

        // RAG debug is stored on the action, but REST test clients consume debug messages.
        this is SendSentenceWithFootnotes && debugEnabled && ragDebug != null -> {
            listOf(DebugMessage("RAG", ragDebug), toMessage())
        }

        else -> {
            listOf(toMessage())
        }
    }

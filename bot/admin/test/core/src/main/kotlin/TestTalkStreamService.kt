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

package ai.tock.bot.admin.test

import ai.tock.bot.admin.test.model.BotDialogRequest
import ai.tock.bot.connector.rest.client.model.ClientMessageRequest
import ai.tock.shared.defaultLocale
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Streaming version of TestTalkService using Server-Sent Events (SSE)
 */
object TestTalkStreamService {
    /**
     * Stream bot response as Server-Sent Events
     * Proxies events from the bot connector to the HTTP response
     */
    suspend fun talkStream(
        request: BotDialogRequest,
        debugEnabled: Boolean,
        sourceWithContent: Boolean,
        context: RoutingContext,
    ) {
        try {
            val conf = TestTalkService.getBotConfiguration(request.botApplicationConfigurationId, request.namespace)
            val restClient = TestTalkService.getRestClient(conf)

            logger.debug { "Starting streaming talk for bot: ${conf.applicationId}" }

            // Get SSE response stream from bot connector's streaming endpoint
            val flow =
                restClient.talkStream(
                    conf.path ?: conf.applicationId,
                    request.currentLanguage ?: defaultLocale,
                    TestTalkService.buildClientRequest(
                        conf,
                        request.currentLanguage,
                        request.userIdModifier,
                        request.message,
                        debugEnabled,
                        sourceWithContent,
                    ),
                )

            // Stream events from bot connector to client.
            // collect() runs on a worker thread; writing a Vert.x response from a non
            // event-loop thread is not flushed until the next event-loop activity, which
            // batches all events together. Marshal each write onto the context so each SSE
            // chunk is flushed to the browser immediately, at the rhythm tokens arrive.
            flow.collect { sse ->
                val chunk = "event: ${sse.event ?: "message"}\ndata: ${sse.data}\n\n"
                context.vertx().runOnContext {
                    if (!context.response().ended()) {
                        context.response().write(chunk)
                    }
                }
            }

            // Send final newline and close (on the event loop, after all queued writes)
            context.vertx().runOnContext {
                if (!context.response().ended()) {
                    context.response().end()
                }
            }
            logger.debug { "Streaming talk completed successfully" }
        } catch (throwable: Throwable) {
            logger.error(throwable) { "Error in streaming talk" }
            val errorEvent = """event: error
data: {"error": "${throwable.message?.replace("\"", "\\\"")}"}

"""
            context.response().write(errorEvent)
            context.response().end()
        }
    }
}

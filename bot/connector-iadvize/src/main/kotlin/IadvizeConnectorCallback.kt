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

package ai.tock.bot.connector.iadvize

import ai.tock.bot.connector.ConnectorCallbackBase
import ai.tock.bot.connector.iadvize.model.request.ConversationsRequest
import ai.tock.bot.connector.iadvize.model.request.IadvizeRequest
import ai.tock.bot.connector.iadvize.model.response.conversation.reply.IadvizeMessage
import ai.tock.bot.connector.iadvize.model.response.conversation.IadvizeMessageReplies
import ai.tock.bot.connector.iadvize.model.response.conversation.IadvizeResponse
import ai.tock.bot.connector.iadvize.model.response.conversation.payload.TextPayload
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.event.Event
import ai.tock.shared.error
import ai.tock.shared.jackson.mapper
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging
import java.time.LocalDateTime

class IadvizeConnectorCallback(override val  applicationId: String,
                               val controller: ConnectorController,
                               val context: RoutingContext,
                               val request: IadvizeRequest) :
    ConnectorCallbackBase(applicationId, iadvizeConnectorType) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun eventSkipped(event: Event) {
        super.eventSkipped(event)
        sendResponse()
    }

    override fun eventAnswered(event: Event) {
        super.eventAnswered(event)
        sendResponse()
    }

    fun sendResponse() {
        try {
            val iadvizeResponse: IadvizeResponse? = buildResponse()
            context.response().endWithJson(iadvizeResponse)
        } catch (t: Throwable) {
            logger.error(t)
            context.fail(t)
        }
    }

    fun buildResponse(): IadvizeResponse? {
       return when(request) {
            is ConversationsRequest ->
               IadvizeMessageReplies(
                    request.idConversation,
                    request.idOperator,
                    LocalDateTime.now(),
                    LocalDateTime.now())
           else -> null
       }
    }

    override fun exceptionThrown(event: Event, throwable: Throwable) {
        super.exceptionThrown(event, throwable)
        sendTechnicalError(throwable)
    }

    fun sendTechnicalError(throwable: Throwable) {
        logger.error(throwable)
        context.fail(throwable)
    }

    private fun <T> HttpServerResponse.endWithJson(response: T) {
        logger.debug { "iAdvize response : $response" }

        val writeValueAsString = mapper.writeValueAsString(response)

        logger.debug { "iAdvize json response: $writeValueAsString" }

        return putHeader("Content-Type", "application/json").end(writeValueAsString)
    }
}
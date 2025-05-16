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

package ai.tock.bot.orchestration.bot.secondary

import ai.tock.bot.connector.ConnectorCallbackBase
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.orchestration.shared.NoOrchestrationStatus.ERROR
import ai.tock.bot.orchestration.shared.OrchestrationMetaData
import ai.tock.bot.orchestration.shared.SecondaryBotNoResponse
import ai.tock.bot.orchestration.shared.SecondaryBotResponse
import ai.tock.shared.error
import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.databind.ObjectMapper
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR
import io.netty.handler.codec.http.HttpResponseStatus.OK
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging

open class RestOrchestrationCallback(
    connectorType: ConnectorType,
    applicationId: String,
    private val context: RoutingContext,
    override val actions: MutableList<Action> = mutableListOf(),
    private val orchestrationMapper: ObjectMapper = mapper
) : OrchestrationCallback, ConnectorCallbackBase(applicationId, connectorType) {

    private val logger = KotlinLogging.logger {}

    override fun sendResponse() {
        sendResponse(SecondaryBotResponse.fromActions(applicationId, actions))
    }

    override fun sendResponse(response: SecondaryBotResponse) {
        try {
            sendHttpResponse(OK, response)
        } catch (exception: Exception) {
            logger.error(exception)
            sendError()
        }
    }

    override fun sendError() {
        sendHttpResponse(
            INTERNAL_SERVER_ERROR,
            SecondaryBotNoResponse(
                status = ERROR,
                metaData = OrchestrationMetaData(
                    playerId = PlayerId("unknownWithError"),
                    applicationId = applicationId,
                    recipientId = PlayerId("bot")
                )
            )
        )
    }

    override fun exceptionThrown(event: Event, throwable: Throwable) {
        super.exceptionThrown(event, throwable)
        sendError()
    }

    private fun sendHttpResponse(httpCode: HttpResponseStatus, response: SecondaryBotResponse) {
        val res = context.response()
        res.putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
        res.statusCode = httpCode.code()
        res.end(orchestrationMapper.writeValueAsString(response))
    }
}

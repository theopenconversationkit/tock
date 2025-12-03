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

package ai.tock.bot.connector.alcmeon

import ai.tock.bot.connector.ConnectorCallbackBase
import ai.tock.bot.engine.action.Action
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging

data class AlcmeonConnectorCallback(
    override val applicationId: String,
    val backend: AlcmeonBackend,
    val context: RoutingContext,
) : ConnectorCallbackBase(applicationId, alcmeonConnectorType) {
    private val logger = KotlinLogging.logger {}

    private val actions = mutableListOf<DelayedAction>()

    fun addAction(
        action: Action,
        delay: Long,
    ) {
        actions.add(DelayedAction(action, delay))
    }

    fun sendResponseWithoutExit() {
        sendResponse()
    }

    fun sendResponseWithExit(
        exitReason: String,
        delayInMs: Long,
    ) {
        sendResponse(exitReason, delayInMs)
    }

    private fun sendResponse(
        exitReason: String? = null,
        delayInMs: Long = 0L,
    ) {
        val response = AlcmeonMessageConverter.toMessageOut(actions, backend, exitReason, delayInMs)
        val serializedResponse = jacksonObjectMapper().writeValueAsString(response)
        logger.info { "Alcmeon connector callback response : $serializedResponse" }
        context.response()
            .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .end(serializedResponse)
    }
}

data class DelayedAction(val action: Action, val delay: Long)

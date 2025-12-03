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
import ai.tock.bot.engine.event.Event
import ai.tock.shared.jackson.mapper
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
) : ConnectorCallbackBase(applicationId, connectorType) {
    companion object {
        private val logger = KotlinLogging.logger {}
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
        if (actions.isEmpty()) {
            context.fail(throwable ?: IllegalStateException("no response available"))
        } else {
            val nlpStats = if (checkNlpStats) (userAction as? SendSentence)?.nlpStats else null
            val r =
                mapper.writeValueAsString(
                    MessageResponse(
                        actions.filter {
                            if (!userAction.metadata.debugEnabled) it !is SendDebug else true
                        }.map { it.toMessage() },
                        applicationId,
                        userAction.id.toString(),
                        nlpStats?.locale ?: locale,
                        userAction is SendSentence,
                    ),
                )
            logger.debug { "response : $r" }
            context.response().end(r)
        }
    }
}

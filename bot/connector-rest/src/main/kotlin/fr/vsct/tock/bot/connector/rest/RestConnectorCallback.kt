/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.connector.rest

import fr.vsct.tock.bot.connector.ConnectorCallbackBase
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.connector.rest.model.MessageResponse
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.event.Event
import fr.vsct.tock.shared.jackson.mapper
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

/**
 *
 */
internal class RestConnectorCallback(
        applicationId: String,
        connectorType: ConnectorType,
        val context: RoutingContext,
        val test: Boolean,
        val locale: Locale,
        val actions: MutableList<Action> = CopyOnWriteArrayList()
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

    override fun exceptionThrown(event: Event, throwable: Throwable) {
        super.exceptionThrown(event, throwable)
        context.fail(throwable)
    }

    private fun sendAnswer() {
        val r = mapper.writeValueAsString(MessageResponse(
                actions.map { it.toMessage() }
        ))
        logger.debug { "response : $r" }
        context.response().end(r)
    }
}
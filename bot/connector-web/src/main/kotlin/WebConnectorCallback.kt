/*
 * Copyright (C) 2017/2019 VSCT
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

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import ai.tock.bot.connector.ConnectorCallbackBase
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.event.Event
import ai.tock.shared.jackson.mapper
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

internal class WebConnectorCallback(
    applicationId: String,
    val locale: Locale,
    private val context: RoutingContext,
    private val actions: MutableList<Action> = CopyOnWriteArrayList()
) : ConnectorCallbackBase(applicationId, webConnectorType) {

    private val logger = KotlinLogging.logger {}
    private val webMapper = mapper.copy().registerModule(
        SimpleModule().apply {
            //fallback for serializing CharSequence
            addSerializer(CharSequence::class.java, ToStringSerializer())
        }
    )

    fun addAction(event: Event) {
        if (event is Action) {
            actions.add(event)
        } else {
            logger.trace { "unsupported event: $event" }
        }
    }

    fun sendResponse() {
        val messages = actions
            .filterIsInstance<SendSentence>()
            .mapNotNull {
                if (it.stringText != null) {
                    WebMessage(it.stringText!!)
                } else it.message(webConnectorType)?.let {
                    it as? WebMessage
                }

            }
        context.response().end(webMapper.writeValueAsString(WebConnectorResponse(messages)))
    }
}
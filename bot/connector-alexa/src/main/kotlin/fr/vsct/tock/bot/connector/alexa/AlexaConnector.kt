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

package fr.vsct.tock.bot.connector.alexa

import com.amazon.speech.speechlet.lambda.LambdaSpeechletRequestHandler
import fr.vsct.tock.bot.connector.ConnectorBase
import fr.vsct.tock.bot.connector.ConnectorCallback
import fr.vsct.tock.bot.engine.BotRepository
import fr.vsct.tock.bot.engine.ConnectorController
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.event.Event
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging

/**
 *
 */
class AlexaConnector internal constructor(
        val applicationId: String,
        val path: String,
        val alexaTockMapper:AlexaTockMapper,
        supportedAlexaApplicationIds: Set<String>)
    : ConnectorBase(AlexaConnectorProvider.connectorType) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val requestHandler = LambdaSpeechletRequestHandler(supportedAlexaApplicationIds)

    override fun register(controller: ConnectorController) {
        controller.registerServices(path, { router ->
            logger.info("deploy rest alexa services for root path $path ")

            router.post(path).blockingHandler { context ->
                try {
                    handleRequest(controller, context)
                } catch (e: Throwable) {
                    context.fail(e)
                }
            }
        })
    }

    override fun send(event: Event, callback: ConnectorCallback, delayInMs: Long) {
        callback as AlexaConnectorCallback
        callback.addAction(event, delayInMs)
        if (event is Action) {
            if (event.metadata.lastAnswer) {
                callback.sendResponse()
            }
        } else {
            logger.trace { "unsupported event: $event" }
        }
    }

    //internal for tests
    internal fun handleRequest(controller: ConnectorController,
                               context: RoutingContext) {
        val timerData = BotRepository.requestTimer.start("alexa_webhook")
        try {
            context.response().end(
                    Buffer.buffer(
                            requestHandler.handleSpeechletCall(
                                    AlexaConnectorCallback(applicationId, controller, alexaTockMapper, context),
                                    context.body.bytes
                            )
                    )
            )
        } catch (t: Throwable) {
            BotRepository.requestTimer.throwable(t, timerData)
            context.fail(t)
        } finally {
            BotRepository.requestTimer.end(timerData)
        }
    }


}
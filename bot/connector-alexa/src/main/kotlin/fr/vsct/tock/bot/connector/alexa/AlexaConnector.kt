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

import com.amazon.speech.Sdk
import com.amazon.speech.speechlet.IntentRequest
import com.amazon.speech.speechlet.SpeechletRequestHandler
import com.amazon.speech.speechlet.SpeechletRequestHandlerException
import com.amazon.speech.speechlet.authentication.SpeechletRequestSignatureVerifier
import com.amazon.speech.speechlet.verifier.ApplicationIdSpeechletRequestEnvelopeVerifier
import com.amazon.speech.speechlet.verifier.SpeechletRequestVerifierWrapper
import com.amazon.speech.speechlet.verifier.TimestampSpeechletRequestVerifier
import fr.vsct.tock.bot.connector.ConnectorBase
import fr.vsct.tock.bot.connector.ConnectorCallback
import fr.vsct.tock.bot.engine.BotRepository
import fr.vsct.tock.bot.engine.ConnectorController
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.event.Event
import fr.vsct.tock.shared.booleanProperty
import fr.vsct.tock.shared.error
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging
import java.util.concurrent.TimeUnit

/**
 * [Connector] for Amazon Alexa.
 */
class AlexaConnector internal constructor(
    val applicationId: String,
    val path: String,
    val alexaTockMapper: AlexaTockMapper,
    supportedAlexaApplicationIds: Set<String>,
    timestampInMs: Long
) : ConnectorBase(AlexaConnectorProvider.connectorType) {

    companion object {
        private val logger = KotlinLogging.logger {}
        private val disableRequestSignatureCheck = booleanProperty("tock_alexa_disable_request_signature_check", false)

        internal fun sendTechnicalError(
            context: RoutingContext,
            throwable: Throwable,
            request: IntentRequest? = null
        ) {
            try {
                logger.error("request: $request", throwable)
                if (throwable is SpeechletRequestHandlerException || throwable is SecurityException) {
                    context.fail(400)
                } else {
                    context.fail(throwable)
                }
            } catch (t: Throwable) {
                logger.error(t)
                context.fail(t)
            }
        }
    }

    private val requestHandler = SpeechletRequestHandler(
        listOf(
            ApplicationIdSpeechletRequestEnvelopeVerifier(supportedAlexaApplicationIds),
            SpeechletRequestVerifierWrapper(TimestampSpeechletRequestVerifier(timestampInMs, TimeUnit.MILLISECONDS))
        )
    )

    override fun register(controller: ConnectorController) {
        controller.registerServices(path) { router ->
            logger.info("deploy rest alexa services for root path $path ")

            router.post(path).blockingHandler { context ->
                try {
                    handleRequest(controller, context)
                } catch (e: Throwable) {
                    context.fail(e)
                }
            }
        }
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
    internal fun handleRequest(
        controller: ConnectorController,
        context: RoutingContext
    ) {
        val timerData = BotRepository.requestTimer.start("alexa_webhook")
        try {
            val bytes = context.body.bytes
            // Check certificate
            if (!disableRequestSignatureCheck) {
                SpeechletRequestSignatureVerifier.checkRequestSignature(
                    bytes,
                    context.request().getHeader(Sdk.SIGNATURE_REQUEST_HEADER),
                    context.request().getHeader(Sdk.SIGNATURE_CERTIFICATE_CHAIN_URL_REQUEST_HEADER)
                )
            }
            context
                .response()
                .putHeader("Content-Type", "application/json")
                .end(
                    Buffer.buffer(
                        requestHandler.handleSpeechletCall(
                            AlexaConnectorCallback(applicationId, controller, alexaTockMapper, context),
                            bytes
                        )
                    )
                )
        } catch (ex: Throwable) {
            BotRepository.requestTimer.throwable(ex, timerData)
            sendTechnicalError(context, ex)
        } finally {
            BotRepository.requestTimer.end(timerData)
        }
    }


}
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

package ai.tock.bot.connector.alexa

import ai.tock.bot.connector.ConnectorCallbackBase
import ai.tock.bot.connector.ConnectorData
import ai.tock.bot.connector.alexa.AlexaConnector.Companion.sendTechnicalError
import ai.tock.bot.engine.BotRepository
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.event.Event
import ai.tock.shared.concat
import ai.tock.shared.defaultLocale
import ai.tock.shared.error
import ai.tock.shared.jackson.mapper
import ai.tock.translator.UserInterfaceType
import ai.tock.translator.isSSML
import com.amazon.speech.json.SpeechletRequestEnvelope
import com.amazon.speech.slu.Intent
import com.amazon.speech.speechlet.IntentRequest
import com.amazon.speech.speechlet.LaunchRequest
import com.amazon.speech.speechlet.SessionEndedRequest
import com.amazon.speech.speechlet.SessionStartedRequest
import com.amazon.speech.speechlet.SpeechletResponse
import com.amazon.speech.speechlet.SpeechletV2
import com.amazon.speech.ui.PlainTextOutputSpeech
import com.amazon.speech.ui.Reprompt
import com.amazon.speech.ui.SsmlOutputSpeech
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

/**
 * The alexa connector callback.
 */
data class AlexaConnectorCallback internal constructor(
    override val applicationId: String,
    private val controller: ConnectorController,
    private val alexaTockMapper: AlexaTockMapper,
    private val context: RoutingContext,
    private val actions: MutableList<ActionWithDelay> = CopyOnWriteArrayList()
) : ConnectorCallbackBase(applicationId, alexaConnectorType), SpeechletV2 {

    @Volatile
    private var answered: Boolean = false

    @Volatile
    internal var alexaResponse: SpeechletResponse? = null

    @Volatile
    private var locale: Locale = defaultLocale

    /**
     * The alexa raw request.
     */
    val alexaRequest: SpeechletRequestEnvelope<*>? get() = _alexaRequest

    @Volatile
    private var _alexaRequest: SpeechletRequestEnvelope<*>? = null

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    internal data class ActionWithDelay(val action: Action, val delayInMs: Long = 0)

    internal fun addAction(event: Event, delayInMs: Long) {
        if (event is Action) {
            actions.add(ActionWithDelay(event, delayInMs))
        } else {
            logger.trace { "unsupported event: $event" }
        }
    }

    private fun buildResponse(): SpeechletResponse {
        val answer = actions.mapNotNull { it.action as? SendSentence }
            .mapNotNull { it.stringText }
            .takeUnless { it.isEmpty() }
            ?.reduce(::concat)
        val end = actions.map { it.action }.filterIsInstance<SendSentence>().any {
            (it.message(alexaConnectorType) as? AlexaMessage?)?.end ?: false
        }
        val card =
            actions.map { it.action }
                .filterIsInstance<SendSentence>()
                .mapNotNull { it.message(alexaConnectorType) as? AlexaMessage? }
                .firstOrNull { it.card != null }
                ?.card
        val speech =
            if (answer == null) PlainTextOutputSpeech().apply {

                text = controller.botDefinition.i18nTranslator(
                    locale,
                    alexaConnectorType,
                    UserInterfaceType.voiceAssistant
                ).translate(controller.botDefinition.defaultUnknownAnswer).toString()
            }
            else if (answer.isSSML()) SsmlOutputSpeech().apply { ssml = answer }
            else PlainTextOutputSpeech().apply { text = answer }

        val reprompt = actions.map { it.action }
            .filterIsInstance<SendSentence>()
            .mapNotNull { it.message(alexaConnectorType) as? AlexaMessage? }
            .firstOrNull { it.reprompt != null }
            ?.reprompt
            ?.let {
                if (it.isSSML()) SsmlOutputSpeech().apply { ssml = it }
                else PlainTextOutputSpeech().apply { text = it }
            }
            ?: speech
        return if (end) {
            if (card != null) {
                SpeechletResponse.newTellResponse(speech, card)
            } else {
                SpeechletResponse.newTellResponse(speech)
            }
        } else {
            if (card != null) {
                SpeechletResponse.newAskResponse(
                    speech,
                    Reprompt().apply { outputSpeech = reprompt },
                    card
                )
            } else {
                SpeechletResponse.newAskResponse(
                    speech,
                    Reprompt().apply { outputSpeech = reprompt }
                )
            }
        }
    }

    internal fun sendResponse() {
        try {
            if (!answered) {
                answered = true
                alexaResponse = buildResponse()
                logResponse()
            } else {
                logger.trace { "already answered: $this" }
            }
        } catch (t: Throwable) {
            logger.error(t)
            context.fail(t)
        }
    }

    override fun exceptionThrown(event: Event, throwable: Throwable) {
        super.exceptionThrown(event, throwable)
        sendTechnicalError(context, throwable)
    }

    private fun logRequest(method: String, req: SpeechletRequestEnvelope<*>) {
        _alexaRequest = req
        try {
            logger.debug {
                "$method : \n${mapper.writeValueAsString(req.context)}\n${mapper.writeValueAsString(req.session)}\n${mapper.writeValueAsString(
                    req.request
                )}"
            }
        } catch (e: Exception) {
            logger.error(e)
        }
    }

    private fun logResponse() {
        try {
            logger.debug {
                "response: ${mapper.writeValueAsString(alexaResponse)}"
            }
        } catch (e: Exception) {
            logger.error(e)
        }
    }

    override fun onSessionStarted(requestEnvelope: SpeechletRequestEnvelope<SessionStartedRequest>) {
        logRequest("onSessionStarted", requestEnvelope)
        controller.handle(
            alexaTockMapper.toStartSessionEvent(requestEnvelope),
            ConnectorData(this)
        )
    }

    override fun onSessionEnded(requestEnvelope: SpeechletRequestEnvelope<SessionEndedRequest>) {
        logRequest("onSessionEnded", requestEnvelope)
        controller.handle(
            alexaTockMapper.toEndSessionEvent(requestEnvelope),
            ConnectorData(this)
        )
    }

    override fun onIntent(requestEnvelope: SpeechletRequestEnvelope<IntentRequest>): SpeechletResponse? {
        logRequest("onIntent", requestEnvelope)

        val timerData = BotRepository.requestTimer.start("alexa_webhook")
        try {
            val event = alexaTockMapper.toEvent(
                requestEnvelope.session.user.userId,
                requestEnvelope.request,
                controller.botDefinition
            )
            locale = Locale(requestEnvelope.request.locale.language)

            controller.handle(event, ConnectorData(this))
        } catch (t: Throwable) {
            BotRepository.requestTimer.throwable(t, timerData)
            sendTechnicalError(context, t, requestEnvelope.request)
        } finally {
            BotRepository.requestTimer.end(timerData)
        }

        return alexaResponse
    }

    override fun onLaunch(requestEnvelope: SpeechletRequestEnvelope<LaunchRequest>): SpeechletResponse? {
        logRequest("onLaunch", requestEnvelope)
        val helloStory = controller.botDefinition.defaultStory.mainIntent().name
        return onIntent(
            SpeechletRequestEnvelope
                .builder<IntentRequest>()
                .withContext(requestEnvelope.context)
                .withSession(requestEnvelope.session)
                .withVersion(requestEnvelope.version)
                .withRequest(
                    IntentRequest
                        .builder()
                        .withRequestId(requestEnvelope.request.requestId)
                        .withTimestamp(requestEnvelope.request.timestamp)
                        .withLocale(requestEnvelope.request.locale)
                        .withIntent(Intent.builder().withName(helloStory).build())
                        .withDialogState(IntentRequest.DialogState.STARTED)
                        .build()
                )
                .build()
        )
    }
}

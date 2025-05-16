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

import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.dialog.EventState
import ai.tock.bot.engine.event.EndSessionEvent
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.event.StartSessionEvent
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.nlp.api.client.model.Entity
import ai.tock.nlp.api.client.model.EntityType
import ai.tock.nlp.api.client.model.NlpEntityValue
import ai.tock.nlp.api.client.model.NlpResult
import com.amazon.speech.json.SpeechletRequestEnvelope
import com.amazon.speech.slu.Slot
import com.amazon.speech.speechlet.IntentRequest
import com.amazon.speech.speechlet.SessionEndedRequest
import com.amazon.speech.speechlet.SessionStartedRequest
import java.util.Locale

/**
 * An Alexa model to Tock model mapper.
 * Provided via [addAlexaConnector.alexaTockMapper] parameter.
 */
open class AlexaTockMapper(val applicationId: String) {

    /**
     * Returns a Tock intent from an Alexa intent.
     */
    open fun alexaIntentToTockIntent(request: IntentRequest, botDefinition: BotDefinition): String {
        val intentName = request.intent.name
        return if (intentName.endsWith("_intent")) {
            intentName.substring(0, intentName.length - "_intent".length)
        } else {
            intentName
        }
    }

    /**
     * Returns a Tock entity from an Alexa slot.
     */
    open fun alexaEntityToTockEntity(
        request: IntentRequest,
        intent: String,
        slot: String,
        botDefinition: BotDefinition
    ): Entity? {
        val entityName = slot.substring(0, slot.length - "_slot".length)
        val namespace = botDefinition.namespace
        return Entity(EntityType("$namespace:$entityName"), entityName)
    }

    /**
     * Returns a Tock [NlpEntityValue] from an Alexa slot.
     */
    open fun alexaEntityToTockEntityValue(
        request: IntentRequest,
        intent: String,
        slot: Slot,
        botDefinition: BotDefinition,
        index: Int
    ): NlpEntityValue {
        val entity = alexaEntityToTockEntity(request, intent, slot.name, botDefinition)!!
        return NlpEntityValue(
            index,
            index + slot.value!!.length,
            entity
        )
    }

    /**
     * Gets slots from the intent request.
     */
    open fun getSlots(request: IntentRequest): Map<String, Slot>? = request.intent?.slots

    /**
     * Returns a [StartSessionEvent] from an Alexa [SessionStartedRequest].
     */
    open fun toStartSessionEvent(requestEnvelope: SpeechletRequestEnvelope<SessionStartedRequest>): StartSessionEvent {
        return StartSessionEvent(
            PlayerId(requestEnvelope.session.user.userId, PlayerType.user),
            applicationId
        )
    }

    /**
     * Returns a [EndSessionEvent] from an Alexa [SessionEndedRequest].
     */
    open fun toEndSessionEvent(requestEnvelope: SpeechletRequestEnvelope<SessionEndedRequest>): EndSessionEvent {
        return EndSessionEvent(
            PlayerId(requestEnvelope.session.user.userId, PlayerType.user),
            applicationId
        )
    }

    /**
     * Returns an [Event] from an Alexa [IntentRequest].
     */
    open fun toEvent(userId: String, request: IntentRequest, botDefinition: BotDefinition): Event {
        val playerId = PlayerId(userId, PlayerType.user)
        val botId = PlayerId(applicationId, PlayerType.bot)
        val namespace = botDefinition.namespace
        val intent = alexaIntentToTockIntent(request, botDefinition)
        var index = 0

        val slots = getSlots(request)?.values
            ?.filter { it.value != null && alexaEntityToTockEntity(request, intent, it.name, botDefinition) != null }
            ?: emptyList()
        val entityValues =
            slots.map {
                val value = alexaEntityToTockEntityValue(request, intent, it, botDefinition, index)
                index += it.value.length + 1
                value
            }

        val precomputedNlp = NlpResult(
            intent,
            namespace,
            Locale(request.locale.language),
            entityValues,
            emptyList(),
            1.0,
            1.0,
            slots.joinToString(" ") { it.value!! }
        )
        return SendSentence(
            playerId,
            applicationId,
            botId,
            null,
            messages = mutableListOf(AlexaInputMessage(request)),
            state = EventState(
                targetConnectorType = alexaConnectorType,
                userInterface = alexaConnectorType.userInterfaceType
            ),
            precomputedNlp = precomputedNlp
        )
    }
}

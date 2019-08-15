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

package fr.vsct.tock.bot.engine.action

import fr.vsct.tock.bot.definition.Intent
import fr.vsct.tock.bot.definition.IntentAware
import fr.vsct.tock.bot.definition.StoryHandlerDefinition
import fr.vsct.tock.bot.definition.StoryStep
import fr.vsct.tock.bot.engine.Bus
import fr.vsct.tock.bot.engine.dialog.EventState
import fr.vsct.tock.bot.engine.event.Event
import fr.vsct.tock.bot.engine.message.Choice
import fr.vsct.tock.bot.engine.message.Message
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.shared.mapNotNullValues
import fr.vsct.tock.shared.security.StringObfuscatorMode
import fr.vsct.tock.shared.security.TockObfuscatorService
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.net.URLDecoder.decode
import java.net.URLEncoder.encode
import java.nio.charset.StandardCharsets.UTF_8
import java.time.Instant

/**
 * A user choice (click on a button or direct action).
 */
class SendChoice(
    playerId: PlayerId,
    applicationId: String,
    recipientId: PlayerId,
    val intentName: String,
    val parameters: Map<String, String> = emptyMap(),
    id: Id<Action> = newId(),
    date: Instant = Instant.now(),
    state: EventState = EventState(),
    metadata: ActionMetadata = ActionMetadata()
) : Action(playerId, recipientId, applicationId, id, date, state, metadata) {

    constructor(
        playerId: PlayerId,
        applicationId: String,
        recipientId: PlayerId,
        intentName: String,
        step: StoryStep<out StoryHandlerDefinition>?,
        parameters: Map<String, String> = emptyMap(),
        id: Id<Action> = newId(),
        date: Instant = Instant.now(),
        state: EventState = EventState(),
        metadata: ActionMetadata = ActionMetadata()
    ) :
        this(
            playerId,
            applicationId,
            recipientId,
            intentName,
            parameters + mapNotNullValues(STEP_PARAMETER to step?.name),
            id,
            date,
            state,
            metadata
        )

    companion object {

        const val TITLE_PARAMETER = "_title"
        const val URL_PARAMETER = "_url"
        const val IMAGE_PARAMETER = "_image"
        const val EXIT_INTENT = "_exit"
        const val PHONE_CALL_INTENT = "_phone_call"
        const val LOGIN_INTENT = "_login"
        const val LOGOUT_INTENT = "_logout"
        const val STEP_PARAMETER = "_step"
        const val PREVIOUS_INTENT_PARAMETER = "_previous_intent"
        const val NLP = "_nlp"

        /**
         * Encodes a choice id where text will be analysed by NLP engine.
         */
        fun encodeNlpChoiceId(text: String): String {
            return "?$NLP=${encode(text, UTF_8.name())}"
        }

        internal fun nlpParametersMap(text: String): Map<String, String> =
            mapOf(
                NLP to text,
                TITLE_PARAMETER to text
            )

        /**
         * Encodes a choice id.
         */
        fun encodeChoiceId(
            /**
             * The bus.
             */
            bus: Bus<*>,
            /**
             * The target intent.
             */
            intent: IntentAware,
            /**
             * The target step.
             */
            step: StoryStep<out StoryHandlerDefinition>? = null,
            /**
             * The custom parameters.
             */
            parameters: Map<String, String> = emptyMap()
        ): String {
            return encodeChoiceId(intent, step?.name, parameters, bus.stepName, bus.intent?.wrappedIntent())
        }

        /**
         * Encodes a choice id.
         */
        fun encodeChoiceId(
            /**
             * The bus.
             */
            bus: Bus<*>,
            /**
             * The target intent.
             */
            intent: IntentAware,
            /**
             * The target step.
             */
            step: String? = null,
            /**
             * The custom parameters.
             */
            parameters: Map<String, String> = emptyMap()
        ): String {
            return encodeChoiceId(intent, step, parameters, bus.stepName, bus.intent?.wrappedIntent())
        }

        /**
         * Encodes a choice id.
         */
        fun encodeChoiceId(
            /**
             * The target intent.
             */
            intent: IntentAware,
            /**
             * The target step.
             */
            step: StoryStep<out StoryHandlerDefinition>? = null,
            /**
             * The custom parameters.
             */
            parameters: Map<String, String> = emptyMap(),
            /**
             * The current step of the bus.
             */
            busStep: StoryStep<out StoryHandlerDefinition>? = null,
            /**
             * The current intent of the bus.
             */
            currentIntent: Intent? = null
        ): String =
            encodeChoiceId(
                intent,
                step?.name,
                parameters,
                busStep?.name,
                currentIntent
            )

        /**
         * Encodes a choice id.
         */
        fun encodeChoiceId(
            /**
             * The target intent.
             */
            intent: IntentAware,
            /**
             * The target step.
             */
            step: String?,
            /**
             * The custom parameters.
             */
            parameters: Map<String, String>,
            /**
             * The current step of the bus.
             */
            busStep: String?,
            /**
             * The current intent of the bus.
             */
            currentIntent: Intent?
        ): String {
            val currentStep = if (step == null) busStep else step
            return StringBuilder().apply {
                append(intent.wrappedIntent().name)
                val params = parameters +
                    listOfNotNull(
                        if (currentStep != null) STEP_PARAMETER to currentStep else null,
                        if (currentIntent != null && currentIntent != intent)
                            PREVIOUS_INTENT_PARAMETER to currentIntent.name else null
                    )

                if (params.isNotEmpty()) {
                    params.map { e ->
                        "${encode(e.key, UTF_8.name())}=${encode(e.value, UTF_8.name())}"
                    }.joinTo(this, "&", "?")
                }
            }.toString()
        }

        /**
         * Decodes an id - returns the [intentName] and the [parameters] map.
         */
        fun decodeChoiceId(id: String): Pair<String, Map<String, String>> {
            val questionMarkIndex = id.indexOf("?")
            return if (questionMarkIndex == -1) {
                id to emptyMap()
            } else {
                id.substring(0, questionMarkIndex) to id.substring(questionMarkIndex + 1)
                    .split("&")
                    .map {
                        it.split("=")
                            .let { decode(it[0], UTF_8.name()) to decode(it[1], UTF_8.name()) }
                    }.toMap()
            }
        }

    }

    override fun toMessage(): Message {
        return Choice(intentName, parameters)
    }

    override fun obfuscate(mode: StringObfuscatorMode, playerId: PlayerId): Event {
        return SendChoice(
            playerId,
            applicationId,
            recipientId,
            intentName,
            TockObfuscatorService.obfuscate(parameters),
            toActionId(),
            date,
            state,
            metadata
        )
    }

    /**
     * The step of this choice (when applicable).
     */
    fun step(): String? = parameters[STEP_PARAMETER]

    internal fun previousIntent(): String? = parameters[PREVIOUS_INTENT_PARAMETER]

    /**
     * Provides the id used by connectors.
     */
    fun toEncodedId(): String = encodeChoiceId(Intent(intentName), step(), parameters, null, null)

    override fun toString(): String {
        return "SendChoice(intentName='$intentName', parameters=$parameters)"
    }


}
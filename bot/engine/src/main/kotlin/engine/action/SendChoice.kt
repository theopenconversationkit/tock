/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.bot.engine.action

import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.definition.StoryHandlerDefinition
import ai.tock.bot.definition.StoryStep
import ai.tock.bot.engine.Bus
import ai.tock.bot.engine.dialog.EventState
import ai.tock.bot.engine.message.Choice
import ai.tock.bot.engine.message.Message
import ai.tock.bot.engine.user.PlayerId
import ai.tock.shared.mapNotNullValues
import java.net.URLDecoder.decode
import java.net.URLEncoder.encode
import java.nio.charset.StandardCharsets.UTF_8
import java.time.Instant
import org.litote.kmongo.Id
import org.litote.kmongo.newId

/**
 * A user choice (click on a button or direct action).
 *
 * @param applicationId the TOCK application id (matches the id of the connector)
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
        const val SOURCE_APP_ID = "_source"
        const val REFERRAL_PARAMETER = "_referral"

        /**
         * Encodes a choice id where text will be analysed by NLP engine.
         */
        fun encodeNlpChoiceId(text: String): String {
            return "?$NLP=${encode(text, UTF_8.name())}"
        }

        internal fun nlpParametersMap(
            title: String,
            nlpText: String? = null,
            imageUrl: String? = null
        ): Map<String, String> =
            mapNotNullValues(
                NLP to (nlpText ?: title),
                TITLE_PARAMETER to title,
                IMAGE_PARAMETER to imageUrl
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
            return encodeChoiceId(
                intent,
                step?.name,
                parameters,
                bus.stepName,
                bus.currentIntent?.wrappedIntent(),
                sourceAppId = bus.applicationId
            )
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
            return encodeChoiceId(
                intent,
                step,
                parameters,
                bus.stepName,
                bus.currentIntent?.wrappedIntent(),
                sourceAppId = bus.applicationId
            )
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
            currentIntent: Intent? = null,
            /**
             * The app id emitter
             */
            sourceAppId: String? = null
        ): String =
            encodeChoiceId(
                intent,
                step?.name,
                parameters,
                busStep?.name,
                currentIntent,
                sourceAppId
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
            currentIntent: Intent?,
            /**
             * The app id emitter
             */
            sourceAppId: String? = null
        ): String {
            val currentStep = step ?: busStep
            return StringBuilder().apply {
                append(intent.wrappedIntent().name)
                val params = parameters +
                    listOfNotNull(
                        if (currentStep != null) STEP_PARAMETER to currentStep else null,
                        if (currentIntent != null && currentIntent != intent)
                            PREVIOUS_INTENT_PARAMETER to currentIntent.name else null,
                        if (sourceAppId != null) SOURCE_APP_ID to sourceAppId else null
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
                    .map { s ->
                        s.split("=")
                            .let { decode(it[0], UTF_8.name()) to decode(it[1], UTF_8.name()) }
                    }.toMap()
            }
        }

        /**
         * Decodes an id and returns an action.
         */
        fun decodeChoice(
            id: String,
            senderId: PlayerId,
            applicationId: String,
            recipientId: PlayerId,
            referralParameter: String? = null
        ): Action =
            decodeChoiceId(id)
                .let { (intentName, parameters) ->
                    if (parameters.containsKey(NLP)) {
                        SendSentence(
                            senderId,
                            applicationId,
                            recipientId,
                            parameters[NLP]
                        )
                    } else {
                        SendChoice(
                            playerId = senderId,
                            applicationId = applicationId,
                            recipientId = recipientId,
                            intentName = intentName,
                            parameters = parameters + (
                                if (referralParameter == null) emptyMap() else mapOf(
                                    REFERRAL_PARAMETER to referralParameter
                                )
                                )
                        )
                    }
                }
    }

    override fun toMessage(): Message {
        return Choice(intentName, parameters)
    }

    /**
     * The step of this choice (when applicable).
     */
    fun step(): String? = parameters[STEP_PARAMETER]

    /**
     * The source application id (if any) ie the creator of the choice.
     */
    fun sourceAppId(): String? = parameters[SOURCE_APP_ID]

    internal fun previousIntent(): String? = parameters[PREVIOUS_INTENT_PARAMETER]

    /**
     * Provides the id used by connectors.
     */
    fun toEncodedId(): String = encodeChoiceId(Intent(intentName), step(), parameters, null, null)

    /**
     * Returns the referral parameter if any.
     */
    val referralParameter: String? get() = parameters[REFERRAL_PARAMETER]

    override fun toString(): String {
        return "SendChoice(intentName='$intentName', parameters=$parameters)"
    }
}

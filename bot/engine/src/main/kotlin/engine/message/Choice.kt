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

package ai.tock.bot.engine.message

import ai.tock.bot.definition.StoryStepDef
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.event.EventType
import ai.tock.bot.engine.message.parser.MessageParser.mapToString
import ai.tock.bot.engine.user.PlayerId

/**
 * A user choice.
 */
data class Choice(
    val intentName: String,
    val parameters: Map<String, String> = emptyMap(),
    override val delay: Long = 0,
) : Message {
    companion object {
        /**
         * Returns a choice from text that will be analyzed by the NLP engine.
         */
        fun fromText(
            text: String,
            nlpText: String? = null,
            imageUrl: String? = null,
        ): Choice =
            Choice(
                "",
                SendChoice.nlpParametersMap(text, nlpText, imageUrl),
            )
    }

    constructor(
        intentName: String,
        step: StoryStepDef,
        parameters: Map<String, String> = emptyMap(),
        delay: Long = 0,
    ) :
        this(intentName, parameters + (SendChoice.STEP_PARAMETER to step.name), delay)

    override val eventType: EventType = EventType.choice

    override fun toAction(
        playerId: PlayerId,
        applicationId: String,
        recipientId: PlayerId,
    ): Action {
        return SendChoice(
            playerId,
            applicationId,
            recipientId,
            intentName,
            parameters,
        )
    }

    override fun toPrettyString(): String {
        return "{$eventType:$intentName,${mapToString(parameters)}}"
    }

    override fun isSimpleMessage(): Boolean = false
}

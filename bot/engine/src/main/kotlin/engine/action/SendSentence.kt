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

package ai.tock.bot.engine.action

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.engine.dialog.EventState
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.message.Message
import ai.tock.bot.engine.message.Sentence
import ai.tock.bot.engine.nlp.NlpCallStats
import ai.tock.bot.engine.user.PlayerId
import ai.tock.nlp.api.client.model.NlpResult
import ai.tock.shared.security.StringObfuscatorMode
import ai.tock.shared.security.TockObfuscatorService
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.time.Instant

/**
 * The most important [Action] class.
 * Could be a simple text, or a complex message using one or more [ConnectorMessage].
 */
open class SendSentence(
        playerId: PlayerId,
        applicationId: String,
        recipientId: PlayerId,
        val text: CharSequence?,
        open val messages: MutableList<ConnectorMessage> = mutableListOf(),
        id: Id<Action> = newId(),
        date: Instant = Instant.now(),
        state: EventState = EventState(),
        metadata: ActionMetadata = ActionMetadata(),
        open var nlpStats: NlpCallStats? = null,
        /**
         * Used by analysed nlp (ie Alexa).
         */
        val precomputedNlp:NlpResult? = null)
    : Action(playerId, recipientId, applicationId, id, date, state, metadata) {

    @Transient
    val stringText: String? = text?.toString()

    fun message(type: ConnectorType): ConnectorMessage? {
        return messages.find { it.connectorType == type }
    }

    fun hasMessage(type: ConnectorType): Boolean {
        return messages.any { it.connectorType == type }
    }

    override fun toMessage(): Message {
        return Sentence(stringText, messages, state.userInterface)
    }

    override fun obfuscate(mode: StringObfuscatorMode, playerId: PlayerId): Event {
        return SendSentence(
                playerId,
                applicationId,
                recipientId,
                TockObfuscatorService.obfuscate(stringText, mode),
                messages.map { it.obfuscate(mode) }.toMutableList(),
                toActionId(),
                date,
                state,
                metadata,
                nlpStats
        )
    }

    override fun toString(): String {
        return if (stringText != null) stringText else if (messages.isNotEmpty()) messages.toString() else ""
    }

    fun hasEmptyText() : Boolean = precomputedNlp == null  && text.isNullOrBlank()

    /**
     * Replace a connectorMessage
     */
    fun changeConnectorMessage(message: ConnectorMessage):SendSentence{
        messages.removeAll { it.connectorType == message.connectorType }
        messages.add(message)
        return this
    }
}
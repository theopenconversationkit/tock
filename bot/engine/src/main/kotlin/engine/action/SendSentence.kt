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

import ai.tock.bot.admin.annotation.BotAnnotation
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.engine.dialog.EventState
import ai.tock.bot.engine.message.Message
import ai.tock.bot.engine.message.Sentence
import ai.tock.bot.engine.nlp.NlpCallStats
import ai.tock.bot.engine.user.PlayerId
import ai.tock.nlp.api.client.model.NlpResult
import java.time.Instant
import org.litote.kmongo.Id
import org.litote.kmongo.newId

/**
 * The most important [Action] class.
 * Could be a simple text, or a complex message using one or more [ConnectorMessage].
 */
open class SendSentence(
    playerId: PlayerId,
    connectorId: String,
    recipientId: PlayerId,
    val text: CharSequence?,
    open val messages: MutableList<ConnectorMessage> = mutableListOf(),
    id: Id<Action> = newId(),
    date: Instant = Instant.now(),
    state: EventState = EventState(),
    metadata: ActionMetadata = ActionMetadata(),
    open var nlpStats: NlpCallStats? = null,
    override var annotation: BotAnnotation? = null,
    /**
     * Used by analysed nlp (ie Alexa).
     */
    var precomputedNlp: NlpResult? = null
) :
    Action(playerId, recipientId, connectorId, id, date, state, metadata) {

    @Deprecated("Use constructor with connectorId", ReplaceWith("SendSentence(" +
            "playerId, " +
            "connectorId = applicationId, " +
            "recipientId, " +
            "text, " +
            "messages, " +
            "id, " +
            "date, " +
            "state, " +
            "metadata, " +
            "nlpStats, " +
            "annotation, " +
            "precomputedNlp)"))
    constructor(
        playerId: PlayerId,
        applicationId: String,
        recipientId: PlayerId,
        text: CharSequence?,
        messages: MutableList<ConnectorMessage> = mutableListOf(),
        id: Id<Action> = newId(),
        date: Instant = Instant.now(),
        state: EventState = EventState(),
        metadata: ActionMetadata = ActionMetadata(),
        nlpStats: NlpCallStats? = null,
        annotation: BotAnnotation? = null,
        precomputedNlp: NlpResult? = null,
        _deprecatedConstructor: Nothing? = null,
    ): this(playerId, applicationId, recipientId, text, messages, id, date, state, metadata, nlpStats, annotation, precomputedNlp)

    @Transient
    val stringText: String? = text?.toString()

    fun message(type: ConnectorType): ConnectorMessage? {
        return messages.find { it.connectorType == type }
    }

    fun hasMessage(type: ConnectorType): Boolean {
        return messages.any { it.connectorType == type }
    }

    fun hasMessage(types : List<ConnectorType>) : Boolean =
        messages.any { types.contains(it.connectorType) }

    override fun toMessage(): Message {
        return Sentence(stringText, messages, state.userInterface) { nlpStats }
    }

    override fun toString(): String {
        return stringText ?: if (messages.isNotEmpty()) messages.toString() else ""
    }

    fun hasEmptyText(): Boolean = precomputedNlp == null && text.isNullOrBlank()

    /**
     * Replace a connectorMessage
     */
    fun changeConnectorMessage(message: ConnectorMessage): SendSentence {
        messages.removeAll { it.connectorType == message.connectorType }
        messages.add(message)
        return this
    }
}

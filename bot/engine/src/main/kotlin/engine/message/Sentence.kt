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

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.event.EventType
import ai.tock.bot.engine.message.parser.MessageParser.elementsToString
import ai.tock.bot.engine.nlp.NlpCallStats
import ai.tock.bot.engine.user.PlayerId
import ai.tock.shared.error
import ai.tock.shared.security.TockObfuscatorService
import ai.tock.translator.UserInterfaceType
import mu.KotlinLogging

/**
 * Could be a simple text, or a complex message using secondary constructor.
 */
data class Sentence(
    val text: String?,
    val messages: MutableList<GenericMessage> = mutableListOf(),
    val userInterface: UserInterfaceType? = null,
    override val delay: Long = 0,
    @Transient private val nlpStatsProvider: NlpStatsProvider? = null,
) : Message {
    companion object {
        private val logger = KotlinLogging.logger {}

        private fun toGenericMessage(message: ConnectorMessage): GenericMessage =
            (
                try {
                    message.toGenericMessage() ?: GenericMessage(message)
                } catch (t: Throwable) {
                    logger.error(t)
                    GenericMessage(message)
                }
            ).copy(connectorType = message.connectorType, connectorMessage = message)
    }

    constructor(
        text: String?,
        messages: MutableList<ConnectorMessage> = mutableListOf(),
        userInterface: UserInterfaceType? = null,
        nlpStatsProvider: NlpStatsProvider? = null,
    ) :
        this(text, messages.map { toGenericMessage(it) }.toMutableList(), userInterface, 0, nlpStatsProvider)

    override val eventType: EventType = EventType.sentence

    override fun toAction(
        playerId: PlayerId,
        applicationId: String,
        recipientId: PlayerId,
    ): Action {
        return SendSentence(
            playerId,
            applicationId,
            recipientId,
            text,
            messages.mapNotNull {
                try {
                    it.findConnectorMessage()
                } catch (e: Exception) {
                    logger.error(e)
                    null
                }
            }.toMutableList(),
        )
    }

    override fun obfuscate(): Sentence =
        copy(
            text = TockObfuscatorService.obfuscate(text, nlpStatsProvider?.invoke()?.obfuscatedRanges() ?: emptyList()),
            messages = messages.asSequence().map { it.obfuscate() }.toMutableList(),
        )

    override fun toPrettyString(): String {
        return text ?: "{$eventType:${elementsToString(messages)}}"
    }

    override fun isSimpleMessage(): Boolean = text != null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Sentence

        if (text != other.text) return false
        if (messages != other.messages) return false
        if (userInterface != other.userInterface) return false
        if (delay != other.delay) return false
        if (eventType != other.eventType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = text?.hashCode() ?: 0
        result = 31 * result + messages.hashCode()
        result = 31 * result + (userInterface?.hashCode() ?: 0)
        result = 31 * result + delay.hashCode()
        result = 31 * result + eventType.hashCode()
        return result
    }
}

fun interface NlpStatsProvider {
    operator fun invoke(): NlpCallStats?
}

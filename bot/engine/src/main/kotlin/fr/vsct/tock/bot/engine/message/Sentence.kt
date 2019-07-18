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

package fr.vsct.tock.bot.engine.message

import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.event.EventType
import fr.vsct.tock.bot.engine.message.parser.MessageParser.elementsToString
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.shared.error
import fr.vsct.tock.translator.UserInterfaceType
import mu.KotlinLogging

/**
 * Could be a simple text, or a complex message using secondary constructor.
 */
data class Sentence(
    val text: String?,
    val messages: MutableList<GenericMessage> = mutableListOf(),
    val userInterface: UserInterfaceType? = null,
    override val delay: Long = 0
) : Message {

    companion object {
        private val logger = KotlinLogging.logger {}

        private fun toSentenceElement(message: ConnectorMessage): GenericMessage =
                try {
                    message.toGenericMessage() ?: GenericMessage(message)
                } catch (t: Throwable) {
                    logger.error(t)
                    GenericMessage(message)
                }
    }

    constructor(text: String?, messages: MutableList<ConnectorMessage> = mutableListOf(), userInterface: UserInterfaceType? = null)
            : this(text, messages.map { toSentenceElement(it) }.toMutableList(), userInterface)

    override val eventType: EventType = EventType.sentence

    override fun toAction(playerId: PlayerId,
                          applicationId: String,
                          recipientId: PlayerId): Action {
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
                }.toMutableList())
    }

    override fun toPrettyString(): String {
        return text ?: "{$eventType:${elementsToString(messages)}}"
    }

    override fun isSimpleMessage(): Boolean = text != null
}
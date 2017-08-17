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

package fr.vsct.tock.bot.admin.message

import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.event.EventType
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.shared.error
import fr.vsct.tock.translator.I18nLabelKey
import fr.vsct.tock.translator.Translator
import fr.vsct.tock.translator.UserInterfaceType
import mu.KotlinLogging
import java.util.Locale

/**
 * Could be a simple text, or a complex message using [ConnectorMessage] constructor.
 */
data class SentenceConfiguration(
        val text: I18nLabelKey?,
        val messages: MutableList<SentenceElementConfiguration> = mutableListOf(),
        override val delay: Long = 0
) : MessageConfiguration {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override val eventType: EventType = EventType.sentence

    override fun toAction(playerId: PlayerId,
                          applicationId: String,
                          recipientId: PlayerId,
                          locale: Locale,
                          userInterfaceType: UserInterfaceType): Action {
        return SendSentence(
                playerId,
                applicationId,
                recipientId,
                if (text != null) Translator.translate(text, locale, userInterfaceType).toString() else null,
                messages.mapNotNull {
                    try {
                        it.findConnectorMessage()
                    } catch(e: Exception) {
                        logger.error(e)
                        null
                    }
                }.toMutableList())
    }

}
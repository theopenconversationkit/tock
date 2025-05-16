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

package ai.tock.bot.admin.message

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.event.EventType
import ai.tock.bot.engine.user.PlayerId
import ai.tock.shared.error
import ai.tock.translator.I18nLabelValue
import ai.tock.translator.UserInterfaceType
import mu.KotlinLogging
import java.util.Locale

/**
 * Could be a simple text, or a complex message using [ConnectorMessage] constructor.
 */
data class SentenceConfiguration(
    val text: I18nLabelValue?,
    val messages: MutableList<SentenceElementConfiguration> = mutableListOf(),
    override val delay: Long = 0
) : MessageConfiguration {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override val eventType: EventType = EventType.sentence

    override fun toAction(
        playerId: PlayerId,
        applicationId: String,
        recipientId: PlayerId,
        locale: Locale,
        userInterfaceType: UserInterfaceType
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
            }.toMutableList()
        )
    }
}

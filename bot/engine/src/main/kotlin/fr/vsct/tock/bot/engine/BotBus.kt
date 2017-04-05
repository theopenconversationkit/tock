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

package fr.vsct.tock.bot.engine

import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.dialog.Story
import fr.vsct.tock.bot.engine.user.UserTimeline
import fr.vsct.tock.translator.I18n
import fr.vsct.tock.translator.I18nKeyProvider
import fr.vsct.tock.translator.I18nLabelKey

/**
 *
 */
class BotBus internal constructor(
        private val connector: ConnectorController,
        val userTimeline: UserTimeline,
        val dialog: Dialog,
        val story: Story,
        val action: Action,
        var i18nProvider: I18nKeyProvider
) {
    private val bot = connector.bot
    private val applicationId = action.applicationId
    private val botId = action.recipientId
    private val userId = action.playerId

    private var currentDelay: Long = 0

    private fun answer(action: Action, delay: Long = 0): BotBus {
        currentDelay += delay
        story.actions.add(action)
        connector.send(action, currentDelay)
        return this
    }

    fun end(action: Action, delay: Long = 0): BotBus {
        action.botMetadata.lastAnswer = true
        return answer(action, delay)
    }

    fun send(text: String, delay: Long = 0): BotBus {
        return answer(SendSentence(botId, applicationId, userId, translate(text)), delay)
    }

    fun end(text: String, delay: Long = 0): BotBus {
        return end(SendSentence(botId, applicationId, userId, translate(text)), delay)
    }

    fun translate(text: String, vararg args: Any?): String {
        if (text.isEmpty()) {
            return ""
        }
        return translate(i18nProvider.i18nKeyFromLabel(text, *args))
    }

    fun translate(key: I18nLabelKey): String {
        return I18n.translate(key,
                userTimeline.userPreferences.locale,
                connector.connectorType.userInterfaceType)
    }
}
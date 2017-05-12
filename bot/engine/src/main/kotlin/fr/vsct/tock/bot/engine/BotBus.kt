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

import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.definition.Intent
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.dialog.ContextValue
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.dialog.EntityStateValue
import fr.vsct.tock.bot.engine.dialog.Story
import fr.vsct.tock.bot.engine.user.UserTimeline
import fr.vsct.tock.translator.I18nKeyProvider
import fr.vsct.tock.translator.I18nLabelKey
import fr.vsct.tock.translator.Translator

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

    private val context: BusContext = BusContext()

    val entities: Map<String, EntityStateValue> = dialog.state.entityValues
    val intent: Intent? = dialog.state.currentIntent

    /**
     * Returns true if the current action has the specified entity role.
     */
    fun hasActionEntity(role: String): Boolean {
        return action.hasEntity(role)
    }

    fun changeEntityValue(role: String, newValue: ContextValue?) {
        dialog.state.changeValue(role, newValue)
    }

    fun removeAllEntityValues() {
        dialog.state.removeAllEntityValues()
    }

    fun removeEntityValue(role: String) {
        dialog.state.removeValue(role)
    }


    private fun answer(action: Action, delay: Long = 0): BotBus {
        context.currentDelay += delay
        if (action is SendSentence) {
            context.getMessage(connector.connectorType)?.let {
                action.messages.add(it)
            }
        }

        story.actions.add(action)
        connector.send(action, context.currentDelay)
        return this
    }

    fun end(action: Action, delay: Long = 0): BotBus {
        action.botMetadata.lastAnswer = true
        return answer(action, delay)
    }

    fun end(delay: Long = 0): BotBus {
        return endPlainText(null, delay)
    }

    fun end(i18nText: String, delay: Long = 0, vararg i18nArgs: Any?): BotBus {
        return endPlainText(translate(i18nText, *i18nArgs), delay)
    }

    fun end(i18nText: String, vararg i18nArgs: Any?): BotBus {
        return endPlainText(translate(i18nText, *i18nArgs))
    }

    fun endPlainText(plainText: String?, delay: Long = 0): BotBus {
        return end(SendSentence(botId, applicationId, userId, plainText), delay)
    }

    fun send(i18nText: String, delay: Long = 0, vararg i18nArgs: Any?): BotBus {
        return sendPlainText(translate(i18nText, *i18nArgs), delay)
    }

    fun send(i18nText: String, vararg i18nArgs: Any?): BotBus {
        return sendPlainText(translate(i18nText, *i18nArgs))
    }

    fun send(delay: Long = 0): BotBus {
        return sendPlainText(null, delay)
    }

    fun sendPlainText(plainText: String?, delay: Long = 0): BotBus {
        return answer(SendSentence(botId, applicationId, userId, plainText), delay)
    }


    fun with(message: ConnectorMessage): BotBus {
        context.addMessage(message)
        return this
    }

    fun translate(text: String?, vararg args: Any?): String {
        if (text.isNullOrBlank()) {
            return ""
        } else {
            return translate(i18nProvider.i18nKeyFromLabel(text!!, *args))
        }
    }

    fun translate(key: I18nLabelKey): String {
        return Translator.translate(key,
                userTimeline.userPreferences.locale,
                connector.connectorType.userInterfaceType)
    }
}
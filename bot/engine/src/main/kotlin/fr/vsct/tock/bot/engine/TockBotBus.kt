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
import fr.vsct.tock.bot.engine.action.ActionSignificance
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.dialog.EntityStateValue
import fr.vsct.tock.bot.engine.dialog.NextUserActionState
import fr.vsct.tock.bot.engine.dialog.Story
import fr.vsct.tock.bot.engine.user.UserPreferences
import fr.vsct.tock.bot.engine.user.UserTimeline
import fr.vsct.tock.translator.I18nKeyProvider
import fr.vsct.tock.translator.I18nLabelKey
import fr.vsct.tock.translator.Translator
import fr.vsct.tock.translator.UserInterfaceType
import mu.KotlinLogging
import java.util.Locale

/**
 *
 */
internal class TockBotBus(
        private val connector: ConnectorController,
        override val userTimeline: UserTimeline,
        override val dialog: Dialog,
        override val story: Story,
        override val action: Action,
        override var i18nProvider: I18nKeyProvider
) : BotBus {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val bot = connector.bot
    override val applicationId = action.applicationId
    override val botId = action.recipientId
    override val userId = action.playerId
    override val userPreferences: UserPreferences = userTimeline.userPreferences
    override val userLocale: Locale = userPreferences.locale
    override val userInterfaceType: UserInterfaceType = connector.connectorType.userInterfaceType

    private val context: BusContext = BusContext()

    override val entities: Map<String, EntityStateValue> = dialog.state.entityValues
    override val intent: Intent? = dialog.state.currentIntent

    override var nextUserActionState: NextUserActionState?
        get() = dialog.state.nextActionState
        set(value) {
            dialog.state.nextActionState = value
        }

    /**
     * Returns the non persistent current context value.
     */
    override fun getBusContextValue(name: String): Any? {
        return context.contextMap[name]
    }

    /**
     * Update the non persistent current context value.
     */
    override fun setBusContextValue(key: String, value: Any?) {
        if (value == null) {
            context.contextMap - key
        } else {
            context.contextMap.put(key, value)
        }
    }

    private fun answer(action: Action, delay: Long = 0): BotBus {
        context.currentDelay += delay
        action.metadata.significance = context.significance
        if (action is SendSentence) {
            action.messages.addAll(context.connectorMessages.values)
        }
        action.state.testEvent = userPreferences.test

        story.actions.add(action)
        connector.send(action, context.currentDelay)
        return this
    }

    override fun end(action: Action, delay: Long): BotBus {
        action.metadata.lastAnswer = true
        return answer(action, delay)
    }

    override fun sendPlainText(plainText: String?, delay: Long): BotBus {
        return answer(SendSentence(botId, applicationId, userId, plainText), delay)
    }

    override fun send(action: Action, delay: Long): BotBus {
        return answer(action, delay)
    }

    override fun with(significance: ActionSignificance): BotBus {
        context.significance = significance
        return this
    }

    override fun with(message: ConnectorMessage): BotBus {
        context.addMessage(message)
        return this
    }

    override fun translate(key: I18nLabelKey?): String {
        return if (key == null) ""
        else Translator.translate(key,
                userTimeline.userPreferences.locale,
                connector.connectorType.userInterfaceType)
    }

    override fun reloadProfile() {
        val newUserPref = connector.loadProfile(applicationId, userId)
        userTimeline.userState.profileLoaded = true
        userPreferences.fillWith(newUserPref)
    }
}
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

package fr.vsct.tock.bot.test

import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.connector.messenger.messengerConnectorType
import fr.vsct.tock.bot.definition.BotDefinition
import fr.vsct.tock.bot.definition.IntentAware
import fr.vsct.tock.bot.definition.Parameters
import fr.vsct.tock.bot.definition.StoryDefinition
import fr.vsct.tock.bot.definition.StoryStep
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.SendChoice
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.dialog.Story
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.UserPreferences
import fr.vsct.tock.bot.engine.user.UserTimeline
import fr.vsct.tock.translator.I18nKeyProvider
import fr.vsct.tock.translator.UserInterfaceType

/**
 * The context for test.
 */
class BotBusMockContext(val userTimeline: UserTimeline,
                        val dialog: Dialog,
                        val story: Story,
                        val action: Action,
                        val botDefinition: BotDefinition,
                        val i18nProvider: I18nKeyProvider,
                        val storyDefinition: StoryDefinition,
                        val userInterfaceType: UserInterfaceType = UserInterfaceType.textChat,
                        val connectorType: ConnectorType = messengerConnectorType) {

    constructor(applicationId: String,
                userId: PlayerId,
                botId: PlayerId,
                botDefinition: BotDefinition,
                storyDefinition: StoryDefinition,
                action: Action = SendSentence(userId, applicationId, botId, ""),
                userInterfaceType: UserInterfaceType = UserInterfaceType.textChat,
                userPreferences: UserPreferences = UserPreferences(),
                connectorType: ConnectorType = messengerConnectorType)
            : this(
            UserTimeline(userId, userPreferences),
            Dialog(setOf(userId, botId)),
            Story(storyDefinition, storyDefinition.mainIntent()),
            action,
            botDefinition,
            storyDefinition.storyHandler as I18nKeyProvider,
            storyDefinition,
            userInterfaceType,
            connectorType
    )

    val applicationId = action.applicationId
    val botId = action.recipientId
    val userId = action.playerId
    val userPreferences: UserPreferences = userTimeline.userPreferences

    /**
     * Create a new sentence for this context
     */
    fun sentence(text: String): SendSentence = SendSentence(userId, applicationId, botId, text)

    /**
     * Create a choice for this context.
     */
    fun choice(intentName: String,
               vararg parameters: Pair<String, String>): SendChoice
            = SendChoice(userId, applicationId, botId, intentName, parameters.toMap())

    /**
     * Create a choice for this context.
     */
    fun choice(intentName: String,
               step: StoryStep,
               vararg parameters: Pair<String, String>): SendChoice
            = SendChoice(userId, applicationId, botId, intentName, step, parameters.toMap())

    /**
     * Create a choice for this context.
     */
    fun choice(intent: IntentAware,
               step: StoryStep,
               parameters: Parameters): SendChoice
            = SendChoice(userId, applicationId, botId, intent.wrappedIntent().name, step, parameters.toMap())

    /**
     * Create a choice for this context.
     */
    fun choice(intent: IntentAware,
               parameters: Parameters): SendChoice
            = SendChoice(userId, applicationId, botId, intent.wrappedIntent().name, parameters.toMap())

}
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

import currentTestContext
import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.connector.messenger.messengerConnectorType
import fr.vsct.tock.bot.definition.BotDefinition
import fr.vsct.tock.bot.definition.IntentAware
import fr.vsct.tock.bot.definition.Parameters
import fr.vsct.tock.bot.definition.StoryDefinition
import fr.vsct.tock.bot.definition.StoryHandlerDefinition
import fr.vsct.tock.bot.definition.StoryStep
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.SendChoice
import fr.vsct.tock.bot.engine.action.SendChoice.Companion.decodeChoiceId
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.dialog.EntityValue
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.dialog.Snapshot
import fr.vsct.tock.bot.engine.dialog.Story
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType
import fr.vsct.tock.bot.engine.user.UserPreferences
import fr.vsct.tock.bot.engine.user.UserTimeline
import fr.vsct.tock.translator.I18nKeyProvider
import fr.vsct.tock.translator.UserInterfaceType

/**
 * The context of the test.
 */
data class BotBusMockContext(
    var userTimeline: UserTimeline,
    var dialog: Dialog,
    var story: Story,
    var firstAction: Action,
    var botDefinition: BotDefinition,
    var i18nProvider: I18nKeyProvider,
    var userInterfaceType: UserInterfaceType = UserInterfaceType.textChat,
    var connectorType: ConnectorType = messengerConnectorType,
    val testContext: TestContext = currentTestContext,
    val snapshots: MutableList<Snapshot> = mutableListOf()
) {

    constructor(
        applicationId: String,
        userId: PlayerId,
        botId: PlayerId,
        botDefinition: BotDefinition,
        storyDefinition: StoryDefinition,
        action: Action = SendSentence(userId, applicationId, botId, ""),
        userInterfaceType: UserInterfaceType = UserInterfaceType.textChat,
        userPreferences: UserPreferences = UserPreferences(),
        connectorType: ConnectorType = messengerConnectorType,
        testContext: TestContext = currentTestContext
    )
            : this(
        UserTimeline(userId, userPreferences),
        Dialog(setOf(userId, botId)),
        Story(storyDefinition, storyDefinition.mainIntent()),
        action,
        botDefinition,
        storyDefinition.storyHandler as I18nKeyProvider,
        userInterfaceType,
        connectorType,
        testContext
    )

    constructor(
        botDefinition: BotDefinition,
        storyDefinition: StoryDefinition,
        applicationId: String = botDefinition.botId,
        userId: PlayerId = PlayerId("user"),
        botId: PlayerId = PlayerId("bot", PlayerType.bot),
        action: Action = SendSentence(userId, applicationId, botId, ""),
        userInterfaceType: UserInterfaceType = UserInterfaceType.textChat,
        userPreferences: UserPreferences = UserPreferences(),
        connectorType: ConnectorType = messengerConnectorType,
        testContext: TestContext = currentTestContext
    )
            : this(
        applicationId,
        userId,
        botId,
        botDefinition,
        storyDefinition,
        action,
        userInterfaceType,
        userPreferences,
        connectorType,
        testContext
    )

    val applicationId get() = firstAction.applicationId
    val botId get() = firstAction.recipientId
    val userId get() = firstAction.playerId
    val userPreferences: UserPreferences get() = userTimeline.userPreferences
    val initialUserPreferences: UserPreferences = userPreferences.copy()
    internal val logsRepository: List<BotBusMockLog> = mutableListOf()

    /**
     * The list of all bot answers recorded.
     */
    val answers: List<BotBusMockLog> get() = logsRepository

    /**
     * The first answer recorded.
     */
    val firstAnswer: BotBusMockLog get() = logsRepository.first()

    /**
     * The second answer recorded.
     */
    val secondAnswer: BotBusMockLog get() = logsRepository[1]

    /**
     * The third answer recorded.
     */
    val thirdAnswer: BotBusMockLog get() = logsRepository[2]

    /**
     * The last answer recorded.
     */
    val lastAnswer: BotBusMockLog get() = logsRepository.last()

    /**
     * Reset user preferences.
     */
    fun resetUserPreferences(userPreferences: UserPreferences) {
        this.userPreferences.fillWith(userPreferences)
        initialUserPreferences.fillWith(userPreferences)
    }

    /**
     * Create a new sentence for this context.
     */
    fun sentence(
        text: String,
        vararg entityValues: EntityValue
    ): SendSentence =
        sentence(text, entityValues.toList())

    /**
     * Create a new sentence for this context.
     */
    fun sentence(
        text: String,
        entityValues: List<EntityValue> = emptyList()
    ): SendSentence =
        sentence(text, null as IntentAware?, entityValues.toList())


    /**
     * Create a new sentence for this context.
     */
    fun sentence(
        text: String,
        intent: IntentAware? = null,
        vararg entityValues: EntityValue
    ) = sentence(text, intent, entityValues.toList())

    /**
     * Create a new sentence for this context.
     */
    fun sentence(
        text: String,
        intent: IntentAware? = null,
        entityValues: List<EntityValue> = emptyList()
    ): SendSentence =
        SendSentence(userId, applicationId, botId, text).apply {
            state.intent = intent?.wrappedIntent()?.name
            state.entityValues.addAll(entityValues)
        }

    /**
     * Create a new sentence for this context.
     */
    fun sentence(
        message: ConnectorMessage,
        vararg entityValues: EntityValue
    ): SendSentence =
        sentence(message, entityValues.toList())

    /**
     * Create a new sentence for this context.
     */
    fun sentence(
        message: ConnectorMessage,
        entityValues: List<EntityValue> = emptyList()
    ): SendSentence =
        sentence(message, null as IntentAware?, entityValues.toList())


    /**
     * Create a new sentence for this context.
     */
    fun sentence(
        message: ConnectorMessage,
        intent: IntentAware? = null,
        vararg entityValues: EntityValue
    ) = sentence(message, intent, entityValues.toList())

    /**
     * Create a new sentence for this context.
     */
    fun sentence(
        message: ConnectorMessage,
        intent: IntentAware? = null,
        entityValues: List<EntityValue> = emptyList()
    ): SendSentence =
        SendSentence(userId, applicationId, botId, null, mutableListOf(message)).apply {
            state.intent = intent?.wrappedIntent()?.name
            state.entityValues.addAll(entityValues)
            state.targetConnectorType = message.connectorType
            state.userInterface = message.connectorType.userInterfaceType
        }

    /**
     * Create a choice for this context.
     */
    fun choice(
        intentName: String,
        vararg parameters: Pair<String, String>
    ): SendChoice = SendChoice(userId, applicationId, botId, intentName, parameters.toMap())

    /**
     * Create a choice for this context.
     */
    fun choice(
        intentName: String,
        step: StoryStep<out StoryHandlerDefinition>,
        vararg parameters: Pair<String, String>
    ): SendChoice = SendChoice(userId, applicationId, botId, intentName, step, parameters.toMap())

    /**
     * Create a choice for this context.
     */
    fun choice(
        intent: IntentAware,
        step: StoryStep<out StoryHandlerDefinition>,
        parameters: Parameters
    ): SendChoice = SendChoice(userId, applicationId, botId, intent.wrappedIntent().name, step, parameters.toMap())

    /**
     * Create a choice for this context.
     */
    fun choice(
        intent: IntentAware,
        parameters: Parameters
    ): SendChoice = SendChoice(userId, applicationId, botId, intent.wrappedIntent().name, parameters.toMap())

    /**
     * Create a choice for this context.
     */
    fun choiceOfId(choiceId: String): SendChoice = decodeChoiceId(choiceId).let { it ->
        choice(it.first, *it.second.map { it.key to it.value }.toTypedArray())

    }
}
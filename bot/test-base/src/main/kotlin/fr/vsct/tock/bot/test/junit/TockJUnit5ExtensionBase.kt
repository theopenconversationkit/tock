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

package fr.vsct.tock.bot.test.junit

import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.definition.BotDefinition
import fr.vsct.tock.bot.definition.IntentAware
import fr.vsct.tock.bot.definition.Parameters
import fr.vsct.tock.bot.definition.StoryDefinition
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.ActionMetadata
import fr.vsct.tock.bot.engine.action.SendChoice
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.dialog.EntityValue
import fr.vsct.tock.bot.engine.dialog.EventState
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType
import fr.vsct.tock.bot.engine.user.UserPreferences
import fr.vsct.tock.bot.test.BotBusMock
import fr.vsct.tock.bot.test.BotBusMockContext
import fr.vsct.tock.bot.test.TestContext
import fr.vsct.tock.bot.test.TestLifecycle
import fr.vsct.tock.bot.test.newBusMockContext
import fr.vsct.tock.translator.UserInterfaceType
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.util.Locale

/**
 * JUnit5 base extension.
 */
open class TockJUnit5ExtensionBase<out T : TestContext>(
    val botDefinition: BotDefinition,
    @Suppress("UNCHECKED_CAST") val lifecycle: TestLifecycle<T> = TestLifecycle(TestContext() as T)
) : BeforeEachCallback, AfterEachCallback {

    /**
     * The [TestContext].
     */
    val testContext: T get() = lifecycle.testContext

    /**
     * Sends a choice and execute the tests.
     */
    fun sendChoice(
        intent: IntentAware = testContext.defaultStoryDefinition(botDefinition),
        parameters: Parameters = Parameters(),
        connectorType: ConnectorType = testContext.defaultConnectorType(),
        userInterfaceType: UserInterfaceType = connectorType.userInterfaceType,
        locale: Locale = testContext.defaultLocale(),
        userId: PlayerId = testContext.defaultPlayerId(),
        botId: PlayerId = PlayerId("bot", PlayerType.bot),
        userPreferences: UserPreferences = UserPreferences(locale = locale),
        tests: BotBusMock.() -> Unit
    ) {
        send(
            intent,
            connectorType,
            userInterfaceType,
            locale,
            userId,
            botId,
            userPreferences,
            {
                SendChoice(
                    userId,
                    botDefinition.botId,
                    botId,
                    intent.wrappedIntent().name,
                    parameters.toMap()
                )
            },
            tests
        )
    }

    /**
     * Sends a sentence and execute the tests.
     */
    fun send(
        text: String = "",
        intent: IntentAware = testContext.defaultStoryDefinition(botDefinition),
        vararg entities: EntityValue,
        connectorType: ConnectorType = testContext.defaultConnectorType(),
        userInterfaceType: UserInterfaceType = connectorType.userInterfaceType,
        locale: Locale = testContext.defaultLocale(),
        userId: PlayerId = testContext.defaultPlayerId(),
        botId: PlayerId = PlayerId("bot", PlayerType.bot),
        userPreferences: UserPreferences = UserPreferences(locale = locale),
        metadata: ActionMetadata = ActionMetadata(),
        tests: BotBusMock.() -> Unit
    ) {
        send(
            intent,
            connectorType,
            userInterfaceType,
            locale,
            userId,
            botId,
            userPreferences,
            {
                SendSentence(
                    userId,
                    botDefinition.botId,
                    botId,
                    text,
                    state = EventState(
                        entities.toMutableList(),
                        intent = intent.wrappedIntent().name
                    ),
                    metadata = metadata
                )
            },
            tests
        )
    }

    /**
     * Sends an action and execute the tests.
     */
    fun send(
        intent: IntentAware = testContext.defaultStoryDefinition(botDefinition),
        connectorType: ConnectorType = testContext.defaultConnectorType(),
        userInterfaceType: UserInterfaceType = connectorType.userInterfaceType,
        locale: Locale = testContext.defaultLocale(),
        userId: PlayerId = testContext.defaultPlayerId(),
        botId: PlayerId = PlayerId("bot", PlayerType.bot),
        userPreferences: UserPreferences = UserPreferences(locale = locale),
        actionProvider: () -> Action,
        tests: BotBusMock.() -> Unit
    ) {
        val action = actionProvider.invoke()
        tests.invoke(
            BotBusMock(
                if (testContext.isInitialized()) {
                    testContext.botBusMockContext
                } else {
                    newBusMockContext(
                        findStoryDefinition(intent),
                        connectorType,
                        locale,
                        userId,
                        botId,
                        action,
                        userInterfaceType,
                        userPreferences
                    )
                },
                action
            ).run()
        )
    }

    /**
     * Creates a new choice request (not yet sent).
     */
    fun newChoiceRequest(
        intent: IntentAware = testContext.defaultStoryDefinition(botDefinition),
        parameters: Parameters,
        connectorType: ConnectorType = testContext.defaultConnectorType(),
        userInterfaceType: UserInterfaceType = connectorType.userInterfaceType,
        locale: Locale = testContext.defaultLocale(),
        userId: PlayerId = testContext.defaultPlayerId(),
        botId: PlayerId = PlayerId("bot", PlayerType.bot),
        userPreferences: UserPreferences = UserPreferences(locale = locale),
        tests: BotBusMock.() -> Unit
    ) {
        newRequest(
            intent,
            connectorType,
            userInterfaceType,
            locale,
            userId,
            botId,
            userPreferences,
            {
                SendChoice(
                    userId,
                    botDefinition.botId,
                    botId,
                    intent.wrappedIntent().name,
                    parameters.toMap()
                )
            },
            tests
        )
    }

    /**
     * Creates a new sentence request (not yet sent).
     */
    fun newRequest(
        text: String = "",
        intent: IntentAware = testContext.defaultStoryDefinition(botDefinition),
        vararg entities: EntityValue,
        connectorType: ConnectorType = testContext.defaultConnectorType(),
        userInterfaceType: UserInterfaceType = connectorType.userInterfaceType,
        locale: Locale = testContext.defaultLocale(),
        userId: PlayerId = testContext.defaultPlayerId(),
        botId: PlayerId = PlayerId("bot", PlayerType.bot),
        userPreferences: UserPreferences = UserPreferences(locale = locale),
        tests: BotBusMock.() -> Unit
    ) {
        newRequest(
            intent,
            connectorType,
            userInterfaceType,
            locale,
            userId,
            botId,
            userPreferences,
            {
                SendSentence(
                    userId,
                    botDefinition.botId,
                    botId,
                    text,
                    state = EventState(
                        entities.toMutableList(),
                        intent = intent.wrappedIntent().name
                    )
                )
            },
            tests
        )
    }

    /**
     * Creates a new action request (not yet sent).
     */
    fun newRequest(
        intent: IntentAware = testContext.defaultStoryDefinition(botDefinition),
        connectorType: ConnectorType = testContext.defaultConnectorType(),
        userInterfaceType: UserInterfaceType = connectorType.userInterfaceType,
        locale: Locale = testContext.defaultLocale(),
        userId: PlayerId = testContext.defaultPlayerId(),
        botId: PlayerId = PlayerId("bot", PlayerType.bot),
        userPreferences: UserPreferences = UserPreferences(locale = locale),
        actionProvider: () -> Action,
        tests: BotBusMock.() -> Unit
    ) {
        val action = actionProvider.invoke()
        tests.invoke(
            BotBusMock(
                if (testContext.isInitialized()) {
                    testContext.botBusMockContext
                } else {
                    newBusMockContext(
                        findStoryDefinition(intent),
                        connectorType,
                        locale,
                        userId,
                        botId,
                        action,
                        userInterfaceType,
                        userPreferences
                    )
                },
                action
            )
        )
    }

    private fun findStoryDefinition(intent: IntentAware): StoryDefinition =
        if (intent is StoryDefinition) {
            intent
        } else {
            if (testContext.isInitialized()
                && testContext.defaultStoryDefinition(botDefinition).supportIntent(intent.wrappedIntent())
            ) {
                testContext.defaultStoryDefinition(botDefinition)
            } else {
                botDefinition.findStoryDefinition(intent)
            }
        }

    /**
     * Provides a mock initialized with the specified [StoryDefinition] and starts the story.
     */
    fun startNewBusMock(
        story: StoryDefinition = testContext.defaultStoryDefinition(botDefinition),
        connectorType: ConnectorType = testContext.defaultConnectorType(),
        locale: Locale = testContext.defaultLocale(),
        userId: PlayerId = testContext.defaultPlayerId()
    ): BotBusMock = newBusMock(story, connectorType, locale, userId).run()

    /**
     * Provides a mock initialized with the specified [StoryDefinition].
     */
    fun newBusMock(
        story: StoryDefinition = testContext.defaultStoryDefinition(botDefinition),
        connectorType: ConnectorType = testContext.defaultConnectorType(),
        locale: Locale = testContext.defaultLocale(),
        userId: PlayerId = testContext.defaultPlayerId()
    ): BotBusMock = BotBusMock(newBusMockContext(story, connectorType, locale, userId))

    /**
     * Provides a mock context initialized with the specified [StoryDefinition].
     */
    fun newBusMockContext(
        story: StoryDefinition = testContext.defaultStoryDefinition(botDefinition),
        connectorType: ConnectorType = testContext.defaultConnectorType(),
        locale: Locale = testContext.defaultLocale(),
        userId: PlayerId = testContext.defaultPlayerId(),
        botId: PlayerId = PlayerId("bot", PlayerType.bot),
        action: Action = SendSentence(userId, botDefinition.botId, botId, ""),
        userInterfaceType: UserInterfaceType = UserInterfaceType.textChat,
        userPreferences: UserPreferences = UserPreferences(locale = locale)
    ): BotBusMockContext =
        botDefinition.newBusMockContext(
            testContext,
            story,
            connectorType,
            locale,
            userId,
            botId,
            action,
            userInterfaceType,
            userPreferences
        ).apply {
            testContext.botBusMockContext = this
            lifecycle.configureTestIoc()
        }

    /**
     * Provides a mock context initialized with the current [testContext] and runs the bus.
     */
    fun startBusMock(): BotBusMock = busMock().run()

    /**
     * Provides a mock context initialized with the current [testContext].
     */
    fun busMock(): BotBusMock = BotBusMock(testContext.botBusMockContext)


    override fun beforeEach(p0: ExtensionContext) {
        lifecycle.start()
    }

    override fun afterEach(p0: ExtensionContext) {
        lifecycle.end()
    }

}
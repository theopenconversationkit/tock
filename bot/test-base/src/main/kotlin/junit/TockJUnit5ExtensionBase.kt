/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
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

package ai.tock.bot.test.junit

import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.definition.Parameters
import ai.tock.bot.definition.StoryDefinition
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.ActionMetadata
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.dialog.EntityValue
import ai.tock.bot.engine.dialog.EventState
import ai.tock.bot.engine.message.Message
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.bot.engine.user.UserPreferences
import ai.tock.bot.test.BotBusMock
import ai.tock.bot.test.BotBusMockContext
import ai.tock.bot.test.BotBusMockLog
import ai.tock.bot.test.TestContext
import ai.tock.bot.test.TestLifecycle
import ai.tock.bot.test.newBusMockContext
import ai.tock.translator.UserInterfaceType
import mu.KotlinLogging
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

    companion object {
        private val logger = KotlinLogging.logger {}
    }

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
    ): BotBusMock {
        return send(
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
     * Sends a message and execute the tests.
     */
    fun sendMessage(
        intent: IntentAware = testContext.defaultStoryDefinition(botDefinition),
        message: Message,
        connectorType: ConnectorType = testContext.defaultConnectorType(),
        userInterfaceType: UserInterfaceType = connectorType.userInterfaceType,
        locale: Locale = testContext.defaultLocale(),
        userId: PlayerId = testContext.defaultPlayerId(),
        botId: PlayerId = PlayerId("bot", PlayerType.bot),
        userPreferences: UserPreferences = UserPreferences(locale = locale),
        tests: BotBusMock.() -> Unit
    ): BotBusMock {
        return send(
            intent,
            connectorType,
            userInterfaceType,
            locale,
            userId,
            botId,
            userPreferences,
            { message.toAction(userId, botDefinition.botId, botId) },
            tests
        )
    }

    /**
     * Sends a message simulating a click on action of a previous bus log and execute the tests.
     */
    fun selectChoice(
        busMockLog: BotBusMockLog,
        buttonTitle: String,
        tests: BotBusMock.() -> Unit
    ): BotBusMock {
        return sendMessage(
            message = busMockLog.choice(buttonTitle) ?: error("No choice $buttonTitle found in bus message $busMockLog"),
            tests = tests
        )
    }

    /**
     * Sends a message simulating a click on action of an element in previous bus log and execute the tests.
     */
    fun selectElementChoice(
        busMockLog: BotBusMockLog,
        elementIndex: Int,
        buttonTitle: String,
        tests: BotBusMock.() -> Unit
    ): BotBusMock {
        return sendMessage(
            message = busMockLog.elementChoice(elementIndex, buttonTitle) ?: error("No choice $buttonTitle found in element $elementIndex of bus message $busMockLog"),
            tests = tests
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
    ): BotBusMock {
        return send(
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
    ): BotBusMock {
        val action = actionProvider.invoke()
        val botBusMock = BotBusMock(
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
        tests.invoke(botBusMock.run())
        return botBusMock
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

    //for some strange reason beforeEach can be called multiple time.
    //we need to check that there is only one call (for performance reason)
    private var start = false
    private var end = false

    override fun beforeEach(context: ExtensionContext) {
        if (!start) {
            start = true
            logger.info { "initialize Test ${context.displayName}" }
            lifecycle.start()
            logger.debug { "end initialize Test ${context.displayName}" }
        }
    }

    override fun afterEach(context: ExtensionContext) {
        if (!end) {
            end = true
            logger.info { "cleanup Test ${context.displayName}" }
            lifecycle.end()
            logger.debug { "end cleanup Test ${context.displayName}" }
        }
    }

}
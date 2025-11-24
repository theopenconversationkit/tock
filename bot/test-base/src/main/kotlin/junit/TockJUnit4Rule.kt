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

package ai.tock.bot.test.junit

import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.definition.StoryDefinition
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.test.BotBusMock
import ai.tock.bot.test.BotBusMockContext
import ai.tock.bot.test.TestContext
import ai.tock.bot.test.TestLifecycle
import ai.tock.bot.test.newBusMockContext
import io.mockk.clearAllMocks
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.util.Locale

/**
 * A JUnit4 Rule to initialize the context for each call.
 */
open class TockJUnit4Rule<out T : TestContext>(
    val botDefinition: BotDefinition,
    @Suppress("UNCHECKED_CAST") val lifecycle: TestLifecycle<T> = TestLifecycle(TestContext() as T),
) : TestRule {
    /**
     * The [TestContext].
     */
    val testContext: T get() = lifecycle.testContext

    /**
     * Provides a mock initialized with the specified [StoryDefinition] and starts the story.
     */
    fun startNewBusMock(
        story: StoryDefinition = testContext.defaultStoryDefinition(botDefinition),
        connectorType: ConnectorType = testContext.defaultConnectorType(),
        locale: Locale = testContext.defaultLocale(),
        userId: PlayerId = testContext.defaultPlayerId(),
    ): BotBusMock = newBusMock(story, connectorType, locale, userId).run()

    /**
     * Provides a mock initialized with the specified [StoryDefinition].
     */
    fun newBusMock(
        story: StoryDefinition = testContext.defaultStoryDefinition(botDefinition),
        connectorType: ConnectorType = testContext.defaultConnectorType(),
        locale: Locale = testContext.defaultLocale(),
        userId: PlayerId = testContext.defaultPlayerId(),
    ): BotBusMock = BotBusMock(newBusMockContext(story, connectorType, locale, userId))

    /**
     * Provides a mock context initialized with the specified [StoryDefinition].
     */
    fun newBusMockContext(
        story: StoryDefinition = testContext.defaultStoryDefinition(botDefinition),
        connectorType: ConnectorType = testContext.defaultConnectorType(),
        locale: Locale = testContext.defaultLocale(),
        userId: PlayerId = testContext.defaultPlayerId(),
    ): BotBusMockContext =
        botDefinition.newBusMockContext(testContext, story, connectorType, locale, userId)
            .apply {
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

    override fun apply(
        base: Statement,
        description: Description,
    ): Statement {
        return object : Statement() {
            override fun evaluate() {
                lifecycle.start()
                try {
                    base.evaluate()
                } finally {
                    clearAllMocks()
                    lifecycle.end()
                }
            }
        }
    }
}

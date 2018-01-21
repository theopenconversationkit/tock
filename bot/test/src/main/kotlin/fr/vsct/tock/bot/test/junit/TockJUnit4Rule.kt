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
import fr.vsct.tock.bot.connector.messenger.messengerConnectorType
import fr.vsct.tock.bot.definition.BotDefinition
import fr.vsct.tock.bot.definition.StoryDefinition
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.test.BotBusMock
import fr.vsct.tock.bot.test.BotBusMockContext
import fr.vsct.tock.bot.test.TestContext
import fr.vsct.tock.bot.test.TestLifecycle
import fr.vsct.tock.bot.test.toBusMockContext
import fr.vsct.tock.shared.defaultLocale
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.util.Locale

/**
 * A JUnit4 Rule to initialize the context for each call.
 */
open class TockJUnit4Rule<out T : TestContext>(
        val botDefinition: BotDefinition,
        @Suppress("UNCHECKED_CAST") val lifecycle: TestLifecycle<T> = TestLifecycle(TestContext() as T)) : TestRule {

    val testContext: T get() = lifecycle.testContext

    /**
     * Provides a mock initialized with the specified [StoryDefinition] and starts the story.
     */
    fun startMock(
            storyDefinition: StoryDefinition = botDefinition.helloStory ?: botDefinition.stories.first(),
            connectorType: ConnectorType = messengerConnectorType,
            locale: Locale = defaultLocale,
            userId: PlayerId = PlayerId("user"))
            : BotBusMock = toBusMock(storyDefinition, connectorType, locale, userId).run()

    /**
     * Provides a mock initialized with the specified [StoryDefinition].
     */
    fun toBusMock(
            storyDefinition: StoryDefinition = botDefinition.helloStory ?: botDefinition.stories.first(),
            connectorType: ConnectorType = messengerConnectorType,
            locale: Locale = defaultLocale,
            userId: PlayerId = PlayerId("user"))
            : BotBusMock = BotBusMock(toBusMockContext(storyDefinition, connectorType, locale, userId))

    /**
     * Provides a mock context initialized with the specified [StoryDefinition].
     */
    fun toBusMockContext(
            storyDefinition: StoryDefinition = botDefinition.helloStory ?: botDefinition.stories.first(),
            connectorType: ConnectorType = messengerConnectorType,
            locale: Locale = defaultLocale,
            userId: PlayerId = PlayerId("user")
    ): BotBusMockContext = botDefinition.toBusMockContext(storyDefinition, connectorType, locale, userId, testContext)
            .apply {
                testContext.botBusMockContext = this
                lifecycle.configureTestIoc()
            }


    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                lifecycle.start()
                try {
                    base.evaluate()
                } finally {
                    lifecycle.end()
                }
            }
        }
    }

}
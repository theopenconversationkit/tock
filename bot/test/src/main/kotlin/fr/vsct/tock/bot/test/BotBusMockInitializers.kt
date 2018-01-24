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
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.definition.BotDefinition
import fr.vsct.tock.bot.definition.StoryDefinition
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.UserPreferences
import java.util.Locale

/**
 * Provides a mock initialized with the specified [StoryDefinition] and starts the story.
 */
fun BotDefinition.startNewBusMock(
        testContext: TestContext = currentTestContext,
        story: StoryDefinition = testContext.defaultStoryDefinition(this),
        connectorType: ConnectorType = testContext.defaultConnectorType(),
        locale: Locale = testContext.defaultLocale(),
        userId: PlayerId = testContext.defaultPlayerId()): BotBusMock = newBusMock(testContext, story, connectorType, locale, userId).run()

/**
 * Provides a mock initialized with the specified [StoryDefinition].
 */
fun BotDefinition.newBusMock(
        testContext: TestContext = currentTestContext,
        story: StoryDefinition = testContext.defaultStoryDefinition(this),
        connectorType: ConnectorType = testContext.defaultConnectorType(),
        locale: Locale = testContext.defaultLocale(),
        userId: PlayerId = testContext.defaultPlayerId())
        : BotBusMock = BotBusMock(newBusMockContext(testContext, story, connectorType, locale, userId))

/**
 * Provides a mock context initialized with the specified [StoryDefinition].
 */
fun BotDefinition.newBusMockContext(
        testContext: TestContext = currentTestContext,
        story: StoryDefinition = testContext.defaultStoryDefinition(this),
        connectorType: ConnectorType = testContext.defaultConnectorType(),
        locale: Locale = testContext.defaultLocale(),
        userId: PlayerId = testContext.defaultPlayerId()
): BotBusMockContext = BotBusMockContext(
        this,
        story,
        userId = userId,
        userPreferences = UserPreferences(locale = locale),
        connectorType = connectorType,
        testContext = testContext)

/**
 * Provides a mock context initialized with the specified [TestContext] and runs the story.
 */
fun startBusMock(testContext: TestContext = currentTestContext): BotBusMock = busMock(testContext).run()

/**
 * Provides a mock context initialized with the specified [TestContext].
 */
fun busMock(testContext: TestContext = currentTestContext): BotBusMock = BotBusMock(testContext.botBusMockContext)
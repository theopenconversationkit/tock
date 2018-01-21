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
import fr.vsct.tock.bot.connector.messenger.messengerConnectorType
import fr.vsct.tock.bot.definition.BotDefinition
import fr.vsct.tock.bot.definition.StoryDefinition
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.UserPreferences
import fr.vsct.tock.shared.defaultLocale
import java.util.Locale

/**
 * Provides a mock initialized with the specified [StoryDefinition] and starts the story.
 */
fun BotDefinition.startMock(
        storyDefinition: StoryDefinition = helloStory ?: stories.first(),
        connectorType: ConnectorType = messengerConnectorType,
        locale: Locale = defaultLocale,
        userId: PlayerId = PlayerId("user"),
        testContext: TestContext = currentTestContext): BotBusMock = toBusMock(storyDefinition, connectorType, locale, userId, testContext).run()

/**
 * Provides a mock initialized with the specified [StoryDefinition].
 */
fun BotDefinition.toBusMock(
        storyDefinition: StoryDefinition = helloStory ?: stories.first(),
        connectorType: ConnectorType = messengerConnectorType,
        locale: Locale = defaultLocale,
        userId: PlayerId = PlayerId("user"),
        testContext: TestContext = currentTestContext)
        : BotBusMock = BotBusMock(toBusMockContext(storyDefinition, connectorType, locale, userId, testContext))

/**
 * Provides a mock context initialized with the specified [StoryDefinition].
 */
fun BotDefinition.toBusMockContext(
        storyDefinition: StoryDefinition = helloStory ?: stories.first(),
        connectorType: ConnectorType = messengerConnectorType,
        locale: Locale = defaultLocale,
        userId: PlayerId = PlayerId("user"),
        testContext: TestContext = currentTestContext
): BotBusMockContext = BotBusMockContext(
        this,
        storyDefinition,
        userId = userId,
        userPreferences = UserPreferences(locale = locale),
        connectorType = connectorType,
        testContext = testContext)
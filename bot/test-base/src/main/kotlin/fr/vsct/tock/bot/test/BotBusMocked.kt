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

import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.connector.messenger.messengerConnectorType
import fr.vsct.tock.bot.connector.messenger.model.MessengerConnectorMessage
import fr.vsct.tock.bot.connector.messenger.model.send.UrlButton
import fr.vsct.tock.bot.connector.messenger.urlButton
import fr.vsct.tock.bot.connector.messenger.withMessenger
import fr.vsct.tock.bot.connector.twitter.model.TwitterConnectorMessage
import fr.vsct.tock.bot.connector.twitter.twitterConnectorType
import fr.vsct.tock.bot.connector.twitter.withTwitter
import fr.vsct.tock.bot.definition.BotDefinition
import fr.vsct.tock.bot.definition.StoryDefinitionBase
import fr.vsct.tock.bot.definition.StoryHandlerBase
import fr.vsct.tock.bot.engine.BotBus
import fr.vsct.tock.bot.engine.dialog.EntityValue
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.UserTimeline
import fr.vsct.tock.nlp.api.client.model.Entity
import fr.vsct.tock.nlp.entity.Value
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic

/**
 * Test a [StoryDefinition] with a mocked (mockk) [BotBus].
 */
fun StoryDefinitionBase.test(bus: BotBus) {
    (storyHandler as? StoryHandlerBase<*>)?.newHandlerDefinition(bus)?.handle()
            ?: error("story handler is not a StoryHandlerBase")
}

/**
 * Default mockk BotBus configuration.
 */
fun mockTockCommon(bus: BotBus) {
    every { bus.step } returns null
    every { bus.currentAnswerIndex } returns 0
    every { bus.choice(any()) } returns null
    every { bus.isIntent(any()) } returns false
    every { bus.withMessage(any()) }.answers {
        if (bus.targetConnectorType == (args[0] as ConnectorMessage).connectorType) {
            @Suppress("UNCHECKED_CAST")
            (args[0] as (() -> ConnectorMessage)).invoke()
        }
        bus
    }
    every { bus.withMessage(any(), any()) }.answers {
        if (bus.targetConnectorType == args[0]) {
            @Suppress("UNCHECKED_CAST")
            (args[1] as (() -> ConnectorMessage)).invoke()
        }
        bus
    }

    every { bus.end(any<Long>()) } returns bus
    every { bus.end(any<String>(), any()) } returns bus
    every { bus.send(any<Long>()) } returns bus
    every { bus.send(any<String>(), any()) } returns bus

    every {
        bus.entityValue(
            any<Entity>(),
            any<(EntityValue) -> Value?>()
        )
    } returns null
    every { bus.changeEntityValue(any(), any<Value>()) } returns Unit

    val playerId = PlayerId("user")

    every { bus.userId } returns playerId
    val userTimeline: UserTimeline = mockk()
    every { bus.userTimeline } returns userTimeline
    every { userTimeline.playerId } returns playerId
    val botDefinition: BotDefinition = mockk()
    every { bus.botDefinition} returns botDefinition
    every { botDefinition.defaultDelay(any())} returns 0
}

/**
 * Mock classic messenger extensions.
 */
fun mockMessenger(bus: BotBus) {
    mockTockCommon(bus)
    mockkStatic("fr.vsct.tock.bot.connector.messenger.MessengerBuildersKt")
    every { bus.targetConnectorType } returns messengerConnectorType
    every { bus.withMessenger(any()) }.answers {
        if (bus.targetConnectorType == messengerConnectorType) {
            @Suppress("UNCHECKED_CAST")
            (args[1] as (() -> MessengerConnectorMessage)).invoke()
        }
        bus
    }
    every { bus.urlButton(any(), any()) }.answers {
        UrlButton(
            args[2] as String, (args[1] as String).toString()
        )
    }
}

/**
 * Mock classic twitter extensions.
 */
fun mockTwitter(bus: BotBus) {
    mockTockCommon(bus)
    mockkStatic("fr.vsct.tock.bot.connector.twitter.TwitterBuildersKt")
    every { bus.targetConnectorType } returns twitterConnectorType
    every { bus.withTwitter(any()) }.answers {
        if (bus.targetConnectorType == twitterConnectorType) {
            @Suppress("UNCHECKED_CAST")
            (args[1] as (() -> TwitterConnectorMessage)).invoke()
        }
        bus
    }
}
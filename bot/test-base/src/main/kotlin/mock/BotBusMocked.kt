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

package ai.tock.bot.test.mock

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.messenger.messengerConnectorType
import ai.tock.bot.connector.messenger.model.MessengerConnectorMessage
import ai.tock.bot.connector.messenger.withMessenger
import ai.tock.bot.connector.twitter.model.TwitterConnectorMessage
import ai.tock.bot.connector.twitter.twitterConnectorType
import ai.tock.bot.connector.twitter.withTwitter
import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.definition.StoryDefinitionBase
import ai.tock.bot.definition.StoryHandlerBase
import ai.tock.bot.definition.StoryStepDef
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.ActionQuote
import ai.tock.bot.engine.action.ActionReply
import ai.tock.bot.engine.action.ActionVisibility
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.dialog.EntityValue
import ai.tock.bot.engine.dialog.EventState
import ai.tock.bot.engine.message.Message
import ai.tock.bot.engine.message.MessagesList
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.UserPreferences
import ai.tock.bot.engine.user.UserState
import ai.tock.bot.engine.user.UserTimeline
import ai.tock.nlp.api.client.model.Entity
import ai.tock.nlp.entity.Value
import ai.tock.shared.defaultLocale
import ai.tock.translator.I18nContext
import ai.tock.translator.Translator
import ai.tock.translator.UserInterfaceType.textAndVoiceAssistant
import ai.tock.translator.UserInterfaceType.textChat
import ai.tock.translator.raw
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import java.time.Instant

private val endCalled = ThreadLocal<Boolean>()

/**
 * Test a [StoryDefinition] with a mocked (mockk) [BotBus].
 */
fun StoryDefinitionBase.test(bus: BotBus) {
    endCalled.remove()
    val handler = storyHandler as? StoryHandlerBase<*>
    handler?.checkPreconditions()?.invoke(bus)
    if (endCalled.get() != true) {
        (storyHandler as? StoryHandlerBase<*>)?.newHandlerDefinition(bus)?.handle()
            ?: error("story handler is not a StoryHandlerBase")
    }
}

/**
 * Default mockk BotBus configuration.
 */
fun provideMockedBusCommon(bus: BotBus = mockk()): BotBus {
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

    // withMessage
    every { bus.withMessage(any(), any()) }.answers {
        if (bus.targetConnectorType == args[0]) {
            @Suppress("UNCHECKED_CAST")
            (args[1] as (() -> ConnectorMessage)).invoke()
        }
        bus
    }

    // send
    every { bus.send(any<Message>(), any()) } returns bus
    every { bus.send(any<Action>(), any()) } returns bus
    every { bus.send(any<Long>()) } returns bus
    every { bus.send(any<CharSequence>(), any()) } returns bus
    every { bus.send(any(), any(), *anyVararg()) } returns bus
    every { bus.send(any(), *anyVararg()) } returns bus
    every { bus.send(any<Long>(), any()) }.answers {
        @Suppress("UNCHECKED_CAST")
        (args[1] as BotBus.() -> Any?).invoke(bus)
        bus
    }

    // end
    fun BotBus.endCall(): BotBus = apply { endCalled.set(true) }

    every { bus.end(any<MessagesList>(), any()) } answers { bus.endCall() }
    every { bus.end(any<Message>(), any()) } answers { bus.endCall() }
    every { bus.end(any<Action>(), any()) } answers { bus.endCall() }
    every { bus.end(any<Long>()) } answers { bus.endCall() }
    every { bus.end(any<CharSequence>(), any()) } answers { bus.endCall() }
    every { bus.end(any(), any(), *anyVararg()) } answers { bus.endCall() }
    every { bus.end(any(), *anyVararg()) } answers { bus.endCall() }
    every { bus.end(any<Long>(), any()) }.answers {
        @Suppress("UNCHECKED_CAST")
        (args[1] as BotBus.() -> Any?).invoke(bus)
        bus.endCall()
    }

    every {
        bus.entityValue<Value>(
            any<Entity>()
        )
    } returns null
    every {
        bus.entityValue(
            any<Entity>(),
            any<(EntityValue) -> Value?>()
        )
    } returns null
    every { bus.changeEntityValue(any(), any<Value>()) } returns Unit
    every {
        bus.entityText(
            any<Entity>()
        )
    } returns null
    every {
        bus.entityValueDetails(
            any<Entity>()
        )
    } returns null
    every {
        bus.entityValueDetails(
            any<String>()
        )
    } returns null
    every { bus.changeEntityText(any(), any()) } returns Unit

    val playerId = PlayerId("user")
    every { bus.userId } returns playerId
    val botId = PlayerId("bot")
    every { bus.botId } returns botId
    every { bus.connectorId } returns "appId"

    val userTimeline: UserTimeline = mockk()
    val userState = UserState(Instant.now())
    every { bus.userTimeline } returns userTimeline
    every { userTimeline.userState } returns userState
    every { userTimeline.playerId } returns playerId

    every { bus.userLocale } returns defaultLocale
    every { bus.userPreferences } returns UserPreferences()
    every { bus.userInterfaceType } returns textAndVoiceAssistant

    val botDefinition: BotDefinition = mockk()
    every { bus.botDefinition } returns botDefinition
    every { botDefinition.defaultDelay(any()) } returns 0
    every { bus.resetDialogState() } returns Unit

    every { bus.translate(any()) } answers { (args[0] as CharSequence).raw }
    every { bus.translate(any(), *anyVararg()) } answers {
        Translator.formatMessage(
            args[0].toString(),
            I18nContext(defaultLocale, textChat, null),
            args.subList(1, args.size)
        ).raw
    }
    every { bus.defaultDelay(any()) } returns 0

    mockkObject(SendChoice.Companion)
    every {
        SendChoice.encodeChoiceId(bus, any(), any<StoryStepDef>(), any())
    } answers {
        @Suppress("UNCHECKED_CAST")
        SendChoice.encodeChoiceId(
            arg<IntentAware>(1).wrappedIntent(),
            arg<StoryStepDef?>(2),
            arg<Map<String, String>>(3),
            null,
            null,
            bus.connectorId
        )
    }
    every {
        SendChoice.encodeChoiceId(bus, any(), any<String>(), any())
    } answers {
        @Suppress("UNCHECKED_CAST")
        SendChoice.encodeChoiceId(
            (args[1] as IntentAware).wrappedIntent(),
            args[2] as? String,
            (args[3] as? Map<String, String>) ?: emptyMap(),
            null,
            null,
            bus.connectorId
        )
    }
    val state = mockk<EventState>(relaxed = true)
    val action = mockk<Action>(relaxed = true)
    every { action.state } returns state
    every { bus.action } returns action
    every { bus.hasActionEntity(any<String>()) } returns false
    every { bus.entities } returns emptyMap()

    return bus
}

/**
 * Execute test with a bus mocked with default BotBus configuration.
 */
fun mockBus(bus: BotBus = mockk(), test: (BotBus) -> Any?) {
    try {
        provideMockedBusCommon(bus)

        test(bus)
    } finally {
        clearAllMocks()
    }
}

/**
 * Execute test with a bus mocked with classic messenger extensions.
 */
fun mockMessenger(bus: BotBus = mockk(), test: (BotBus) -> Any?) {
    try {
        provideMockedMessengerBus(bus)

        test(bus)
    } finally {
        clearAllMocks()
    }
}

/**
 * Mock classic messenger extensions.
 */
fun provideMockedMessengerBus(bus: BotBus = mockk()): BotBus {
    provideMockedBusCommon(bus)

    mockkStatic("ai.tock.bot.connector.messenger.MessengerBuildersKt")
    every { bus.targetConnectorType } returns messengerConnectorType
    every { bus.withMessenger(any()) }.answers {
        if (bus.targetConnectorType == messengerConnectorType) {
            @Suppress("UNCHECKED_CAST")
            (args[1] as (() -> MessengerConnectorMessage)).invoke()
        }
        bus
    }

    return bus
}

/**
 * Execute test with a bus mocked with classic twitter extensions.
 */
fun mockTwitter(bus: BotBus, test: (BotBus) -> Any?) {
    try {
        provideMockedTwitterBus(bus)

        test(bus)
    } finally {
        clearAllMocks()
    }
}

/**
 * Mock classic twitter extensions.
 */
fun provideMockedTwitterBus(bus: BotBus): BotBus {
    provideMockedBusCommon(bus)

    mockkStatic("ai.tock.bot.connector.twitter.TwitterBuildersKt")
    every { bus.targetConnectorType } returns twitterConnectorType
    every { bus.action.metadata.visibility } returns ActionVisibility.UNKNOWN
    every { bus.action.metadata.quoteMessage } returns ActionQuote.UNKNOWN
    every { bus.action.metadata.replyMessage } returns ActionReply.UNKNOWN
    every { bus.withTwitter(any()) }.answers {
        if (bus.targetConnectorType == twitterConnectorType) {
            @Suppress("UNCHECKED_CAST")
            (args[1] as (() -> TwitterConnectorMessage)).invoke()
        }
        bus
    }

    return bus
}

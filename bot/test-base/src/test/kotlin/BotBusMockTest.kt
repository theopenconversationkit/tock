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

package ai.tock.bot.test

import ai.tock.bot.connector.messenger.model.send.TextMessage
import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.SimpleStoryHandlerBase
import ai.tock.bot.definition.SimpleStoryStep
import ai.tock.bot.definition.StoryDefinition
import ai.tock.bot.definition.StoryHandlerListener
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.ActionMetadata
import ai.tock.bot.engine.dialog.EventState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 *
 */
class BotBusMockTest {

    enum class Step : SimpleStoryStep { a }

    val intent = Intent("main")
    val storyHandler: SimpleStoryHandlerBase = mockk()
    val storyDefinition: StoryDefinition = mockk()
    val action: Action = mockk()
    val testContext: TestContext = mockk()
    val metadata = ActionMetadata()
    val botDefinition: BotDefinition = mockk()

    val context: BotBusMockContext by lazy {
        BotBusMockContext(
            botDefinition,
            storyDefinition,
            testContext = testContext
        )
    }

    @BeforeEach
    fun before() {
        every { storyHandler.handle(any()) } answers {}

        every { storyDefinition.storyHandler } returns storyHandler
        every { storyDefinition.mainIntent() } returns intent
        every { storyDefinition.supportIntent(any()) } returns true
        every { storyDefinition.steps } returns emptySet()
        every { storyDefinition.id } returns "storyId"

        every { action.applicationId } returns "appId"
        every { action.state } returns EventState()
        every { action.metadata } returns metadata

        every { botDefinition.defaultDelay(any()) } returns 1000
        every { botDefinition.botId } returns "botId"
        every { botDefinition.findIntent(any()) } returns intent

        every { testContext.storyHandlerListeners } returns mutableListOf()

        every { testContext.botAnswerInterceptors } returns mutableListOf()

    }

    @Test
    fun `storyHandlerListener start method is called AND storyhandler not called if it returns false`() {
        val listener1: StoryHandlerListener = mockk()
        every { listener1.startAction(any(), any()) } returns true
        val listener2: StoryHandlerListener = mockk()
        every { listener2.startAction(any(), any()) } returns false

        every { testContext.storyHandlerListeners } returns mutableListOf(listener1, listener2)

        val botBus = BotBusMock(context, action)
        botBus.run()

        verify { listener1.startAction(any(), any()) }
        verify { listener2.startAction(any(), any()) }
        verify(exactly = 0) { storyHandler.handle(any()) }
    }

    @Test
    fun `storyHandlerListener end method is called`() {
        val listener1: StoryHandlerListener = mockk()
        every { listener1.startAction(any(), any()) } returns true
        every { listener1.endAction(any(), any()) } answers {}
        val listener2: StoryHandlerListener = mockk()
        every { listener2.startAction(any(), any()) } returns true
        every { listener2.endAction(any(), any()) } answers {}

        every { testContext.storyHandlerListeners } returns mutableListOf(listener1, listener2)

        val botBus = BotBusMock(context, action)
        botBus.run()

        verify { listener1.startAction(any(), any()) }
        verify { listener2.startAction(any(), any()) }
        verify { storyHandler.handle(any()) }
        verify { listener1.endAction(any(), any()) }
        verify { listener2.endAction(any(), any()) }
    }

    @Test
    fun `not end called throws Exception`() {
        val botBus = BotBusMock(context, action)
        val action: Action = mockk()
        every { action.metadata } returns ActionMetadata()
        every { action.state } returns EventState()

        every { storyHandler.handle(any()) } answers {
            botBus.send(action)
        }

        botBus.run()

        assertThrows(
            IllegalStateException::class.java
        ) { botBus.checkEndCalled() }
    }

    @Test
    fun `end called twice throws Exception`() {
        val botBus = BotBusMock(context, action)
        val action: Action = mockk()
        every { action.metadata } returns ActionMetadata()
        every { action.state } returns EventState()

        every { storyHandler.handle(any()) } answers {
            botBus.end(action)
            botBus.end(action)
        }

        botBus.run()

        assertThrows(
            IllegalStateException::class.java
        ) { botBus.checkEndCalled() }
    }

    @Test
    fun `unsupported step is not persisted`() {
        val botBus = BotBusMock(context, context.choice("intent", Step.a))
        val action: Action = mockk()
        every { action.metadata } returns ActionMetadata()
        every { action.state } returns EventState()

        botBus.run()

        assertNull(botBus.story.currentStep)
    }

    @Test
    fun `supported step is persisted`() {
        every { storyDefinition.steps } returns setOf(Step.a)
        val botBus = BotBusMock(context, context.choice("intent", Step.a))
        val action: Action = mockk()
        every { action.metadata } returns ActionMetadata()
        every { action.state } returns EventState()

        botBus.run()

        assertEquals(Step.a, botBus.story.currentStep)
    }

    @Test
    fun `send message with no content is throwing an error`() {
        val botBus = BotBusMock(context)
        assertThrows(IllegalStateException::class.java) {
            botBus.end()
        }
    }

    @Test
    fun `forgot to send a message is throwing an error`() {
        val botBus = BotBusMock(context)
        botBus.withMessage(TextMessage("one"))
        assertThrows(IllegalStateException::class.java) {
            botBus.withMessage(TextMessage("one"))
        }
    }
}
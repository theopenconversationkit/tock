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

import fr.vsct.tock.bot.definition.BotDefinition
import fr.vsct.tock.bot.definition.Intent
import fr.vsct.tock.bot.definition.SimpleStoryHandlerBase
import fr.vsct.tock.bot.definition.StoryDefinition
import fr.vsct.tock.bot.definition.StoryHandlerListener
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.ActionMetadata
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.dialog.DialogState
import fr.vsct.tock.bot.engine.dialog.EventState
import fr.vsct.tock.bot.engine.dialog.Story
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 *
 */
class BotBusMockTest {

    val storyHandler: SimpleStoryHandlerBase = mockk()
    val storyDefinition: StoryDefinition = mockk()
    val story: Story = mockk()
    val action: Action = mockk()
    val testContext: TestContext = mockk()
    val dialog: Dialog = mockk()
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
        every { storyDefinition.mainIntent() } returns Intent("main")

        every { story.definition } returns storyDefinition
        every { story.actions } returns mutableListOf()

        every { action.applicationId } returns "appId"
        every { action.state } returns EventState()
        every { action.metadata } returns metadata

        every { dialog.state } returns DialogState()
        every { dialog.stories } returns mutableListOf()

        every { botDefinition.defaultDelay(any()) } returns 1000
        every { botDefinition.botId } returns "botId"

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
}
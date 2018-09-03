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

import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.definition.StoryDefinition
import fr.vsct.tock.bot.definition.StoryHandler
import fr.vsct.tock.bot.definition.StoryHandlerListener
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.dialog.DialogState
import fr.vsct.tock.bot.engine.dialog.EventState
import fr.vsct.tock.bot.engine.dialog.Story
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 *
 */
class BotBusMockTest {

    val storyHandler: StoryHandler = mockk()
    val storyDefinition: StoryDefinition = mockk()
    val story: Story = mockk()
    val action: Action = mockk()
    val context: BotBusMockContext = mockk()
    val testContext: TestContext = mockk()
    val dialog: Dialog = mockk()

    @BeforeEach
    fun before() {
        every { storyHandler.handle(any()) } answers {}

        every { storyDefinition.storyHandler } returns storyHandler

        every { story.definition } returns storyDefinition

        every { action.applicationId } returns "appId"
        every { action.state } returns EventState()

        every { context.connectorType } returns ConnectorType.none
        every { context.story } returns story
        every { context.firstAction } returns action

        every { context.testContext } returns testContext

        every { context.dialog } returns dialog
        every { dialog.state } returns DialogState()
        every { dialog.stories } returns mutableListOf()
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
}
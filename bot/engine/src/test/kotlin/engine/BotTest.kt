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

package ai.tock.bot.engine

import ai.tock.bot.definition.Intent
import ai.tock.bot.engine.StepTest.s4
import ai.tock.bot.engine.TestStoryDefinition.test
import ai.tock.bot.engine.TestStoryDefinition.unknown
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.Story
import ai.tock.bot.engine.message.Choice
import ai.tock.bot.engine.message.Sentence
import io.mockk.every
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

/**
 *
 */
class BotTest : BotEngineTest() {

    @Test
    fun handleSendSentence_whenNotWaitingRawInput_shouldSendNlpQuery() {
        bot.handle(userAction, userTimeline, connectorController, connectorData)

        verify { nlp.parseSentence(any(), any(), any(), any(), any()) }
    }

    @Test
    fun handleSendChoice_shouldNotReturnUnknownStory_whenIntentIsSecondaryIntentAndNoStoryExists() {
        val choice = action(
            Choice(
                secondaryIntent.name,
                mapOf(
                    SendChoice.PREVIOUS_INTENT_PARAMETER to test.name
                )
            )
        )
        dialog.stories.clear()
        bot.handle(choice, userTimeline, connectorController, connectorData)

        assertEquals(story.definition.id, dialog.currentStory!!.definition.id)
        assertEquals(test.mainIntent(), dialog.currentStory!!.starterIntent)
        assertEquals(secondaryIntent, dialog.state.currentIntent)
    }

    @Test
    fun `handle new story with steps does not select a step if there is no step that support this intent`() {
        val sentence = action(Sentence("other"))

        val sendSentence = slot<SendSentence>()
        val capturedDialog = slot<Dialog>()

        every { nlp.parseSentence(capture(sendSentence), any(), capture(capturedDialog), any(), any()) } answers {
            sendSentence.captured.state.intent = otherStory.name
            capturedDialog.captured.state.currentIntent = otherStory.mainIntent()
        }

        bot.handle(sentence, userTimeline, connectorController, connectorData)

        assertEquals(otherStory, dialog.currentStory?.definition)
        assertNull(dialog.currentStory?.step)
    }

    @Test
    fun `GIVEN selected step WHEN secondary intent of the step is called THEN step (and story) is still selected`() {
        val def = story.definition
        story.step = s4.name
        val sentence = action(Sentence("s4_secondary"))

        val sendSentence = slot<SendSentence>()
        val capturedDialog = slot<Dialog>()

        every { nlp.parseSentence(capture(sendSentence), any(), capture(capturedDialog), any(), any()) } answers {
            sendSentence.captured.state.intent = "s4_secondary"
            capturedDialog.captured.state.currentIntent = Intent("s4_secondary")
        }

        bot.handle(sentence, userTimeline, connectorController, connectorData)

        assertEquals(def, dialog.currentStory?.definition)
        assertEquals(s4.name, dialog.currentStory?.step)
    }

    @Test
    fun `GIVEN no selected step WHEN secondary intent of the story is called THEN story is not selected anymore`() {
        val def = story.definition
        story.step = null
        val sentence = action(Sentence("s4_secondary"))

        val sendSentence = slot<SendSentence>()
        val capturedDialog = slot<Dialog>()

        every { nlp.parseSentence(capture(sendSentence), any(), capture(capturedDialog), any(), any()) } answers {
            sendSentence.captured.state.intent = "s4_secondary"
            capturedDialog.captured.state.currentIntent = Intent("s4_secondary")
        }

        bot.handle(sentence, userTimeline, connectorController, connectorData)

        assertEquals(unknown, dialog.currentStory?.definition)
        assertNull(dialog.currentStory?.step)
    }

    @Test
    fun `GIVEN selected step WHEN secondary intent of the story is called THEN step (and story) is still selected`() {
        val def = story.definition
        story.step = s4.name
        val sentence = action(Sentence("secondaryIntent"))

        val sendSentence = slot<SendSentence>()
        val capturedDialog = slot<Dialog>()

        every { nlp.parseSentence(capture(sendSentence), any(), capture(capturedDialog), any(), any()) } answers {
            sendSentence.captured.state.intent = "secondaryIntent"
            capturedDialog.captured.state.currentIntent = secondaryIntent
        }

        bot.handle(sentence, userTimeline, connectorController, connectorData)

        assertEquals(def, dialog.currentStory?.definition)
        assertEquals(s4.name, dialog.currentStory?.step)
    }

    @Test
    fun `GIVEN no selected step WHEN secondary intent of one step is called THEN story is not selected any more`() {
        val def = story.definition
        story.step = null
        val sentence = action(Sentence("s4_secondary"))

        val sendSentence = slot<SendSentence>()
        val capturedDialog = slot<Dialog>()

        every { nlp.parseSentence(capture(sendSentence), any(), capture(capturedDialog), any(), any()) } answers {
            sendSentence.captured.state.intent = "s4_secondary"
            capturedDialog.captured.state.currentIntent = Intent("s4_secondary")
        }

        bot.handle(sentence, userTimeline, connectorController, connectorData)

        assertEquals(unknown, dialog.currentStory?.definition)
        assertNull(dialog.currentStory?.step)
    }

    @Test
    fun `GIVEN disabled user THEN handle notification action does not persist action in history`() {
        userTimeline.userState.botDisabled = true
        val sentence = action(Sentence("other")).apply { state.notification = true }
        assertTrue(connectorData.saveTimeline)
        bot.handle(sentence, userTimeline, connectorController, connectorData)
        assertFalse(connectorData.saveTimeline)
        assertTrue(userTimeline.userState.botDisabled)
    }

    @Test
    fun `GIVEN enabled user THEN handle notification disable user and action is persisted in history`() {
        userTimeline.userState.botDisabled = false
        val sentence = action(Sentence("other")).apply { state.notification = true }
        assertTrue(connectorData.saveTimeline)
        bot.handle(sentence, userTimeline, connectorController, connectorData)
        assertTrue(connectorData.saveTimeline)
        assertTrue(userTimeline.userState.botDisabled)
    }

    @Test
    fun `GIVEN enabled user THEN handle action persists action in history`() {
        val sentence = action(Sentence("other"))
        assertTrue(connectorData.saveTimeline)
        bot.handle(sentence, userTimeline, connectorController, connectorData)
        assertTrue(connectorData.saveTimeline)
        assertFalse(userTimeline.userState.botDisabled)
    }

    @Test
    fun `GIVEN enabled user AND disabled action THEN handle action persist action in history`() {
        val sentence = action(Sentence("other"))
        dialog.state.currentIntent = disableStory.mainIntent()
        assertTrue(connectorData.saveTimeline)
        bot.handle(sentence, userTimeline, connectorController, connectorData)
        assertTrue(connectorData.saveTimeline)
        assertTrue(userTimeline.userState.botDisabled)
    }

    @Test
    fun `GIVEN disabled user AND enable action THEN handle action persist action in history`() {
        userTimeline.userState.botDisabled = true
        dialog.state.currentIntent = enableStory.mainIntent()
        val sentence = action(Sentence("other"))
        assertTrue(connectorData.saveTimeline)
        bot.handle(sentence, userTimeline, connectorController, connectorData)
        assertTrue(connectorData.saveTimeline)
        assertFalse(userTimeline.userState.botDisabled)
    }

    @Test
    fun `GIVEN disabled intent tagged story WHEN call intent THEN send message before disabling bot`() {

        // Given
        val sentence = action(Sentence("disable bot"))
        val connectorControllerSpy = spyk(connectorController)

        // When
        dialog.state.currentIntent = disableBotTaggedStory.mainIntent()
        bot.handle(sentence, userTimeline, connectorControllerSpy, connectorData)

        // Then
        verify { connectorControllerSpy.startTypingInAnswerTo(sentence, connectorData) }
        assertTrue(userTimeline.userState.botDisabled)
    }

    @Test
    fun `GIVEN When ASK_AGAIN TAG is present on previous story THEN return no more rounds and hasCurrentAskAgainProcess true`() {
        dialog.stories.clear()


        val sentence = action(Sentence("unknown"))
        val sendSentence = slot<SendSentence>()
        val capturedDialog = slot<Dialog>()

        every { nlp.parseSentence(capture(sendSentence), any(), capture(capturedDialog), any(), any()) } answers {
            capturedDialog.captured.stories.addAll(
                listOf<Story>(
                    Story(
                        TestStoryDefinition.withAskAgainTag,
                        TestStoryDefinition.withAskAgainTag.wrappedIntent()
                    )
                )
            )
            capturedDialog.captured.state.currentIntent = unknown.wrappedIntent()
        }

        bot.handle(sentence, userTimeline, connectorController, connectorData)

        assertEquals(0, dialog.state.askAgainRound)
        assertEquals(true, dialog.state.hasCurrentAskAgainProcess)
    }

    @Test
    fun `GIVEN When ASK_AGAIN TAG is not present on previous story THEN default rounds and hasCurrentAskAgainProcess false`() {
        dialog.stories.clear()


        val sentence = action(Sentence("unknown"))
        val sendSentence = slot<SendSentence>()
        val capturedDialog = slot<Dialog>()

        every { nlp.parseSentence(capture(sendSentence), any(), capture(capturedDialog), any(), any()) } answers {
            capturedDialog.captured.stories.addAll(
                listOf<Story>(
                    Story(
                        TestStoryDefinition.withoutStep,
                        TestStoryDefinition.withoutStep.wrappedIntent()
                    )
                )
            )
            capturedDialog.captured.state.currentIntent = unknown.wrappedIntent()
        }

        bot.handle(sentence, userTimeline, connectorController, connectorData)

        assertEquals(1, dialog.state.askAgainRound)
        assertEquals(false, dialog.state.hasCurrentAskAgainProcess)
    }

    @Test
    fun `GIVEN When ASK_AGAIN TAG is present and after no more ask again rounds on previous story THEN default rounds and no currentAskAgainProcess`() {
        dialog.stories.clear()

        val sentence = action(Sentence("unknown"))
        val sendSentence = slot<SendSentence>()
        val capturedDialog = slot<Dialog>()

        every { nlp.parseSentence(capture(sendSentence), any(), capture(capturedDialog), any(), any()) } answers {
            capturedDialog.captured.stories.addAll(
                listOf(
                    Story(TestStoryDefinition.withAskAgainTag, TestStoryDefinition.withAskAgainTag.wrappedIntent()),
                    Story(
                        unknown,
                        Intent.unknown
                    )
                )
            )
            capturedDialog.captured.state.currentIntent = unknown.wrappedIntent()
        }

        bot.handle(sentence, userTimeline, connectorController, connectorData)

        assertEquals(1, dialog.state.askAgainRound)
        assertEquals(false, dialog.state.hasCurrentAskAgainProcess)
    }
}

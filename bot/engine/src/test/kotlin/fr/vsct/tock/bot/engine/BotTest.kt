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

package fr.vsct.tock.bot.engine

import fr.vsct.tock.bot.engine.TestStoryDefinition.test
import fr.vsct.tock.bot.engine.action.SendChoice
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.message.Choice
import fr.vsct.tock.bot.engine.message.Sentence
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
}
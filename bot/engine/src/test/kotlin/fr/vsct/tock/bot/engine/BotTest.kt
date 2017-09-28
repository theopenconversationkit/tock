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

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.verify
import fr.vsct.tock.bot.engine.TestStoryDefinition.test
import fr.vsct.tock.bot.engine.action.SendChoice
import fr.vsct.tock.bot.engine.message.Choice
import org.junit.Test
import kotlin.test.assertEquals

/**
 *
 */
class BotTest : BotEngineTest() {

    @Test
    fun handleSendSentence_whenNotWaitingRawInput_shouldSendNlpQuery() {
        bot.handle(userAction, userTimeline, connectorController)

        verify(nlp).parseSentence(any(), any(), any(), any(), any())
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
        bot.handle(choice, userTimeline, connectorController)

        assertEquals(story.definition.id, dialog.currentStory()!!.definition.id)
        assertEquals(test.mainIntent(), dialog.currentStory()!!.starterIntent)
        assertEquals(secondaryIntent, dialog.state.currentIntent)
    }
}
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

package fr.vsct.tock.bot.engine.nlp

import fr.vsct.tock.bot.engine.BotEngineTest
import fr.vsct.tock.bot.engine.BotRepository
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.dialog.ContextValue
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.dialog.NextUserActionState
import fr.vsct.tock.bot.engine.user.UserTimeline
import fr.vsct.tock.nlp.api.client.model.NlpIntentQualifier
import fr.vsct.tock.nlp.api.client.model.NlpQuery
import fr.vsct.tock.nlp.api.client.model.NlpResult
import fr.vsct.tock.nlp.entity.Value
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 *
 */
class NlpTest : BotEngineTest() {

    @Test
    fun parseSentence_shouldCallNlpClientParse_whenExpectedIntentIsNullInDialogState() {
        Nlp().parseSentence(userAction as SendSentence, userTimeline, dialog, connectorController, botDefinition)
        verify { nlpClient.parse(any()) }
        verify(exactly = 0) { nlpClient.parse(match { it.intentsSubset.isNotEmpty() }) }
    }

    @Test
    fun `GIVEN intent qualifiers not null in dialog state, THEN parse is_called with intentsSubset not empty`() {
        dialog.state.nextActionState = NextUserActionState(listOf(NlpIntentQualifier("test2")))
        Nlp().parseSentence(userAction as SendSentence, userTimeline, dialog, connectorController, botDefinition)
        verify { nlpClient.parse(match { it.intentsSubset.isNotEmpty() }) }
        verify(exactly = 0) { nlpClient.parse(match { it.intentsSubset.isEmpty() }) }
    }

    @Test
    fun parseSentence_shouldNotRegisterQuery_whenBotIsDisabled() {

        userTimeline.userState.botDisabled = true
        Nlp().parseSentence(userAction as SendSentence, userTimeline, dialog, connectorController, botDefinition)

        val slot = slot<NlpQuery>()
        verify {
            nlpClient.parse(capture(slot))
        }

        assertFalse(slot.captured.context.test)
        assertFalse(slot.captured.context.registerQuery)

    }

    @Test
    fun parseSentence_shouldRegisterQuery_whenBotIsNotDisabledAndItIsNotATestContext() {

        Nlp().parseSentence(userAction as SendSentence, userTimeline, dialog, connectorController, botDefinition)

        val slot = slot<NlpQuery>()
        verify {
            nlpClient.parse(capture(slot))
        }

        assertFalse(slot.captured.context.test)
        assertTrue(slot.captured.context.registerQuery)

    }

    @Test
    fun parseSentence_shouldUseNlpListenersEntityEvaluation_WhenAvailable() {

        every { nlpClient.parse(any()) } returns nlpResult
        every { nlpClient.parse(match { it.intentsSubset.isNotEmpty() }) } returns nlpResult

        val customValue = ContextValue(entityB, object : Value {}, "b")
        val nlpListener = object : NlpListener {
            override fun evaluateEntities(
                userTimeline: UserTimeline,
                dialog: Dialog,
                nlpResult: NlpResult
            ): List<ContextValue> {
                return listOf(customValue)
            }
        }
        BotRepository.nlpListeners.add(nlpListener)
        Nlp().parseSentence(userAction as SendSentence, userTimeline, dialog, connectorController, botDefinition)

        assertEquals(3, userAction.state.entityValues.size)
        assertTrue(userAction.state.entityValues.contains(customValue))
        assertTrue(userAction.state.entityValues.contains(ContextValue(nlpResult, entityAValue)))
        assertTrue(userAction.state.entityValues.contains(ContextValue(nlpResult, entityCValue)))
    }
}
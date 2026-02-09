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

package ai.tock.bot.engine.nlp

import ai.tock.bot.definition.IntentAware
import ai.tock.bot.engine.BotEngineTest
import ai.tock.bot.engine.BotRepository
import ai.tock.bot.engine.TestStoryDefinition.test
import ai.tock.bot.engine.TestStoryDefinition.test2
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.DialogState
import ai.tock.bot.engine.dialog.EntityStateValue
import ai.tock.bot.engine.dialog.EntityValue
import ai.tock.bot.engine.dialog.NextUserActionState
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.user.UserTimeline
import ai.tock.nlp.api.client.model.NlpIntentQualifier
import ai.tock.nlp.api.client.model.NlpQuery
import ai.tock.nlp.api.client.model.NlpResult
import ai.tock.nlp.api.client.model.merge.ValuesMergeQuery
import ai.tock.nlp.entity.Value
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
internal class NlpTest : BotEngineTest() {
    @Test
    suspend fun parseSentence_shouldCallNlpClientParse_whenExpectedIntentIsNullInDialogState() {
        Nlp().parseSentence(userAction as SendSentence, userTimeline, dialog, connectorController, botDefinition)
        verify { nlpClient.parse(any()) }
        verify(exactly = 0) { nlpClient.parse(match { it.intentsSubset.isNotEmpty() }) }
    }

    @Test
    suspend fun `GIVEN intent qualifiers not null in dialog state THEN parse is_called with intentsSubset not empty`() {
        dialog.state.nextActionState = NextUserActionState(listOf(NlpIntentQualifier("test2")))
        Nlp().parseSentence(userAction as SendSentence, userTimeline, dialog, connectorController, botDefinition)
        verify { nlpClient.parse(match { it.intentsSubset.isNotEmpty() }) }
        verify(exactly = 0) { nlpClient.parse(match { it.intentsSubset.isEmpty() }) }
    }

    @Test
    suspend fun `GIVEN intent qualifiers not null in dialog state WHEN parse call returns an intent not in the list THEN the the best modifier intent is returned`() {
        dialog.state.nextActionState =
            NextUserActionState(listOf(NlpIntentQualifier("test3", 0.2), NlpIntentQualifier("test2", 0.5)))
        every { nlpClient.parse(any()) } returns nlpResult
        val sentence = userAction as SendSentence
        Nlp().parseSentence(sentence, userTimeline, dialog, connectorController, botDefinition)
        assertEquals("test2", sentence.nlpStats?.intentResult?.name)
    }

    @Test
    suspend fun parseSentence_shouldNotRegisterQuery_whenBotIsDisabled() {
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
    suspend fun parseSentence_shouldRegisterQuery_whenBotIsNotDisabledAndItIsNotATestContext() {
        Nlp().parseSentence(userAction as SendSentence, userTimeline, dialog, connectorController, botDefinition)

        val slot = slot<NlpQuery>()
        verify {
            nlpClient.parse(capture(slot))
        }

        assertFalse(slot.captured.context.test)
        assertTrue(slot.captured.context.registerQuery)
    }

    @Test
    suspend fun parseSentence_shouldUseNlpListenersEntityEvaluation_WhenAvailable() {
        every { nlpClient.parse(any()) } returns nlpResult
        every { nlpClient.parse(match { it.intentsSubset.isNotEmpty() }) } returns nlpResult

        val customValue = EntityValue(entityB, object : Value {}, "b")
        val nlpListener =
            object : NlpListener {
                override fun evaluateEntities(
                    userTimeline: UserTimeline,
                    dialog: Dialog,
                    event: Event,
                    nlpResult: NlpResult,
                ): List<EntityValue> {
                    return listOf(customValue)
                }
            }
        BotRepository.registerNlpListener(nlpListener)
        Nlp().parseSentence(userAction as SendSentence, userTimeline, dialog, connectorController, botDefinition)

        assertEquals(5, userAction.state.entityValues.size)
        assertTrue(userAction.state.entityValues.contains(customValue))
        assertTrue(userAction.state.entityValues.contains(EntityValue(nlpResult, entityAValue)))
        assertTrue(userAction.state.entityValues.contains(EntityValue(nlpResult, entityCValue)))
    }

    @Test
    suspend fun `parseSentence uses NlpListener#findIntent when available`() {
        every { nlpClient.parse(any()) } returns nlpResult
        every { nlpClient.parse(match { it.intentsSubset.isNotEmpty() }) } returns nlpResult

        val nlpListener =
            object : NlpListener {
                override fun findIntent(
                    userTimeline: UserTimeline,
                    dialog: Dialog,
                    event: Event,
                    nlpResult: NlpResult,
                ): IntentAware? {
                    return test2
                }
            }
        BotRepository.registerNlpListener(nlpListener)
        Nlp().parseSentence(userAction as SendSentence, userTimeline, dialog, connectorController, botDefinition)

        assertEquals(test2.wrappedIntent(), dialog.state.currentIntent)
        assertEquals(test2.wrappedIntent(), (userAction as SendSentence).nlpStats?.intentResult)
        assertEquals(test.wrappedIntent().name, (userAction as SendSentence).nlpStats?.nlpResult?.intent)
    }

    @Test
    suspend fun `NlpListener#configureEntityValuesMerge can be used to configure entity values merge`() {
        every { nlpClient.parse(any()) } returns nlpResult
        every { nlpClient.parse(match { it.intentsSubset.isNotEmpty() }) } returns nlpResult
        val mergeQuery = slot<ValuesMergeQuery>()
        every { nlpClient.mergeValues(capture(mergeQuery)) } returns null

        val customInitialValue = EntityStateValue(EntityValue(entityWithMergeSupport, object : Value {}, "Z"))
        val nlpListener =
            object : NlpListener {
                override fun mergeEntityValues(
                    dialogState: DialogState,
                    action: Action,
                    entityToMerge: NlpEntityMergeContext,
                ): NlpEntityMergeContext =
                    if (entityToMerge.entityRole == entityWithMergeSupport.role) {
                        NlpEntityMergeContext(
                            entityWithMergeSupport.role,
                            EntityStateValue(customInitialValue.value),
                            entityToMerge.newValues,
                        )
                    } else {
                        entityToMerge
                    }
            }
        BotRepository.registerNlpListener(nlpListener)
        Nlp().parseSentence(userAction as SendSentence, userTimeline, dialog, connectorController, botDefinition)

        verify { nlpClient.mergeValues(any()) }
        val query = mergeQuery.captured
        assertEquals(3, query.values.size)
        assertEquals(customInitialValue.value?.content, query.values.first { it.initial }.content)
    }
}

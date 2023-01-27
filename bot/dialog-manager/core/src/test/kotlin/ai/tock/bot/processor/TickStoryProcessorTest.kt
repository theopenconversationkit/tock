/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

package ai.tock.bot.processor

import ai.tock.bot.*
import ai.tock.bot.bean.*
import ai.tock.bot.bean.unknown.*
import ai.tock.bot.graphsolver.GraphSolver
import ai.tock.bot.handler.ActionHandlersRepository
import ai.tock.bot.sender.TickSender
import ai.tock.bot.sender.TickSenderDefault
import ai.tock.bot.statemachine.State
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.*

internal class TickStoryProcessorTest {

    enum class StateIds(val value: String) {
        GLOBAL("Global"),
        STATE_1("State1"),
        STATE_2("State2"),
        STATE_3("State3")
    }

    enum class IntentNames(val value: String) {
        INTENT_1("intent1"),
        UNKNOWN_INTENT(UNKNOWN)
    }

    enum class TriggerNames(val  value: String){
         TRIGGER_1("trigger1")
    }

    enum class HandlerNames(val value: String) {
        HANDLER_1("handler1"),
        HANDLER_2("handler2"),
    }

    enum class ContextNames(val value: String) {
        CONTEXT_1("context1")
    }

    private lateinit var configuration: TickConfiguration
    private lateinit var session: TickSession
    private val sender = mockk<TickSender>()

    @BeforeEach
    internal fun setUp() {
        mockkObject(GraphSolver)
        mockkObject(ActionHandlersRepository)


        configuration = TickConfiguration(
            State(StateIds.GLOBAL.value,
                states = mutableMapOf(),
                on = mutableMapOf()
            ),
            actions = mutableSetOf(),
            contexts = mutableSetOf(),
            intentsContexts = mutableSetOf(),
            unknownHandleConfiguration = TickUnknownConfiguration(),
            storySettings = TickStorySettings(2)
        )

        session = TickSession()
    }

    @AfterEach
    internal fun tearDown() {
        unmockkObject(GraphSolver)
        unmockkObject(ActionHandlersRepository)
    }

    @Test
    fun `process when action is repeated more than the max number of repetitions`() {

        val produceProcessor: TSupplier<TickStoryProcessor> = {
            TickStoryProcessor(
                session = session.copy(
                    handlingStep = TickActionHandlingStep(
                        action = StateIds.STATE_3.value,
                        repeated = 3
                    )
                ),
                configuration.copy(
                    stateMachine = configuration.stateMachine.copy(
                        states =  mapOf(
                            StateIds.STATE_1.value to State(StateIds.STATE_1.value),
                            StateIds.STATE_2.value to State(StateIds.STATE_2.value),
                            StateIds.STATE_3.value to State(StateIds.STATE_3.value)
                        ),
                        on = mapOf(
                            IntentNames.INTENT_1.value to "#${StateIds.STATE_1.value}"
                        )
                    ),
                    actions =  setOf(
                        TickAction(
                            StateIds.STATE_1.value,
                            handler = HandlerNames.HANDLER_1.value,
                            inputContextNames = setOf(),
                            outputContextNames = setOf(),
                            final = false
                        ),
                        TickAction(
                            StateIds.STATE_2.value,
                            handler = HandlerNames.HANDLER_2.value,
                            inputContextNames = setOf(),
                            outputContextNames = setOf(),
                            final = false
                        ),
                        TickAction(
                            StateIds.STATE_3.value,
                            inputContextNames = setOf(),
                            outputContextNames = setOf(),
                            final = false
                        )
                    ),
                    storySettings = TickStorySettings(
                        2,
                        "storyId"
                    )
                ),
                TickSenderDefault(),
                false
            )
        }

        val mockBehaviours: TRunnable = {
            every { GraphSolver.solve(any(), any(), any(), any(), any(), any()) } returns listOf(StateIds.STATE_3.value)
            every { ActionHandlersRepository.invoke(any(), any()) } returns mapOf(ContextNames.CONTEXT_1.value to null)
        }

        val processCall: TFunction<TickStoryProcessor?, ProcessingResult> = {
            it!!.process(TickUserAction(IntentNames.INTENT_1.value, emptyMap()))
        }

        val checkResult: TConsumer<ProcessingResult?> = {

            assertNotNull(it)
            assert(it is Redirect)

        }

        TestCase<TickStoryProcessor, ProcessingResult>("process when action is repeated more than the max number of repetitions")

            .given("""
    - user intent "intent1" leads to a primary objective "State1"
    - secondary objective has no handler and no trigger
                   """, produceProcessor)

            .and("""
    - graph resolver find a secondary objective "State3"
    - secondary objective has no handler and no trigger
                    """, mockBehaviours)

            .`when`("""
    - processor.process method is called with a user intent "intent1"
                 """, processCall)

            .then("result should be a redirect", checkResult)

            .run()
    }

    @Test
    fun `process when action is repeated `() {

        val produceProcessor: TSupplier<TickStoryProcessor> = {
            TickStoryProcessor(
                session = session.copy(
                    handlingStep = TickActionHandlingStep(
                        action = StateIds.STATE_3.value,
                        repeated = 1
                    )
                ),
                configuration.copy(
                    stateMachine = configuration.stateMachine.copy(
                        states =  mapOf(
                            StateIds.STATE_1.value to State(StateIds.STATE_1.value),
                            StateIds.STATE_2.value to State(StateIds.STATE_2.value),
                            StateIds.STATE_3.value to State(StateIds.STATE_3.value)
                        ),
                        on = mapOf(
                            IntentNames.INTENT_1.value to "#${StateIds.STATE_1.value}"
                        )
                    ),
                    actions =  setOf(
                        TickAction(
                            StateIds.STATE_1.value,
                            handler = HandlerNames.HANDLER_1.value,
                            inputContextNames = setOf(),
                            outputContextNames = setOf(),
                            final = false
                        ),
                        TickAction(
                            StateIds.STATE_2.value,
                            handler = HandlerNames.HANDLER_2.value,
                            inputContextNames = setOf(),
                            outputContextNames = setOf(),
                            final = false
                        ),
                        TickAction(
                            StateIds.STATE_3.value,
                            inputContextNames = setOf(),
                            outputContextNames = setOf(),
                            final = false
                        )
                    ),
                    storySettings = TickStorySettings(
                        2,
                        "storyId"
                    )
                ),
                TickSenderDefault(),
                false
            )
        }

        val mockBehaviours: TRunnable = {
            every { GraphSolver.solve(any(), any(), any(), any(), any(), any()) } returns listOf(StateIds.STATE_3.value)
            every { ActionHandlersRepository.invoke(any(), any()) } returns mapOf(ContextNames.CONTEXT_1.value to null)
        }

        val processCall: TFunction<TickStoryProcessor?, ProcessingResult> = {
            it!!.process(TickUserAction(IntentNames.INTENT_1.value, emptyMap()))
        }

        val checkResult: TConsumer<ProcessingResult?> = {

            assertNotNull(it)
            assert(it is Success)

            val result = it as Success

            with(result.session.handlingStep) {
                assertNotNull(this)
                assertEquals(StateIds.STATE_3.value, action)
                assertEquals(2, repeated)
            }

        }

        TestCase<TickStoryProcessor, ProcessingResult>("process when action is repeated")

            .given("""
    - user intent "intent1" leads to a primary objective "State1"
    - secondary objective has no handler and no trigger
                   """, produceProcessor)

            .and("""
    - graph resolver find a secondary objective "State3"
    - secondary objective has no handler and no trigger
                    """, mockBehaviours)

            .`when`("""
    - processor.process method is called with a user intent "intent1"
                 """, processCall)

            .then("handlingStep must be the same with the repeated property incremented by 1", checkResult)

            .run()
    }

    @Test
    fun `process when executedAction with no trigger and no handler`() {

        val produceProcessor: TSupplier<TickStoryProcessor> = {
            TickStoryProcessor(
                session,
                configuration.copy(
                    stateMachine = configuration.stateMachine.copy(
                        states =  mapOf(
                            StateIds.STATE_1.value to State(StateIds.STATE_1.value),
                            StateIds.STATE_2.value to State(StateIds.STATE_2.value),
                            StateIds.STATE_3.value to State(StateIds.STATE_3.value)
                        ),
                        on = mapOf(
                            IntentNames.INTENT_1.value to "#${StateIds.STATE_1.value}"
                        )
                    ),
                    actions =  setOf(
                        TickAction(
                            StateIds.STATE_1.value,
                            handler = HandlerNames.HANDLER_1.value,
                            inputContextNames = setOf(),
                            outputContextNames = setOf(),
                            final = false
                        ),
                        TickAction(
                            StateIds.STATE_2.value,
                            handler = HandlerNames.HANDLER_2.value,
                            inputContextNames = setOf(),
                            outputContextNames = setOf(),
                            final = false
                        ),
                        TickAction(
                            StateIds.STATE_3.value,
                            inputContextNames = setOf(),
                            outputContextNames = setOf(),
                            final = false
                        )
                    ),
                ),
                TickSenderDefault(),
                false
            )
        }

        val mockBehaviours: TRunnable = {
            every { GraphSolver.solve(any(), any(), any(), any(), any(), any()) } returns listOf(StateIds.STATE_3.value)
            every { ActionHandlersRepository.invoke(any(), any()) } returns mapOf(ContextNames.CONTEXT_1.value to null)
        }

        val processCall: TFunction<TickStoryProcessor?, ProcessingResult> = {
            it!!.process(TickUserAction(IntentNames.INTENT_1.value, emptyMap()))
        }

        val checkResult: TConsumer<ProcessingResult?> = {

            assertNotNull(it)
            assert(it is Success)

            val result = it as Success
            with(result.session) {
                assertEquals(StateIds.STATE_3.value, currentState)
                assertEquals(1, ranHandlers.size)
                assertTrue { contexts.isEmpty() }
                assertEquals(StateIds.STATE_3.value, handlingStep?.action)
                assertEquals(1, handlingStep?.repeated)
            }

            assertFalse { result.isFinal }
        }

        TestCase<TickStoryProcessor, ProcessingResult>("process when executedAction with no trigger and no handler")

            .given("""
    - user intent "intent1" leads to a primary objective "State1"
    - secondary objective has no handler and no trigger
                   """, produceProcessor)

            .and("""
    - graph resolver find a secondary objective "State3"
    - secondary objective has no handler and no trigger
                    """, mockBehaviours)

            .`when`("""
    - processor.process method is called with a user intent "intent1"
                 """, processCall)

            .then("""
    - current state should be "State3"
    - session's ranHandlers should have one item "State3"
    - session's contexts should be empty
                """, checkResult)

            .run()
    }

    @Test
    fun `process when executedAction with handler and no trigger`() {

        val produceProcessor: TSupplier<TickStoryProcessor> = {
            TickStoryProcessor(
                session.copy(
                    objectivesStack = listOf(
                        StateIds.STATE_1.value
                    )
                ),
                configuration.copy(
                    stateMachine = configuration.stateMachine.copy(
                        states =  mapOf(
                            StateIds.STATE_1.value to State(StateIds.STATE_1.value),
                            StateIds.STATE_2.value to State(StateIds.STATE_2.value),
                            StateIds.STATE_3.value to State(StateIds.STATE_3.value)
                        ),
                        on = mapOf(
                            IntentNames.INTENT_1.value to "#${StateIds.STATE_2.value}"
                        )
                    ),
                    actions =  setOf(
                        TickAction(
                            StateIds.STATE_1.value,
                            inputContextNames = setOf(),
                            outputContextNames = setOf(),
                            final = true
                        ),
                        TickAction(
                            StateIds.STATE_2.value,
                            handler = HandlerNames.HANDLER_2.value,
                            inputContextNames = setOf(),
                            outputContextNames = setOf(),
                            final = false
                        ),
                        TickAction(
                            StateIds.STATE_3.value,
                            inputContextNames = setOf(),
                            outputContextNames = setOf(),
                            final = false
                        )
                    ),
                ),
                TickSenderDefault(),
                false
            )
        }

        val mockBehaviours: TRunnable = {
            every { GraphSolver.solve(any(), any(), any(), any(), match { it.name == StateIds.STATE_2.value }, any()) } returns listOf(StateIds.STATE_2.value)
            every { GraphSolver.solve(any(), any(), any(), any(), match { it.name == StateIds.STATE_1.value }, any()) } returns listOf(StateIds.STATE_1.value)
            every { ActionHandlersRepository.invoke(any(), any()) } returns mapOf(ContextNames.CONTEXT_1.value to null)
        }

        val processCall: TFunction<TickStoryProcessor?, ProcessingResult> = {
            it!!.process(TickUserAction(IntentNames.INTENT_1.value, emptyMap()))
        }

        val checkResult: TConsumer<ProcessingResult?> = {

            assertNotNull(it)
            val result = it as Success
            with(result.session) {
                assertEquals(StateIds.STATE_1.value, currentState)
                assertEquals(2, ranHandlers.size)
                assertTrue { ranHandlers.contains(StateIds.STATE_1.value) }
                assertTrue { ranHandlers.contains(StateIds.STATE_2.value) }
                assertTrue { contexts.containsKey(ContextNames.CONTEXT_1.value) }
            }

            assertTrue { result.isFinal }
        }

        TestCase<TickStoryProcessor, ProcessingResult>("process when executedAction with handler and no trigger")

            .given("""
    - user intent "intent1" leads to a primary objective "State2"
    - "State2" objective has a handler and no trigger
    - session objectiveStack has one item "State1"
                   """, produceProcessor)

            .and("""
    - graph resolver find a secondary objective "State2" when primary objective is "State2"
    - graph resolver find a secondary objective "State1 when primary objective is "State1"
    - secondary objective has no handler and no trigger
                    """, mockBehaviours)

            .`when`("""
    - processor.process method is called with a user intent "intent1"
                 """, processCall)

            .then("""
    - current state should be "State1"
    - session's ranHandlers should have two items "State2" and "State1"
    - session's contexts should have one item
                """, checkResult)

            .run()
    }

    @Test
    fun `process when executedAction with trigger and no handler`() {

        val produceProcessor: TSupplier<TickStoryProcessor> = {
            TickStoryProcessor(
                session,
                configuration.copy(
                    stateMachine = configuration.stateMachine.copy(
                        states =  mapOf(
                            StateIds.STATE_1.value to State(StateIds.STATE_1.value),
                            StateIds.STATE_2.value to State(StateIds.STATE_2.value),
                            StateIds.STATE_3.value to State(StateIds.STATE_3.value)
                        ),
                        on = mapOf(
                            IntentNames.INTENT_1.value to "#${StateIds.STATE_1.value}",
                            TriggerNames.TRIGGER_1.value to "#${StateIds.STATE_2.value}"
                        )
                    ),
                    actions =  setOf(
                        TickAction(
                            StateIds.STATE_1.value,
                            handler = HandlerNames.HANDLER_1.value,
                            inputContextNames = setOf(),
                            outputContextNames = setOf(),
                            final = false
                        ),
                        TickAction(
                            StateIds.STATE_2.value,
                            inputContextNames = setOf(),
                            outputContextNames = setOf(),
                            final = false
                        ),
                        TickAction(
                            StateIds.STATE_3.value,
                            trigger = TriggerNames.TRIGGER_1.value,
                            inputContextNames = setOf(),
                            outputContextNames = setOf(),
                            final = false
                        )
                    ),
                ),
                TickSenderDefault(),
                false
            )
        }

        val mockBehaviours: TRunnable = {
            every { GraphSolver.solve(any(), any(), any(), any(), any(), any()) } returns listOf(StateIds.STATE_3.value)
            every { GraphSolver.solve(any(), any(), any(), any(), match { it.name == StateIds.STATE_2.value }, any()) } returns listOf(StateIds.STATE_2.value)
            every { ActionHandlersRepository.invoke(any(), any()) } returns mapOf(ContextNames.CONTEXT_1.value to null)
        }

        val processCall: TFunction<TickStoryProcessor?, ProcessingResult> = {
            it!!.process(TickUserAction(IntentNames.INTENT_1.value, emptyMap()))
        }

        val checkResult: TConsumer<ProcessingResult?> = {

            assertNotNull(it)
            val result = it as Success
            with(result.session) {
                assertEquals(StateIds.STATE_2.value, currentState)
                assertEquals(2, ranHandlers.size)
                assertTrue { ranHandlers.contains(StateIds.STATE_3.value) }
                assertTrue { ranHandlers.contains(StateIds.STATE_2.value) }
                assertTrue { contexts.isEmpty() }
            }

            assertFalse { result.isFinal }
        }

        TestCase<TickStoryProcessor, ProcessingResult>("process when executedAction with trigger and no handler")

            .given("""
    - user intent "intent1" leads to a primary objective "State1"
    - trigger "trigger1" leads to a primary objective "State2"
    - secondary objective "State3" has trigger "trigger1"
                   """, produceProcessor)

            .and("""
    - graph resolver find a secondary objective "State2" when primary objective is "State2"
    - graph resolver find a secondary objective "State3" when primary objective another one
                    """, mockBehaviours)

            .`when`("""
    - processor.process method is called with a user intent "intent1"
                 """, processCall)

            .then("""
    - current state should be "State2"
    - session's ranHandlers should have two items "State3" and "State2"
    - session's contexts should be empty
                """, checkResult)

            .run()
    }

    @Test
    fun `process when unknown intent is detected and unknownAnswerConfig is provided`() {

        val msgCapture = slot<String>()

        val answerConfig1 = UnknownAnswerConfig(
            action = StateIds.STATE_1.value,
            answerId ="unknown 1"
        )
        val answerConfig2 = UnknownAnswerConfig(
            action = StateIds.STATE_2.value,
            answerId ="unknown 2"
        )
        val produceProcessor: TSupplier<TickStoryProcessor> = {
            TickStoryProcessor(
                session.copy(ranHandlers = listOf(
                    StateIds.STATE_1.value,
                    StateIds.STATE_2.value
                ), currentState = StateIds.STATE_2.value),
                configuration.copy(
                    stateMachine = configuration.stateMachine.copy(
                        states =  mapOf(
                            StateIds.STATE_1.value to State(StateIds.STATE_1.value),
                            StateIds.STATE_2.value to State(StateIds.STATE_2.value),
                            StateIds.STATE_3.value to State(StateIds.STATE_3.value)
                        )
                    ),
                    actions =  setOf(
                        TickAction(
                            StateIds.STATE_1.value,
                            handler = HandlerNames.HANDLER_1.value,
                            inputContextNames = setOf(),
                            outputContextNames = setOf(),
                            final = false
                        ),
                        TickAction(
                            StateIds.STATE_2.value,
                            handler = HandlerNames.HANDLER_2.value,
                            inputContextNames = setOf(),
                            outputContextNames = setOf(),
                            final = false
                        ),
                        TickAction(
                            StateIds.STATE_3.value,
                            inputContextNames = setOf(),
                            outputContextNames = setOf(),
                            final = false
                        )
                    ),
                    unknownHandleConfiguration = configuration.unknownHandleConfiguration.copy(
                        unknownAnswerConfigs = setOf(
                            answerConfig1,
                            answerConfig2
                        )
                    )
                ),
                sender,
                false
            )
        }

        val processCall: TFunction<TickStoryProcessor?, ProcessingResult> = {
            it!!.process(TickUserAction(IntentNames.UNKNOWN_INTENT.value, emptyMap()))
        }

        val mockBehaviours: TRunnable = {
            every { sender.endById(capture(msgCapture)) } answers {}
        }

        val checkResult: TConsumer<ProcessingResult?> = {

            assertNotNull(it)
            val result= it as Success
            with(result.session) {
                assertEquals(StateIds.STATE_2.value, currentState)
                assertEquals(2, ranHandlers.size)
                assertTrue { contexts.isEmpty() }
                with(result.session.unknownHandlingStep) uhs@{
                    assertNotNull(this@uhs)
                    assertEquals(1, repeated)
                    assertEquals(answerConfig2, answerConfig)
                }

            }

            assertFalse { result.isFinal }

            assertEquals(answerConfig2.answerId, msgCapture.captured)

            verify(exactly = 1) { sender.endById(answerConfig2.answerId) }
        }

        TestCase<TickStoryProcessor, ProcessingResult>("process when unknown intent is detected and unknownAnswerConfig is provided")

            .given("""
    - current state is "state2"
    - ranHandlers are "intent1" and "intent2"
    - unknown handlers are provided for actions "state1" and "state2"
                   """, produceProcessor)

            .and("""
    - sended message is capture     
            """, mockBehaviours)

            .`when`("""
    - processor.process method is called with a user intent "unknown"
                 """, processCall)

            .then("""
    - current state should be "State2"
    - session's ranHandlers should have two items
    - session's contexts should be empty
    - session should have a not null unknownHandlingStep
    - the session's unknownHandlingStep must have repeated equals 1
    - the session's unknownHandlingStep must be linked to answerConfig2
                """, checkResult)

            .run()
    }

    @Test
    fun `process when unknown intent is detected and unknownAnswerConfig is not provided`() {

        val msgCapture = slot<String>()

        val answerConfig1 = UnknownAnswerConfig(
            action = StateIds.STATE_1.value,
            answerId ="unknown 1"
        )
        val answerConfig2 = UnknownAnswerConfig(
            action = StateIds.STATE_2.value,
            answerId ="unknown 2"
        )
        val produceProcessor: TSupplier<TickStoryProcessor> = {
            TickStoryProcessor(
                session.copy(ranHandlers = listOf(
                    StateIds.STATE_1.value,
                    StateIds.STATE_2.value
                ), currentState = StateIds.STATE_2.value),
                configuration.copy(
                    stateMachine = configuration.stateMachine.copy(
                        states =  mapOf(
                            StateIds.STATE_1.value to State(StateIds.STATE_1.value),
                            StateIds.STATE_2.value to State(StateIds.STATE_2.value),
                            StateIds.STATE_3.value to State(StateIds.STATE_3.value)
                        )
                    ),
                    actions =  setOf(
                        TickAction(
                            StateIds.STATE_1.value,
                            handler = HandlerNames.HANDLER_1.value,
                            inputContextNames = setOf(),
                            outputContextNames = setOf(),
                            final = false
                        ),
                        TickAction(
                            StateIds.STATE_2.value,
                            inputContextNames = setOf(),
                            outputContextNames = setOf(),
                            final = false
                        ),
                        TickAction(
                            StateIds.STATE_3.value,
                            inputContextNames = setOf(),
                            outputContextNames = setOf(),
                            final = false
                        )
                    ),
                    unknownHandleConfiguration = configuration.unknownHandleConfiguration.copy(
                        unknownAnswerConfigs = setOf(
                            answerConfig1
                        )
                    )
                ),
                sender,
                false
            )
        }

        val processCall: TFunction<TickStoryProcessor?, ProcessingResult> = {
            it!!.process(TickUserAction(IntentNames.UNKNOWN_INTENT.value, emptyMap()))
        }

        val mockBehaviours: TRunnable = {
            every { sender.sendById(capture(msgCapture)) } answers {}
            every { sender.sendPlainText(any()) } answers {}
            every { sender.endPlainText(any()) } answers {}
            every { GraphSolver.solve(any(), any(), any(), any(), any(), any()) } returns listOf(StateIds.STATE_3.value)
            every { ActionHandlersRepository.invoke(any(), any()) } returns mapOf(ContextNames.CONTEXT_1.value to null)

        }
        val checkResult: TConsumer<ProcessingResult?> = {

            assertNotNull(it)

            val result =  it as Success
            with(result.session) {
                assertEquals(StateIds.STATE_3.value, currentState)
                assertEquals(3, ranHandlers.size)
                assertTrue { contexts.isEmpty() }
                assertNull(unknownHandlingStep)
            }

            assertFalse { result.isFinal }

            assertFalse(msgCapture.isCaptured)

            verify(exactly = 0) { sender.sendById(answerConfig2.answerId) }
        }

        TestCase<TickStoryProcessor, ProcessingResult>("process when unknown intent is detected and unknownAnswerConfig is not provided")

            .given("""
    - current state is "state2"
    - ranHandlers are "state1" and "state2"
    - unknown handler is provided for action "state1" only
                   """, produceProcessor)

            .and("""
    - sended message is capture  
    - graphsolver always returns state3
            """, mockBehaviours)

            .`when`("""
    - processor.process method is called with a user intent "unknown"
                 """, processCall)

            .then("""
    - current state should be "State2"
    - session's ranHandlers should have two items
    - session's contexts should be empty
    - session should have a not null unknownHandlingStep
    - the session's unknownHandlingStep must have repeated equals 1
    - the session's unknownHandlingStep must be linked to answerConfig2
                """, checkResult)

            .run()
    }

    @Test
    fun `process when unknown intent is detected and unknownHandlingStep already exist`() {

        val msgCapture = slot<String>()

        val answerConfig1 = UnknownAnswerConfig(
            action = StateIds.STATE_1.value,
            answerId ="unknown 1"
        )
        val answerConfig2 = UnknownAnswerConfig(
            action = StateIds.STATE_2.value,
            answerId ="unknown 2"
        )
        val produceProcessor: TSupplier<TickStoryProcessor> = {
            TickStoryProcessor(
                session.copy(ranHandlers = listOf(
                    StateIds.STATE_1.value,
                    StateIds.STATE_2.value
                ),
                    currentState = StateIds.STATE_2.value,
                    unknownHandlingStep = UnknownHandlingStep(1, answerConfig2)
                ),
                configuration.copy(
                    stateMachine = configuration.stateMachine.copy(
                        states =  mapOf(
                            StateIds.STATE_1.value to State(StateIds.STATE_1.value),
                            StateIds.STATE_2.value to State(StateIds.STATE_2.value),
                            StateIds.STATE_3.value to State(StateIds.STATE_3.value)
                        )
                    ),
                    actions =  setOf(
                        TickAction(
                            StateIds.STATE_1.value,
                            handler = HandlerNames.HANDLER_1.value,
                            inputContextNames = setOf(),
                            outputContextNames = setOf(),
                            final = false
                        ),
                        TickAction(
                            StateIds.STATE_2.value,
                            handler = HandlerNames.HANDLER_2.value,
                            inputContextNames = setOf(),
                            outputContextNames = setOf(),
                            final = false
                        ),
                        TickAction(
                            StateIds.STATE_3.value,
                            inputContextNames = setOf(),
                            outputContextNames = setOf(),
                            final = false
                        )
                    ),
                    unknownHandleConfiguration = configuration.unknownHandleConfiguration.copy(
                        unknownAnswerConfigs = setOf(
                            answerConfig1,
                            answerConfig2
                        )
                    )
                ),
                sender,
                false
            )
        }

        val processCall: TFunction<TickStoryProcessor?, ProcessingResult> = {
            it!!.process(TickUserAction(IntentNames.UNKNOWN_INTENT.value, emptyMap()))
        }

        val mockBehaviours: TRunnable = {
            every { sender.endById(capture(msgCapture)) } answers {}

        }
        val checkResult: TConsumer<ProcessingResult?> = {

            assertNotNull(it)
            val result = it as Success
            with(result.session) {
                assertEquals(StateIds.STATE_2.value, currentState)
                assertEquals(2, ranHandlers.size)
                assertTrue { contexts.isEmpty() }
                with(result.session.unknownHandlingStep) uhs@{
                    assertNotNull(this@uhs)
                    assertEquals(2, repeated)
                    assertEquals(answerConfig2, answerConfig)
                }

            }

            assertFalse { result.isFinal }

            assertEquals(answerConfig2.answerId, msgCapture.captured)

            verify(exactly = 1) { sender.endById(answerConfig2.answerId) }
        }

        TestCase<TickStoryProcessor, ProcessingResult>("process when unknown intent is detected and unknownHandlingStep already exist")

            .given("""
    - current state is "state2"
    - ranHandlers are "intent1" and "intent2"
    - unknown handlers are provided for actions "state1" and "state2"
                   """, produceProcessor)

            .and("""
    - sended message is capture     
            """.trimIndent(), mockBehaviours)

            .`when`("""
    - processor.process method is called with a user intent "unknown"
                 """, processCall)

            .then("""
    - current state should be "State2"
    - session's ranHandlers should have two items
    - session's contexts should be empty
    - session should have a not null unknownHandlingStep
    - the session's unknownHandlingStep must have repeated equals 2
    - the session's unknownHandlingStep must be linked to answerConfig2
                """, checkResult)

            .run()
    }

    @Test
    fun `process when unknown intent is detected and unknownAnswerConfig is provided and repetitionNb is exceeded`() {

        val answerConfig1 = UnknownAnswerConfig(
            action = StateIds.STATE_1.value,
            answerId ="unknown 1"
        )
        val answerConfig2 = UnknownAnswerConfig(
            action = StateIds.STATE_2.value,
            answerId ="unknown 2"
        )
        val produceProcessor: TSupplier<TickStoryProcessor> = {
            TickStoryProcessor(
                session.copy(ranHandlers = listOf(
                    StateIds.STATE_1.value,
                    StateIds.STATE_2.value
                ),
                    currentState = StateIds.STATE_2.value,
                    unknownHandlingStep = UnknownHandlingStep(2, answerConfig2)),
                configuration.copy(
                    stateMachine = configuration.stateMachine.copy(
                        states =  mapOf(
                            StateIds.STATE_1.value to State(StateIds.STATE_1.value),
                            StateIds.STATE_2.value to State(StateIds.STATE_2.value),
                            StateIds.STATE_3.value to State(StateIds.STATE_3.value)
                        )
                    ),
                    actions =  setOf(
                        TickAction(
                            StateIds.STATE_1.value,
                            handler = HandlerNames.HANDLER_1.value,
                            inputContextNames = setOf(),
                            outputContextNames = setOf(),
                            final = false
                        ),
                        TickAction(
                            StateIds.STATE_2.value,
                            handler = HandlerNames.HANDLER_2.value,
                            inputContextNames = setOf(),
                            outputContextNames = setOf(),
                            final = false
                        ),
                        TickAction(
                            StateIds.STATE_3.value,
                            inputContextNames = setOf(),
                            outputContextNames = setOf(),
                            final = false
                        )
                    ),
                    unknownHandleConfiguration = configuration.unknownHandleConfiguration.copy(
                        unknownAnswerConfigs = setOf(
                            answerConfig1,
                            answerConfig2
                        )
                    )
                ),
                sender,
                false
            )
        }

        val processCall: TFunction<TickStoryProcessor?, RetryExceededError> = {
            assertThrows {
                it!!.process(TickUserAction(IntentNames.UNKNOWN_INTENT.value, emptyMap()))
            }
        }

        val checkResult: TConsumer<RetryExceededError?> = {
            assertNotNull(it)
        }

        TestCase<TickStoryProcessor, RetryExceededError>("process when executedAction with no trigger and no handler")

            .given("""
    - current state is "state2"
    - ranHandlers are "intent1" and "intent2"
    - unknown handlers are provided for actions "state1" and "state2"
                   """, produceProcessor)

            .`when`("""
    - processor.process method is called with a user intent "unknown"
                 """, processCall)

            .then("""
    - A RetryExceededError is returned
                """, checkResult)

            .run()
    }

    @Test
    fun `process when unknown intent is detected and unknownAnswerConfig is provided, repetitionNb is exceeded and redirectStoryId is provided`() {

        val answerConfig1 = UnknownAnswerConfig(
            action = StateIds.STATE_1.value,
            answerId ="unknown 1"
        )
        val answerConfig2 = UnknownAnswerConfig(
            action = StateIds.STATE_2.value,
            answerId ="unknown 2"
        )
        val produceProcessor: TSupplier<TickStoryProcessor> = {
            TickStoryProcessor(
                session.copy(ranHandlers = listOf(
                    StateIds.STATE_1.value,
                    StateIds.STATE_2.value
                ),
                    currentState = StateIds.STATE_2.value,
                    unknownHandlingStep = UnknownHandlingStep(2, answerConfig2)),
                configuration.copy(
                    stateMachine = configuration.stateMachine.copy(
                        states =  mapOf(
                            StateIds.STATE_1.value to State(StateIds.STATE_1.value),
                            StateIds.STATE_2.value to State(StateIds.STATE_2.value),
                            StateIds.STATE_3.value to State(StateIds.STATE_3.value)
                        )
                    ),
                    actions =  setOf(
                        TickAction(
                            StateIds.STATE_1.value,
                            handler = HandlerNames.HANDLER_1.value,
                            inputContextNames = setOf(),
                            outputContextNames = setOf(),
                            final = false
                        ),
                        TickAction(
                            StateIds.STATE_2.value,
                            handler = HandlerNames.HANDLER_2.value,
                            inputContextNames = setOf(),
                            outputContextNames = setOf(),
                            final = false
                        ),
                        TickAction(
                            StateIds.STATE_3.value,
                            inputContextNames = setOf(),
                            outputContextNames = setOf(),
                            final = true
                        )
                    ),
                    storySettings = TickStorySettings(
                        2,
                        "storyId"
                    ),
                    unknownHandleConfiguration = configuration.unknownHandleConfiguration.copy(
                        unknownAnswerConfigs = setOf(
                            answerConfig1,
                            answerConfig2
                        )
                    )
                ),
                sender,
                false
            )
        }

        val mockBehaviours: TRunnable = {
            every { GraphSolver.solve(any(), any(), any(), any(), any(), any()) } returns listOf(StateIds.STATE_3.value)
        }
        val processCall: TFunction<TickStoryProcessor?, ProcessingResult> = {
            it!!.process(TickUserAction(IntentNames.UNKNOWN_INTENT.value, emptyMap()))
        }

        val checkResult: TConsumer<ProcessingResult?> = {
            assertNotNull(it)
            assert(it is Redirect)
        }

        TestCase<TickStoryProcessor,ProcessingResult>("process when unknown intent is detected and unknownAnswerConfig is provided, repetitionNb is exceeded and redirectStoryId is provided")

            .given("""
    - current state is "state2"
    - ranHandlers are "intent1" and "intent2"
    - unknown handlers are provided for actions "state1" and "state2"
                   """, produceProcessor)
            .and("", mockBehaviours)
            .`when`("""
    - processor.process method is called with a user intent "unknown"
                 """, processCall)

            .then("""
    - A RetryExceededError is returned
                """, checkResult)

            .run()
    }

}
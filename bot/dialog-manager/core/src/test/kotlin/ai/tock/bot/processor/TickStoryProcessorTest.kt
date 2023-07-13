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
            storySettings = TickStorySettings.default
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
                        actionName = StateIds.STATE_3.value,
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
                        ),
                        initial = StateIds.STATE_1.value
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
    fun `process when action executed has a target story`() {

        val produceProcessor: TSupplier<TickStoryProcessor> = {
            TickStoryProcessor(
                session = session.copy(
                    handlingStep = TickActionHandlingStep(
                        actionName = StateIds.STATE_3.value,
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
                        ),
                        initial = StateIds.STATE_1.value
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
                            final = false,
                            targetStory = "TARGET_STORY"
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
            ((it as Redirect).storyId == "TARGET_STORY").let { assertTrue { it } }
        }

        TestCase<TickStoryProcessor, ProcessingResult>("process when action executed has a target story")

            .given("""
    - user intent "intent1" leads to a primary objective "State1"
    - secondary objective is an action with a target story
                   """, produceProcessor)

            .and("""
    - graph resolver find a secondary objective "State3"
    - Action "State3" has a target story
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
                        actionName = StateIds.STATE_3.value,
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
                        ),
                        initial = StateIds.STATE_1.value
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
                assertEquals(StateIds.STATE_3.value, actionName)
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
                        ),
                        initial = StateIds.STATE_1.value
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
                assertEquals(StateIds.STATE_3.value, handlingStep?.actionName)
                assertEquals(1, handlingStep?.repeated)
                assertFalse { finished }
            }
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


}
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
import ai.tock.bot.bean.unknown.TickUnknownConfiguration
import ai.tock.bot.handler.ActionHandlersRepository
import ai.tock.bot.sender.TickSender
import ai.tock.bot.statemachine.State
import ai.tock.shared.exception.scenario.group.ScenarioGroupDuplicatedException
import io.mockk.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TickStoryProcessorExecuteActionTest {
    init {
        System.setProperty("tock_bot_dialog_manager_debug_enabled", "false")
    }

    private val intentOui = "oui"
    private val intentNon = "non"
    private val intentBonjourRobot = "bonjourRobot"

    private val contextJeVeuxJouer = TickContext("JE_VEUX_JOUER")
    private val contextJeNeVeuxPasJouer = TickContext("JE_NE_VEUX_PAS_JOUER")
    private val contextDev1 = TickContext("DEV_CONTEXT_1")
    private val contextDev2 = TickContext("DEV_CONTEXT_2")
    private val contextDev3 = TickContext("DEV_CONTEXT_3")

    private val stateBonjour = State(id = "BONJOUR_HUMAIN")
    private val stateVeuxTuJouer = State(id = "VEUX_TU_JOUER")
    private val stateTicTacToe = State(id = "TIC_TAC_TOE")
    private val stateTantPis = State(id = "TANT_PIS")
    private val stateTantMieux = State(id = "TANT_MIEUX")
    private val stateAurevoir = State(id = "AU_REVOIR_HUMAIN")

    private val stateGroup = State(id = "GROUP", initial = stateAurevoir.id,
        states = setOf(stateBonjour, stateVeuxTuJouer, stateTicTacToe, stateTantPis, stateTantMieux, stateAurevoir).associateBy { it.id },
        on = mapOf(
                intentOui to "#${stateAurevoir.id}",
                intentNon to "#${stateAurevoir.id}",
            )
    )

    private val stateGlobal = State(id = "Global", initial = stateGroup.id,
        states = setOf(stateGroup).associateBy { it.id },
        on = mapOf(intentBonjourRobot to "#${stateGroup.id}")
    )

    private val stateMachine = State(
        id = "root", initial = "Global",
        states = mapOf("Global" to stateGlobal),
    )

    private val actionBonjour = TickAction(
        name =  stateBonjour.id,
        answerId = "app_scenario_Bonjour Humain",
        handler = "dev-tools:set_context_1",
        outputContextNames = setOf(contextDev1).map { it.name }.toSet(),
    )

    private val actionVeuxTuJouer = TickAction(
        name =  stateVeuxTuJouer.id,
        answerId = "app_scenario_Veux-tu jouer ?",
        handler = "dev-tools:set_context_2",
        inputContextNames = setOf(contextDev1).map { it.name }.toSet(),
        outputContextNames = setOf(contextDev2).map { it.name }.toSet(),
    )

    private val actionTicTacToe = TickAction(
        name =  stateTicTacToe.id,
        answerId = "app_scenario_Tic tac toe ?",
        inputContextNames = setOf(contextDev2).map { it.name }.toSet(),
        outputContextNames = setOf(contextJeVeuxJouer, contextJeNeVeuxPasJouer).map { it.name }.toSet(),
    )

    private val actionTantPis = TickAction(
        name =  stateTantPis.id,
        answerId = "app_scenario_Tant pis",
        handler = "dev-tools:set_context_3",
        inputContextNames = setOf(contextJeNeVeuxPasJouer).map { it.name }.toSet(),
        outputContextNames = setOf(contextDev3).map { it.name }.toSet(),
    )

    private val actionTantMieux = TickAction(
        name =  stateTantMieux.id,
        answerId = "app_scenario_Tant mieux",
        handler = "dev-tools:set_context_3",
        inputContextNames = setOf(contextJeVeuxJouer).map { it.name }.toSet(),
        outputContextNames = setOf(contextDev3).map { it.name }.toSet(),
    )

    private val actionAurevoir = TickAction(
        name =  stateAurevoir.id,
        answerId = "app_scenario_Au revoir Humain !",
        inputContextNames = setOf(contextDev3).map { it.name }.toSet(),
        final = true
    )

    private val actionAurevoirSansAnswer = TickAction(
        name =  stateAurevoir.id,
        inputContextNames = setOf(contextDev3).map { it.name }.toSet(),
        final = true
    )

    private val intentsContexts = setOf(
        TickIntent(
            intentName = intentOui,
            associations = setOf(
                TickIntentAssociation(
                    actionName = actionTicTacToe.name,
                    contextNames = setOf(contextJeVeuxJouer.name)
                )
            )
        ),
        TickIntent(
            intentName = intentNon,
            associations = setOf(
                TickIntentAssociation(
                    actionName = actionTicTacToe.name,
                    contextNames = setOf(contextJeNeVeuxPasJouer.name)
                )
            )
        ),
    )

    private val tickConfigBonjourRobot = TickConfiguration(
        stateMachine = stateMachine,
        contexts = setOf(contextJeVeuxJouer, contextJeNeVeuxPasJouer, contextDev1),
        actions = setOf(actionBonjour, actionVeuxTuJouer, actionTicTacToe, actionTantPis, actionTantMieux, actionAurevoir),
        intentsContexts = intentsContexts,
        unknownHandleConfiguration = TickUnknownConfiguration(),
        storySettings = TickStorySettings.default
    )

    private val tickSender = mockk<TickSender>()


    @Test
    fun `execute action when action is not final and has answerId and story has no ending rule then the sender send action answer id`() {
        val sendAnswersCaptured = mutableListOf<String>()

        val tickStoryProcessor = TickStoryProcessor(
            session = TickSession(),
            configuration = tickConfigBonjourRobot,
            sender = tickSender
        )

        val produceProcessor: TSupplier<TickStoryProcessor> = { tickStoryProcessor }

        val mockBehaviours: TRunnable = {
            every { tickSender.sendById(capture(sendAnswersCaptured)) } answers {}
            mockkObject(ActionHandlersRepository)
            every { ActionHandlersRepository.invoke(any(), any()) } returns mapOf()
        }

        val executeCall: TFunction<TickStoryProcessor?, Unit> = {
            it!!.execute(actionVeuxTuJouer)
        }

        val checkResult: TConsumer<Unit?> = {
            assertEquals(1, sendAnswersCaptured.size)
            verify(exactly = 1) { tickSender.sendById(actionVeuxTuJouer.answerId!!) }
        }

        TestCase<TickStoryProcessor, Unit>("execute tick action - cas 1")
            .given(
                setOf(
                    "action is not final",
                    "answerId is not NullOrBlank",
                    "story has no ending rule",
                ), produceProcessor)
            .and(
                setOf(
                    "sender is mocked and messages are captured",
                    "handler invocation is mocked"
                ), mockBehaviours)
            .`when`(
                setOf(
                    "execute method is called"
                ), executeCall)
            .then(
                setOf(
                    "the sender send the answer by id"
                ), checkResult)
            .run()
    }

    @Test
    fun `execute action when action is final and has no answerId and story has no ending rule then the sender end discussion with empty message`() {
        val tickStoryProcessor = TickStoryProcessor(
            session = TickSession(),
            configuration = tickConfigBonjourRobot,
            sender = tickSender
        )

        val produceProcessor: TSupplier<TickStoryProcessor> = { tickStoryProcessor }

        val mockBehaviours: TRunnable = {
            every { tickSender.end() } answers {}
            mockkObject(ActionHandlersRepository)
            every { ActionHandlersRepository.invoke(any(), any()) } returns mapOf()
        }

        val executeCall: TFunction<TickStoryProcessor?, Unit> = {
            it!!.execute(actionAurevoirSansAnswer)
        }

        val checkResult: TConsumer<Unit?> = {
            verify(exactly = 1) { tickSender.end() }
            verify(exactly = 0) { tickSender.endById(any()) }
            verify(exactly = 0) { tickSender.sendById(any()) }
        }

        TestCase<TickStoryProcessor, Unit>("execute tick action - cas 2")
            .given(
                setOf(
                    "action is final",
                    "has no answer",
                    "story has no ending rule",
                ), produceProcessor)
            .and(
                setOf(
                    "sender is mocked and messages are captured",
                    "handler invocation is mocked"
                ), mockBehaviours)
            .`when`(
                setOf(
                    "execute method is called",
                ), executeCall)
            .then(
                setOf(
                    "the sender end discussion with empty message",
                ), checkResult)
            .run()
    }

    @Test
    fun `execute action when action is final and has answerId and story has no ending rule then the sender end response with the answer id`() {
        val endAnswersCaptured = mutableListOf<String>()

        val tickStoryProcessor = TickStoryProcessor(
            session = TickSession(),
            configuration = tickConfigBonjourRobot,
            sender = tickSender
        )

        val produceProcessor: TSupplier<TickStoryProcessor> = { tickStoryProcessor }

        val mockBehaviours: TRunnable = {
            every { tickSender.endById(capture(endAnswersCaptured)) } answers {}
            mockkObject(ActionHandlersRepository)
            every { ActionHandlersRepository.invoke(any(), any()) } returns mapOf()
        }

        val executeCall: TFunction<TickStoryProcessor?, Unit> = {
            it!!.execute(actionAurevoir)
        }

        val checkResult: TConsumer<Unit?> = {
            assertEquals(1, endAnswersCaptured.size)
            verify(exactly = 1) { tickSender.endById(actionAurevoir.answerId!!) }
        }

        TestCase<TickStoryProcessor, Unit>("execute tick action - cas 3")
            .given(
                setOf(
                    "action is final",
                    "answerId is not NullOrBlank",
                    "story has no ending rule"
                ), produceProcessor)
            .and(
                setOf(
                    "sender is mocked and messages are captured",
                    "handler invocation is mocked"
                ), mockBehaviours)
            .`when`(
                setOf(
                    "execute method is called"
                ), executeCall)
            .then(
                setOf(
                    "the sender end response with the answer id"
                ), checkResult)
            .run()
    }

    @Test
    fun `execute action when action is final and has answerId and story has ending rule then sender send action answer id and doesn't end discussion`() {
        val sendAnswersCaptured = mutableListOf<String>()

        val tickStoryProcessor = TickStoryProcessor(
            session = TickSession(),
            configuration = tickConfigBonjourRobot,
            sender = tickSender,
            endingStoryRuleExists = true
        )

        val produceProcessor: TSupplier<TickStoryProcessor> = { tickStoryProcessor }

        val mockBehaviours: TRunnable = {
            every { tickSender.sendById(capture(sendAnswersCaptured)) } answers {}
            mockkObject(ActionHandlersRepository)
            every { ActionHandlersRepository.invoke(any(), any()) } returns mapOf()
        }

        val executeCall: TFunction<TickStoryProcessor?, Unit> = {
            it!!.execute(actionAurevoir)
        }

        val checkResult: TConsumer<Unit?> = {
            assertEquals(1, sendAnswersCaptured.size)
            verify(exactly = 1) { tickSender.sendById(actionAurevoir.answerId!!) }
            verify(exactly = 0) { tickSender.endById(any()) }
            verify(exactly = 0) { tickSender.end() }
        }

        TestCase<TickStoryProcessor, Unit>("execute tick action - cas 4")
            .given(
                setOf(
                    "action is final",
                    "answerId is not NullOrBlank",
                    "story has an ending rule"
                ), produceProcessor)
            .and(
                setOf(
                    "sender is mocked and messages are captured",
                    "handler invocation is mocked"
                ), mockBehaviours)
            .`when`(
                setOf(
                    "execute method is called"
                ), executeCall)
            .then(
                setOf(
                    "the sender send action answer id and doesn't end discussion",
                ), checkResult)
            .run()
    }

    @Test
    fun `execute action when action is not final, has a handler, has an answerId and story has no ending rule then sender send action answer id`() {
        val sendAnswersCaptured = mutableListOf<String>()
        val handlersCaptured = mutableListOf<String>()

        val tickStoryProcessor = TickStoryProcessor(
            session = TickSession(),
            configuration = tickConfigBonjourRobot,
            sender = tickSender
        )

        val produceProcessor: TSupplier<TickStoryProcessor> = { tickStoryProcessor }

        val mockBehaviours: TRunnable = {
            every { tickSender.sendById(capture(sendAnswersCaptured)) } answers {}
            mockkObject(ActionHandlersRepository)
            every { ActionHandlersRepository.invoke(capture(handlersCaptured), any()) } returns mapOf()
        }

        val executeCall: TFunction<TickStoryProcessor?, Unit> = {
            it!!.execute(actionTantPis)
        }

        val checkResult: TConsumer<Unit?> = {
            assertEquals(1, sendAnswersCaptured.size)
            verify(exactly = 1) { tickSender.sendById(actionTantPis.answerId!!) }
            verify(exactly = 0) { tickSender.endById(any()) }
            verify(exactly = 0) { tickSender.end() }

            assertEquals(1, handlersCaptured.size)
            verify(exactly = 1) { ActionHandlersRepository.invoke(actionTantPis.handler!!, any()) }
        }

        TestCase<TickStoryProcessor, Unit>("execute tick action - cas 4")
            .given(
                setOf(
                    "action is not final",
                    "answerId is not NullOrBlank",
                    "has a handler",
                    "story has an not ending rule"
                ), produceProcessor)
            .and(
                setOf(
                    "sender is mocked and messages are captured",
                    "handler invocation is mocked and handler name is captured"
                ), mockBehaviours)
            .`when`(
                setOf(
                    "execute method is called"
                ), executeCall)
            .then(
                setOf(
                    "then sender send action answer id",
                ), checkResult)
            .run()
    }


}
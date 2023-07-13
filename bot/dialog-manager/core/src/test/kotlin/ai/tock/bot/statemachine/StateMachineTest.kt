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

package ai.tock.bot.statemachine

import ai.tock.bot.DialogManagerTest
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StateMachineTest : DialogManagerTest() {

    @Test
    fun `given state machine when has duplicate states then throw IllegalArgumentException`() {
        val root = getStateMachineFromFile("xstate-invalid-duplicate-states")
        assertThrows<IllegalArgumentException> { StateMachine(root) }
    }

    @Test
    fun `given state machine when has self-loop states then throw IllegalArgumentException`() {
        val root = getStateMachineFromFile("xstate-invalid-self-loop")
        assertThrows<IllegalArgumentException> { StateMachine(root) }
    }

    /**
     * Given a state machine
     * When has no loop
     * Then check existing and non-existing state
     */
    @Test
    fun getState() {
        val root = getStateMachineFromFile("xstate-valid")
        val stateMachine = StateMachine(root)

        val global = root.states?.get("Global")!!
        val introduction = global.states?.get("INTRODUCTION")!!
        val sBookMeeting = global.states?.get("S_BOOK_MEETING")!!
        val sAskChannel = sBookMeeting.states?.get("S_ASK_CHANNEL")!!
        val sShowProcedureBookMeeting = sBookMeeting.states?.get("S_SHOW_PROCEDURE_BOOK_MEETING")!!

        // Non-existing State
        assertNull(stateMachine.getState("TOTO"))

        // Existing State
        assertEquals(root, stateMachine.getState("root"))
        assertEquals(global, stateMachine.getState("Global"))
        assertEquals(introduction, stateMachine.getState("INTRODUCTION"))
        assertEquals(sBookMeeting, stateMachine.getState("S_BOOK_MEETING"))
        assertEquals(sAskChannel, stateMachine.getState("S_ASK_CHANNEL"))
        assertEquals(sShowProcedureBookMeeting, stateMachine.getState("S_SHOW_PROCEDURE_BOOK_MEETING"))
    }

    /**
     * Given a state machine
     * When has no loop
     * Then get a state parent if exist
     */
    @Test
    fun getParent() {
        val root = getStateMachineFromFile("xstate-valid")
        val stateMachine = StateMachine(root)

        val global = root.states?.get("Global")!!
        val sBookMeeting = global.states?.get("S_BOOK_MEETING")!!

        // Non-existing State
        assertNull(stateMachine.getParent("root"))
        assertNull(stateMachine.getParent("TOTO"))

        // Existing State
        assertEquals(root, stateMachine.getParent("Global"))
        assertEquals(global, stateMachine.getParent("INTRODUCTION"))
        assertEquals(global, stateMachine.getParent("S_BOOK_MEETING"))
        assertEquals(sBookMeeting, stateMachine.getParent("S_ASK_CHANNEL"))
        assertEquals(sBookMeeting, stateMachine.getParent("S_SHOW_PROCEDURE_BOOK_MEETING"))
    }

    /**
     * Given a state machine
     * When has no loop
     * Then get the initial state if exist
     */
    @Test
    fun getInitial() {
        val root = getStateMachineFromFile("xstate-valid")
        val stateMachine = StateMachine(root)

        val global = root.states?.get("Global")!!
        val introduction = global.states?.get("INTRODUCTION")!!
        val sBookMeeting = global.states?.get("S_BOOK_MEETING")!!
        val sAskChannel = sBookMeeting.states?.get("S_ASK_CHANNEL")!!
        val sShowProcedureBookMeeting = sBookMeeting.states?.get("S_SHOW_PROCEDURE_BOOK_MEETING")!!

        // Non-existing State
        assertNull(stateMachine.getInitial("TOTO"))

        // Existing State
        assertEquals(introduction, stateMachine.getInitial("root"))
        assertEquals(introduction, stateMachine.getInitial("Global"))
        assertEquals(introduction, stateMachine.getInitial("INTRODUCTION"))
        assertEquals(sShowProcedureBookMeeting, stateMachine.getInitial("S_BOOK_MEETING"))
        assertEquals(sAskChannel, stateMachine.getInitial("S_ASK_CHANNEL"))
        assertEquals(sShowProcedureBookMeeting, stateMachine.getInitial("S_SHOW_PROCEDURE_BOOK_MEETING"))
    }

    /**
     * Given a state machine
     * When has no loop
     * Then use a transition and get the next existing and non-existing state
     */
    @Test
    fun getNext() {
        val root = getStateMachineFromFile("xstate-valid")
        val stateMachine = StateMachine(root)

        val global = root.states?.get("Global")!!
        val sBookMeeting = global.states?.get("S_BOOK_MEETING")!!
        val sShowProcedureBookMeeting4 = global.states?.get("S_SHOW_PROCEDURE_BOOK_MEETING_4")!!
        val sAskChannel = sBookMeeting.states?.get("S_ASK_CHANNEL")!!
        val sShowProcedureBookMeeting = sBookMeeting.states?.get("S_SHOW_PROCEDURE_BOOK_MEETING")!!

        // Non-existing State
        assertNull(stateMachine.getNext("root", "ANY"))
        assertNull(stateMachine.getNext("TOTO", "ANY"))
        assertNull(stateMachine.getNext("Global", "i_book_physical_or_tel"))
        assertNull(stateMachine.getNext("S_BOOK_MEETING", "i_book_physical_or_tel_3"))

        // Existing State
        assertEquals(sShowProcedureBookMeeting, stateMachine.getNext("Global", "i_ask_book_visio"))
        assertEquals(sShowProcedureBookMeeting, stateMachine.getNext("S_BOOK_MEETING", "i_book_physical_or_tel"))
        assertEquals(sAskChannel, stateMachine.getNext("S_BOOK_MEETING", "i_book_physical_or_tel_2"))
        assertEquals(sShowProcedureBookMeeting4, stateMachine.getNext("S_BOOK_MEETING", "i_book_physical_or_tel_4"))
    }

    /**
     * Given a state machine
     * When has no loop
     * Then get all states that are not a group
     */
    @Test
    fun getAllStatesNotGroup() {
        val root = getStateMachineFromFile("xstate-valid")
        val stateMachine = StateMachine(root)

        val allStatesNotGroup = stateMachine.getAllStatesNotGroup()

        val expectedStatesNotGroup = listOf(
            "INTRODUCTION",
            "S_ASK_CHANNEL",
            "S_SHOW_PROCEDURE_BOOK_MEETING",
            "S_SHOW_PROCEDURE_BOOK_MEETING_2",
            "S_ASK_CHANNEL_2",
            "S_SHOW_PROCEDURE_BOOK_MEETING_4"
        )

        assertEquals(expectedStatesNotGroup.size, allStatesNotGroup.size)
        assertTrue(expectedStatesNotGroup.containsAll(allStatesNotGroup))
    }

    /**
     * Given a state machine
     * When has no loop
     * Then get all transitions
     */
    @Test
    fun getAllTransitions() {
        val root = getStateMachineFromFile("xstate-valid")
        val stateMachine = StateMachine(root)

        val allTransitions = stateMachine.getAllTransitions()

        val expectedTransitions = listOf(
            "i_ask_book_visio",
            "i_book_physical_or_tel",
            "i_book_physical_or_tel_2",
            "i_book_physical_or_tel_3",
            "i_book_physical_or_tel_4"
        )

        assertEquals(expectedTransitions.size, allTransitions.size)
        assertTrue(expectedTransitions.containsAll(allTransitions))
    }

    /**
     * Given a state machine
     * When has no loop
     * Then check presence of transition
     */
    @Test
    fun containsTransition() {
        val root = getStateMachineFromFile("xstate-valid")
        val stateMachine = StateMachine(root)

        // Non-existing Transition
        assertFalse(stateMachine.containsTransition("TOTO"))

        // Existing Transition
        assertTrue(stateMachine.containsTransition("i_ask_book_visio"))
        assertTrue(stateMachine.containsTransition("i_book_physical_or_tel"))
        assertTrue(stateMachine.containsTransition("i_book_physical_or_tel_2"))
        assertTrue(stateMachine.containsTransition("i_book_physical_or_tel_3"))
    }

}
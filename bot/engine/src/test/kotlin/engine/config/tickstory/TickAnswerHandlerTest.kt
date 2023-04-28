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

package ai.tock.bot.engine.config.tickstory

import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.TickState
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.*

class TickAnswerHandlerTest {

    private val dialog = Dialog(setOf())
    private val storyId = "storyId"
    private val conversationData: Map<String, String> = mapOf("DATA_1" to "VALUE_1")
    private val tickState = TickState(
        currentState = "currentState",
        contexts = mapOf("CONTEXT_2" to null),
        ranHandlers = listOf("HANDLER_1"),
        objectivesStack = listOf("OBJECTIVE_1"),
        initDate = Instant.MAX,
        finished = true
    )

    @BeforeEach
    fun beforeEach() {
        mockkStatic(Instant::class)
        every { Instant.now() } returns Instant.MIN
    }

    @AfterEach
    fun afterEach() {
        clearAllMocks()
    }

    @Test
    fun `when tickState does not exists then session is initialed only with dialog lastDateUpdate and conversationData`(){
        val tickSession = TickAnswerHandler.initTickSession(dialog, storyId, conversationData)

        assertNotNull(tickSession)
        assertNull(tickSession.currentState)
        assertEquals(conversationData, tickSession.contexts)
        assertEquals(emptyList(), tickSession.ranHandlers)
        assertEquals(emptyList(), tickSession.objectivesStack)
        assertEquals(dialog.lastDateUpdate, tickSession.initDate)
        assertFalse(tickSession.finished)
    }

    @Test
    fun `when tickState exists but finished then session is initialed only with dialog lastDateUpdate and conversationData`(){
        val tickSession = TickAnswerHandler.initTickSession(
            dialog = dialog.copy(tickStates = mutableMapOf(storyId to tickState)),
            storyId = storyId,
            conversationData = conversationData
        )

        assertNotNull(tickSession)
        assertNull(tickSession.currentState)
        assertEquals(conversationData, tickSession.contexts)
        assertEquals(emptyList(), tickSession.ranHandlers)
        assertEquals(emptyList(), tickSession.objectivesStack)
        assertEquals(dialog.lastDateUpdate, tickSession.initDate)
        assertFalse(tickSession.finished)
    }

    @Test
    fun `when tickState exists and not finished then session is initialed with a tickState and conversationData`(){
        val tickSession = TickAnswerHandler.initTickSession(
            dialog = dialog.copy(tickStates = mutableMapOf(storyId to tickState.copy(finished = false))),
            storyId = storyId,
            conversationData = conversationData
        )

        assertNotNull(tickSession)
        assertEquals(tickState.currentState, tickSession.currentState)
        assertEquals(conversationData.plus(tickState.contexts), tickSession.contexts)
        assertEquals(tickState.ranHandlers, tickSession.ranHandlers)
        assertEquals(tickState.objectivesStack, tickSession.objectivesStack)
        assertEquals(tickState.initDate, tickSession.initDate)
        assertFalse(tickSession.finished)
    }
}
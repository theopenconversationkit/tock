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

package ai.tock.bot.handler

import ai.tock.bot.HandlerNamespace
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.text.MessageFormat
import kotlin.test.Test
import kotlin.test.assertEquals


class ActionHandlerTest {

    private val inputTestContexts = setOf("CONTEXT_1", "CONTEXT_2")
    private val outputTestContexts = setOf("CONTEXT_3", "CONTEXT_4", "CONTEXT_5")

    private val actionHandler = ActionHandler(
        id = "id",
        namespace = HandlerNamespace.UNKNOWN,
        description = "description",
        inputContexts = inputTestContexts,
        outputContexts = outputTestContexts,
        handler = { outputTestContexts.associateWith { null } }
    )

    @Test
    fun `when the provided and computed contexts are exactly those expected then no exception is expected`(){
        val providedInputContexts = inputTestContexts.associateWith { null }

        assertDoesNotThrow {
            actionHandler.invokeHandler(providedInputContexts)
        }
    }

    @Test
    fun `when the provided contexts contain an undeclared input context then no exception is expected`(){
        val providedInputContexts = inputTestContexts.plus("CONTEXT_X").associateWith { null }

        assertDoesNotThrow {
            actionHandler.invokeHandler(providedInputContexts)
        }
    }

    @Test
    fun `when the provided contexts does not contains all declared input contexts then exception is expected`(){
        val inputContextsExcluded = setOf("CONTEXT_2")
        val providedInputContexts = inputTestContexts.minus(inputContextsExcluded).associateWith { null }

        val exception = assertThrows<IllegalArgumentException> {
            actionHandler.invokeHandler(providedInputContexts)
        }

        assertEquals(
            expected = MessageFormat.format(ERR_INPUT_CONTEXT_NOT_PROVIDED, actionHandler.name, inputContextsExcluded),
            actual = exception.message
        )
    }

    @Test
    fun `when the computed contexts contains undeclared output contexts then exception is expected`(){
        val providedInputContexts = inputTestContexts.associateWith { null }
        val unexpectedOutputContexts = setOf("CONTEXT_X")

        val exception = assertThrows<IllegalArgumentException> {
            actionHandler
                .copy(handler = { unexpectedOutputContexts.associateWith { null } })
                .invokeHandler(providedInputContexts)
        }

        assertEquals(
            expected = MessageFormat.format(ERR_OUTPUT_CONTEXT_NOT_DECLARED, actionHandler.name, unexpectedOutputContexts),
            actual = exception.message
        )
    }

    @Test
    fun `when action handler has declared input contexts but no output contexts was computed then exception is expected`(){
        val providedInputContexts = inputTestContexts.associateWith { null }
        val unexpectedOutputContexts = emptySet<String>()

        val exception = assertThrows<IllegalArgumentException> {
            actionHandler
                .copy(handler = { unexpectedOutputContexts.associateWith { null } })
                .invokeHandler(providedInputContexts)
        }

        assertEquals(
            expected = MessageFormat.format(ERR_NO_OUTPUT_CONTEXT_COMPUTED, actionHandler.name, actionHandler.outputContexts),
            actual = exception.message
        )
    }
}
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

package ai.tock.bot

import ai.tock.iadvize.client.graphql.IadvizeGraphQLClient
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class IAdvizeHandlersProviderTest {
    init {
        System.setProperty(DATA_KEY, "IADVIZE_KEY")
        System.setProperty(DATA_VALUE, "IADVIZE_VALUE")
    }

    private val iadvizeGraphQLClient: IadvizeGraphQLClient = mockk(relaxed = true)
    private val iAdvizeHandlersProvider = IAdvizeHandlersProvider(iadvizeGraphQLClient)

    @AfterEach
    fun after() {
        clearAllMocks()
    }

    @Test
    fun `invoke all handlers except CHECK_CLIENT_CONNECTED`(){
        iAdvizeHandlersProvider.getActionHandlers().filter { it.id != IAdvizeHandlersProvider.HandlerId.CHECK_CLIENT_CONNECTED.name }
            .forEach {
                assertDoesNotThrow("Requirements failed") {
                    it.invokeHandler(it.inputContexts.associateWith { null })
                }
            }
    }

    @Test
    fun `invoke CHECK_CLIENT_CONNECTED handler when CONVERSATION_ID context is not provided`() {
        val handler = iAdvizeHandlersProvider
            .getActionHandlerById(IAdvizeHandlersProvider.HandlerId.CHECK_CLIENT_CONNECTED.name)
        assertNotNull(handler)

        val inputContexts = mapOf(IAdvizeHandlersProvider.ContextName.CONVERSATION_ID.name to null)

        assertThrows<IllegalArgumentException>("Requirements failed") {
            handler.invokeHandler(inputContexts)
        }
    }

    @Test
    fun `invoke CHECK_CLIENT_CONNECTED handler when CONVERSATION_ID context is provided and custom data exist`() {
        val handler = iAdvizeHandlersProvider
            .getActionHandlerById(IAdvizeHandlersProvider.HandlerId.CHECK_CLIENT_CONNECTED.name)
        assertNotNull(handler)

        every { iadvizeGraphQLClient.isCustomDataExist(any(), any()) } returns true

        val inputContexts = mapOf(IAdvizeHandlersProvider.ContextName.CONVERSATION_ID.name to "123")

        assertDoesNotThrow("Requirements failed") {
            handler.invokeHandler(inputContexts)
        }

        val result = handler.invokeHandler(inputContexts)

        assertEquals(1, result.size)
        assertEquals(IAdvizeHandlersProvider.ContextName.CLIENT_CONNECTED.name, result.keys.first())
    }

    @Test
    fun `invoke CHECK_CLIENT_CONNECTED handler when CONVERSATION_ID context is provided and custom data not exist`() {
        val handler = iAdvizeHandlersProvider
            .getActionHandlerById(IAdvizeHandlersProvider.HandlerId.CHECK_CLIENT_CONNECTED.name)
        assertNotNull(handler)

        every { iadvizeGraphQLClient.isCustomDataExist(any(), any()) } returns false

        val inputContexts = mapOf(IAdvizeHandlersProvider.ContextName.CONVERSATION_ID.name to "123")

        assertDoesNotThrow("Requirements failed") {
            handler.invokeHandler(inputContexts)
        }

        val result = handler.invokeHandler(inputContexts)

        assertEquals(1, result.size)
        assertEquals(IAdvizeHandlersProvider.ContextName.CLIENT_DISCONNECTED.name, result.keys.first())
    }
}
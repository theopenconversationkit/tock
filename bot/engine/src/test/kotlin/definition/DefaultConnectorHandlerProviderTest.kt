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

package ai.tock.bot.definition

import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.engine.AsyncBus
import ai.tock.bot.engine.BotBus
import ai.tock.shared.coroutines.ExperimentalTockCoroutines
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

@OptIn(ExperimentalTockCoroutines::class)
class DefaultConnectorHandlerProviderTest {
    @Test
    fun `AsyncStoryHandling creation is ok`() {
        val context = AsyncDefWithData(mockk<AsyncBus>(), StoryData())
        val c = DefaultConnectorHandlerProvider.provide(context, ConnectorType.none)
        assertInstanceOf<AsyncConn>(c)
        assertEquals(context, c.context)
    }

    @Test
    fun `ConnectorStoryHandlerBase creation is ok`() {
        val context = TestDef(mockk<BotBus> { every { targetConnectorType } returns ConnectorType.none })
        val c = DefaultConnectorHandlerProvider.provide(context, ConnectorType.none)
        assertInstanceOf<TestConnDef<*>>(c)
        assertEquals(context, c.context)
    }

    @Test
    fun `ConnectorStoryHandlerBase creation without a context parameter is ko`() {
        val context = InvalidTestDef(mockk<BotBus> { every { targetConnectorType } returns ConnectorType.none })
        assertThrows<NoSuchElementException> {
            DefaultConnectorHandlerProvider.provide(context, ConnectorType.none)
        }
    }

    @TestHandler(TestConnDef::class)
    class TestDef(bus: BotBus) : HandlerDef<TestConnDef<HandlerDef<*>>>(bus)

    class TestConnDef<out T : StoryHandlerDefinition>(context: T) : ConnectorStoryHandlerBase<T>(context)

    class InvalidTestConnDef(provider: () -> TestDef) : ConnectorStoryHandlerBase<TestDef>(provider())

    @TestHandler(InvalidTestConnDef::class)
    class InvalidTestDef(bus: BotBus) : HandlerDef<InvalidTestConnDef>(bus)
}

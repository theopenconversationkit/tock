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

import ai.tock.bot.engine.AsyncBotBus
import ai.tock.bot.engine.CoroutineBridgeBus
import ai.tock.shared.SimpleExecutor
import ai.tock.shared.coroutines.ExperimentalTockCoroutines
import io.mockk.coVerify
import io.mockk.spyk
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.test.assertEquals
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Test

@OptIn(ExperimentalTockCoroutines::class)
class AsyncBotBusTest : AsyncBotEngineTest() {
    @Test
    fun retrieveCurrentBus() = runBlocking {
        val story = storyDef<AsyncDef>(
            "async",
            handling = ::AsyncDef,
            preconditionsChecker = {
                AsyncBotBus.retrieveCurrentBus()?.end("Hello")
            },
        )

        val asyncBotBus = spyk(AsyncBotBus(bus))

        withContext(AsyncBotBus.Ref(asyncBotBus)) {
            story.handle(asyncBotBus)
        }

        coVerify(exactly = 1) {
            asyncBotBus.end("Hello")
        }
    }

    @Test
    fun `handleAndSwitchStory preserves structured concurrency`() = runBlocking {
        val bus = spyk(bus as CoroutineBridgeBus)
        bus.maxWaitMillis = 200L
        val asyncBus = spyk(AsyncBotBus(bus))
        val done = CopyOnWriteArrayList<String>()

        val async2 = storyDef<AsyncDef>(
            "async2",
            handling = ::AsyncDef,
            preconditionsChecker = {
                done += "startAsync2"
                delay(100)
                end("Hi")
                done += "endAsync2"
            },
        )
        val sync1 = storyDef<SimpleDef>(
            "sync1",
            preconditionsChecker = {
                done += "startSync"
                handleAndSwitchStory(async2)
                done += "endSync"
            },
        )
        val async1 = storyDef<AsyncDef>(
            "async1",
            handling = ::AsyncDef,
            preconditionsChecker = {
                done += "startAsync1"
                handleAndSwitchStory(sync1)
                done += "endAsync1"
            },
        )
        (botDefinition.stories as MutableList).addAll(listOf(async1, sync1, async2))

        async1.handle(asyncBus)
        assertEquals(
            listOf("startAsync1", "startSync", "startAsync2", "endAsync2", "endSync", "endAsync1"),
            done
        )
    }

    @Test
    fun `handleAndSwitchStory does not deadlock`() = runBlocking {
        val bus = spyk(bus as CoroutineBridgeBus)
        bus.maxWaitMillis = 100L
        val asyncBus = spyk(AsyncBotBus(bus))
        val done = CopyOnWriteArrayList<String>()

        val async2 = storyDef<AsyncDef>(
            "async2",
            handling = ::AsyncDef,
            preconditionsChecker = {
                done += "startAsync2"
                delay(100)
                end("Hi")
                done += "endAsync2"
            },
        )
        val sync1 = storyDef<SimpleDef>(
            "sync1",
            preconditionsChecker = {
                done += "startSync"
                handleAndSwitchStory(async2)
                done += "endSync"
            },
        )
        val async1 = storyDef<AsyncDef>(
            "async1",
            handling = ::AsyncDef,
            preconditionsChecker = {
                done += "startAsync1"
                handleAndSwitchStory(sync1)
                done += "endAsync1"
            },
        )
        (botDefinition.stories as MutableList).addAll(listOf(async1, sync1, async2))

        async1.handle(asyncBus, SimpleExecutor(1).asCoroutineDispatcher())
        assertEquals(
            listOf("startAsync1", "startSync", "endSync", "startAsync2", "endAsync2", "endAsync1"),
            done
        )
    }
}

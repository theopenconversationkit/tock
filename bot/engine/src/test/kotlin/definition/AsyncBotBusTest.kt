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
import ai.tock.shared.coroutines.ExperimentalTockCoroutines
import io.mockk.Ordering
import io.mockk.coVerify
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Test
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.test.assertEquals

@OptIn(ExperimentalTockCoroutines::class)
internal class AsyncBotBusTest : AsyncBotEngineTest() {
    @Test
    fun retrieveCurrentBus() =
        runBlocking {
            val story =
                storyDef<AsyncDef>(
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
    fun `handleAndSwitchStory preserves structured concurrency`() =
        runBlocking {
            val bus = spyk(bus)
            val asyncBus = spyk(AsyncBotBus(bus))
            val done = CopyOnWriteArrayList<String>()

            val sync =
                storyDef<SimpleDef>(
                    "sync1",
                    preconditionsChecker = {
                        done += "startSync"
                        end("Bye")
                        done += "endSync"
                    },
                )
            val async2 =
                storyDef<AsyncDef>(
                    "async2",
                    handling = ::AsyncDef,
                    preconditionsChecker = {
                        done += "startAsync2"
                        delay(100)
                        send("Hi")
                        handleAndSwitchStory(sync)
                        done += "endAsync2"
                    },
                )
            val async1 =
                storyDef<AsyncDef>(
                    "async1",
                    handling = ::AsyncDef,
                    preconditionsChecker = {
                        done += "startAsync1"
                        handleAndSwitchStory(async2)
                        done += "endAsync1"
                    },
                )
            (botDefinition.stories as MutableList).addAll(listOf(sync, async1, async2))

            async1.handle(asyncBus)
            assertEquals(
                listOf("startAsync1", "startAsync2", "startSync", "endSync", "endAsync2", "endAsync1"),
                done,
            )
            verify(ordering = Ordering.ORDERED) {
                bus.send("Hi")
                bus.end("Bye")
            }
        }
}

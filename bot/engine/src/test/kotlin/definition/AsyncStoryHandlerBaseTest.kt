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
import ai.tock.bot.engine.TockBotBus
import ai.tock.bot.engine.action.SendSentence
import ai.tock.shared.coroutines.ExperimentalTockCoroutines
import io.mockk.coEvery
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import kotlin.system.measureTimeMillis
import kotlin.test.assertTrue

@OptIn(ExperimentalTockCoroutines::class)
class AsyncStoryHandlerBaseTest : AsyncBotEngineTest() {
    @Test
    fun `messages are sent in sequence`() =
        runBlocking {
            val connectorWaitTime = 100L
            val totalConnectorWaitTime = connectorWaitTime * 3
            val testData = StoryData("", ZonedDateTime.now())

            val bus = spyk(bus as TockBotBus)
            every { bus.doSend(any(), any()) } answers {
                Thread.sleep(connectorWaitTime)
            }
            val asyncBus = spyk(AsyncBotBus(bus))

            val storyHandling = spyk(AsyncDefWithData(asyncBus, testData))

            var handlingDuration = 0L
            coEvery { storyHandling.handle() } coAnswers {
                handlingDuration =
                    measureTimeMillis {
                        callOriginal()
                    }
            }

            val storyDef =
                storyDef<AsyncDefWithData, StoryData>(
                    "async",
                    handling = { _, _ -> storyHandling },
                    preconditionsChecker = { testData },
                )
            val totalStoryTime =
                measureTimeMillis {
                    storyDef.handle(asyncBus)
                }

            verify {
                bus.doSend(
                    match<SendSentence> {
                        it.text.toString() == "message 1"
                    },
                    eq(0),
                )
                bus.doSend(
                    match<SendSentence> {
                        it.text.toString() == "message 2"
                    },
                    eq(BotDefinition.defaultBreath),
                )
                bus.doSend(
                    match<SendSentence> {
                        it.text.toString() == "ok"
                    },
                    eq(BotDefinition.defaultBreath * 2),
                )
            }

            // Note: this check may fail when debugging breakpoints are used
            assertTrue("Calls to connector.send should be concurrent to story execution") {
                handlingDuration < totalConnectorWaitTime
            }
            assertTrue("Calls to connector.send should happen in sequence") {
                totalStoryTime >= totalConnectorWaitTime
            }
        }
}

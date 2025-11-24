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

package ai.tock.bot.engine

import ai.tock.bot.engine.TockBotBus.QueuedAction
import ai.tock.shared.TestExecutorRecorder
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertTrue

class TockBotBusTest : BotEngineTest() {
    @BeforeEach
    fun beforeTest() {
        TestExecutorRecorder.clear()
    }

    @Test
    fun `deferMessageSending close messageChanel asynchronously WHERE not closed before`() =
        runBlocking {
            val messageChannel: Channel<QueuedAction> = mockk(relaxed = true)
            val close = (bus as TockBotBus).deferMessageSending(this, messageChannel, Duration.ofMillis(50))
            close.invoke()
            assertTrue { TestExecutorRecorder.executeBlockingDuration.size == 1 }
        }

    @Test
    fun `deferMessageSending does not close messageChanel asynchronously WHERE closed before`() =
        runBlocking {
            val messageChannel: Channel<QueuedAction> = mockk(relaxed = true)
            val close = (bus as TockBotBus).deferMessageSending(this, messageChannel, Duration.ofMillis(50))
            bus.end()
            delay(100)
            close.invoke()
            assertTrue { TestExecutorRecorder.executeBlockingDuration.size == 0 }
        }
}

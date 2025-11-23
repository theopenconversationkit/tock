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
import ai.tock.bot.engine.AsyncBus
import ai.tock.bot.engine.BotBus
import ai.tock.shared.coroutines.ExperimentalTockCoroutines
import kotlinx.coroutines.runBlocking

/**
 * Receive a sentence or action, and send the answer asynchronously.
 *
 * Story handlers should usually not directly extend this class, but instead extend [AsyncStoryHandlerBase].
 */
@ExperimentalTockCoroutines
interface AsyncStoryHandler : StoryHandler {
    @Deprecated("Use coroutines to call this interface", replaceWith = ReplaceWith("handle(asyncBus)"))
    override fun handle(bus: BotBus) {
        // This should only happen in automated tests
        runBlocking { handle(AsyncBotBus(bus)) }
    }

    /**
     * Receive a message from the bus.
     *
     * @param bus the bus used to get the message and send the answer
     */
    suspend fun handle(bus: AsyncBus)
}

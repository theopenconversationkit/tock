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
 * Coroutine-based variant of [StoryHandlerListener].
 */
@ExperimentalTockCoroutines
interface AsyncStoryHandlerListener : StoryHandlerListener {
    @Deprecated("Use coroutines to call this interface", replaceWith = ReplaceWith("startAction(asyncBus, handler)"))
    override fun startAction(
        botBus: BotBus,
        handler: StoryHandler,
    ): Boolean =
        runBlocking {
            startAction(
                AsyncBotBus(botBus),
                handler,
            )
        }

    /**
     * Called before [StoryHandler.handle].
     * If it returns false, the [StoryHandlerListener]s registered after this listener
     * and the [StoryHandler] are not called.
     * (however [endAction] of each [StoryHandlerListener] is called).
     */
    suspend fun startAction(
        botBus: AsyncBus,
        handler: StoryHandler,
    ): Boolean = true

    @Deprecated("Use coroutines to call this interface", replaceWith = ReplaceWith("endAction(asyncBus, handler)"))
    override fun endAction(
        botBus: BotBus,
        handler: StoryHandler,
    ) {
        runBlocking {
            endAction(AsyncBotBus(botBus), handler)
        }
    }

    /**
     * Called when [StoryHandler] handling is over.
     */
    suspend fun endAction(
        botBus: AsyncBus,
        handler: StoryHandler,
    ) = Unit
}

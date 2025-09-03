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

import ai.tock.bot.engine.AsyncBus
import ai.tock.bot.engine.BotBus
import ai.tock.shared.coroutines.ExperimentalTockCoroutines

@ExperimentalTockCoroutines
class AsyncConfigurableStoryHandler<T : AsyncStoryHandling, D>(
    /**
     * The main intent of the story definition.
     */
    mainIntent: Intent? = null,
    /**
     * The [AsyncStoryHandling] creator. Defines [AsyncDelegatingStoryHandlerBase.newHandlerDefinition].
     */
    private val handlerDefCreator: AsyncStoryHandlingCreator<T, D>,
    /**
     * Check preconditions. if [BotBus.end] is called in this function,
     * [StoryHandlerDefinition.handle] is not called and the handling of bot answer is over.
     */
    private val preconditionsChecker: suspend AsyncBus.() -> D,
): AsyncDelegatingStoryHandlerBase<T, D>(mainIntent) {
    override fun checkPreconditions(): suspend AsyncBus.() -> D = preconditionsChecker

    override fun newHandlerDefinition(bus: AsyncBus, preconditionResult: D): T = handlerDefCreator.create(bus, preconditionResult)
}

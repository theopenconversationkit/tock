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
interface AsyncStoryDataStep<in T : AsyncStoryHandling, TD, D> : AsyncStoryStep<T> {
    override suspend fun T.answer() {
        answer(checkPreconditions(null))
    }

    /**
     * Does this Step have to be selected for the current context and data?
     *
     * This method is called if [AsyncDelegatingStoryHandlerBase.checkPreconditions] does not call [AsyncBus.end].
     * If this method returns true, the step is selected and remaining steps are not tested.
     * This method is called even if [selectFromAction] previously returned `false`.
     *
     * Returning `true` causes the step to be selected even if another step got previously selected.
     * Returning `false` does not deselect the step if it was already selected.
     *
     * @see selectFromAction
     */
    fun T.selectFromContextAndData(mainData: TD?): Boolean = false

    /**
     * Checks preconditions - if [BotBus.end] is called,
     * [StoryHandlerDefinition.handle] is not called and the handling of bot answer is over.
     * Returned data is used in subsequent call of [answer]
     */
    suspend fun T.checkPreconditions(mainData: TD?): D

    suspend fun T.answer(data: D)
}

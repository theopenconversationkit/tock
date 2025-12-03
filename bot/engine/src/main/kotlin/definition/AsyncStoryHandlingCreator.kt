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
import ai.tock.shared.coroutines.ExperimentalTockCoroutines

@ExperimentalTockCoroutines
fun interface AsyncStoryHandlingCreator<T : AsyncStoryHandling, D> {
    fun create(
        bus: AsyncBus,
        data: D,
    ): T
}

@ExperimentalTockCoroutines
fun interface SimpleAsyncStoryHandlingCreator<T : AsyncStoryHandling> : AsyncStoryHandlingCreator<T, Unit> {
    override fun create(
        bus: AsyncBus,
        data: Unit,
    ): T = create(bus)

    fun create(bus: AsyncBus): T
}

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
import ai.tock.bot.engine.BotEngineTest
import ai.tock.bot.engine.CoroutineBridgeBus
import ai.tock.shared.Executor
import ai.tock.shared.SimpleExecutor
import ai.tock.shared.coroutines.ExperimentalTockCoroutines
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import io.mockk.spyk
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext

@OptIn(ExperimentalTockCoroutines::class)
abstract class AsyncBotEngineTest : BotEngineTest() {
    val executor = spyk(SimpleExecutor(4))

    override fun baseModule(): Kodein.Module {
        return Kodein.Module {
            import(super.baseModule())
            bind<Executor>(overrides = true) with provider { executor }
        }
    }

    suspend fun AsyncStoryDefinition.handle(asyncBus: AsyncBotBus) {
        withContext(executor.asCoroutineDispatcher()) {
            (asyncBus.botBus as CoroutineBridgeBus).deferMessageSending(this)
            storyHandler.handle(asyncBus)
        }
    }
}

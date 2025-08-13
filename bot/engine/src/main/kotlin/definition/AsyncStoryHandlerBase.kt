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

import ai.tock.bot.definition.BotDefinition.Companion.defaultBreath
import ai.tock.bot.engine.BotBus
import ai.tock.shared.coroutines.ExperimentalTockCoroutines
import ai.tock.shared.defaultNamespace

@ExperimentalTockCoroutines
abstract class AsyncStoryHandlerBase<out T : AsyncStoryHandlerDefinition>(
    mainIntentName: String? = null,
    i18nNamespace: String = defaultNamespace,
    breath: Long = defaultBreath,
) : StoryHandlerBase<T>(mainIntentName, i18nNamespace, breath), AsyncStoryHandler {
    final override suspend fun handleAsync(bus: BotBus) {
        handle0(bus) {
            it.handleAsync()
        }
    }
}

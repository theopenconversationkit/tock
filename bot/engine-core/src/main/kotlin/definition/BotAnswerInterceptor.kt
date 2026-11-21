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

import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.action.Action

/**
 * Change or update answer before sending to the user
 * Need to be registered using [ai.tock.bot.engine.BotRepository.registerBotAnswerInterceptor].
 */
interface BotAnswerInterceptor {
    /**
     * Returns the replacement action.
     */
    fun handle(
        action: Action,
        bus: BotBus,
    ): Action = action
}

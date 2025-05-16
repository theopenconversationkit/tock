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

/**
 * Receive a sentence or action, and send the answer.
 *
 * Story handlers should usually not directly extend this class, but instead extend [StoryHandlerBase].
 */
interface StoryHandler {

    /**
     * Receive a message from the bus.
     *
     * @param bus the bus used to get the message and send the answer
     */
    fun handle(bus: BotBus)

    /**
     * What is the probability of bot support for the current request?
     *
     * @return a probability between 0.0 (not supported) and 1.0 (supported!)
     */
    fun support(bus: BotBus): Double = 1.0
}

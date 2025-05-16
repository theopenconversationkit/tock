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
 * A step that can have specific preconditions and can use input data object in handler.
 *
 * @param T StoryDef
 * @param TD the data of the StoryDef
 * @param D the data of the step
 */
interface StoryDataStep<T : StoryHandlerDefinition, TD, D> : StoryStep<T> {

    override fun answer(): T.() -> Any? = { handler().invoke(this, null) }

    /**
     * Does this Step has to be selected from the Bus?
     * This method is called if [StoryHandlerBase.checkPreconditions] does not call [BotBus.end].
     * If this functions returns true, the step is selected and remaining steps are not tested.
     */
    fun selectFromBusAndData(): T.(TD?) -> Boolean = { false }

    /**
     * Checks preconditions - if [BotBus.end] is called,
     * [StoryHandlerDefinition.handle] is not called and the handling of bot answer is over.
     * Returned data is used in subsequent call of [handler] if not null
     * - else [StoryHandlerBase.checkPreconditions] returned data is used.
     */
    fun checkPreconditions(): T.(TD?) -> D? = { null }

    /**
     * The custom handler for this step.
     * When returning a null value,
     * it means that the step is not able to answer to the current request.
     *
     * Default implementation returns null.
     */
    fun handler(): T.(D?) -> Any? = { null }
}

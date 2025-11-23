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
import java.util.concurrent.ConcurrentHashMap

/**
 * step -> intent default behaviour.
 */
internal val stepToIntentRepository = ConcurrentHashMap<StoryStepDef, IntentAware>()

/**
 * Use this step when you want to set a null [StoryStep].
 */
val noStep =
    object : SimpleStoryStep {
        override val name: String = "_NO_STEP_"
    }

/**
 * A step is a part of a [StoryDefinition].
 * Used to manage workflow in a [StoryHandler].
 */
interface StoryStep<T : StoryHandlerDefinition> : StoryStepDef {
    /**
     * The custom answer for this step.
     * When returning a null value,
     * it means that the step is not able to answer to the current request.
     *
     * Default implementation returns null.
     */
    fun answer(): T.() -> Any? = { null }

    /**
     * Does this Step have to be selected for the current context?
     *
     * This method is called if [StoryHandlerBase.checkPreconditions] does not call [BotBus.end].
     * If this method returns true, the step is selected and remaining steps are not tested.
     * This method is called even if [selectFromAction] previously returned `false`.
     *
     * Returning `true` causes the step to be selected even if another step got previously selected.
     * Returning `false` does not deselect the step if it was already selected.
     *
     * @see selectFromAction
     */
    fun selectFromBus(): BotBus.() -> Boolean = { false }

    override val children: Set<StoryStep<T>> get() = emptySet()
}

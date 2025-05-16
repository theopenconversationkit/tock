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

import ai.tock.bot.definition.StoryHandlerBase.Companion.isEndCalled

/**
 * Base class for [StoryDataStep] implementations.
 *
 * @param T the [StoryDef]
 * @param TD the StoryDef data
 * @param D the step data
 */
abstract class StoryDataStepBase<T : StoryHandlerDefinition, TD, D>(
    private val select: TD.(T) -> Boolean = { false },
    private val setup: T.(TD) -> D = {
        @Suppress("UNCHECKED_CAST")
        EmptyData as D
    },
    private val reply: T.(D) -> Any?
) : StoryDataStep<T, TD, D> {

    fun select(): TD.(T) -> Boolean = select

    fun setup(): T.(TD) -> D = setup

    fun reply(): T.(D) -> Any? = reply

    fun execute(storyDef: T, configuration: TD): Any? {
        storyDef.step = this
        val d = setup()(storyDef, configuration)
        return reply()(storyDef, d)
    }

    final override fun selectFromBusAndData(): T.(TD?) -> Boolean = {
        if (it == null) {
            error("story data has to be not null")
        }
        select()(it, this)
    }

    final override fun checkPreconditions(): T.(TD?) -> D? = {
        if (it == null) {
            error("story data has to be not null")
        }
        setup()(this, it)
    }

    final override fun handler(): T.(D?) -> Any? = {
        val c = this
        if (it == null) {
            error("data step has to be not null")
        }
        val reply = reply()(c, it)
        // in order to manage switch inside the reply
        if (!isEndCalled(c)) {
            end {
                reply
            }
        }
    }

    override val name: String get() = this::class.simpleName!!
}

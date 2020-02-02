/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
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
 * Base class for [StoryDataStep] implementations.
 *
 * @param T the [StoryDef]
 * @param TD the StoryDef data
 * @param D the step data
 */
abstract class StoryDataStepBase<T : StoryHandlerDefinition, TD, D>(
    private val select: TD.(BotBus) -> Boolean = { false },
    private val setup: BotBus.(TD) -> D = {
        @Suppress("UNCHECKED_CAST")
        Unit as D
    },
    private val reply: T.(D) -> Any?
) : StoryDataStep<T, TD, D> {

    fun select(): TD.(BotBus) -> Boolean = select

    fun setup(): BotBus.(TD) -> D = setup

    fun reply(): T.(D) -> Any? = reply

    fun execute(storyDef: T, configuration: TD): Any? {
        val d = setup()(storyDef, configuration)
        return handler()(storyDef, d)
    }

    final override fun selectFromBusAndData(): BotBus.(TD?) -> Boolean = {
        select()(it!!, this)
    }

    final override fun checkPreconditions(): BotBus.(TD?) -> D? = {
        setup()(this, it!!)
    }

    final override fun handler(): T.(D?) -> Any? = {
        val c = this
        end {
            reply()(c, it!!)
        }
    }

    override val name: String get() = this::class.simpleName!!
}

/*
 * Copyright (C) 2017/2019 VSCT
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
import kotlin.reflect.full.primaryConstructor

/**
 * Returns default [HandlerStoryDefinitionCreator].
 */
inline fun <reified T : StoryHandlerDefinition> defaultHandlerStoryDefinitionCreator(): HandlerStoryDefinitionCreator<T> = T::class.let {
    object : HandlerStoryDefinitionCreator<T> {
        override fun create(bus: BotBus, data: Any?): T {
            val pC = it.primaryConstructor ?: error("No primary constructor for $it")
            return if (pC.parameters.size == 2) {
                pC.call(bus, data)
            } else {
                pC.call(bus)
            }
        }
    }
}

/**
 * In order to create [StoryHandlerDefinition].
 */
interface HandlerStoryDefinitionCreator<T : StoryHandlerDefinition> {

    /**
     * Creates a new [StoryHandlerDefinition].
     */
    fun create(bus: BotBus, data: Any? = null): T
}
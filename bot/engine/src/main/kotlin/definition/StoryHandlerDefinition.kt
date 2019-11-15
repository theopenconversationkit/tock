/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.bot.definition

import ai.tock.bot.engine.BotBus

/**
 * Story handler definitions are used in [StoryHandler] to provide custom context and to manage specific connector behaviour.
 *
 * Implementations should usually use [StoryHandlerDefinitionBase].
 */
interface StoryHandlerDefinition : BotBus {

    /**
     * The [ConnectorStoryHandler] provided for the current [BotBus.targetConnectorType] - null if it does not exist.
     */
    val connector: ConnectorStoryHandler<*>? get() = null

    /**
     * The main method to implement.
     */
    fun handle()

}
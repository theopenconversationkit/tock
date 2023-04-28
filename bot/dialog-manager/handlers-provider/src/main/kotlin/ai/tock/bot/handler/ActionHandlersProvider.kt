/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

package ai.tock.bot.handler

import ai.tock.bot.HandlerNamespace

/**
 * A handlers provider
 */
interface ActionHandlersProvider {

    /**
     * Get all action handlers
     */
    fun getActionHandlers(): Set<ActionHandler>

    fun getActionHandlerById(id: String): ActionHandler? = getActionHandlers().firstOrNull { it.id == id }

    fun getNameSpace(): HandlerNamespace

    fun createActionHandler(
        id: String,
        description: String? = null,
        inputContexts: Set<String> = emptySet(),
        outputContexts: Set<String> = emptySet(),
        handler: (Map<String, String?>) -> Map<String, String?> = { emptyMap() }): ActionHandler {
        return ActionHandler(
            id = id,
            namespace = getNameSpace(),
            description = description,
            inputContexts = inputContexts,
            outputContexts = outputContexts,
            handler = handler
        )
    }
}
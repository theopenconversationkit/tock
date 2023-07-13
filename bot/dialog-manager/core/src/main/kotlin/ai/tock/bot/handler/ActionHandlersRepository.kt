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
import ai.tock.bot.exception.TickActionHandlerException
import ai.tock.bot.provider.SupportedActionHandlersProvider

/**
 * A handler repository, it contains all available handlers by namespace
 */
object ActionHandlersRepository {

    private val actionHandlers =
        mutableMapOf<String, ActionHandler>()

    init {
        SupportedActionHandlersProvider
            .providers()
            .forEach { subscribe(it) }
    }

    /**
     * Subscription of handlers provider
     * @param provider : the action handlers provider
     */
    private fun subscribe(provider: ActionHandlersProvider) {
        provider.getActionHandlers().forEach(::add)
    }

    /**
     * Add a handler if it does not exist, or throws error exception
     * @param actionHandler : the action handler
     */
    private fun add(actionHandler: ActionHandler) {
        if(actionHandlers.contains(actionHandler.name)) {
            throw TickActionHandlerException("Action handler <${actionHandler.name}> already exists")
        }

        actionHandlers[actionHandler.name] = actionHandler
    }

    /**
     * Invoke a handler if it exists, or throws error exception
     * @param handlerName : the handler name
     * @param contexts : input contexts of invoked handler
     */
    fun invoke(handlerName: String, contexts: Map<String, String?>): Map<String, String?> {
        val handlerCallback = actionHandlers[handlerName]
        handlerCallback ?: throw TickActionHandlerException("TickAction handler <$handlerName> not found")

        return handlerCallback.invokeHandler(contexts)
    }

    /**
     * Checks if the map handlers contains the given handler name.
     * @param handlerName : the handler name
     */
    fun contains(handlerName: String) = actionHandlers.contains(handlerName)

    /**
     * Get action handlers by namespace
     * @param namespace : the handler namespace
     */
    fun getActionHandlers(namespace: HandlerNamespace = HandlerNamespace.UNKNOWN): Set<ActionHandler> {
        return actionHandlers.values.filter { it.namespace == namespace || it.namespace.shared }.toSet()
    }
}

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

package ai.tock.bot

import ai.tock.bot.handler.ActionHandler
import ai.tock.bot.handler.ActionHandlersProvider

/**
 * The [DevToolsHandlersProvider] is a set of developer handlers made available to speed up scenario design
 */
class DevToolsHandlersProvider: ActionHandlersProvider {

    override fun getActionHandlers(): Map<String, ActionHandler> = emptyMap()

    @Deprecated("Use the new method 'getActionHandlers' once developed", level = DeprecationLevel.WARNING)
    override fun getHandlers(): Map<String, (Map<String, String?>) -> Map<String, String?>> {
        val genericHandlers = (1..10).associate {
            "dev_tools_set_context_$it" to ::setContext.invoke(it)
        }

        val doNothing : (Map<String, String?>) -> Map<String, String?> = { emptyMap() }

        val customHandlers = mapOf(
            "dev_tools_do_nothing" to doNothing,
        )

        return genericHandlers.plus(customHandlers)
    }

    private fun setContext(id: Int): (Map<String, String?>) -> Map<String, String?> {
        return { mapOf("DEV_CONTEXT_$id" to null) }
    }

}
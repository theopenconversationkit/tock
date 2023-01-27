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

    override fun getNameSpace() = HandlerNamespace.DEV_TOOLS

    override fun getActionHandlers(): Set<ActionHandler> =
        setOf(
            createActionHandler(
                id = "do_nothing",
                description = "Handler who does nothing. It is used to force the next round",
                handler = { emptyMap() })
        ).plus(
            (1..7).map {counter ->
                createActionHandler(
                    id = "set_context_$counter",
                    description = "Handler that just sets <DEV_CONTEXT_$counter>",
                    outputContexts = setOf("DEV_CONTEXT_$counter"),
                    handler = { mapOf("DEV_CONTEXT_$counter" to null) }
                )
            }
        ).toSet()
}
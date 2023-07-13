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


class DevToolsHandlersProvider : ActionHandlersProvider {

    override fun getNameSpace() = HandlerNamespace.DEV_TOOLS

    private enum class HandlerId {
        DO_NOTHING,
        SET_CONTEXT
    }

    private enum class ContextName {
        DEV_CONTEXT
    }

    override fun getActionHandlers(): Set<ActionHandler> =
        setOf(
            createActionHandler(
                id = HandlerId.DO_NOTHING.name,
                description = "Handler witch does nothing. It is used to force the next round",
                handler = { emptyMap() })
        ).plus(
            (1..7).map { counter ->
                createActionHandler(
                    id = "${HandlerId.SET_CONTEXT.name}_$counter",
                    description = "Handler that just sets <${ContextName.DEV_CONTEXT.name}_$counter>",
                    outputContexts = setOf("${ContextName.DEV_CONTEXT.name}_$counter"),
                    handler = { mapOf("${ContextName.DEV_CONTEXT.name}_$counter" to null) }
                )
            }
        )

}
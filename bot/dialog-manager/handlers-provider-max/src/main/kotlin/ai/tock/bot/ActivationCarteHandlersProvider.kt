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

class ActivationCarteHandlersProvider: ActionHandlersProvider {

    override fun getNameSpace() = HandlerNamespace.MAX

    enum class HandlerId {
        SET_RESOLVE_MAX,
        SET_RESOLVE_ACTIVATION,
        SET_RESOLVE_ACTIVATION_GLOBAL
    }
    enum class ContextName {
        RESOLVE_MAX,
        RESOLVE_ACTIVATION,
        RESOLVE_ACTIVATION_GLOBAL
    }

    override fun getActionHandlers(): Set<ActionHandler> =
        setOf(
            createActionHandlerThatJustSetsContexts(
                HandlerId.SET_RESOLVE_MAX,
                setOf(ContextName.RESOLVE_MAX)
            ),
            createActionHandlerThatJustSetsContexts(
                HandlerId.SET_RESOLVE_ACTIVATION,
                setOf(ContextName.RESOLVE_ACTIVATION)
            ),
            createActionHandlerThatJustSetsContexts(
                HandlerId.SET_RESOLVE_ACTIVATION_GLOBAL,
                setOf(ContextName.RESOLVE_ACTIVATION_GLOBAL)
            )
        )

    private fun createActionHandlerThatJustSetsContexts(handlerId : HandlerId, contexts: Set<ContextName>) =
        createActionHandler(
            id = handlerId.name,
            description = "Handler that just sets <${contexts.map { it.name }.joinToString(", ")}>",
            outputContexts = contexts.map { it.name }.toSet(),
            handler = { contexts.associate { it.name to null } }
        )


}
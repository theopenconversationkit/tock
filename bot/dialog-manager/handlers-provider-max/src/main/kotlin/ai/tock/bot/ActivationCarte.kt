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
import com.google.common.base.CaseFormat

class ActivationCarte: ActionHandlersProvider {

    override fun getActionHandlers(): Map<String, ActionHandler> {

        val resolveMax = ActionHandler(
            name = HandlerName.SET_RESOLVE_MAX.name,
            description = "Set ${ContextName.RESOLVE_MAX.name} context",
            inputContexts = emptySet(),
            outputContexts = setOf(ContextName.RESOLVE_MAX.name),
            handler = ::handlerSetResolveMax
        )

        val resolveActivation = ActionHandler(
            name = HandlerName.SET_RESOLVE_ACTIVATION.name,
            description = "Set ${ContextName.RESOLVE_ACTIVATION.name} context",
            outputContexts = setOf(ContextName.RESOLVE_ACTIVATION.name),
            handler = { mapOf(ContextName.RESOLVE_ACTIVATION.name to null) }
        )

        return setOf(resolveMax, resolveActivation)
            .associateBy { it.name }
    }

    override fun getHandlers(): Map<String, (Map<String, String?>) -> Map<String, String?>> =
        mapOf(
            "handler_set_resolve_max" to ::handlerSetResolveMax,
            "handler_set_resolve_activation" to ::handlerSetResolveActivation,
            "handler_set_resolve_activation_global" to ::handlerSetResolveActivationGlobal,
        )

    private fun handlerSetResolveMax(contexts: Map<String, String?>): Map<String, String?> {
        return mapOf("RESOLVE_MAX" to null)
    }

    private fun handlerSetResolveActivation(contexts: Map<String, String?>): Map<String, String?> {
        return mapOf("RESOLVE_ACTIVATION" to null)
    }

    private fun handlerSetResolveActivationGlobal(contexts: Map<String, String?>): Map<String, String?> {
        return mapOf("RESOLVE_ACTIVATION_GLOBAL" to null)
    }
}
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

class PriseRdvHandlersProvider: ActionHandlersProvider {

    override fun getNameSpace() = HandlerNamespace.JOIGNABILITE

    private enum class HandlerId {
        SET_RESOLVE_RDV
    }

    private enum class ContextName {
        RESOLVE_RDV
    }

    override fun getActionHandlers(): Set<ActionHandler> =
        setOf(
            createActionHandler(
                id = HandlerId.SET_RESOLVE_RDV.name,
                description = "Set ${ContextName.RESOLVE_RDV.name} context",
                outputContexts = setOf(ContextName.RESOLVE_RDV.name),
                handler = { mapOf(ContextName.RESOLVE_RDV.name to null) }
            )
        )

}
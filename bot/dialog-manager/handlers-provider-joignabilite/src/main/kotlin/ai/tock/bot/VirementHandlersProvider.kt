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

class VirementHandlersProvider: ActionHandlersProvider {

    override fun getNameSpace() = HandlerNamespace.JOIGNABILITE

    private enum class HandlerId {
        CHECK_TRANSFER,
        CHECK_SERVICE_AVAILABLE,
        SHOW_MSG_CANNOT_CHANGE_LIMIT,
        SHOW_MSG_CAN_CHANGE_LIMIT,
        SERVICE_UNAVAILABLE_REDIRECT,
    }

    private enum class ContextName {
        MONTANT_VIREMENT,
        DESTINATION_VIREMENT,
        LIMIT_EXCEDED,
        CAN_CHANGE_LIMIT,
        CANNOT_CHANGE_LIMIT,
        SERVICE_AVAILABLE,
        SERVICE_UNAVAILABLE,
        RESOLVE_LIMIT_DONE
    }

    override fun getActionHandlers(): Set<ActionHandler> =
        setOf(
            createActionHandler(
                id = HandlerId.CHECK_TRANSFER.name,
                description = "Check the transfer",
                inputContexts = setOf(
                    ContextName.MONTANT_VIREMENT.name,
                    ContextName.DESTINATION_VIREMENT.name),
                outputContexts = setOf(
                    ContextName.LIMIT_EXCEDED.name,
                    ContextName.CAN_CHANGE_LIMIT.name,
                    ContextName.CANNOT_CHANGE_LIMIT.name),
                handler = ::checkTransfer
            ),
            createActionHandler(
                id = HandlerId.CHECK_SERVICE_AVAILABLE.name,
                description = "Check availability of the service",
                outputContexts = setOf(
                    ContextName.SERVICE_AVAILABLE.name,
                    ContextName.SERVICE_UNAVAILABLE.name),
                handler = ::checkService
            ),
            createActionHandler(
                id = HandlerId.SHOW_MSG_CAN_CHANGE_LIMIT.name,
                description = "Shows a message that the limit can be changed",
                outputContexts = setOf(
                    ContextName.RESOLVE_LIMIT_DONE.name),
                handler = { mapOf(ContextName.RESOLVE_LIMIT_DONE.name to null) }
            ),
            createActionHandler(
                id = HandlerId.SHOW_MSG_CANNOT_CHANGE_LIMIT.name,
                description = "Shows a message that the limit cannot be changed",
                outputContexts = setOf(
                    ContextName.RESOLVE_LIMIT_DONE.name),
                handler = { mapOf(ContextName.RESOLVE_LIMIT_DONE.name to null) }
            ),
            createActionHandler(
                id = HandlerId.SERVICE_UNAVAILABLE_REDIRECT.name,
                description = "Redirect to human",
                outputContexts = setOf(
                    ContextName.RESOLVE_LIMIT_DONE.name),
                handler = { mapOf(ContextName.RESOLVE_LIMIT_DONE.name to null) }
            )
        )


    var serviceAvailable = false

    private fun checkTransfer(contexts: Map<String, String?>): Map<String, String?> {
        val transferAmount = contexts.get("MONTANT_VIREMENT")!!.toInt()
        val transferDestination = contexts.get("DESTINATION_VIREMENT")

        // CALL API: GetLimitExeded

        val limit: Int = 3000
        val outputContexts = mutableMapOf<String, String?>()
        outputContexts.put("LIMIT_EXCEDED", limit.toString())
        if(transferAmount <= limit) {
            outputContexts.put("CAN_CHANGE_LIMIT", null)
        } else {
            outputContexts.put("CANNOT_CHANGE_LIMIT", null)
        }

        return outputContexts.toMap()
    }

    private fun checkService(contexts: Map<String, String?>): Map<String, String?> {
        val outputContexts = mutableMapOf<String, String?>()
        if(serviceAvailable) {
            outputContexts.put("SERVICE_AVAILABLE", null)
        }else {
            outputContexts.put("SERVICE_UNAVAILABLE", null)
        }
        serviceAvailable = !serviceAvailable

        return outputContexts.toMap()
    }
}
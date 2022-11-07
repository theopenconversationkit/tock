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

package ai.tock.bot.handler.provider.joignabilite

import ai.tock.bot.handler.provider.ActionHandlersProvider

/**
 * A handlers provider for tick "JoignabilitePlafondsVirement"
 */
object JoignabilitePlafondsVirement : ActionHandlersProvider {

    //TODO : work in progress
    // TODO: complete the implementation

    override fun getHandlers(): Map<String, (Map<String, String?>) -> Map<String, String?>> =
        mapOf(
            "s_check_transfer" to JoignabilitePlafondsVirement::checkTransfer,
            "s_check_service_available" to JoignabilitePlafondsVirement::checkService,
            "s_show_msg_cannot_change_limit" to JoignabilitePlafondsVirement::showMsgCannotChangeLimit,
            "s_show_msg_can_change_limit" to JoignabilitePlafondsVirement::showMsgCanChangeLimit,
            "s_service_unavailable_redirect" to JoignabilitePlafondsVirement::serviceUnavailableRedirect
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

    private fun showMsgCannotChangeLimit(contexts: Map<String, String?>): Map<String, String?> {
        return mapOf("RESOLVE_LIMIT_DONE" to null)
    }

    private fun showMsgCanChangeLimit(contexts: Map<String, String?>): Map<String, String?> {
        return mapOf("RESOLVE_LIMIT_DONE" to null)
    }

    private fun serviceUnavailableRedirect(contexts: Map<String, String?>): Map<String, String?> {
        return mapOf("RESOLVE_LIMIT_DONE" to null)
    }
}
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
 * A handlers provider for tick "JoignabiliteReclamation"
 */
object JoignabiliteReclamation : ActionHandlersProvider {

    //TODO : work in progress
    // TODO: complete the implementation

    override fun getHandlers(): Map<String, (Map<String, String?>) -> Map<String, String?>> =
        mapOf(
            "s_send_reclamation" to JoignabiliteReclamation::handlerSendReclamation,
            "s_set_resolve_reclamation" to JoignabiliteReclamation::handlerSetResolveReclamation
        )

    var reclamationSent = true

    private fun handlerSendReclamation(contexts: Map<String, String?>): Map<String, String?> {
        val outputContexts = mutableMapOf<String, String?>()

        val objetRecalamation = contexts.get("OBJET_RECLAMATION")
        val dateReclamatoin = contexts.get("DATE_RECLAMATION")
        //Appel à l'API permettant d'envoyer la réclamation

        if(reclamationSent) {
            outputContexts.put("RECLAMATION_SENT", null)
        } else {
            outputContexts.put("SEND_RECLAMATION_KO", null)
        }

        reclamationSent = !reclamationSent

        return outputContexts.toMap()
    }

    private fun handlerSetResolveReclamation(contexts: Map<String, String?>): Map<String, String?> {
        return mapOf("RESOLVE_RECLAMATION" to null)
    }
}
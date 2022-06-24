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
 * A handlers provider for tick "JoignabiliteDemandeAttestation"
 */
object JoignabiliteDemandeAttestation : ActionHandlersProvider {

    //TODO : work in progress
    // TODO: complete the implementation

    // Voir avec JY -> impossible d'implementer ce scenario, car il faut :
    // - Gérer les valeur par defaut. ex: introduction
    // - Prendre en compte l'intention dans le handler ex: handler_S_RESOLVE_POSTMAIL
    // Ceci n'est pas prévu pour le moment

    override fun getHandlers(): Map<String, (Map<String, String?>) -> Map<String, String?>> =
        mapOf(
            "INTRODUCTION" to JoignabiliteDemandeAttestation::introduction,
            "S_GET_CLIENT_CONTRACTS" to JoignabiliteDemandeAttestation::s_get_client_contracts,
            "S_DISPLAY_DOWNLOAD_LINK" to JoignabiliteDemandeAttestation::s_display_download_link,
            "S_ASK_CONFIRM_EMAI" to JoignabiliteDemandeAttestation::s_ask_confirm_email,
            "S_SEND_MAIL" to JoignabiliteDemandeAttestation::s_send_mail,
            "S_SEND_POSTMAIL" to JoignabiliteDemandeAttestation::s_send_postmail
        )
    

    private fun introduction(contexts: Map<String, String?>): Map<String, String?> {
        return mapOf("DEFAULT_ADDRESS" to "8 rue des acacias")
    }

    private fun s_get_client_contracts(contexts: Map<String, String?>): Map<String, String?> {
        return mapOf("CONTRACT_LIST" to "ABCD1234,EDGH5678")
    }

    private fun s_display_download_link(contexts: Map<String, String?>): Map<String, String?> {
        return mapOf("RESOLVE_CERTIF" to "True")
    }

    private fun s_ask_confirm_email(contexts: Map<String, String?>): Map<String, String?> {
        return mapOf("SHOULD_RESOLVE_MAIL" to "True")
    }

    private fun s_send_mail(contexts: Map<String, String?>): Map<String, String?> {
        return mapOf("EMAIL_SENT" to "True")
    }

    private fun s_send_postmail(contexts: Map<String, String?>): Map<String, String?> {
        return mapOf("POSTMAIL_SENT" to "True")
    }

//    def handler_S_RESOLVE_POSTMAIL(bot,intent):
//    print(intent)
//    if intent == "confirm" :
//    bot.create_context('VALID_ADDRESS', value='8 rue des acacias')
//    else :
//    bot.create_context('ADDRESS_REFUSED', value='True')
//    bot.must_continue()
}
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

package ai.tock.bot.suravenirassurance

import ai.tock.bot.handler.NewActionHandlersProvider

/**
 * A handlers provider for tick "SADeclarationBrisDeGlace"
 */
class SADeclarationBrisDeGlace: NewActionHandlersProvider {
    override fun getHandlers(): Map<String, (Map<String, String?>) -> Map<String, String?>> =
        mapOf(
            "handler_get_contract_by_immat" to ::handlerGetContractByImmat,
            "handler_set_resolve_verif_tel" to ::handlerSetResolveVerifTel,
            "handler_set_resolve_sinistre" to ::handlerSetResolveSinistre,
            "handler_set_client_identification_ok" to ::handlerSetClientIdentificationOk,
            "handler_should_check_phone_and_mail" to ::handlerShouldCheckPhoneAndMail,
            "handler_confirm_verif_mail_and_tel" to ::handlerConfirmVerifMailAndTel,
            "HANDLER_VERIFIER_LES_DEUX" to ::handlerVerifierLesDeux,
            "handler_validate_check_mail" to ::handlerValidateCheckMail,
            "handler_resolve_oui_non_maj_tel" to ::handlerResolveOuiNonMajTel,
            "handler_resolve_oui_non_confirm_maj_tel" to ::handlerResolveOuiNonConfirmMajTel,
            "handler_resolve_oui_non_maj_mail" to ::handlerResolveOuiNonMajMail,
            "handler_save_new_mail" to ::handlerSaveNewMail,
            "handler_resolve_verif_mail" to ::handlerResolveVerifMail,
            "handler_create_sinistre" to ::handlerCreateSinistre,
            "handler_get_client_infos_by_contract" to ::handlerGetClientInfosByContract,
            "handler_send_partenaires" to ::handlerSendPartenaires,
            "handler_validate_check_tel" to ::handlerValidateCheckTel,
            "handler_update_client_phone" to ::handlerUpdateClientPhone,
            "handler_validate_confirm_maj_tel" to ::handlerValidateConfirmMajTel,
            "action_fictive" to :: actionFictive,
            "setEndResolveTel" to :: setEndResolveTel,
            "setEndResolveMail" to :: setEndResolveMail,
            "handler_check_besoin_verif_tel" to ::handlerCheckBesoinVerifTel,
            "handler_check_besoin_verif_mail" to :: handlerCheckBesoinVerifMail
        )

    private val sousObjectifKey = "SOUS-OBJECTIF"
    var besoinVerifTel = true

    private fun handlerCheckBesoinVerifTel(contexts: Map<String, String?>): Map<String, String?> {
        val outputContexts = mutableMapOf<String, String?>()

        if(besoinVerifTel){
            outputContexts.put("BESOIN_VERIFIER_TEL", "true")
        } else {
            outputContexts.put("RESOLVE_VERIF_TEL", "true")
        }

        outputContexts.put(sousObjectifKey, "RESOLVE_VALIDATE_CLIENT_PHONE")
        besoinVerifTel = !besoinVerifTel
        return outputContexts.toMap()
    }
    var besoinVerifMail = true

    private fun handlerCheckBesoinVerifMail(contexts: Map<String, String?>): Map<String, String?> {
        val outputContexts = mutableMapOf<String, String?>()

        if(besoinVerifMail){
            outputContexts.put("BESOIN_VERIFIER_MAIL", "true")
        } else {
            outputContexts.put("RESOLVE_VERIF_MAIL", "true")
        }

        besoinVerifMail = !besoinVerifMail
        outputContexts.put(sousObjectifKey, "RESOLVE_VALIDATE_CLIENT_EMAIL")
        return outputContexts.toMap()
    }

    private fun actionFictive(contexts: Map<String, String?>): Map<String, String?> {
        // Nothing to do
        return mapOf()
    }

    private fun setEndResolveTel(contexts: Map<String, String?>): Map<String, String?> {
        return mapOf("END_RESOLVE_TEL" to null)
    }

    private fun setEndResolveMail(contexts: Map<String, String?>): Map<String, String?> {
        return mapOf("END_RESOLVE_MAIL" to null)
    }


    private fun handlerGetContractByImmat(contexts: Map<String, String?>): Map<String, String?> {
        val immat = contexts.get("IMMATRICULATION")
        //Appel à l'API permettant d'envoyer la réclamation

        return mapOf("NUM_CONTRAT" to "74125825")
    }

    private fun handlerSetClientIdentificationOk(contexts: Map<String, String?>): Map<String, String?> {
        return mapOf("IDENT_CLIENT_OK" to "true")
    }

    private fun handlerConfirmVerifMailAndTel(contexts: Map<String, String?>): Map<String, String?> {
        val outputContexts = mutableMapOf<String, String?>()

        //outputContexts.put("BESOIN_VERIFIER_TEL", "true")
        //outputContexts.put("BESOIN_VERIFIER_MAIL", "true")
        outputContexts.put("BESOIN_VERIFIER_LES_DEUX", "true")

        return outputContexts.toMap()
    }

    private fun handlerVerifierLesDeux(contexts: Map<String, String?>): Map<String, String?> {
        val outputContexts = mutableMapOf<String, String?>()

        outputContexts.put("BESOIN_VERIFIER_TEL", "true")
        outputContexts.put("BESOIN_VERIFIER_MAIL", "true")

        return outputContexts.toMap()
    }
    var shouldCheckClientTelAndMail = true
    private fun handlerShouldCheckPhoneAndMail(contexts: Map<String, String?>): Map<String, String?> {
        val outputContexts = mutableMapOf<String, String?>()

        if(shouldCheckClientTelAndMail) {
            outputContexts.put("BESOIN_VERIFIER_MAIL_ET_TEL", "true")
        } else {
            outputContexts.put("END_RESOLVE_MAIL", "true")
            outputContexts.put("END_RESOLVE_TEL", "true")
        }

        shouldCheckClientTelAndMail = !shouldCheckClientTelAndMail

        return outputContexts.toMap()
    }

    private fun handlerValidateCheckTel(contexts: Map<String, String?>): Map<String, String?> {
        return mapOf("SHOULD_RESOLVE_OUI_NON_TEL" to "true")
    }

    private fun handlerValidateConfirmMajTel(contexts: Map<String, String?>): Map<String, String?> {
        return mapOf("SHOULD_RESOLVE_ON_CONFIRM_MAJ_TEL" to "true")
    }

    private fun handlerValidateCheckMail(contexts: Map<String, String?>): Map<String, String?> {
        return mapOf("SHOULD_RESOLVE_ON_MAJ_MAIL" to "true")
    }

    var validateONMajTel = true
    //Besoin d'avoir l'intent en input pour vérifier si le client a dit oui ou non
    //En attendant ce sera un coup sur deux
    private fun handlerResolveOuiNonMajTel(contexts: Map<String, String?>): Map<String, String?> {
        val outputContexts = mutableMapOf<String, String?>()

        if(validateONMajTel) {
            outputContexts.put("RESOLVE_VERIF_TEL", "true")
        } else {
            outputContexts.put("TEL_KO", "true")
        }

        validateONMajTel = !validateONMajTel

        return outputContexts.toMap()
    }

    var validateONConfirmMajTel = true
    //Besoin d'avoir l'intent en input pour vérifier si le client a dit oui ou non
    //En attendant ce sera un coup sur deux
    private fun handlerResolveOuiNonConfirmMajTel(contexts: Map<String, String?>): Map<String, String?> {
        val outputContexts = mutableMapOf<String, String?>()

        if(validateONConfirmMajTel) {
            outputContexts.put("NEW_TEL", "true")
        } else {
            outputContexts.put("REFUSE_UPDATE_TEL", "true")
        }

        validateONConfirmMajTel = !validateONConfirmMajTel

        return outputContexts.toMap()
    }

    var validateONMajMail = true
    //Besoin d'avoir l'intent en input pour vérifier si le client a dit oui ou non
    //En attendant ce sera un coup sur deux
    private fun handlerResolveOuiNonMajMail(contexts: Map<String, String?>): Map<String, String?> {
        val outputContexts = mutableMapOf<String, String?>()

        if(validateONMajMail) {
            outputContexts.put("RESOLVE_VERIF_MAIL", "true")
        } else {
            outputContexts.put("MAIL_KO", "true")
        }

        validateONMajMail = !validateONMajMail

        return outputContexts.toMap()
    }

    private fun handlerUpdateClientPhone(contexts: Map<String, String?>): Map<String, String?> {
        //Appel API maj tel client

        return mapOf("RESOLVE_VERIF_TEL" to "true")
    }

    private fun handlerSaveNewMail(contexts: Map<String, String?>): Map<String, String?> {
        //Appel API maj mail client

        return mapOf("SHOULD_RESOLVE_ON_MAJ_MAIL" to "true")
    }

    private fun handlerResolveVerifMail(contexts: Map<String, String?>): Map<String, String?> {
        //Appel API maj mail client

        return mapOf("RESOLVE_VERIF_MAIL" to "true")
    }

    private fun handlerCreateSinistre(contexts: Map<String, String?>): Map<String, String?> {
        //Appel API création du dossier de sinistre

        return mapOf("NUM_DOSSIER_SINISTRE" to "true")
    }

    private fun handlerGetClientInfosByContract(contexts: Map<String, String?>): Map<String, String?> {
        //Appel API récupération infos client par numéro de contrat

        val outputContexts = mutableMapOf<String, String?>()

        outputContexts.put("NOM_CLIENT", "Michel Barbeau")
        outputContexts.put("LABEL_VEHICULE", "Peugeot 3008")

        return outputContexts.toMap()
    }

    private fun handlerSendPartenaires(contexts: Map<String, String?>): Map<String, String?> {
        val villeSinistre = contexts.get("VILLE_SINISTRE")
        //Appel API récupération liste des partenaires à proximité

        //Appel API envoi de mail au client

        return mapOf("RESOLVE_SINISTRE" to "true")
    }

    private fun handlerSetResolveVerifTel(contexts: Map<String, String?>): Map<String, String?> {
        return mapOf("RESOLVE_VERIF_TEL" to "true")
    }

    private fun handlerSetResolveSinistre(contexts: Map<String, String?>): Map<String, String?> {
        return mapOf("RESOLVE_SINISTRE" to "true")
    }
}
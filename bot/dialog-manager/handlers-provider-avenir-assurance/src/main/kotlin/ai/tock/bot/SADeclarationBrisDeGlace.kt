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

class SADeclarationBrisDeGlace: ActionHandlersProvider {

    override fun getNameSpace() = HandlerNamespace.AVENIR_ASSURANCE

    enum class HandlerId {
        GET_CONTRACT_BY_IMMAT,
        SET_RESOLVE_VERIF_TEL,
        SET_RESOLVE_SINISTRE,
        SET_CLIENT_IDENTIFICATION_OK,
        SHOULD_CHECK_PHONE_AND_MAIL,
        CONFIRM_VERIF_MAIL_AND_TEL,
        VERIFIER_LES_DEUX,
        VALIDATE_CHECK_MAIL,
        RESOLVE_OUI_NON_MAJ_TEL,
        RESOLVE_OUI_NON_CONFIRM_MAJ_TEL,
        RESOLVE_OUI_NON_MAJ_MAIL,
        SAVE_NEW_MAIL,
        RESOLVE_VERIF_MAIL,
        CREATE_SINISTRE,
        GET_CLIENT_INFOS_BY_CONTRACT,
        SEND_PARTENAIRES,
        VALIDATE_CHECK_TEL,
        UPDATE_CLIENT_PHONE,
        VALIDATE_CONFIRM_MAJ_TEL,
        SET_END_RESOLVE_TEL,
        SET_END_RESOLVE_MAIL,
        CHECK_BESOIN_VERIF_TEL,
        CHECK_BESOIN_VERIF_MAIL,
    }
    enum class ContextName {
        BESOIN_VERIFIER_TEL,
        BESOIN_VERIFIER_MAIL,
        BESOIN_VERIFIER_MAIL_ET_TEL,
        BESOIN_VERIFIER_LES_DEUX,

        RESOLVE_VERIF_TEL,
        RESOLVE_VERIF_MAIL,
        RESOLVE_VALIDATE_CLIENT_PHONE,
        RESOLVE_VALIDATE_CLIENT_EMAIL,

        END_RESOLVE_TEL,
        END_RESOLVE_MAIL,

        IMMATRICULATION,
        NUM_CONTRAT,
        NUM_DOSSIER_SINISTRE,
        VILLE_SINISTRE,
        NOM_CLIENT,
        LABEL_VEHICULE,

        IDENT_CLIENT_OK,
        SHOULD_RESOLVE_OUI_NON_TEL,
        SHOULD_RESOLVE_ON_CONFIRM_MAJ_TEL,
        SHOULD_RESOLVE_ON_MAJ_MAIL,

        TEL_KO,
        MAIL_KO,
        NEW_TEL,
        REFUSE_UPDATE_TEL,

        RESOLVE_SINISTRE,
    }

    override fun getActionHandlers(): Set<ActionHandler> =
        setOf(
            createActionHandler(
                id = HandlerId.GET_CONTRACT_BY_IMMAT.name,
                description = "Get contract",
                inputContexts = setOf(ContextName.IMMATRICULATION.name),
                outputContexts = setOf(ContextName.NUM_CONTRAT.name),
                handler = ::handlerGetContractByImmat
            ),
            createActionHandlerThatJustSetsContexts(
                HandlerId.SET_RESOLVE_VERIF_TEL,
                setOf(ContextName.RESOLVE_VERIF_TEL)
            ),
            createActionHandlerThatJustSetsContexts(
                HandlerId.SET_RESOLVE_SINISTRE,
                setOf(ContextName.RESOLVE_SINISTRE)
            ),
            createActionHandlerThatJustSetsContexts(
                HandlerId.SET_END_RESOLVE_TEL,
                setOf(ContextName.END_RESOLVE_TEL)
            ),
            createActionHandlerThatJustSetsContexts(
                HandlerId.SET_END_RESOLVE_MAIL,
                setOf(ContextName.END_RESOLVE_MAIL)
            ),
            createActionHandlerThatJustSetsContexts(
                HandlerId.SET_CLIENT_IDENTIFICATION_OK,
                setOf(ContextName.IDENT_CLIENT_OK)
            ),
            createActionHandlerThatJustSetsContexts(
                HandlerId.VALIDATE_CHECK_TEL,
                setOf(ContextName.SHOULD_RESOLVE_OUI_NON_TEL)
            ),
            createActionHandlerThatJustSetsContexts(
                HandlerId.VALIDATE_CONFIRM_MAJ_TEL,
                setOf(ContextName.SHOULD_RESOLVE_ON_CONFIRM_MAJ_TEL)
            ),
            createActionHandlerThatJustSetsContexts(
                HandlerId.VALIDATE_CHECK_MAIL,
                setOf(ContextName.SHOULD_RESOLVE_ON_MAJ_MAIL)
            ),
            createActionHandler(
                id = HandlerId.CHECK_BESOIN_VERIF_TEL.name,
                outputContexts = setOf(
                    ContextName.BESOIN_VERIFIER_TEL.name,
                    ContextName.RESOLVE_VERIF_TEL.name
                ),
                handler = ::handlerCheckBesoinVerifTel
            ),
            createActionHandler(
                id = HandlerId.CHECK_BESOIN_VERIF_MAIL.name,
                outputContexts = setOf(
                    ContextName.BESOIN_VERIFIER_MAIL.name,
                    ContextName.RESOLVE_VERIF_MAIL.name
                ),
                handler = ::handlerCheckBesoinVerifMail
            ),
            createActionHandler(
                id = HandlerId.UPDATE_CLIENT_PHONE.name,
                outputContexts = setOf(
                    ContextName.RESOLVE_VERIF_TEL.name
                ),
                handler = ::handlerUpdateClientPhone
            ),
            createActionHandler(
                id = HandlerId.SHOULD_CHECK_PHONE_AND_MAIL.name,
                outputContexts = setOf(
                    ContextName.BESOIN_VERIFIER_MAIL_ET_TEL.name,
                    ContextName.END_RESOLVE_MAIL.name,
                    ContextName.END_RESOLVE_TEL.name
                ),
                handler = ::handlerShouldCheckPhoneAndMail
            ),
            createActionHandlerThatJustSetsContexts(
                HandlerId.CONFIRM_VERIF_MAIL_AND_TEL,
                setOf(ContextName.BESOIN_VERIFIER_LES_DEUX)
            ),
            createActionHandlerThatJustSetsContexts(
                HandlerId.VERIFIER_LES_DEUX,
                setOf(
                    ContextName.BESOIN_VERIFIER_TEL,
                    ContextName.BESOIN_VERIFIER_MAIL
                )
            ),
            createActionHandler(
                id = HandlerId.SAVE_NEW_MAIL.name,
                outputContexts = setOf(ContextName.SHOULD_RESOLVE_ON_MAJ_MAIL.name),
                handler = ::handlerSaveNewMail
            ),
            createActionHandler(
                id = HandlerId.RESOLVE_OUI_NON_MAJ_TEL.name,
                outputContexts = setOf(
                    ContextName.RESOLVE_VERIF_TEL.name,
                    ContextName.TEL_KO.name
                ),
                handler = ::handlerResolveOuiNonMajTel
            ),
            createActionHandler(
                id = HandlerId.RESOLVE_OUI_NON_CONFIRM_MAJ_TEL.name,
                outputContexts = setOf(
                    ContextName.NEW_TEL.name,
                    ContextName.REFUSE_UPDATE_TEL.name
                ),
                handler = ::handlerResolveOuiNonConfirmMajTel
            ),
            createActionHandler(
                id = HandlerId.RESOLVE_OUI_NON_MAJ_MAIL.name,
                outputContexts = setOf(
                    ContextName.RESOLVE_VERIF_MAIL.name,
                    ContextName.MAIL_KO.name
                ),
                handler = ::handlerResolveOuiNonMajMail
            ),
            createActionHandler(
                id = HandlerId.SEND_PARTENAIRES.name,
                inputContexts = setOf(ContextName.VILLE_SINISTRE.name),
                outputContexts = setOf(ContextName.RESOLVE_SINISTRE.name),
                handler = ::handlerSendPartenaires
            ),
            createActionHandler(
                id = HandlerId.RESOLVE_VERIF_MAIL.name,
                outputContexts = setOf(ContextName.RESOLVE_VERIF_MAIL.name),
                handler = ::handlerResolveVerifMail
            ),
            createActionHandler(
                id = HandlerId.CREATE_SINISTRE.name,
                outputContexts = setOf(ContextName.NUM_DOSSIER_SINISTRE.name),
                handler = ::handlerCreateSinistre
            ),
            createActionHandler(
                id = HandlerId.GET_CLIENT_INFOS_BY_CONTRACT.name,
                outputContexts = setOf(
                    ContextName.NOM_CLIENT.name,
                    ContextName.LABEL_VEHICULE.name,
                ),
                handler = ::handlerGetClientInfosByContract
            ),
        )


    var besoinVerifTel = true

    private fun handlerCheckBesoinVerifTel(contexts: Map<String, String?>): Map<String, String?> {
        val outputContexts = mutableMapOf<String, String?>()

        if(besoinVerifTel){
            outputContexts.put("BESOIN_VERIFIER_TEL", "true")
        } else {
            outputContexts.put("RESOLVE_VERIF_TEL", "true")
        }
        
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
        return outputContexts.toMap()
    }
    

    private fun handlerGetContractByImmat(contexts: Map<String, String?>): Map<String, String?> {
        val immat = contexts.get("IMMATRICULATION")
        //Appel à l'API permettant d'envoyer la réclamation

        return mapOf("NUM_CONTRAT" to "74125825")
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

    private fun handlerSetResolveSinistre(contexts: Map<String, String?>): Map<String, String?> {
        return mapOf("RESOLVE_SINISTRE" to "true")
    }

    private fun createActionHandlerThatJustSetsContexts(handlerId : HandlerId, contexts: Set<ContextName>) =
        createActionHandler(
            id = handlerId.name,
            description = "Handler that just sets <${contexts.map { it.name }.joinToString(", ")}>",
            outputContexts = contexts.map { it.name }.toSet(),
            handler = { contexts.associate { it.name to null } }
        )
}
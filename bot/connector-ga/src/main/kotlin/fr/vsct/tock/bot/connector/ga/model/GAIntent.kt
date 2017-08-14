/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.connector.ga.model

import com.fasterxml.jackson.annotation.JsonValue

/**
 *
 */
enum class GAIntent(
        @get:JsonValue val value: String,
        val type: GAInputValueDataType? = null) {

    main("actions.intent.MAIN"),
    text("actions.intent.TEXT"),
    option("actions.intent.OPTION", GAInputValueDataType.option),
    confirmation("actions.intent.CONFIRMATION", GAInputValueDataType.confirmation),
    transactionRequirementsCheck("actions.intent.TRANSACTION_REQUIREMENTS_CHECK", GAInputValueDataType.transactionRequirementsCheck),
    deliveryAddress("actions.intent.DELIVERY_ADDRESS", GAInputValueDataType.deliveryAddress),
    transactionDecision("actions.intent.TRANSACTION_DECISION", GAInputValueDataType.transactionDecision),
    permission("actions.intent.PERMISSION"),
    datetime("actions.intent.DATETIME"),
    signIn("actions.intent.SIGN_IN");

    companion object {
        fun findIntent(name: String): GAIntent? {
            return GAIntent.values().firstOrNull { it.value == name }
        }
    }

}
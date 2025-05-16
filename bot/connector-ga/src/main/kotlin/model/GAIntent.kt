/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
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

package ai.tock.bot.connector.ga.model

import com.fasterxml.jackson.annotation.JsonValue

/**
 *
 */
enum class GAIntent(
    @get:JsonValue val value: String,
    val type: GAInputValueDataType? = null
) {

    main("actions.intent.MAIN"),
    text("actions.intent.TEXT"),
    cancel("actions.intent.CANCEL"),
    noInput("actions.intent.NO_INPUT"),
    option("actions.intent.OPTION", GAInputValueDataType.option),
    confirmation("actions.intent.CONFIRMATION", GAInputValueDataType.confirmation),
    transactionRequirementsCheckV3("actions.intent.TRANSACTION_REQUIREMENTS_CHECK", GAInputValueDataType.transactionRequirementsCheckV3),
    deliveryAddress("actions.intent.DELIVERY_ADDRESS", GAInputValueDataType.deliveryAddress),
    transactionDecisionV3("actions.intent.TRANSACTION_DECISION", GAInputValueDataType.transactionDecisionV3),
    permission("actions.intent.PERMISSION", GAInputValueDataType.permission),
    datetime("actions.intent.DATETIME", GAInputValueDataType.datetime),
    signIn("actions.intent.SIGN_IN"),
    newSurface("actions.intent.NEW_SURFACE", GAInputValueDataType.newSurface),
    mediaStatus("actions.intent.MEDIA_STATUS", GAInputValueDataType.mediaStatus);

    companion object {
        fun findIntent(name: String): GAIntent? {
            return GAIntent.values().firstOrNull { it.value == name }
        }
    }
}

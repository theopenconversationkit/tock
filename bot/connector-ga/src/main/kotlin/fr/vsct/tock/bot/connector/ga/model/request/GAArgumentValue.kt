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

package fr.vsct.tock.bot.connector.ga.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "@type")
@JsonSubTypes(
        JsonSubTypes.Type(value = GATransactionRequirementsCheckResult::class, name = "type.googleapis.com/google.actions.v2.TransactionRequirementsCheckResult"),
        JsonSubTypes.Type(value = GATransactionDecisionValue::class, name = "type.googleapis.com/google.actions.v2.TransactionDecisionValue"),
        JsonSubTypes.Type(value = GAHoldValue::class, name = "type.googleapis.com/google.actions.v2.HoldValue"),
        JsonSubTypes.Type(value = GASignInValue::class, name = "type.googleapis.com/google.actions.v2.SignInValue")
)
abstract class  GAArgumentValue(@get:JsonProperty("@type") val type: GAArgumentValueType) {
}
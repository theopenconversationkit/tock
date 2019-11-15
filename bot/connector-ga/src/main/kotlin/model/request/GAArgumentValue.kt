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

package ai.tock.bot.connector.ga.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import ai.tock.bot.connector.ga.model.request.transaction.v3.GATransactionDecisionValueV3

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "@type")
@JsonSubTypes(
        JsonSubTypes.Type(value = GATransactionDecisionValueV3::class, name = "type.googleapis.com/google.actions.transactions.v3.TransactionDecisionValue"),
        JsonSubTypes.Type(value = GAHoldValue::class, name = "type.googleapis.com/google.actions.v2.HoldValue"),
        JsonSubTypes.Type(value = GASignInValue::class, name = "type.googleapis.com/google.actions.v2.SignInValue"),
        JsonSubTypes.Type(value = GANewSurfaceValue::class, name = "type.googleapis.com/google.actions.v2.NewSurfaceValue")
)
abstract class  GAArgumentValue(@get:JsonProperty("@type") val type: GAArgumentValueType) {
}
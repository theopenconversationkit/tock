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

package ai.tock.bot.connector.ga.model.response

import ai.tock.bot.connector.ga.model.GAInputValueDataType
import ai.tock.bot.connector.ga.model.response.transaction.v3.GATransactionDecisionValueSpecV3
import ai.tock.bot.connector.ga.model.response.transaction.v3.GATransactionRequirementsCheckSpecV3
import ai.tock.bot.engine.message.GenericMessage
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

/**
 *
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "@type",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = GAOptionValueSpec::class, name = "type.googleapis.com/google.actions.v2.OptionValueSpec"),
    JsonSubTypes.Type(value = GAPermissionValueSpec::class, name = "type.googleapis.com/google.actions.v2.PermissionValueSpec"),
    JsonSubTypes.Type(value = GATransactionRequirementsCheckSpecV3::class, name = "type.googleapis.com/google.actions.transactions.v3.TransactionRequirementsCheckSpec"),
    JsonSubTypes.Type(value = GATransactionDecisionValueSpecV3::class, name = "type.googleapis.com/google.actions.transactions.v3.TransactionDecisionValueSpec"),
    JsonSubTypes.Type(value = GANewSurfaceValueSpec::class, name = "type.googleapis.com/google.actions.v2.NewSurfaceValueSpec"),
)
abstract class GAInputValueData(
    @get:JsonProperty("@type") val type: GAInputValueDataType,
) {
    open fun toGenericMessage(): GenericMessage? = null
}

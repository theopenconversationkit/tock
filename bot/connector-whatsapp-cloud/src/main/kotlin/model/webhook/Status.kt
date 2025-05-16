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

package ai.tock.bot.connector.whatsapp.cloud.model.webhook

import ai.tock.bot.connector.whatsapp.cloud.model.common.Error
import com.fasterxml.jackson.annotation.JsonProperty

data class Status(
        @JsonProperty("id") val id: String,
        @JsonProperty("conversation") val conversation: Conversation?,
        @JsonProperty("pricing") val pricing: Pricing?,
        @JsonProperty("recipient_id") val recipientId: String,
        @JsonProperty("status") val status: MessageStatus,
        @JsonProperty("timestamp") val timestamp: String,
        @JsonProperty("errors") val errors: List<Error> = emptyList()
)

data class Conversation(
        @JsonProperty("expiration_timestamp") val expirationTimestamp: String?,
        @JsonProperty("origin") val origin: Origin,
        @JsonProperty("id") val id: String
)

data class Origin (
        @JsonProperty("type")val type : String
)

data class Pricing(
        @JsonProperty("pricing_model") val pricingModel: String,
        @JsonProperty("category") val category: String,
)

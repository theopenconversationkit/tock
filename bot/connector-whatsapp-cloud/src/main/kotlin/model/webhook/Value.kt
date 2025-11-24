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

import ai.tock.bot.connector.whatsapp.cloud.model.webhook.message.WhatsAppCloudMessage
import com.fasterxml.jackson.annotation.JsonProperty

data class Value(
    @JsonProperty("messaging_product") val messagingProduct: String,
    @JsonProperty("metadata") val metadata: Metadata,
    @JsonProperty("contacts") val contacts: List<Contact> = emptyList(),
    @JsonProperty("messages") val messages: List<WhatsAppCloudMessage> = emptyList(),
    @JsonProperty("statuses") val statuses: List<Status> = emptyList(),
    @JsonProperty("errors") val errors: List<Error> = emptyList(),
)

data class Metadata(
    @JsonProperty("display_phone_number") val displayPhoneNumber: String,
    @JsonProperty("phone_number_id") val phoneNumberId: String,
)

data class Contact(
    @JsonProperty("profile") val profile: Profile,
    @JsonProperty("wa_id") val waId: String,
)

data class Profile(
    @JsonProperty("name") val name: String,
)

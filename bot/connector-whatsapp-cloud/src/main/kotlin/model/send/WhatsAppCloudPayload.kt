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

package ai.tock.bot.connector.whatsapp.cloud.model.send

import com.fasterxml.jackson.annotation.JsonProperty

data class SendSuccessfulResponse(
        @JsonProperty("messaging_product") val messagingProduct: String,
        val contacts: List<Contact>,
        val messages: List<Message>
)

data class Contact(
        val input: String,
        @JsonProperty("wa_id") val waId: String
)

data class Message(
        val id: String
)

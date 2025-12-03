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

package ai.tock.bot.connector.whatsapp.cloud.model.webhook.message.content

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL) // Exclut les champs null de la sérialisation JSON
data class SystemContent(
    val body: String,
    val identity: String?,
    // Pour les versions de webhook v11.0 et antérieures
    @JsonProperty("new_wa_id") val newWaIdV11: String?,
    // Pour les versions de webhook v12.0 et ultérieures
    @JsonProperty("wa_id") val waIdV12: String?,
    val type: SystemUpdateType,
    val customer: String?,
)

enum class SystemUpdateType(val typeName: String) {
    CUSTOMER_CHANGED_NUMBER("customer_changed_number"),
    CUSTOMER_IDENTITY_CHANGED("customer_identity_changed"),
    ;

    companion object {
        fun fromTypeName(typeName: String): SystemUpdateType? = values().firstOrNull { it.typeName == typeName }
    }
}

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

package ai.tock.bot.connector.whatsapp.model.webhook

import com.fasterxml.jackson.annotation.JsonProperty

enum class WhatsAppParameterType {
    text, image, payload, currency, date_time,
}

data class WhatsAppParameter(
    val type: WhatsAppParameterType,
    val text: String? = null,
    val image: WhatsAppImageParameter? = null,
    val currency: String? = null,
    val payload: String? = null
)

data class WhatsAppImageParameter(val link: String)

data class WhatsAppCurrencyParameter(
    @get:JsonProperty("fallback_value")
    val fallbackValue: String,
    val code: String,
    @get:JsonProperty("amount_1000")
    val amount: Int
)

data class WhatsAppDateTimeParameter(
    @get:JsonProperty("fallback_value")
    val fallbackValue: String,
    @get:JsonProperty("day_of_week")
    val dayOfWeek: Int? = null,
    @get:JsonProperty("day_of_month")
    val dayOfMonth: Int? = null,
    val year: Int? = null,
    val month: Int? = null,
    val hour: Int? = null,
    val minute: Int? = null,
    val timestamp: Int? = null
)

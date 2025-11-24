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

package ai.tock.bot.connector.whatsapp.cloud.model.send.message

import com.fasterxml.jackson.annotation.JsonProperty

data class WhatsAppCloudBotMessageOld(
    @JsonProperty("messaging_product") val messagingProduct: String,
    @JsonProperty("to") val recipient: String,
    val type: String,
    val text: TextMessage? = null,
    val reaction: ReactionMessage? = null,
    val image: ImageMessage? = null,
    val location: LocationMessage? = null,
    val interactive: InteractiveMessage? = null,
    val contacts: ContactsMessageList? = null,
)

data class TextMessage(
    val body: String,
)

data class ReactionMessage(
    @JsonProperty("message_id") val messageId: String,
    val emoji: String,
)

data class ImageMessage(
    // use link if image is in public server
    val link: String,
)

data class LocationMessage(
    val longitude: Double,
    val latitude: Double,
    val name: String?,
    val address: String?,
)

data class InteractiveMessage(
    val body: String,
)

// ************* Contacts message *************

data class ContactsMessageList(
    val contacts: List<Contact>,
)

data class Contact(
    val addresses: List<Address>,
    val birthday: String,
    val emails: List<Email>,
    @JsonProperty("name")
    val name: Name,
    val org: Organization,
    val phones: List<Phone>,
    val urls: List<Url>,
)

data class Address(
    val street: String,
    val city: String,
    val state: String,
    val zip: String,
    val country: String,
    @JsonProperty("country_code")
    val countryCode: String,
    val type: String,
)

data class Email(
    val email: String,
    val type: String,
)

data class Name(
    @JsonProperty("formatted_name")
    val formattedName: String,
    @JsonProperty("first_name")
    val firstName: String,
    @JsonProperty("last_name")
    val lastName: String,
    @JsonProperty("middle_name")
    val middleName: String?,
    val suffix: String?,
    val prefix: String?,
)

data class Organization(
    val company: String,
    val department: String?,
    val title: String?,
)

data class Phone(
    val phone: String,
    val type: String,
    @JsonProperty("wa_id")
    val waId: String?,
)

data class Url(
    val url: String,
    val type: String,
)

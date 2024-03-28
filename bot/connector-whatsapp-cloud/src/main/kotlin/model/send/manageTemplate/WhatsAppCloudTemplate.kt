/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.bot.connector.whatsapp.cloud.model.send.manageTemplate

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName

enum class LanguageCode {
    en_US, fr
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
        JsonSubTypes.Type(value = BodyComponent::class, name = "BODY"),
        JsonSubTypes.Type(value = CarouselComponent::class, name = "CAROUSEL")
)
sealed class TemplateComponent

@JsonTypeName("BODY")
data class BodyComponent(
        val text: String,
        val example: BodyExample? = null
) : TemplateComponent()

data class BodyExample(val body_text: List<List<String>>)

@JsonTypeName("CAROUSEL")
data class CarouselComponent(
        val cards: List<Card>
) : TemplateComponent()

data class Card(val components: List<CardComponent>)

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
        JsonSubTypes.Type(value = HeaderComponent::class, name = "HEADER"),
        JsonSubTypes.Type(value = Body::class, name = "BODY"),
        JsonSubTypes.Type(value = ButtonsComponent::class, name = "BUTTONS")
)
sealed class CardComponent

@JsonTypeName("HEADER")
data class HeaderComponent(
        val format: String,
        val example: HeaderExample? = null
) : CardComponent()

data class HeaderExample(
        @JsonProperty("header_handle")
        val headerHandle: List<String>
)

@JsonTypeName("BODY")
data class Body(
        val text: String,
        val example: BodyExample
) : CardComponent()

@JsonTypeName("BUTTONS")
data class ButtonsComponent(val buttons: List<Button>) : CardComponent()

data class Button(
        val type: String,
        val text: String,
        val url: String? = null, // Seulement pour le type URL
        val example: List<String>? = null
)

data class WhatsAppCloudTemplate(
        val name: String,
        val language: LanguageCode,
        val category: String,
        val components: List<TemplateComponent>
)
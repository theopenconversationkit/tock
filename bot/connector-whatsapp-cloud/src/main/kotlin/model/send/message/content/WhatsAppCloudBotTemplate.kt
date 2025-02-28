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

package ai.tock.bot.connector.whatsapp.cloud.model.send.message.content

import com.fasterxml.jackson.annotation.JsonProperty

enum class ComponentType { BODY, CAROUSEL, HEADER, BUTTON }
enum class ParameterType { TEXT, IMAGE, VIDEO, PAYLOAD }
enum class ButtonSubType { QUICK_REPLY, URL, PHONE_NUMBER }
data class WhatsAppCloudBotTemplate(
    val name: String,
    val language: Language,
    val components: List<Component>
)

data class Language(@JsonProperty("code") val code: String)

// TODO (breaking) finish renaming Component to WhatsappTemplateComponent
typealias WhatsappTemplateComponent = Component
sealed class Component {
    abstract val type: ComponentType

    data class Body(
        override val type: ComponentType = ComponentType.BODY,
        val parameters: List<TextParameter>
    ) : Component()

    data class Header(
        override val type: ComponentType = ComponentType.HEADER,
        val parameters: List<HeaderParameter>
    ) : Component()

    data class Button(
        override val type: ComponentType = ComponentType.BUTTON,
        @JsonProperty("sub_type") val subType: ButtonSubType,
        val index: String,
        val parameters: List<PayloadParameter>
    ) : Component()

    data class Carousel(
        override val type: ComponentType = ComponentType.CAROUSEL,
        val cards: List<Card>
    ) : Component()

    data class Card(
        @JsonProperty("card_index") val cardIndex: Int,
        val components: List<Component>
    )
}


data class TextParameter(val type: ParameterType, val text: String)
data class PayloadParameter(val type: ParameterType, val payload: String?, val text: String)


sealed class HeaderParameter {
    data class Image(
        val type: ParameterType,
        val image: ImageId
    ) : HeaderParameter()

    data class Video(
        val type: ParameterType,
        val video: VideoId
    ) : HeaderParameter()
}

data class ImageId(@JsonProperty("id") var id: String?)
data class VideoId(@JsonProperty("id") val id: String)
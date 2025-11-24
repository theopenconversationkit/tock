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

package ai.tock.bot.connector.whatsapp.cloud.model.template

import ai.tock.bot.connector.whatsapp.cloud.model.common.MetaUploadHandle
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.annotation.JsonValue

data class WhatsappTemplate(
    val name: String,
    val language: String,
    val components: List<TemplateComponent>,
    val category: WhatsappTemplateCategory = WhatsappTemplateCategory.MARKETING,
    val id: String? = null,
    val status: WhatsappTemplateStatus? = null,
) {
    constructor(name: String, language: String, vararg components: TemplateComponent) : this(
        name,
        language,
        listOf(*components),
    )

    fun contentEquals(other: WhatsappTemplate): Boolean {
        if (name == other.name && language == other.language) {
            for (i in components.indices) {
                if (!components[i].looselyEquals(other.components[i])) {
                    return false
                }
            }
            return true
        }
        return false
    }
}

enum class WhatsappTemplateCategory {
    AUTHENTICATION,
    MARKETING,
    UTILITY,
}

enum class WhatsappTemplateStatus {
    PENDING,
    APPROVED,
    REJECTED,
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
sealed class TemplateComponent {
    open fun looselyEquals(other: TemplateComponent) = equals(other)
}

enum class TemplateHeaderFormat {
    TEXT,
    LOCATION,
    IMAGE,
    VIDEO,
    DOCUMENT,
}

@JsonTypeName("HEADER")
data class TemplateHeader(val format: TemplateHeaderFormat, val text: String? = null, val example: BodyExample? = null) : TemplateComponent() {
    companion object {
        fun text(
            text: String,
            example: BodyExample? = null,
        ) = TemplateHeader(TemplateHeaderFormat.TEXT, text, example)

        fun location() = TemplateHeader(TemplateHeaderFormat.LOCATION)

        fun image(imageHandle: MetaUploadHandle) = TemplateHeader(TemplateHeaderFormat.IMAGE, example = BodyExample(imageHandle.value))

        fun video(videoHandle: MetaUploadHandle) = TemplateHeader(TemplateHeaderFormat.VIDEO, example = BodyExample(videoHandle.value))

        fun document(documentHandle: MetaUploadHandle) = TemplateHeader(TemplateHeaderFormat.DOCUMENT, example = BodyExample(documentHandle.value))
    }
}

@JsonTypeName("BODY")
data class TemplateBody(val text: String, val example: BodyExample? = null) : TemplateComponent() {
    constructor(text: String, singleVariableExample: String) : this(text, BodyExample(singleVariableExample))
    constructor(text: String, vararg variableExamples: String) : this(text, BodyExample(*variableExamples))
}

@JsonTypeName("FOOTER")
data class TemplateFooter(val text: String) : TemplateComponent()

@JsonTypeName("BUTTONS")
data class TemplateButtons(val buttons: List<WhatsappTemplateButton>) : TemplateComponent() {
    constructor(vararg buttons: WhatsappTemplateButton) : this(listOf(*buttons))
}

data class BodyExample(
    @JsonProperty("body_text") val bodyText: List<List<TemplateExampleVariable>>,
) {
    constructor(vararg examples: List<String>) : this(examples.map { variables -> variables.map { TemplateExampleVariable.Positional(it) } })
    constructor(vararg exampleValues: String) : this(listOf(exampleValues.map { TemplateExampleVariable.Positional(it) }))
    constructor(vararg exampleValues: Pair<String, String>) : this(listOf(exampleValues.map { (key, value) -> TemplateExampleVariable.Named(key, value) }))
}

sealed interface TemplateExampleVariable {
    data class Positional(
        @JsonValue val value: String,
    ) : TemplateExampleVariable

    data class Named(
        @JsonProperty("param_name") val name: String,
        val example: String,
    ) : TemplateExampleVariable

    companion object {
        @JsonCreator
        @JvmStatic
        fun readPositional(value: String): TemplateExampleVariable = Positional(value)

        @JsonCreator
        @JvmStatic
        fun readNamed(value: Named): TemplateExampleVariable = value
    }
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
sealed interface WhatsappTemplateButton {
    val text: String
}

@JsonTypeName("QUICK_REPLY")
data class TemplateQuickReply(override val text: String) : WhatsappTemplateButton

@JsonTypeName("URL")
data class TemplateUrlButton(override val text: String, val url: String) : WhatsappTemplateButton

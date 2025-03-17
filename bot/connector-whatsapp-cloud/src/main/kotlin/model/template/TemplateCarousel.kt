package ai.tock.bot.connector.whatsapp.cloud.model.template

import ai.tock.bot.connector.whatsapp.cloud.model.common.MetaUploadHandle
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.annotation.JsonValue

@JsonTypeName("CAROUSEL")
data class TemplateCarousel(val cards: List<TemplateCard>) : TemplateComponent() {
    constructor(vararg cards: TemplateCard) : this(listOf(*cards))

    override fun looselyEquals(other: TemplateComponent): Boolean {
        if (other !is TemplateCarousel) return false
        for (i in cards.indices) {
            if (!cards[i].looselyEquals(other.cards[i])) {
                return false
            }
        }
        return true
    }
}

data class TemplateCard(val components: List<TemplateCardComponent>) {
    constructor(
        header: TemplateCardHeader,
        body: TemplateCardBody,
        firstButton: WhatsappTemplateButton,
        secondButton: WhatsappTemplateButton? = null,
    ) : this(listOf(header, body, TemplateCardButtons(firstButton, secondButton)))

    fun looselyEquals(other: TemplateCard): Boolean {
        for (i in components.indices) {
            if (!components[i].looselyEquals(other.components[i])) {
                return false
            }
        }
        return true
    }
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
sealed class TemplateCardComponent {
    open fun looselyEquals(other: TemplateCardComponent) = equals(other)
}

@JsonTypeName("HEADER")
data class TemplateCardHeader private constructor(val format: HeaderFormat, val example: Map<String, List<String>>): TemplateCardComponent() {
    constructor(format: HeaderFormat, imageHandle: MetaUploadHandle) : this(format, mapOf("header_handle" to listOf(imageHandle.value)))

    override fun looselyEquals(other: TemplateCardComponent): Boolean {
        // Image IDs are brittle, and since they are only examples, we do not have to always update them
        return other is TemplateCardHeader && format == other.format && (format == HeaderFormat.IMAGE || example == other.example)
    }

    companion object {
        fun image(handle: MetaUploadHandle) = TemplateCardHeader(HeaderFormat.IMAGE, handle)
        fun video(handle: MetaUploadHandle) = TemplateCardHeader(HeaderFormat.VIDEO, handle)
    }
}

enum class HeaderFormat(@JsonValue val id: String) {
    IMAGE("IMAGE"), VIDEO("VIDEO"),
}

@JsonTypeName("BODY")
data class TemplateCardBody(
    /**
     * Message body text. Supports variables.
     *
     * Required.
     *
     * Maximum 1024 characters.
     */
    val text: String,
    /**
     * Message body text example variable string(s).
     *
     * Required if message body text string uses variables.
     * Number of strings must match the number of variable placeholders in the message body text string.
     *
     * If message body text uses a single variable, body_text value can be a string, otherwise it must be an array containing an array of strings.
     */
    val example: BodyExample?
) : TemplateCardComponent() {
    constructor(text: String) : this(text, null)
    constructor(text: String, vararg textVariableExamples: String) : this(text, BodyExample(*textVariableExamples))
}

@JsonTypeName("BUTTONS")
data class TemplateCardButtons private constructor(val buttons: List<WhatsappTemplateButton>) : TemplateCardComponent() {
    constructor(first: WhatsappTemplateButton, second: WhatsappTemplateButton? = null) : this(listOfNotNull(first, second))
}

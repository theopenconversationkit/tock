package ai.tock.bot.connector.web

import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.web.send.Button
import ai.tock.bot.connector.web.send.WebCard
import ai.tock.bot.connector.web.send.WebCarousel
import ai.tock.bot.engine.message.GenericMessage
import ai.tock.shared.mapNotNullValues
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class WebMessage(
    val text: String? = null,
    val buttons: List<Button> = emptyList(),
    val card: WebCard? = null,
    val carousel: WebCarousel? = null,
    val version: String = "1"
) : WebConnectorMessage {

    @get:JsonIgnore
    override val connectorType: ConnectorType = webConnectorType

    override fun toGenericMessage(): GenericMessage? =
        card?.toGenericMessage()
            ?: carousel?.toGenericMessage()
            ?: GenericMessage(
                connectorType = webConnectorType,
                texts = mapNotNullValues(GenericMessage.TEXT_PARAM to text),
                choices = buttons.map { it.toChoice() }
            )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WebMessage) return false

        if (text != other.text) return false
        if (version != other.version) return false

        if (buttons != other.buttons) return false
        if (card != other.card) return false
        if (carousel != other.carousel) return false
        return true
    }

    override fun hashCode(): Int {
        var result = text?.hashCode() ?: 0
        result = 31 * result + buttons.hashCode()
        result = 31 * result + (card?.hashCode() ?: 0)
        result = 31 * result + (carousel?.hashCode() ?: 0)
        result = 31 * result + version.hashCode()
        return result
    }
}

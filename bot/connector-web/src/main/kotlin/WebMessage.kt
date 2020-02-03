package ai.tock.bot.connector.web

import ai.tock.bot.connector.ConnectorMessage
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
) : ConnectorMessage {

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
}

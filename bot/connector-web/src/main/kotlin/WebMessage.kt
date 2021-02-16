package ai.tock.bot.connector.web

import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.web.send.Button
import ai.tock.bot.connector.web.send.WebCard
import ai.tock.bot.connector.web.send.WebCarousel
import ai.tock.bot.connector.web.send.WebImage
import ai.tock.bot.connector.web.send.WebMessageContent
import ai.tock.bot.connector.web.send.WebWidget
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
    val widget: WebWidget? = null,
    val image: WebImage? = null,
    val version: String = "1"
) : WebConnectorMessage {

    constructor(content: WebMessageContent) : this(
        content.text,
        content.buttons,
        content.card,
        content.carousel,
        content.widget,
        content.image,
        content.version
    )

    @get:JsonIgnore
    override val connectorType: ConnectorType = webConnectorType

    override fun toGenericMessage(): GenericMessage =
        card?.toGenericMessage()
            ?: carousel?.toGenericMessage()
            ?: widget?.toGenericMessage()
            ?: image?.toGenericMessage()
            ?: GenericMessage(
                connectorType = webConnectorType,
                texts = mapNotNullValues(GenericMessage.TEXT_PARAM to text),
                choices = buttons.map { it.toChoice() }
            )
}


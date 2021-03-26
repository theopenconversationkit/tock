package ai.tock.bot.connector.web

import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.web.send.Button
import ai.tock.bot.connector.web.send.WebCard
import ai.tock.bot.connector.web.send.WebCarousel
import ai.tock.bot.connector.web.send.WebDeepLink
import ai.tock.bot.connector.web.send.WebImage
import ai.tock.bot.connector.web.send.WebMessageContent
import ai.tock.bot.connector.web.send.WebMessageContract
import ai.tock.bot.connector.web.send.WebWidget
import ai.tock.bot.engine.message.GenericMessage
import ai.tock.shared.mapNotNullValues
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class WebMessage(
    override val text: String? = null,
    override val buttons: List<Button> = emptyList(),
    override val card: WebCard? = null,
    override val carousel: WebCarousel? = null,
    override val widget: WebWidget? = null,
    override val image: WebImage? = null,
    override val version: String = "1",
    override val deepLink: WebDeepLink? = null,
) : WebMessageContract, WebConnectorMessage {

    constructor(content: WebMessageContent) : this(
        content.text,
        content.buttons,
        content.card,
        content.carousel,
        content.widget,
        content.image,
        content.version,
        content.deepLink
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

import ai.tock.bot.connector.web.WebConnectorResponse
import ai.tock.bot.connector.web.WebMediaFile
import ai.tock.bot.connector.web.WebMessage
import ai.tock.bot.connector.web.send.PostbackButton
import ai.tock.bot.connector.web.send.QuickReply
import ai.tock.bot.connector.web.send.UrlButton
import ai.tock.bot.connector.web.send.WebCard
import ai.tock.bot.connector.web.send.WebCarousel
import ai.tock.bot.engine.action.SendAttachment
import ai.tock.shared.jackson.mapper
import ai.tock.shared.resourceAsStream
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class WebConnectorResponseTest {

    @Test
    fun `text only`() {
        val expected = WebConnectorResponse(
            responses = listOf(
                WebMessage(text = "Text only")
            )
        )
        val deserializedEvent = mapper.readValue<WebConnectorResponse>(resourceAsStream("/text_only.json"))
        Assertions.assertThat(deserializedEvent).isEqualTo(expected)
    }

    @Test
    fun `text with buttons`() {
        val expected = WebConnectorResponse(
            responses = listOf(
                WebMessage(
                    text = "Text with Buttons",
                    buttons = listOf(
                        PostbackButton(
                            title = "title",
                            payload = "payload"
                        ),
                        QuickReply(
                            title = "title",
                            payload = "payload",
                            imageUrl = null
                        ),
                        UrlButton(
                            title = "title",
                            url = "http://www.sncf.com"
                        )
                    )
                )

            )
        )
        val deserializedEvent = mapper.readValue<WebConnectorResponse>(resourceAsStream("/text_with_buttons.json"))
        Assertions.assertThat(deserializedEvent).isEqualTo(expected)
    }

    @Test
    fun `web card with buttons`() {
        val expected = WebConnectorResponse(
            responses = listOf(
                WebMessage(
                    card = WebCard(
                        title = "title",
                        subTitle = "subTitle",
                        file = WebMediaFile(
                            url = "http://www.sncf.com/image.png",
                            name = "imageName",
                            type = SendAttachment.AttachmentType.image
                        ),
                        buttons = listOf(
                            PostbackButton(
                                title = "title",
                                payload = "payload"
                            ),
                            UrlButton(
                                title = "title",
                                url = "http://www.sncf.com"
                            )
                        )
                    )
                )
            )
        )
        val deserializedEvent = mapper.readValue<WebConnectorResponse>(resourceAsStream("/card_with_buttons.json"))
        Assertions.assertThat(deserializedEvent).isEqualTo(expected)
    }

    @Test
    fun `carousel with two cards and one button`() {
        val expected = WebConnectorResponse(
            responses = listOf(
                WebMessage(
                    text = "carousel with two cards and one button",
                    carousel = WebCarousel(
                        cards = listOf(
                            WebCard(
                                title = "item 1",
                                subTitle = "subtitle 1",
                                file = WebMediaFile(
                                    url = "http://www.sncf.com/image1.png",
                                    name = "imageName 1",
                                    type = SendAttachment.AttachmentType.image
                                ),
                                buttons = listOf(
                                    PostbackButton(
                                        title = "choice item 1",
                                        payload = "payload"
                                    )
                                )
                            ),
                            WebCard(
                                title = "item 2",
                                subTitle = "subtitle 2",
                                file = WebMediaFile(
                                    url = "http://www.sncf.com/image2.png",
                                    name = "imageName 2",
                                    type = SendAttachment.AttachmentType.image
                                ),
                                buttons = listOf(
                                    PostbackButton(
                                        title = "choice item 2",
                                        payload = "payload"
                                    )
                                )
                            )
                        )
                    ),
                    buttons = listOf(
                        PostbackButton(
                            title = "refresh",
                            payload = "payload"
                        )
                    )
                )
            )
        )
        val deserializedEvent =
            mapper.readValue<WebConnectorResponse>(resourceAsStream("/carousel_with_2_cards_and_1_button.json"))
        Assertions.assertThat(deserializedEvent).isEqualTo(expected)
    }

}

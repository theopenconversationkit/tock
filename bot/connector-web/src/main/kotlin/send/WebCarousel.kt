package ai.tock.bot.connector.web.send

import ai.tock.bot.connector.media.MediaMessage
import ai.tock.bot.engine.message.GenericElement
import ai.tock.bot.engine.message.GenericMessage

/**
 * A list of [WebCard].
 */
data class WebCarousel(val cards: List<WebCard>) {
    fun toGenericMessage(): GenericMessage? =
        GenericMessage(subElements = cards.mapNotNull { it.toGenericMessage() }.map { GenericElement(it) })
}
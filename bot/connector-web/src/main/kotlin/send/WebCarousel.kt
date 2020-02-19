package ai.tock.bot.connector.web.send

import ai.tock.bot.engine.message.GenericElement
import ai.tock.bot.engine.message.GenericMessage

/**
 * A list of [WebCard].
 */
data class WebCarousel(val cards: List<WebCard>) {
    fun toGenericMessage(): GenericMessage? =
        GenericMessage(subElements = cards.mapNotNull { it.toGenericMessage() }.map { GenericElement(it) })

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WebCarousel) return false
        if (cards != other.cards) return false
        return true
    }

    override fun hashCode(): Int {
        return cards.hashCode()
    }
}
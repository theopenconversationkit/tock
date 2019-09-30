package ai.tock.bot.connector.businesschat.model.input

import ai.tock.bot.engine.BotBus

/**
 * A Rich link used on the bot side to be sent on the [BotBus]
 */
data class BusinessChatConnectorRichLinkMessage(
    override val sourceId: String,
    override val destinationId: String,
    val url: String,
    val title: String,
    val image: ByteArray,
    val mimeType: String
) : BusinessChatConnectorMessage() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BusinessChatConnectorRichLinkMessage

        if (sourceId != other.sourceId) return false
        if (destinationId != other.destinationId) return false
        if (url != other.url) return false
        if (title != other.title) return false
        if (!image.contentEquals(other.image)) return false
        if (mimeType != other.mimeType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sourceId.hashCode()
        result = 31 * result + destinationId.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + image.contentHashCode()
        result = 31 * result + mimeType.hashCode()
        return result
    }
}
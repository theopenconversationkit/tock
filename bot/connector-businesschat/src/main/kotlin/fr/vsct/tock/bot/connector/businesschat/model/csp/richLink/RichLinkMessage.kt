package fr.vsct.tock.bot.connector.businesschat.model.csp.richLink

import fr.vsct.tock.bot.connector.businesschat.model.common.MessageType
import fr.vsct.tock.bot.connector.businesschat.model.csp.BusinessChatCommonModel

class RichLinkMessage(
    sourceId: String,
    destinationId: String,
    val richLinkData: RichLinkData?
) : BusinessChatCommonModel(sourceId = sourceId, destinationId = destinationId, type = MessageType.richLink)

data class RichLinkData(
    val url: String,
    val title: String,
    val assets: Assets
)

data class Assets (
    val image: Image
)

data class Image (
    val data: ByteArray,
    val mimeType: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Image

        if (!data.contentEquals(other.data)) return false
        if (mimeType != other.mimeType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + mimeType.hashCode()
        return result
    }
}


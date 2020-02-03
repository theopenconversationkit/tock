package ai.tock.bot.connector.web.send

import ai.tock.bot.connector.media.MediaFile
import ai.tock.bot.engine.action.SendAttachment.AttachmentType.image
import ai.tock.bot.engine.message.Attachment
import ai.tock.bot.engine.message.GenericMessage
import ai.tock.shared.mapNotNullValues

data class WebCard(
    val title: CharSequence? = null,
    val subTitle: CharSequence? = null,
    val file: MediaFile? = null,
    val buttons: List<Button> = emptyList()
) {
    fun toGenericMessage(): GenericMessage? {
        return GenericMessage(
            choices = buttons.map { it.toChoice() },
            texts = mapNotNullValues(
                GenericMessage.TITLE_PARAM to title?.toString(),
                GenericMessage.SUBTITLE_PARAM to subTitle?.toString()
            ),
            attachments = file
                ?.let { listOf(Attachment(file.url, image)) }
                ?: emptyList()
        )
    }
}

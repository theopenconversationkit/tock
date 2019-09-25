package fr.vsct.tock.bot.connector.twitter.model

import fr.vsct.tock.bot.connector.media.MediaFile
import fr.vsct.tock.bot.connector.twitter.model.MediaCategory.GIF
import fr.vsct.tock.bot.connector.twitter.model.MediaCategory.IMAGE
import fr.vsct.tock.bot.connector.twitter.model.MediaCategory.VIDEO
import fr.vsct.tock.bot.engine.action.SendAttachment.AttachmentType.image
import fr.vsct.tock.bot.engine.action.SendAttachment.AttachmentType.video

enum class MediaCategory(val mediaCategory: String) {
    GIF("dm_gif"),
    IMAGE("dm_image"),
    VIDEO("dm_video")
}

fun MediaFile.toMediaCategory(): MediaCategory? =
    when (type) {
        image -> if (name.endsWith(".gif", true)) GIF else IMAGE
        video -> VIDEO
        else -> null
    }
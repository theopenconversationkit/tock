/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.bot.connector.web

import ai.tock.bot.connector.media.MediaFile
import ai.tock.bot.connector.web.send.Button
import ai.tock.bot.connector.web.send.PostbackButton
import ai.tock.bot.connector.web.send.QuickReply
import ai.tock.bot.connector.web.send.UrlButton
import ai.tock.bot.connector.web.send.WebCard
import ai.tock.bot.connector.web.send.WebCarousel
import ai.tock.bot.connector.web.send.WebImage
import ai.tock.bot.connector.web.send.WebMediaFile
import ai.tock.bot.connector.web.send.WebWidget
import ai.tock.bot.engine.action.SendAttachment.AttachmentType
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.action.SendChoice.Companion.EXIT_INTENT
import ai.tock.bot.engine.action.SendChoice.Companion.IMAGE_PARAMETER
import ai.tock.bot.engine.action.SendChoice.Companion.TITLE_PARAMETER
import ai.tock.bot.engine.action.SendChoice.Companion.URL_PARAMETER
import ai.tock.bot.engine.config.UploadedFilesService.attachmentType
import ai.tock.bot.engine.message.Attachment
import ai.tock.bot.engine.message.Choice
import ai.tock.bot.engine.message.GenericElement
import ai.tock.bot.engine.message.GenericMessage
import ai.tock.shared.mapNotNullValues

fun WebCard.toGenericMessage(): GenericMessage {
    return GenericMessage(
        choices = buttons.map { it.toChoice() },
        texts = mapNotNullValues(
            GenericMessage.TITLE_PARAM to title?.toString(),
            GenericMessage.SUBTITLE_PARAM to subTitle?.toString()
        ),
        attachments = file
            ?.let { listOf(Attachment(it.url, attachmentType(it.url))) }
            ?: emptyList()
    )
}

fun WebCarousel.toGenericMessage(): GenericMessage =
    GenericMessage(subElements = cards.map { it.toGenericMessage() }.map { GenericElement(it) })

fun WebImage.toGenericMessage(): GenericMessage {
    return GenericMessage(
        texts = mapNotNullValues(
            GenericMessage.TITLE_PARAM to title.toString(),
        ),
        attachments = listOf(Attachment(file.url, attachmentType(file.url)))
    )
}

fun WebWidget.toGenericMessage(): GenericMessage = GenericMessage(texts = mapOf("widget" to this.toString()))

fun Button.toChoice(): Choice =
    when (this) {
        is PostbackButton ->
            payload?.let { p ->
                SendChoice.decodeChoiceId(p).let { (intent, params) ->
                    Choice(
                        intent,
                        params +
                            mapNotNullValues(
                                TITLE_PARAMETER to title,
                                IMAGE_PARAMETER to imageUrl
                            )
                    )
                }
            } ?: Choice.fromText(text = title, nlpText = title, imageUrl = imageUrl)

        is QuickReply ->
            payload
                ?.let { p ->
                    SendChoice.decodeChoiceId(p)
                        .let { (intent, params) ->
                            Choice(
                                intent,
                                params +
                                    mapNotNullValues(
                                        TITLE_PARAMETER to title,
                                        IMAGE_PARAMETER to imageUrl
                                    )
                            )
                        }
                } ?: Choice.fromText(title, nlpText, imageUrl)

        is UrlButton ->
            Choice(
                EXIT_INTENT,
                mapNotNullValues(
                    TITLE_PARAMETER to title,
                    URL_PARAMETER to url,
                    IMAGE_PARAMETER to imageUrl
                )
            )

        else -> error("unsupported Button type: $this")
    }

fun WebMediaFile.toMediaFile(): MediaFile =
    MediaFile(url, name, attachmentType(type), description)

fun MediaFile.toWebMediaFile(): WebMediaFile =
    WebMediaFile(url, name, type, description?.toString())

fun WebMediaFile(
    url: String,
    name: String,
    type: AttachmentType = attachmentType(url),
    description: String? = null
): WebMediaFile =
    WebMediaFile(url, name, type.name, description)

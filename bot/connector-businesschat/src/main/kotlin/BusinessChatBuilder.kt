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

package ai.tock.bot.connector.businesschat

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.businesschat.model.input.BusinessChatConnectorImageMessage
import ai.tock.bot.connector.businesschat.model.input.BusinessChatConnectorListPickerMessage
import ai.tock.bot.connector.businesschat.model.input.BusinessChatConnectorMessage
import ai.tock.bot.connector.businesschat.model.input.BusinessChatConnectorRichLinkMessage
import ai.tock.bot.connector.businesschat.model.input.BusinessChatConnectorTextMessage
import ai.tock.bot.connector.businesschat.model.input.ListPickerItem
import ai.tock.bot.engine.Bus

/**
 * Adds a Business Chat [ConnectorMessage] if the current connector is Business Chat.
 * You need to call [BotBus.send] or [BotBus.end] later to send this message.
 */
fun <T : Bus<T>> T.withBusinessChat(messageProvider: () -> BusinessChatConnectorMessage): T {
    return withMessage(businessChatConnectorType, messageProvider)
}

/**
 * Creates a [BusinessChatText].
 *
 * @param text the text sent
 *
 */
fun <T : Bus<T>> T.businessChatText(
    text: CharSequence
): BusinessChatConnectorMessage =
    BusinessChatConnectorTextMessage(
        sourceId = botId.id,
        destinationId = userId.id,
        body = translate(text).toString()
    )

/**
 * Creates a [BusinessChatConnectorImageMessage]
 *
 * @param attachment an array of bytes containing an image
 * @param mimeType the mime type of the image, which is image/png by default
 */
fun <T : Bus<T>> T.businessChatAttachement(
    attachment: ByteArray,
    mimeType: String = "image/png"
): BusinessChatConnectorMessage =
    BusinessChatConnectorImageMessage(
        sourceId = botId.id,
        destinationId = userId.id,
        bytes = attachment,
        mimeType = mimeType
    )

/**
 * Creates a [BusinessChatListPicker].
 *
 * @param title the list title
 * @param subtitle the list subtitle
 * @param listDetails the list details, in the top of items list
 * @param items the items list
 */
fun <T : Bus<T>> T.businessChatListPicker(
    title: CharSequence,
    subtitle: CharSequence,
    listDetails: CharSequence,
    items: List<ListPickerItem>
): BusinessChatConnectorMessage =
    BusinessChatConnectorListPickerMessage(
        sourceId = botId.id,
        destinationId = userId.id,
        title = translate(title).toString(),
        subtitle = translate(subtitle).toString(),
        listDetails = translate(listDetails).toString(),
        multipleSelection = false,
        items = items
    )

/**
 *  Creates a [BusinessChatRichLink].
 *
 *  @param url
 *  @param title
 *  @param image
 *  @param mimeType
 */
fun <T : Bus<T>> T.businessChatRichLink(
    url: String,
    title: CharSequence,
    image: ByteArray,
    mimeType: String
): BusinessChatConnectorMessage =
    BusinessChatConnectorRichLinkMessage(
        sourceId = botId.id,
        destinationId = userId.id,
        url = url,
        title = translate(title).toString(),
        image = image,
        mimeType = mimeType
    )

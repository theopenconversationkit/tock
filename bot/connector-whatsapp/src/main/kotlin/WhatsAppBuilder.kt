/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.vsct.tock.bot.connector.whatsapp

import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.connector.whatsapp.model.common.WhatsAppTextBody
import fr.vsct.tock.bot.connector.whatsapp.model.send.WhatsAppBotAttachment
import fr.vsct.tock.bot.connector.whatsapp.model.send.WhatsAppBotImageMessage
import fr.vsct.tock.bot.connector.whatsapp.model.send.WhatsAppBotMessage
import fr.vsct.tock.bot.connector.whatsapp.model.send.WhatsAppBotTextMessage
import fr.vsct.tock.bot.engine.BotBus
import fr.vsct.tock.bot.engine.Bus

internal const val WHATS_APP_CONNECTOR_TYPE_ID = "whatsapp"

/**
 * The WhatsApp connector type.
 */
val whatsAppConnectorType = ConnectorType(WHATS_APP_CONNECTOR_TYPE_ID)

/**
 * Sends an WhatsApp message only if the [ConnectorType] of the current [BotBus] is [whatsAppConnectorType].
 */
fun <T : Bus<T>> T.sendToWhatsApp(
    messageProvider: T.() -> WhatsAppBotMessage,
    delay: Long = defaultDelay(currentAnswerIndex)
): T {
    if (targetConnectorType == whatsAppConnectorType) {
        withMessage(messageProvider(this))
        send(delay)
    }
    return this
}

/**
 * Sends an WhatsApp message as last bot answer, only if the [ConnectorType] of the current [BotBus] is [whatsAppConnectorType].
 */
fun <T : Bus<T>> T.endForWhatsApp(
    messageProvider: T.() -> WhatsAppBotMessage,
    delay: Long = defaultDelay(currentAnswerIndex)
): T {
    if (targetConnectorType == whatsAppConnectorType) {
        withMessage(messageProvider(this))
        end(delay)
    }
    return this
}

/**
 * Adds a WhatsApp [ConnectorMessage] if the current connector is WhatsApp.
 * You need to call [BotBus.send] or [BotBus.end] later to send this message.
 */
fun <T : Bus<T>> T.withWhatsApp(messageProvider: () -> WhatsAppBotMessage): T {
    return withMessage(whatsAppConnectorType, messageProvider)
}

/**
 * Creates a [WhatsAppBotTextMessage].
 *
 * @param text the text sent
 * @param previewUrl is preview mode is used?
 */
fun BotBus.whatsAppText(
    text: CharSequence,
    previewUrl: Boolean = false
): WhatsAppBotTextMessage =
    WhatsAppBotTextMessage(
        WhatsAppTextBody(translate(text).toString()),
        (connectorData.callback as WhatsAppConnectorCallback).recipientType,
        userId.id,
        previewUrl
    )

/**
 * Creates a [WhatsAppBotImageMessage].
 */
fun BotBus.whatsAppImage(
    byteImages: ByteArray,
    contentType: String = "image/png",
    caption: CharSequence? = null
): WhatsAppBotImageMessage =
    WhatsAppBotImageMessage(
        WhatsAppBotAttachment(
            byteImages,
            contentType,
            caption?.let { translate(it).toString() }
        ),
        (connectorData.callback as WhatsAppConnectorCallback).recipientType,
        userId.id
    )
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
import fr.vsct.tock.bot.connector.whatsapp.model.send.WhatsAppBotAttachment
import fr.vsct.tock.bot.connector.whatsapp.model.send.WhatsAppBotImageMessage
import fr.vsct.tock.bot.connector.whatsapp.model.send.WhatsAppBotMessage
import fr.vsct.tock.bot.engine.BotBus

internal const val WHATS_APP_CONNECTOR_TYPE_ID = "whatsapp"

/**
 * The WhatsApp connector type.
 */
val whatsAppConnectorType = ConnectorType(WHATS_APP_CONNECTOR_TYPE_ID)

/**
 * Sends an WhatsApp message only if the [ConnectorType] of the current [BotBus] is [whatsAppConnectorType].
 */
fun BotBus.sendToWhatsApp(
    messageProvider: () -> WhatsAppBotMessage,
    delay: Long = botDefinition.defaultDelay(currentAnswerIndex)
): BotBus {
    if (targetConnectorType == whatsAppConnectorType) {
        withWhatsApp(messageProvider)
        send(delay)
    }
    return this
}

/**
 * Sends an WhatsApp message as last bot answer, only if the [ConnectorType] of the current [BotBus] is [whatsAppConnectorType].
 */
fun BotBus.endForWhatsApp(
    messageProvider: () -> WhatsAppBotMessage,
    delay: Long = botDefinition.defaultDelay(currentAnswerIndex)
): BotBus {
    if (targetConnectorType == whatsAppConnectorType) {
        withWhatsApp(messageProvider)
        end(delay)
    }
    return this
}

/**
 * Adds a WhatsApp [ConnectorMessage] if the current connector is WhatsApp.
 * You need to call [BotBus.send] or [BotBus.end] later to send this message.
 */
fun BotBus.withWhatsApp(messageProvider: () -> WhatsAppBotMessage): BotBus {
    return withMessage(whatsAppConnectorType, messageProvider)
}

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
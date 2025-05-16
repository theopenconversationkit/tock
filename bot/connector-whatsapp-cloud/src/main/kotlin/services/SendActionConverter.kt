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

package ai.tock.bot.connector.whatsapp.cloud.services

import ai.tock.bot.connector.ConnectorException
import ai.tock.bot.connector.whatsapp.cloud.UserHashedIdCache
import ai.tock.bot.connector.whatsapp.cloud.model.common.TextContent
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudBotMessage
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudBotRecipientType.individual
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudSendBotMessage
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudSendBotTextMessage
import ai.tock.bot.connector.whatsapp.cloud.whatsAppCloudConnectorType
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.user.PlayerId
import ai.tock.shared.error
import mu.KotlinLogging


object SendActionConverter {
    private val logger = KotlinLogging.logger {}

    fun toBotMessage(whatsAppCloudApiService: WhatsAppCloudApiService, action: Action): WhatsAppCloudSendBotMessage? {
        return if (action is SendSentence) {
            val whatsappMessage = action.message(whatsAppCloudConnectorType)
            val stringText = action.stringText

            if (whatsappMessage is WhatsAppCloudBotMessage) {
                prepareBotMessage(whatsappMessage, whatsAppCloudApiService, action.recipientId)
            } else if (stringText != null) {
                WhatsAppCloudSendBotTextMessage(
                    TextContent(stringText),
                    individual,
                    UserHashedIdCache.getRealId(action.recipientId.id)
                )
            } else {
                throw ConnectorException("Action has neither bare text nor whatsapp-specific connector message: $action")
            }
        } else {
            null
        }
    }

    private fun prepareBotMessage(
        message: WhatsAppCloudBotMessage,
        apiService: WhatsAppCloudApiService,
        recipientId: PlayerId
    ) = try {
        message.prepareMessage(
            apiService,
            (message.userId ?: recipientId.id).let { id -> UserHashedIdCache.getRealId(id) })
    } catch (e: Exception) {
        logger.error(e)
        null
    }
}

/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.whatsapp.cloud.UserHashedIdCache
import ai.tock.bot.connector.whatsapp.cloud.model.common.TextContent
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudBotMessage
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudSendBotMessage
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudSendBotTextMessage
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudBotRecipientType.individual
import ai.tock.bot.connector.whatsapp.cloud.whatsAppCloudConnectorType

import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendSentence


object SendActionConverter {

    fun toBotMessage(action: Action): WhatsAppCloudSendBotMessage? {
        return if (action is SendSentence) {
            (action.message(whatsAppCloudConnectorType) as? WhatsAppCloudBotMessage)
                ?.let {
                    it.toSendBotMessage(
                        (it.userId ?: action.recipientId.id).let { id -> UserHashedIdCache.getRealId(id) }
                    )
                }
                ?: action.stringText?.let { text ->
                    WhatsAppCloudSendBotTextMessage(
                        "whatsapp",
                        TextContent(text),
                        individual,
                        UserHashedIdCache.getRealId(action.recipientId.id)
                    )
                } ?: error("null text in action $action")

        } else {
            null
        }

    }
}

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

package ai.tock.bot.connector.whatsapp

import ai.tock.bot.connector.whatsapp.model.common.WhatsAppTextBody
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotMessage
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotRecipientType.individual
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppSendBotMessage
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppSendBotTextMessage
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendSentence

/**
 *
 */
object SendActionConverter {
    fun toBotMessage(action: Action): WhatsAppSendBotMessage? {
        return if (action is SendSentence) {
            (action.message(whatsAppConnectorType) as? WhatsAppBotMessage)
                ?.let {
                    it.toSendBotMessage(
                        (it.userId ?: action.recipientId.id).let { id -> UserHashedIdCache.getRealId(id) },
                    )
                }
                ?: action.stringText?.let { text ->
                    WhatsAppSendBotTextMessage(
                        WhatsAppTextBody(text),
                        individual,
                        UserHashedIdCache.getRealId(action.recipientId.id),
                    )
                }
        } else {
            null
        }
    }
}

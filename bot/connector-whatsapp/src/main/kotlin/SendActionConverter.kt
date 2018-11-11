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

import fr.vsct.tock.bot.connector.whatsapp.model.common.WhatsAppTextBody
import fr.vsct.tock.bot.connector.whatsapp.model.send.WhatsAppBotMessage
import fr.vsct.tock.bot.connector.whatsapp.model.send.WhatsAppBotRecipientType.individual
import fr.vsct.tock.bot.connector.whatsapp.model.send.WhatsAppBotTextMessage
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.SendSentence

/**
 *
 */
internal object SendActionConverter {

    fun toBotMessage(action: Action): WhatsAppBotMessage? {
        return if (action is SendSentence && action.stringText != null) {
            WhatsAppBotTextMessage(
                WhatsAppTextBody(action.stringText!!),
                individual,
                action.playerId.id
            )
        } else {
            null
        }
    }
}
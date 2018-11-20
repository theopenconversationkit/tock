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

import com.github.salomonbrys.kodein.instance
import fr.vsct.tock.bot.connector.whatsapp.model.webhook.WhatsAppMessage
import fr.vsct.tock.bot.connector.whatsapp.model.webhook.WhatsAppTextMessage
import fr.vsct.tock.bot.connector.whatsapp.model.webhook.WhatsAppVoiceMessage
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.event.Event
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType
import fr.vsct.tock.shared.injector
import fr.vsct.tock.stt.STT

/**
 *
 */
internal object WebhookActionConverter {

    private val stt: STT by injector.instance()

    fun toEvent(message: WhatsAppMessage, applicationId: String, client: WhatsAppClient): Event? {
        return when (message) {
            is WhatsAppTextMessage -> SendSentence(
                PlayerId(message.from),
                applicationId,
                PlayerId(applicationId, PlayerType.bot),
                message.text.body
            )
            is WhatsAppVoiceMessage -> {
                client.getMedia(message.voice.id)
                    ?.let { audio ->
                        stt.parse(audio)?.let { text ->
                            SendSentence(
                                PlayerId(message.from),
                                applicationId,
                                PlayerId(applicationId, PlayerType.bot),
                                text
                            )
                        }
                    }
            }
            else -> null
        }
    }
}
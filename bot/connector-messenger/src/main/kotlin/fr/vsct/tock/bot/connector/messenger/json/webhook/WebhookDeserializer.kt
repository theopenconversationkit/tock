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

package fr.vsct.tock.bot.connector.messenger.json.webhook

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import fr.vsct.tock.bot.connector.messenger.model.Recipient
import fr.vsct.tock.bot.connector.messenger.model.Sender
import fr.vsct.tock.bot.connector.messenger.model.webhook.*
import fr.vsct.tock.shared.jackson.JacksonDeserializer
import fr.vsct.tock.shared.jackson.read
import fr.vsct.tock.shared.jackson.readValue
import mu.KotlinLogging

/**
 *
 */
internal class WebhookDeserializer : JacksonDeserializer<Webhook>() {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Webhook? {
        data class WebhookFields(
                var sender: Sender? = null,
                var recipient: Recipient? = null,
                var timestamp: Long? = null,
                var message: Message? = null,
                var optin: Optin? = null,
                var postback: UserActionPayload? = null
        )

        val (sender, recipient, timestamp, message, optin, postback)
                = jp.read<WebhookFields> { fields, name ->
            with(fields) {
                when (name) {
                    Webhook::sender.name -> sender = jp.readValue<Sender>()
                    Webhook::recipient.name -> recipient = jp.readValue()
                    Webhook::timestamp.name -> timestamp = jp.longValue
                    MessageWebhook::message.name -> message = jp.readValue()
                    OptinWebhook::optin.name -> optin = jp.readValue()
                    PostbackWebhook::postback.name -> postback = jp.readValue()
                    else -> unknownValue
                }
            }
        }

        if (recipient == null || timestamp == null) {
            logger.warn { "invalid webhook $recipient $timestamp" }
            return null
        }

        if (sender == null) {
            return if (optin != null) {
                OptinWebhook(sender, recipient, timestamp, optin)
            } else {
                logger.warn { "invalid webhook - null sender" }
                return null
            }
        }

        return if (message != null) {
            when (message) {
                is MessageEcho -> MessageEchoWebhook(sender, recipient, timestamp, message)
                else -> MessageWebhook(sender, recipient, timestamp, message)
            }

        } else if (optin != null) {
            OptinWebhook(sender, recipient, timestamp, optin)
        } else if (postback != null) {
            PostbackWebhook(sender, recipient, timestamp, postback)
        } else {
            logger.warn { "unknown webhook" }
            null
        }
    }
}
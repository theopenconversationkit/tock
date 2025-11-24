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

package ai.tock.bot.connector.messenger.json.webhook

import ai.tock.bot.connector.messenger.model.Recipient
import ai.tock.bot.connector.messenger.model.Sender
import ai.tock.bot.connector.messenger.model.handover.AppRolesWebhook
import ai.tock.bot.connector.messenger.model.handover.PassThreadControl
import ai.tock.bot.connector.messenger.model.handover.PassThreadControlWebhook
import ai.tock.bot.connector.messenger.model.handover.RequestThreadControl
import ai.tock.bot.connector.messenger.model.handover.RequestThreadControlWebhook
import ai.tock.bot.connector.messenger.model.handover.TakeThreadControl
import ai.tock.bot.connector.messenger.model.handover.TakeThreadControlWebhook
import ai.tock.bot.connector.messenger.model.webhook.AccountLinking
import ai.tock.bot.connector.messenger.model.webhook.AccountLinkingWebhook
import ai.tock.bot.connector.messenger.model.webhook.Message
import ai.tock.bot.connector.messenger.model.webhook.MessageEcho
import ai.tock.bot.connector.messenger.model.webhook.MessageEchoWebhook
import ai.tock.bot.connector.messenger.model.webhook.MessageWebhook
import ai.tock.bot.connector.messenger.model.webhook.Optin
import ai.tock.bot.connector.messenger.model.webhook.OptinWebhook
import ai.tock.bot.connector.messenger.model.webhook.PostbackWebhook
import ai.tock.bot.connector.messenger.model.webhook.PriorMessage
import ai.tock.bot.connector.messenger.model.webhook.Referral
import ai.tock.bot.connector.messenger.model.webhook.ReferralParametersWebhook
import ai.tock.bot.connector.messenger.model.webhook.UserActionPayload
import ai.tock.bot.connector.messenger.model.webhook.Webhook
import ai.tock.shared.jackson.JacksonDeserializer
import ai.tock.shared.jackson.read
import ai.tock.shared.jackson.readValue
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import mu.KotlinLogging

/**
 *
 */
internal class WebhookDeserializer : JacksonDeserializer<Webhook>() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun deserialize(
        jp: JsonParser,
        ctxt: DeserializationContext,
    ): Webhook? {
        data class WebhookFields(
            var sender: Sender? = null,
            var recipient: Recipient? = null,
            var timestamp: Long? = null,
            var message: Message? = null,
            var optin: Optin? = null,
            var postback: UserActionPayload? = null,
            var priorMessage: PriorMessage? = null,
            var accountLinking: AccountLinking? = null,
            var passThreadControl: PassThreadControl? = null,
            var takeThreadControl: TakeThreadControl? = null,
            var requestThreadControl: RequestThreadControl? = null,
            var appRoles: Map<String, List<String>>? = null,
            var referral: Referral? = null,
            var other: EmptyJson? = null,
        )

        val (
            sender, recipient, timestamp,
            message, optin, postback,
            priorMessage, accountLinking,
            passThreadControl, takeThreadControl, requestThreadControl, appRoles, referral,
        ) =
            jp.read<WebhookFields> { fields, name ->
                with(fields) {
                    when (name) {
                        Webhook::sender.name -> sender = jp.readValue()
                        Webhook::recipient.name -> recipient = jp.readValue()
                        Webhook::timestamp.name -> timestamp = jp.longValue
                        MessageWebhook::message.name -> message = jp.readValue()
                        OptinWebhook::optin.name -> optin = jp.readValue()
                        PostbackWebhook::postback.name -> postback = jp.readValue()
                        "prior_message" -> priorMessage = jp.readValue()
                        "account_linking" -> accountLinking = jp.readValue()
                        "pass_thread_control" -> passThreadControl = jp.readValue()
                        "take_thread_control" -> takeThreadControl = jp.readValue()
                        "request_thread_control" -> requestThreadControl = jp.readValue()
                        "app_roles" -> appRoles = jp.readValue()
                        "referral" -> referral = jp.readValue()
                        else -> other = jp.readUnknownValue()
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
            } else if (appRoles != null) {
                AppRolesWebhook(recipient, timestamp, appRoles)
            } else {
                logger.warn { "invalid webhook - null sender" }
                return null
            }
        }

        return if (message != null) {
            when (message) {
                is MessageEcho -> MessageEchoWebhook(sender, recipient, timestamp, message)
                else -> MessageWebhook(sender, recipient, timestamp, message, priorMessage)
            }
        } else if (optin != null) {
            OptinWebhook(sender, recipient, timestamp, optin)
        } else if (accountLinking != null) {
            AccountLinkingWebhook(sender, recipient, timestamp, accountLinking)
        } else if (postback != null) {
            PostbackWebhook(sender, recipient, timestamp, postback, priorMessage)
        } else if (passThreadControl != null) {
            PassThreadControlWebhook(sender, recipient, timestamp, passThreadControl)
        } else if (takeThreadControl != null) {
            TakeThreadControlWebhook(sender, recipient, timestamp, takeThreadControl)
        } else if (requestThreadControl != null) {
            RequestThreadControlWebhook(sender, recipient, timestamp, requestThreadControl)
        } else if (referral != null) {
            ReferralParametersWebhook(sender, recipient, timestamp, referral)
        } else {
            logger.error { "unknown webhook" }
            null
        }
    }
}

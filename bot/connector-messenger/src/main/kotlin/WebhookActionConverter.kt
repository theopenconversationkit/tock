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

package ai.tock.bot.connector.messenger

import ai.tock.bot.connector.messenger.model.handover.AppRolesWebhook
import ai.tock.bot.connector.messenger.model.handover.PassThreadControlWebhook
import ai.tock.bot.connector.messenger.model.handover.RequestThreadControlWebhook
import ai.tock.bot.connector.messenger.model.handover.TakeThreadControlWebhook
import ai.tock.bot.connector.messenger.model.webhook.AccountLinkingStatus
import ai.tock.bot.connector.messenger.model.webhook.AccountLinkingWebhook
import ai.tock.bot.connector.messenger.model.webhook.Attachment
import ai.tock.bot.connector.messenger.model.webhook.AttachmentType
import ai.tock.bot.connector.messenger.model.webhook.LocationPayload
import ai.tock.bot.connector.messenger.model.webhook.MessageWebhook
import ai.tock.bot.connector.messenger.model.webhook.OptinWebhook
import ai.tock.bot.connector.messenger.model.webhook.PostbackWebhook
import ai.tock.bot.connector.messenger.model.webhook.ReferralParametersWebhook
import ai.tock.bot.connector.messenger.model.webhook.UrlPayload
import ai.tock.bot.connector.messenger.model.webhook.Webhook
import ai.tock.bot.engine.action.SendAttachment
import ai.tock.bot.engine.action.SendAttachment.AttachmentType.audio
import ai.tock.bot.engine.action.SendAttachment.AttachmentType.image
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.action.SendLocation
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.event.AppRole
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.event.GetAppRolesEvent
import ai.tock.bot.engine.event.LoginEvent
import ai.tock.bot.engine.event.LogoutEvent
import ai.tock.bot.engine.event.PassThreadControlEvent
import ai.tock.bot.engine.event.ReferralParametersEvent
import ai.tock.bot.engine.event.RequestThreadControlEvent
import ai.tock.bot.engine.event.SubscribingEvent
import ai.tock.bot.engine.event.TakeThreadControlEvent
import ai.tock.bot.engine.user.PlayerType
import mu.KotlinLogging
import org.litote.kmongo.toId

/**
 *
 */
internal object WebhookActionConverter {
    private val logger = KotlinLogging.logger {}

    fun toEvent(
        message: Webhook,
        applicationId: String,
    ): Event? {
        return when (message) {
            is MessageWebhook ->
                with(message.message) {
                    if (quickReply != null) {
                        if (quickReply!!.hasEmailPayloadFromMessenger()) {
                            readSentence(message, applicationId)
                        } else {
                            quickReply
                                ?.payload
                                ?.let { payload ->
                                    SendChoice.decodeChoice(
                                        payload,
                                        message.playerId(PlayerType.user),
                                        applicationId,
                                        message.recipientId(PlayerType.bot),
                                    )
                                }
                        }
                    } else {
                        val a = attachments
                        if (a.isNotEmpty()) {
                            val first = a.first()
                            when (first.type) {
                                AttachmentType.location -> readLocation(message, first, applicationId)
                                AttachmentType.image -> readAttachment(message, first, applicationId, image)
                                AttachmentType.audio -> readAttachment(message, first, applicationId, audio)
                                // ignore for now
                                else -> readSentence(message, applicationId)
                            }
                        } else {
                            readSentence(message, applicationId)
                        }
                    }
                }
            is PostbackWebhook -> {
                message.postback.payload?.let { payload ->
                    SendChoice.decodeChoice(
                        payload,
                        message.playerId(PlayerType.user),
                        applicationId,
                        message.recipientId(PlayerType.bot),
                        message.postback.referral?.ref,
                    )
                }
            }
            is OptinWebhook ->
                SubscribingEvent(
                    message.playerId(PlayerType.user),
                    message.recipientId(PlayerType.bot),
                    message.optin.ref,
                    applicationId,
                )
            is AccountLinkingWebhook -> {
                when (message.accountLinking.status) {
                    AccountLinkingStatus.linked ->
                        LoginEvent(
                            message.playerId(PlayerType.user),
                            message.recipientId(PlayerType.bot),
                            message.accountLinking.authorizationCode!!,
                            applicationId,
                        )
                    AccountLinkingStatus.unlinked ->
                        LogoutEvent(
                            message.playerId(PlayerType.user),
                            message.recipientId(PlayerType.bot),
                            applicationId,
                        )
                }
            }
            is AppRolesWebhook -> {
                GetAppRolesEvent(
                    message.recipientId(PlayerType.bot),
                    applicationId,
                    message
                        .appRoles
                        .mapValues {
                            it.value.mapNotNull {
                                when (it) {
                                    "primary_receiver" -> AppRole.primaryReceiver
                                    "secondary_receiver" -> AppRole.secondaryReceiver
                                    else -> {
                                        logger.warn { "unknown role $it" }
                                        null
                                    }
                                }
                            }
                                .toSet()
                        },
                )
            }
            is RequestThreadControlWebhook -> {
                RequestThreadControlEvent(
                    message.playerId(PlayerType.user),
                    message.recipientId(PlayerType.bot),
                    applicationId,
                    message.requestThreadControl.requestOwnerAppId,
                    message.requestThreadControl.metadata,
                )
            }
            is PassThreadControlWebhook -> {
                PassThreadControlEvent(
                    message.playerId(PlayerType.user),
                    message.recipientId(PlayerType.bot),
                    applicationId,
                    message.passThreadControl.newOwnerAppId,
                    message.passThreadControl.metadata,
                )
            }
            is TakeThreadControlWebhook -> {
                TakeThreadControlEvent(
                    message.playerId(PlayerType.user),
                    message.recipientId(PlayerType.bot),
                    applicationId,
                    message.takeThreadControl.previousOwnerAppId,
                    message.takeThreadControl.metadata,
                )
            }
            is ReferralParametersWebhook -> {
                ReferralParametersEvent(
                    message.playerId(PlayerType.user),
                    message.recipientId(PlayerType.bot),
                    applicationId,
                    message.referral.ref,
                )
            }
            else -> {
                logger.error { "unknown message $message" }
                null
            }
        }
    }

    private fun readSentence(
        message: MessageWebhook,
        applicationId: String,
    ): SendSentence {
        return SendSentence(
            message.playerId(PlayerType.user),
            applicationId,
            message.recipientId(PlayerType.bot),
            message.message.text ?: "",
            mutableListOf(message),
            message.getMessageId().toId(),
        )
    }

    private fun readLocation(
        message: MessageWebhook,
        attachment: Attachment,
        applicationId: String,
    ): SendLocation {
        logger.debug { "read location attachment : $attachment" }
        return SendLocation(
            message.playerId(PlayerType.user),
            applicationId,
            message.recipientId(PlayerType.bot),
            (attachment.payload as LocationPayload).coordinates.toUserLocation(),
            message.getMessageId().toId(),
        )
    }

    private fun readAttachment(
        message: MessageWebhook,
        attachment: Attachment,
        applicationId: String,
        attachmentType: SendAttachment.AttachmentType,
    ): SendAttachment {
        logger.debug { "read attachment : $attachment" }
        return SendAttachment(
            message.playerId(PlayerType.user),
            applicationId,
            message.recipientId(PlayerType.bot),
            (attachment.payload as UrlPayload).url,
            attachmentType,
            message.getMessageId().toId(),
        )
    }
}

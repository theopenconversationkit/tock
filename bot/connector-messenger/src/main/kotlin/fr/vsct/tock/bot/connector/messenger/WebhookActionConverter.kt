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

package fr.vsct.tock.bot.connector.messenger

import fr.vsct.tock.bot.connector.messenger.model.handover.AppRolesWebhook
import fr.vsct.tock.bot.connector.messenger.model.handover.PassThreadControlWebhook
import fr.vsct.tock.bot.connector.messenger.model.handover.RequestThreadControlWebhook
import fr.vsct.tock.bot.connector.messenger.model.handover.TakeThreadControlWebhook
import fr.vsct.tock.bot.connector.messenger.model.webhook.AccountLinkingStatus
import fr.vsct.tock.bot.connector.messenger.model.webhook.AccountLinkingWebhook
import fr.vsct.tock.bot.connector.messenger.model.webhook.Attachment
import fr.vsct.tock.bot.connector.messenger.model.webhook.AttachmentType
import fr.vsct.tock.bot.connector.messenger.model.webhook.LocationPayload
import fr.vsct.tock.bot.connector.messenger.model.webhook.MessageWebhook
import fr.vsct.tock.bot.connector.messenger.model.webhook.OptinWebhook
import fr.vsct.tock.bot.connector.messenger.model.webhook.PostbackWebhook
import fr.vsct.tock.bot.connector.messenger.model.webhook.UrlPayload
import fr.vsct.tock.bot.connector.messenger.model.webhook.Webhook
import fr.vsct.tock.bot.engine.action.SendAttachment
import fr.vsct.tock.bot.engine.action.SendAttachment.AttachmentType.audio
import fr.vsct.tock.bot.engine.action.SendAttachment.AttachmentType.image
import fr.vsct.tock.bot.engine.action.SendChoice
import fr.vsct.tock.bot.engine.action.SendLocation
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.event.AppRole
import fr.vsct.tock.bot.engine.event.Event
import fr.vsct.tock.bot.engine.event.GetAppRolesEvent
import fr.vsct.tock.bot.engine.event.LoginEvent
import fr.vsct.tock.bot.engine.event.LogoutEvent
import fr.vsct.tock.bot.engine.event.PassThreadControlEvent
import fr.vsct.tock.bot.engine.event.RequestThreadControlEvent
import fr.vsct.tock.bot.engine.event.SubscribingEvent
import fr.vsct.tock.bot.engine.event.TakeThreadControlEvent
import fr.vsct.tock.bot.engine.user.PlayerType
import mu.KotlinLogging
import org.litote.kmongo.toId

/**
 *
 */
internal object WebhookActionConverter {

    private val logger = KotlinLogging.logger {}

    fun toEvent(message: Webhook, applicationId: String): Event? {
        return when (message) {
            is MessageWebhook ->
                with(message.message) {
                    if (quickReply != null) {
                        if(quickReply!!.hasEmailPayloadFromMessenger()){
                            readSentence(message,applicationId)
                        }else{
                            SendChoice.decodeChoiceId(quickReply!!.payload)
                                .let { (intentName, parameters) ->
                                    if (parameters.containsKey(SendChoice.NLP)) {
                                        SendSentence(
                                            message.playerId(PlayerType.user),
                                            applicationId,
                                            message.recipientId(PlayerType.bot),
                                            parameters[SendChoice.NLP]
                                        )
                                    } else {
                                        SendChoice(
                                            message.playerId(PlayerType.user),
                                            applicationId,
                                            message.recipientId(PlayerType.bot),
                                            intentName,
                                            parameters
                                        )
                                    }
                                }
                        }
                    } else {
                        val a = attachments
                        if (a.isNotEmpty()) {
                            val type = a.first().type
                            when (type) {
                                AttachmentType.location -> readLocation(message, a.first(), applicationId)
                                AttachmentType.image -> readAttachment(message, a.first(), applicationId, image)
                                AttachmentType.audio -> readAttachment(message, a.first(), applicationId, audio)
                                // ignore for now
                                else -> readSentence(message, applicationId)
                            }
                        } else {
                            readSentence(message, applicationId)
                        }
                    }
                }
            is PostbackWebhook ->
                SendChoice.decodeChoiceId(message.postback.payload)
                    .let { (intentName, parameters) ->
                        SendChoice(
                            message.playerId(PlayerType.user),
                            applicationId,
                            message.recipientId(PlayerType.bot),
                            intentName,
                            parameters
                        )
                    }
            is OptinWebhook ->
                SubscribingEvent(
                    message.playerId(PlayerType.user),
                    message.recipientId(PlayerType.bot),
                    message.optin.ref,
                    applicationId
                )
            is AccountLinkingWebhook -> {
                when (message.accountLinking.status) {
                    AccountLinkingStatus.linked -> LoginEvent(
                        message.playerId(PlayerType.user),
                        message.recipientId(PlayerType.bot),
                        message.accountLinking.authorizationCode!!,
                        applicationId
                    )
                    AccountLinkingStatus.unlinked ->
                        LogoutEvent(
                            message.playerId(PlayerType.user),
                            message.recipientId(PlayerType.bot),
                            applicationId
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
                        }
                )
            }
            is RequestThreadControlWebhook -> {
                RequestThreadControlEvent(
                    message.playerId(PlayerType.user),
                    message.recipientId(PlayerType.bot),
                    applicationId,
                    message.requestThreadControl.requestOwnerAppId,
                    message.requestThreadControl.metadata
                )
            }
            is PassThreadControlWebhook -> {
                PassThreadControlEvent(
                    message.playerId(PlayerType.user),
                    message.recipientId(PlayerType.bot),
                    applicationId,
                    message.passThreadControl.newOwnerAppId,
                    message.passThreadControl.metadata
                )
            }
            is TakeThreadControlWebhook -> {
                TakeThreadControlEvent(
                    message.playerId(PlayerType.user),
                    message.recipientId(PlayerType.bot),
                    applicationId,
                    message.takeThreadControl.previousOwnerAppId,
                    message.takeThreadControl.metadata
                )
            }
            else -> {
                logger.error { "unknown message $message" }
                null
            }
        }
    }

    private fun readSentence(message: MessageWebhook, applicationId: String): SendSentence {
        return SendSentence(
            message.playerId(PlayerType.user),
            applicationId,
            message.recipientId(PlayerType.bot),
            message.message.text ?: "",
            mutableListOf(message),
            message.getMessageId().toId()
        )
    }

    private fun readLocation(message: MessageWebhook, attachment: Attachment, applicationId: String): SendLocation {
        logger.debug { "read location attachment : $attachment" }
        return SendLocation(
            message.playerId(PlayerType.user),
            applicationId,
            message.recipientId(PlayerType.bot),
            (attachment.payload as LocationPayload).coordinates.toUserLocation(),
            message.getMessageId().toId()
        )
    }

    private fun readAttachment(
        message: MessageWebhook,
        attachment: Attachment,
        applicationId: String,
        attachmentType: SendAttachment.AttachmentType
    ): SendAttachment {
        logger.debug { "read attachment : $attachment" }
        return SendAttachment(
            message.playerId(PlayerType.user),
            applicationId,
            message.recipientId(PlayerType.bot),
            (attachment.payload as UrlPayload).url,
            attachmentType,
            message.getMessageId().toId()
        )
    }

}
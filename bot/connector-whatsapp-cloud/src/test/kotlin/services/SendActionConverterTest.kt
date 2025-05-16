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

package services

import ai.tock.bot.connector.whatsapp.cloud.UserHashedIdCache
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudBotRecipientType
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudSendBotInteractiveMessage
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.content.WhatsAppCloudBotAction
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.content.WhatsAppCloudBotActionButton
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.content.WhatsAppCloudBotActionButtonReply
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.content.WhatsAppCloudBotBody
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.content.WhatsAppCloudBotHeaderType
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.content.WhatsAppCloudBotInteractive
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.content.WhatsAppCloudBotInteractiveHeader
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.content.WhatsAppCloudBotInteractiveMessage
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.content.WhatsAppCloudBotInteractiveType
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.content.WhatsAppCloudBotMediaImage
import ai.tock.bot.connector.whatsapp.cloud.services.SendActionConverter
import ai.tock.bot.connector.whatsapp.cloud.services.WhatsAppCloudApiService
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.user.PlayerId
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

class SendActionConverterTest {
    @Test
    fun `message conversion is correct for reply button`() {
        mockkObject(UserHashedIdCache)
        val userId = "4567876543"
        every { UserHashedIdCache.getRealId(userId) } returns userId
        val whatsAppCloudApiService = mockk<WhatsAppCloudApiService> {
            every { getUploadedImageId("fish.png") } answers {
                "test-image-id"
            }
            every { shortenPayload("button1") } answers {
                "button1id"
            }
            every { shortenPayload("button2") } answers {
                "button2id"
            }
        }

        val result = SendActionConverter.toBotMessage(
            whatsAppCloudApiService, SendSentence(
                PlayerId("test-user"), "test", PlayerId(userId), text = null, messages = mutableListOf(
                    WhatsAppCloudBotInteractiveMessage(
                        recipientType = WhatsAppCloudBotRecipientType.individual,
                        interactive = WhatsAppCloudBotInteractive(
                            type = WhatsAppCloudBotInteractiveType.button,
                            header = WhatsAppCloudBotInteractiveHeader(
                                WhatsAppCloudBotHeaderType.image,
                                image = WhatsAppCloudBotMediaImage("fish.png")
                            ),
                            body = WhatsAppCloudBotBody("test body"),
                            action = WhatsAppCloudBotAction(
                                buttons = listOf(
                                    WhatsAppCloudBotActionButton(
                                        reply = WhatsAppCloudBotActionButtonReply(
                                            "Button 1",
                                            "button1"
                                        )
                                    ),
                                    WhatsAppCloudBotActionButton(
                                        reply = WhatsAppCloudBotActionButtonReply(
                                            "Button 2",
                                            "button2"
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
        assertEquals(WhatsAppCloudSendBotInteractiveMessage(
            interactive = WhatsAppCloudBotInteractive(
                type = WhatsAppCloudBotInteractiveType.button,
                header = WhatsAppCloudBotInteractiveHeader(
                    WhatsAppCloudBotHeaderType.image,
                    image = WhatsAppCloudBotMediaImage("test-image-id")
                ),
                body = WhatsAppCloudBotBody("test body"),
                action = WhatsAppCloudBotAction(
                    buttons = listOf(
                        WhatsAppCloudBotActionButton(
                            reply = WhatsAppCloudBotActionButtonReply(
                                "Button 1",
                                "button1id"
                            )
                        ),
                        WhatsAppCloudBotActionButton(
                            reply = WhatsAppCloudBotActionButtonReply(
                                "Button 2",
                                "button2id"
                            )
                        )
                    )
                )
            ),
            recipientType = WhatsAppCloudBotRecipientType.individual,
            to = userId,
        ), result)
    }
}

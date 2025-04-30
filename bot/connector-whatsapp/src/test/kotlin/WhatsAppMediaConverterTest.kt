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

import ai.tock.bot.connector.media.MediaAction
import ai.tock.bot.connector.media.MediaCard
import ai.tock.bot.connector.whatsapp.WhatsAppMediaConverter
import ai.tock.bot.connector.whatsapp.nlpQuickReply
import ai.tock.bot.connector.whatsapp.replyButtonMessage
import ai.tock.bot.connector.whatsapp.whatsAppText
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.user.PlayerId
import ai.tock.shared.defaultLocale
import ai.tock.translator.I18nContext
import ai.tock.translator.Translator
import ai.tock.translator.UserInterfaceType.textChat
import ai.tock.translator.raw
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class WhatsAppMediaConverterTest {

    val bus = mockk<BotBus> {
        every { connectorData } returns mockk(relaxed = true)
        every { userId } returns PlayerId("user")
        every { translate(any()) } answers { (args[0] as CharSequence).raw }
        every { translateAndReturnBlankAsNull(any()) } answers { (args[0] as CharSequence).raw }
        every { translate(any(), *anyVararg()) } answers {
            Translator.formatMessage(
                args[0].toString(),
                I18nContext(defaultLocale, textChat, null),
                args.subList(1, args.size)
            ).raw
        }
    }

    @Test
    fun `toConnectorMessage should return text message and links if no nlp actions`() {
        val mediaCard = MediaCard(
            title = "title",
            subTitle = "subtitle",
            file = null,
            actions = listOf(
                MediaAction("Lien 1", "https://example.com/1"),
                MediaAction("Lien 2", "https://example.com/2"),
            ),
        )
        val result = WhatsAppMediaConverter.toConnectorMessage(mediaCard).invoke(bus)
        assertEquals(
            bus.whatsAppText(
                "title\n" +
                        "\n" +
                        "subtitle"
            ), result.first()
        )
        assertEquals(
            bus.whatsAppText(
                "Lien 1 :\n" +
                        "https://example.com/1", true
            ), result[1]
        )
        assertEquals(
            bus.whatsAppText(
                "Lien 2 :\n" +
                        "https://example.com/2", true
            ), result[2]
        )
    }

    @Test
    fun `toConnectorMessage should return reply button message and links if some nlp actions`() {
        val mediaCard = MediaCard(
            title = "title",
            subTitle = "subtitle",
            file = null,
            actions = listOf(
                MediaAction("Action 1"),
                MediaAction("Action 2"),
                MediaAction("Lien 1", "https://example.com/1"),
            ),
        )
        val result = WhatsAppMediaConverter.toConnectorMessage(mediaCard).invoke(bus)
        assertEquals(
            bus.replyButtonMessage(
                "title\n" +
                        "\n" +
                        "subtitle",
               bus.nlpQuickReply("Action 1"),
                bus.nlpQuickReply("Action 2"),
            ), result.first()
        )
        assertEquals(
            bus.whatsAppText(
                "Lien 1 :\n" +
                        "https://example.com/1", true
            ), result[1]
        )
    }


}
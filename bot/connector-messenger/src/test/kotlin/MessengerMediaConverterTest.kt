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

import ai.tock.bot.connector.media.MediaAction
import ai.tock.bot.connector.media.MediaCard
import ai.tock.bot.connector.media.MediaFile
import ai.tock.bot.connector.messenger.model.send.AttachmentMessage
import ai.tock.bot.connector.messenger.model.send.GenericPayload
import ai.tock.bot.engine.BotBus
import ai.tock.shared.defaultLocale
import ai.tock.translator.I18nContext
import ai.tock.translator.Translator
import ai.tock.translator.UserInterfaceType.textChat
import ai.tock.translator.raw
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertNull

class MessengerMediaConverterTest {
    @Test
    fun `MediaCard does not generate generic template with empty buttons`() {
        val bus = mockk<BotBus>()
        every { bus.translate(any()) } answers { (args[0] as CharSequence).raw }
        every { bus.translateAndReturnBlankAsNull(any()) } answers { (args[0] as CharSequence).raw }
        every { bus.translate(any(), *anyVararg()) } answers {
            Translator.formatMessage(
                args[0].toString(),
                I18nContext(defaultLocale, textChat, null),
                args.subList(1, args.size),
            ).raw
        }

        val mediaCard =
            MediaCard(
                "title",
                "subtitle",
                MediaFile("https://a/image.png", "image", description = "description File"),
                listOf(MediaAction("Test")),
            )
        val result = MessengerMediaConverter.toConnectorMessage(mediaCard).invoke(bus)
        assertNull(((result.first() as AttachmentMessage).attachment.payload as GenericPayload).elements.first().buttons)
    }
}

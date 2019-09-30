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

package ai.tock.bot.connector.messenger

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import ai.tock.bot.connector.messenger.model.send.Attachment
import ai.tock.bot.connector.messenger.model.send.AttachmentMessage
import ai.tock.bot.connector.messenger.model.send.AttachmentType.audio
import ai.tock.bot.connector.messenger.model.send.AttachmentType.image
import ai.tock.bot.connector.messenger.model.send.AttachmentType.video
import ai.tock.bot.connector.messenger.model.send.ListElementStyle
import ai.tock.bot.connector.messenger.model.send.ListPayload
import ai.tock.bot.connector.messenger.model.send.UrlPayload
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.user.UserPreferences
import ai.tock.shared.sharedTestModule
import ai.tock.shared.tockInternalInjector
import ai.tock.translator.raw
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

/**
 *
 */
class MessengerBuildersTest {

    val bus: BotBus = mockk(relaxed = true)

    @BeforeEach
    fun before() {
        tockInternalInjector = KodeinInjector().apply {
            inject(Kodein {
                import(sharedTestModule)
            })
        }

        every { bus.targetConnectorType } returns messengerConnectorType
        every { bus.applicationId } returns "appId"
        every { bus.userPreferences } returns UserPreferences()
        every { bus.translate(allAny()) } answers { firstArg() ?: "".raw }
    }

    @Test
    fun `listTemplate throws exception WHEN at least one element does not contain an image url AND list style is not compact`() {
        assertThrows<IllegalStateException> {
            listTemplate(bus.listElement("title"), bus.listElement("title2"))
        }
    }

    @Test
    fun `flexibleListTemplate does not throw exception and keep only first four items WHEN more than four items`() {
        val elements = (1..5).map { bus.listElement("title$it") }
        val template = flexibleListTemplate(elements, ListElementStyle.compact)
        assertEquals(elements.subList(0, 4), (template.attachment.payload as ListPayload).elements)
    }

    @Test
    fun testImage() {
        assertEquals(
            AttachmentMessage(
                attachment = Attachment(
                    type = image,
                    payload = UrlPayload("http://test", null, true)
                )
            ), bus.image("http://test")
        )
    }

    @Test
    fun testVideo() {
        assertEquals(
            AttachmentMessage(
                attachment = Attachment(
                    type = video,
                    payload = UrlPayload("http://test", null, true)
                )
            ), bus.video("http://test")
        )
    }

    @Test
    fun testAudio() {
        assertEquals(
            AttachmentMessage(
                attachment = Attachment(
                    type = audio,
                    payload = UrlPayload("http://test", null, true)
                )
            ), bus.audio("http://test")
        )
    }

    @Test
    fun testSendToMessenger() {
        bus.sendToMessenger { buttonsTemplate("Button") }

        verify { bus.withMessage(any()) }
        verify { bus.send(any<Long>()) }
    }

    @Test
    fun testSendToMessengerWithDelay() {
        bus.sendToMessenger(10) { buttonsTemplate("Button") }

        verify { bus.withMessage(any()) }
        verify { bus.send(10) }
    }

    @Test
    fun testEndForMessenger() {
        bus.endForMessenger { buttonsTemplate("Button") }

        verify { bus.withMessage(any()) }
        verify { bus.end(any<Long>()) }
    }
}
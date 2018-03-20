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

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import fr.vsct.tock.bot.connector.messenger.model.send.Attachment
import fr.vsct.tock.bot.connector.messenger.model.send.AttachmentMessage
import fr.vsct.tock.bot.connector.messenger.model.send.AttachmentType.audio
import fr.vsct.tock.bot.connector.messenger.model.send.AttachmentType.image
import fr.vsct.tock.bot.connector.messenger.model.send.AttachmentType.video
import fr.vsct.tock.bot.connector.messenger.model.send.UrlPayload
import fr.vsct.tock.bot.engine.BotBus
import fr.vsct.tock.bot.engine.user.UserPreferences
import fr.vsct.tock.shared.sharedTestModule
import fr.vsct.tock.shared.tockInternalInjector
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

/**
 *
 */
class MessengerBuildersTest {

    val bus: BotBus = mockk(relaxed = true)

    @Before
    fun before() {
        tockInternalInjector = KodeinInjector().apply {
            inject(Kodein {
                import(sharedTestModule)
            })
        }

        every { bus.applicationId } returns "appId"
        every { bus.userPreferences } returns UserPreferences()
        every { bus.translate(allAny()) } answers { firstArg() ?: "" }
    }

    @Test(expected = IllegalStateException::class)
    fun `listTemplate throws exception WHEN at least one element does not contain an image url AND list style is not compact`() {
        listTemplate(bus.listElement("title"), bus.listElement("title2"))
    }

    @Test
    fun testImage() {
        assertEquals(AttachmentMessage(
                attachment = Attachment(type = image,
                        payload = UrlPayload("http://test", null, true))
        ), bus.image("http://test")
        )
    }

    @Test
    fun testVideo() {
        assertEquals(AttachmentMessage(
                attachment = Attachment(type = video,
                        payload = UrlPayload("http://test", null, true))
        ), bus.video("http://test")
        )
    }

    @Test
    fun testAudio() {
        assertEquals(AttachmentMessage(
                attachment = Attachment(type = audio,
                        payload = UrlPayload("http://test", null, true))
        ), bus.audio("http://test")
        )
    }
}
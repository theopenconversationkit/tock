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

package fr.vsct.tock.bot.connector.slack

import fr.vsct.tock.bot.connector.slack.model.AttachmentField
import fr.vsct.tock.bot.engine.BotBus
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals


class SlackBuildersTest {

    val bus: BotBus = mockk<BotBus>(relaxed = true)

    @Before
    fun init() {
        every { bus.translate(any<CharSequence>()) } answers { firstArg() }
        every { bus.translateAndReturnBlankAsNull(any()) } answers { firstArg() }
    }

    @Test
    fun testAttachmentMessage() {
        val field = arrayOf(AttachmentField("title", "value", false))
        val fallback = "fallback"

        val attachmentMessage = bus.attachmentMessage(*field, fallback = fallback)
        assertEquals("good", attachmentMessage.color)
        assertEquals(fallback, attachmentMessage.fallback)
        assertEquals(field.toList(), attachmentMessage.fields)
    }

    @Test
    fun testMultiLineMessage() {
        val messages = listOf("line 1", "line 2", "line 3")
        val expectedMessage = "line 1\nline 2\nline 3"
        val multiLineMessage = bus.multiLineMessage(messages)
        assertEquals(expectedMessage, multiLineMessage.text)
    }

}
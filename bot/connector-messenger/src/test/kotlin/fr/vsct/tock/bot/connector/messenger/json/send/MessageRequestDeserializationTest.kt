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

package fr.vsct.tock.bot.connector.messenger.json.send

import com.fasterxml.jackson.module.kotlin.readValue
import fr.vsct.tock.bot.connector.messenger.model.Recipient
import fr.vsct.tock.bot.connector.messenger.model.send.Attachment
import fr.vsct.tock.bot.connector.messenger.model.send.AttachmentMessage
import fr.vsct.tock.bot.connector.messenger.model.send.AttachmentType
import fr.vsct.tock.bot.connector.messenger.model.send.Element
import fr.vsct.tock.bot.connector.messenger.model.send.GenericPayload
import fr.vsct.tock.bot.connector.messenger.model.send.MessageRequest
import fr.vsct.tock.bot.connector.messenger.model.send.PostbackButton
import fr.vsct.tock.bot.connector.messenger.model.send.UrlPayload
import fr.vsct.tock.shared.jackson.mapper
import org.junit.Test
import kotlin.test.assertEquals

/**
 *
 */
class MessageRequestDeserializationTest {

    @Test
    fun testMessageRequestWithButtonDeserialization() {
        val m = MessageRequest(Recipient("2"), AttachmentMessage(Attachment(AttachmentType.template, GenericPayload(listOf(Element("title", buttons = listOf(PostbackButton("payload", "titleButton"))))))))
        val s = mapper.writeValueAsString(m)
        println(s)
        assertEquals(m, mapper.readValue<MessageRequest>(s))
    }

    @Test
    fun testMessageRequestWithUrlPayload() {
        val m = MessageRequest(Recipient("2"), AttachmentMessage(Attachment(AttachmentType.image, UrlPayload("http://test/test.png", null, null))))
        val s = mapper.writeValueAsString(m)
        println(s)
        assertEquals(m, mapper.readValue<MessageRequest>(s))
    }
}
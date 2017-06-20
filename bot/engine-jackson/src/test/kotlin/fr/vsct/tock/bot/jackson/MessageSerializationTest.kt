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

package fr.vsct.tock.bot.jackson

import com.fasterxml.jackson.module.kotlin.readValue
import fr.vsct.tock.bot.engine.action.SendAttachment
import fr.vsct.tock.bot.engine.message.Attachment
import fr.vsct.tock.bot.engine.message.Choice
import fr.vsct.tock.bot.engine.message.Location
import fr.vsct.tock.bot.engine.message.Message
import fr.vsct.tock.bot.engine.message.Sentence
import fr.vsct.tock.bot.engine.user.UserLocation
import fr.vsct.tock.shared.jackson.mapper
import org.junit.Test
import kotlin.test.assertEquals

/**
 *
 */
class MessageSerializationTest {
    data class MessageRequest(
            val userId: String,
            val recipientId: String,
            val message: Message)

    init {
        BotEngineJacksonConfiguration.init()
    }

    @Test
    fun serializingAndDeserializingMessageRequestContainingSentence_shouldSucceed() {
        val request = MessageRequest("a", "bot", Sentence("text"))
        val content = mapper.writeValueAsString(request)
        assertEquals(request, mapper.readValue(content))
    }

    @Test
    fun serializingAndDeserializingMessageRequestContainingChoice_shouldSucceed() {
        val request = MessageRequest("a", "bot", Choice("intent"))
        assertEquals(request, mapper.readValue(mapper.writeValueAsString(request)))
    }

    @Test
    fun serializingAndDeserializingMessageRequestContainingAttachment_shouldSucceed() {
        val request = MessageRequest("a", "bot", Attachment("img.png", SendAttachment.AttachmentType.image))
        assertEquals(request, mapper.readValue(mapper.writeValueAsString(request)))
    }

    @Test
    fun serializingAndDeserializingMessageRequestContainingLocation_shouldSucceed() {
        val request = MessageRequest("a", "bot", Location(UserLocation(1.0, 1.0)))
        assertEquals(request, mapper.readValue(mapper.writeValueAsString(request)))
    }
}
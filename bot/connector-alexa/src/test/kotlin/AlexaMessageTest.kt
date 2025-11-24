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

package ai.tock.bot.connector.alexa

import ai.tock.bot.engine.action.SendAttachment
import ai.tock.bot.engine.message.Attachment
import ai.tock.bot.engine.message.GenericMessage
import com.amazon.speech.ui.Image
import com.amazon.speech.ui.StandardCard
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 *
 */
class AlexaMessageTest {
    @Test
    fun `toSentenceElement provides reprompt and images`() {
        val m =
            AlexaMessage(
                true,
                StandardCard().apply {
                    title = "title"
                    image =
                        Image().apply {
                            smallImageUrl = "url1"
                            largeImageUrl = "url2"
                            text = "text"
                        }
                },
                "hey?",
            )
        val e = m.toGenericMessage()
        assertEquals(
            GenericMessage(
                attachments =
                    listOf(
                        Attachment("url1", SendAttachment.AttachmentType.image),
                    ),
                texts =
                    mapOf(
                        "reprompt" to "hey?",
                        "title" to "title",
                        "text" to "text",
                    ),
                metadata = mapOf("end" to "true"),
            ),
            e,
        )
    }
}

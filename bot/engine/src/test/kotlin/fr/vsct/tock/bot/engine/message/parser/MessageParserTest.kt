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

package fr.vsct.tock.bot.engine.message.parser

import fr.vsct.tock.bot.engine.action.SendAttachment.AttachmentType.file
import fr.vsct.tock.bot.engine.action.SendAttachment.AttachmentType.image
import fr.vsct.tock.bot.engine.message.Attachment
import fr.vsct.tock.bot.engine.message.Choice
import fr.vsct.tock.bot.engine.message.Location
import fr.vsct.tock.bot.engine.message.Sentence
import fr.vsct.tock.bot.engine.message.SentenceElement
import fr.vsct.tock.bot.engine.message.SentenceSubElement
import fr.vsct.tock.bot.engine.user.UserLocation
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**

 */
class MessageParserTest {

    @Test
    fun parse_shouldWork_forSentenceText() {
        val s = Sentence("ok")
        assertEquals(s, MessageParser.parse(s.toPrettyString()).first())
    }

    @Test
    fun parse_shouldWork_forImageAttachment() {
        val a = Attachment("img.png", image)
        assertEquals(a, MessageParser.parse(a.toPrettyString()).first())
    }

    @Test
    fun parse_shouldWork_forFileAttachment() {
        val a = Attachment("file.txt", file)
        assertEquals(a, MessageParser.parse(a.toPrettyString()).first())
    }

    @Test
    fun parse_shouldWork_forChoiceWithoutParameters() {
        val c = Choice("intent")
        assertEquals(c, MessageParser.parse(c.toPrettyString()).first())
    }

    @Test
    fun parse_shouldWork_forChoiceWithParameters() {
        val c = Choice("intent", mapOf("1" to "2", "3" to "4"))
        assertEquals(c, MessageParser.parse(c.toPrettyString()).first())
    }

    @Test
    fun parse_shouldWork_forLocation() {
        val l = Location(UserLocation(1.0, 2.0))
        assertEquals(l, MessageParser.parse(l.toPrettyString()).first())
    }

    @Test
    fun parse_shouldWork_forSentenceWithChoicesAndTextsAndMetaData() {
        val s = Sentence(
            null,
            mutableListOf(
                SentenceElement(
                    texts = mapOf("1" to "2", "3" to "4"),
                    metadata = mapOf("a" to "b", "c" to "d"),
                    choices = listOf(
                        Choice("intent1"),
                        Choice(
                            "intent2", mapOf("1" to "2", "3" to "4")
                        )
                    )
                )
            )
        )
        assertEquals(s, MessageParser.parse(s.toPrettyString()).first())
    }

    @Test
    fun parse_shouldWork_forSentenceWithChoicesAndTextsAndMetaDataAndSubElements() {
        val s = Sentence(
            null,
            mutableListOf(
                SentenceElement(
                    texts = mapOf("1" to "2", "3" to "4"),
                    metadata = mapOf("a" to "b", "c" to "d"),
                    choices = listOf(
                        Choice("intent1"),
                        Choice(
                            "intent2", mapOf("1" to "2", "3" to "4")
                        )
                    ),
                    subElements = listOf(
                        SentenceSubElement(
                            texts = mapOf("1" to "2", "3" to "4"),
                            metadata = mapOf("a" to "b", "c" to "d"),
                            choices = listOf(
                                Choice("intent1"),
                                Choice(
                                    "intent2", mapOf("1" to "2", "3" to "4")
                                )
                            )
                        ),
                        SentenceSubElement(
                            texts = mapOf("1" to "2", "3" to "4"),
                            metadata = mapOf("a" to "b", "c" to "d"),
                            choices = listOf(
                                Choice("intent1"),
                                Choice(
                                    "intent2", mapOf("1" to "2", "3" to "4")
                                )
                            )
                        )
                    )
                )
            )
        )
        assertEquals(s, MessageParser.parse(s.toPrettyString()).first())
    }

    @Test
    fun parse_withMutliMessagesSeparator_shouldReturnsAllMessages() {
        val s = Sentence("ok")
        val c = Choice("intent")

        assertEquals(listOf(s, c), MessageParser.parse("${s.toPrettyString()} |_| ${c.toPrettyString()}"))
    }

}
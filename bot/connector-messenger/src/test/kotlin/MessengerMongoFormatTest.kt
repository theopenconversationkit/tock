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

import ai.tock.bot.connector.messenger.model.send.Attachment
import ai.tock.bot.connector.messenger.model.send.AttachmentMessage
import ai.tock.bot.connector.messenger.model.send.AttachmentType
import ai.tock.bot.connector.messenger.model.send.ButtonPayload
import ai.tock.bot.connector.messenger.model.send.Element
import ai.tock.bot.connector.messenger.model.send.GenericPayload
import ai.tock.bot.connector.messenger.model.send.PostbackButton
import ai.tock.bot.connector.messenger.model.send.TextMessage
import ai.tock.bot.connector.messenger.model.send.UrlButton
import ai.tock.shared.jackson.AnyValueWrapper
import ai.tock.shared.sharedTestModule
import ai.tock.shared.tockInternalInjector
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import org.bson.RawBsonDocument
import org.bson.json.JsonMode
import org.bson.json.JsonWriterSettings
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.util.KMongoConfiguration
import kotlin.test.assertEquals

/**
 * The connector messages are stored in Mongo: see `ConnectorMessageCol`.
 * The documents of `mongo-format` have been written by KMongo 5.12 (Jackson 2).
 */
class MessengerMongoFormatTest {
    data class Messages(
        val messages: List<AnyValueWrapper?>,
        val after: String = "after",
    )

    private val settings =
        JsonWriterSettings
            .builder()
            .outputMode(JsonMode.EXTENDED)
            .indent(true)
            .build()

    @BeforeEach
    fun initMapping() {
        tockInternalInjector = KodeinInjector()
        tockInternalInjector.inject(Kodein { import(sharedTestModule) })
        // internal to tock-shared
        Class.forName("ai.tock.shared.TockKMongoConfiguration").getField("INSTANCE").get(null)
    }

    private fun toJson(bson: ByteArray): String = RawBsonDocument(bson).toJson(settings)

    private fun assertFormat(
        name: String,
        value: Any,
    ) {
        val bson = KMongoConfiguration.bsonMapper.writeValueAsBytes(value)
        val expected = checkNotNull(javaClass.getResource("/mongo-format/$name.json")) { "no document for $name" }.readText()
        assertEquals(expected, toJson(bson))
        val read = KMongoConfiguration.bsonMapper.readValue(bson, value.javaClass)
        assertEquals(expected, toJson(KMongoConfiguration.bsonMapper.writeValueAsBytes(read)))
    }

    @Test
    fun `text, buttons and generic messages`() =
        assertFormat(
            "messenger-messages",
            Messages(
                listOf(
                    AnyValueWrapper(TextMessage("text")),
                    AnyValueWrapper(
                        AttachmentMessage(
                            Attachment(
                                AttachmentType.template,
                                ButtonPayload("text", listOf(UrlButton("https://a.b", "title"), PostbackButton("payload", "title"))),
                            ),
                        ),
                    ),
                    AnyValueWrapper(
                        AttachmentMessage(
                            Attachment(
                                AttachmentType.template,
                                GenericPayload(listOf(Element("title", null, "subtitle", listOf(PostbackButton("payload", "title"))))),
                            ),
                        ),
                    ),
                ),
            ),
        )
}

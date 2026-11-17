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
package ai.tock.bot.connector.web.sse.channel

import ai.tock.bot.connector.web.WebConnectorResponseContent
import ai.tock.bot.connector.web.send.WebMessageContent
import ai.tock.shared.sharedTestModule
import ai.tock.shared.tockInternalInjector
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import org.bson.RawBsonDocument
import org.bson.json.JsonMode
import org.bson.json.JsonWriterSettings
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.toId
import org.litote.kmongo.util.KMongoConfiguration
import java.time.Instant

/**
 * The document of `mongo-format` has been written by KMongo 5.12 (Jackson 2).
 */
class ChannelEventMongoFormatTest {
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
    fun `ChannelEvent keeps its format`() =
        assertFormat(
            "channel-event",
            ChannelEvent(
                "app",
                "recipient",
                WebConnectorResponseContent(listOf(WebMessageContent("text")), mapOf("key" to "value")),
                ChannelEvent.Status.PROCESSED,
                Instant.parse("2026-11-05T10:15:30.123Z"),
                "id".toId(),
            ),
        )
}

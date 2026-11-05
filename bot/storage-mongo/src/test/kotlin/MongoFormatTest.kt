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
package ai.tock.bot.mongo

import ai.tock.bot.admin.answer.AnswerConfiguration
import ai.tock.bot.admin.answer.BuiltInAnswerConfiguration
import ai.tock.bot.admin.answer.ScriptAnswerConfiguration
import ai.tock.bot.admin.answer.ScriptAnswerVersionedConfiguration
import ai.tock.bot.admin.answer.SimpleAnswerConfiguration
import ai.tock.bot.admin.bot.ArtifactVersion
import ai.tock.bot.admin.bot.BotVersion
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.definition.Intent
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.dialog.EntityValue
import ai.tock.bot.engine.dialog.NextUserActionState
import ai.tock.bot.engine.message.Choice
import ai.tock.bot.engine.message.Message
import ai.tock.bot.engine.message.Sentence
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.UserLocation
import ai.tock.bot.mongo.DialogCol.DialogStateMongoWrapper
import ai.tock.bot.mongo.DialogCol.EntityStateValueWrapper
import ai.tock.bot.mongo.DialogCol.SendSentenceMongoWrapper
import ai.tock.bot.mongo.DialogCol.StoryMongoWrapper
import ai.tock.bot.mongo.UserTimelineCol.TimeBoxedFlagWrapper
import ai.tock.bot.mongo.UserTimelineCol.UserStateWrapper
import ai.tock.nlp.api.client.model.Entity
import ai.tock.nlp.api.client.model.EntityType
import ai.tock.nlp.api.client.model.NlpIntentQualifier
import ai.tock.nlp.entity.CustomValueWrapper
import ai.tock.nlp.entity.EmailValue
import ai.tock.nlp.entity.NumberValue
import ai.tock.nlp.entity.StringValue
import ai.tock.nlp.entity.UnknownValue
import ai.tock.nlp.entity.Value
import ai.tock.nlp.entity.ValueResolverRepository
import ai.tock.nlp.entity.date.DateEntityGrain
import ai.tock.nlp.entity.date.DateEntityValue
import ai.tock.shared.jackson.AnyValueWrapper
import ai.tock.shared.jackson.ConstrainedValueWrapper
import ai.tock.shared.jackson.addConstrainedTypes
import ai.tock.shared.jackson.mapper
import org.bson.RawBsonDocument
import org.bson.json.JsonMode
import org.bson.json.JsonWriterSettings
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.Id
import org.litote.kmongo.toId
import org.litote.kmongo.util.KMongoConfiguration
import java.time.Duration
import java.time.Instant
import java.time.Period
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * The documents of `mongo-format` have been written by KMongo 5.12 (Jackson 2):
 * the mapping must keep this format, and be able to read it.
 */
class MongoFormatTest : AbstractTest(false) {
    data class Times(
        val duration: Duration = Duration.ofSeconds(90, 500_000_000),
        val period: Period = Period.of(1, 2, 3),
        val zoneId: ZoneId = ZoneId.of("Europe/Paris"),
        val zoneOffset: ZoneOffset = ZoneOffset.ofHours(2),
        val instant: Instant = DATE,
        val zonedDateTime: ZonedDateTime = ZonedDateTime.ofInstant(DATE, ZoneId.of("UTC")),
    )

    data class Custom(
        val name: String = "custom",
        val date: Instant = DATE,
    )

    data class Values(
        val values: List<Value>,
    )

    data class NullableValues(
        val values: List<Value?>,
        val after: String = "after",
    )

    class NotRegistered(
        val name: String,
    ) {
        companion object {
            var created = false
        }

        init {
            created = true
        }
    }

    data class Ids(
        val id: Id<Custom>,
        val map: Map<Id<Custom>, String>,
    )

    data class Messages(
        val messages: List<Message>,
    )

    data class Answers(
        val answers: List<AnswerConfiguration>,
    )

    companion object {
        private val DATE = Instant.parse("2026-11-05T10:15:30.123Z")
        private val version = ArtifactVersion("1", "0", "0")
        private val settings =
            JsonWriterSettings
                .builder()
                .outputMode(JsonMode.EXTENDED)
                .indent(true)
                .build()
    }

    @BeforeEach
    fun initMapping() {
        // internal to tock-shared
        Class.forName("ai.tock.shared.TockKMongoConfiguration").getField("INSTANCE").get(null)
        addConstrainedTypes(setOf(Custom::class))
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
    fun `java time types`() = assertFormat("times", Times())

    @Test
    fun `AnyValueWrapper with a string`() = assertFormat("any-value-string", AnyValueWrapper("text"))

    @Test
    fun `AnyValueWrapper with an object`() = assertFormat("any-value-object", AnyValueWrapper(Custom()))

    @Test
    fun `AnyValueWrapper with a null value`() = assertFormat("any-value-null", AnyValueWrapper(Custom::class, null))

    @Test
    fun `ConstrainedValueWrapper with an object`() = assertFormat("constrained-value", ConstrainedValueWrapper(Custom()))

    @Test
    fun `values use the ids of the ValueResolverRepository`() =
        assertFormat(
            "values",
            Values(
                listOf(
                    StringValue("text"),
                    NumberValue(3),
                    EmailValue("a@b.c"),
                    DateEntityValue(ZonedDateTime.ofInstant(DATE, ZoneId.of("UTC")), DateEntityGrain.day),
                ),
            ),
        )

    @Test
    fun `the Tock mapper does not write the internal properties of StoryDefinitionConfiguration`() {
        val properties =
            mapper.serializerProviderInstance
                .findValueSerializer(StoryDefinitionConfiguration::class.java)
                .properties()
                .asSequence()
                .map { it.name }
                .toList()

        assertTrue("intent" in properties)
        assertFalse(properties.any { it.startsWith("mainIntent") })
    }

    @Test
    fun `CustomValueWrapper is written with its class name`() {
        val bson = KMongoConfiguration.bsonMapper.writeValueAsBytes(Values(listOf(CustomValueWrapper(Custom()))))
        assertEquals(javaClass.getResource("/mongo-format/custom-value.json")!!.readText(), toJson(bson))
    }

    @Test
    fun `CustomValueWrapper of a registered class is read by the Mongo mapping`() {
        ValueResolverRepository.registerCustomValueType(Custom::class)
        val values = NullableValues(listOf(CustomValueWrapper(Custom()), StringValue("next")))

        val bson = KMongoConfiguration.bsonMapper.writeValueAsBytes(values)

        assertEquals(values, KMongoConfiguration.bsonMapper.readValue(bson, NullableValues::class.java))
    }

    @Test
    fun `CustomValueWrapper of a registered class is read by the Tock mapper`() {
        ValueResolverRepository.registerCustomValueType(Custom::class)
        val values = NullableValues(listOf(CustomValueWrapper(Custom()), StringValue("next")))

        val json = mapper.writeValueAsString(values)

        assertEquals(values, mapper.readValue(json, NullableValues::class.java))
    }

    @Test
    fun `CustomValueWrapper of a class that is not registered is skipped without creating an instance`() {
        val document = """{"@type":"customWrapper","klass":"${NotRegistered::class.java.name}","value":{"name":"a"}}"""
        val json = """{"values":[$document,{"@type":"string","value":"next","candidates":[]}],"after":"after"}"""
        NotRegistered.created = false

        val read = mapper.readValue(json, NullableValues::class.java)
        val readFromBson =
            RawBsonDocument.parse(json).run {
                KMongoConfiguration.bsonMapper.readValue(byteBuffer.array().copyOf(byteBuffer.remaining()), NullableValues::class.java)
            }

        val expected = NullableValues(listOf(null, StringValue("next")))
        assertEquals(expected, read)
        assertEquals(expected, readFromBson)
        assertFalse(NotRegistered.created)
    }

    @Test
    fun `UnknownValue is read without its content`() {
        val bson = KMongoConfiguration.bsonMapper.writeValueAsBytes(Values(listOf(UnknownValue())))
        assertEquals(javaClass.getResource("/mongo-format/unknown-value.json")!!.readText(), toJson(bson))
        assertIs<UnknownValue>(
            KMongoConfiguration.bsonMapper
                .readValue(bson, Values::class.java)
                .values
                .single(),
        )
    }

    @Test
    fun `Id values and map keys`() = assertFormat("ids", Ids("id1".toId(), mapOf("id2".toId<Custom>() to "value")))

    @Test
    fun `DialogStateMongoWrapper with entity values and a context`() =
        assertFormat(
            "dialog-state",
            DialogStateMongoWrapper(
                Intent("test"),
                mapOf(
                    "role" to
                        EntityStateValueWrapper(
                            EntityValue(
                                0,
                                1,
                                Entity(EntityType("type"), "role"),
                                "content",
                                NumberValue(3),
                            ),
                            DATE,
                            "state".toId(),
                        ),
                ),
                mapOf("key" to AnyValueWrapper(Custom()), "empty" to null),
                UserLocation(1.0, 2.0),
                NextUserActionState(
                    listOf(NlpIntentQualifier("test")),
                    ZonedDateTime.ofInstant(DATE, ZoneId.of("UTC")),
                    ZoneId.of("Europe/Paris"),
                ),
            ),
        )

    @Test
    fun `StoryMongoWrapper with polymorphic actions`() =
        assertFormat(
            "story",
            StoryMongoWrapper(
                "story",
                Intent("test"),
                null,
                listOf(SendSentenceMongoWrapper(SendSentence(PlayerId("a"), "app", PlayerId("b"), "text", id = "action".toId(), date = DATE))),
            ),
        )

    @Test
    fun `UserStateWrapper flags`() {
        val bson =
            KMongoConfiguration.bsonMapper.writeValueAsBytes(
                UserStateWrapper(DATE, DATE, mapOf("flag" to TimeBoxedFlagWrapper(false, "value", DATE))),
            )
        assertEquals(javaClass.getResource("/mongo-format/user-state.json")!!.readText(), toJson(bson))
        val read = KMongoConfiguration.bsonMapper.readValue(bson, UserStateWrapper::class.java)
        assertEquals(TimeBoxedFlagWrapper(false, "value", DATE), read.flags["flag"])
    }

    @Test
    fun `UserStateWrapper ignores flags that are not an object`() {
        val document = RawBsonDocument.parse("""{"flags": ["old format"]}""")
        val bson = document.byteBuffer.array().copyOf(document.byteBuffer.remaining())
        assertTrue(
            KMongoConfiguration.bsonMapper
                .readValue(bson, UserStateWrapper::class.java)
                .flags
                .isEmpty(),
        )
    }

    @Test
    fun `ConnectorMessageCol generated serializer`() =
        assertFormat(
            "connector-message",
            ConnectorMessageCol(ConnectorMessageColId("a".toId(), "b".toId()), listOf(AnyValueWrapper(Custom()), null), DATE),
        )

    @Test
    fun `messages use the mixin type property`() = assertFormat("messages", Messages(listOf(Sentence("text"), Choice("intent", mapOf("a" to "b")))))

    @Test
    fun `answer configurations use the mixin type property and exclude the story definition`() =
        assertFormat(
            "answers",
            Answers(
                listOf(
                    SimpleAnswerConfiguration(emptyList()),
                    BuiltInAnswerConfiguration("handler"),
                    ScriptAnswerConfiguration(
                        listOf(ScriptAnswerVersionedConfiguration("script", emptyList(), BotVersion(version, version), "main", DATE)),
                    ),
                ),
            ),
        )
}

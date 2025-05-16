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

import ai.tock.bot.definition.Intent
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.EntityValue
import ai.tock.bot.engine.dialog.NextUserActionState
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.bot.engine.user.UserLocation
import ai.tock.bot.engine.user.UserTimeline
import ai.tock.bot.mongo.DialogCol.ActionMongoWrapper
import ai.tock.bot.mongo.DialogCol.DialogStateMongoWrapper
import ai.tock.bot.mongo.DialogCol.EntityStateValueWrapper
import ai.tock.bot.mongo.DialogCol.SendChoiceMongoWrapper
import ai.tock.bot.mongo.DialogCol.SendSentenceMongoWrapper
import ai.tock.bot.mongo.DialogCol.StoryMongoWrapper
import ai.tock.nlp.api.client.model.Entity
import ai.tock.nlp.api.client.model.EntityType
import ai.tock.nlp.api.client.model.NlpIntentQualifier
import ai.tock.shared.jackson.AnyValueWrapper
import ai.tock.shared.jackson.mapper
import ai.tock.shared.security.MapObfuscator
import ai.tock.shared.security.TockObfuscatorService
import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.litote.kmongo.toId
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 *
 */
class DialogColDeserializationTest : AbstractTest(false) {

    class TestParamObfuscator : MapObfuscator {
        override fun obfuscate(map: Map<String, String>): Map<String, String> {
            return map.mapValues { "" }
        }
    }

    @Test
    fun serializeAndDeserializeAnyValueWrapper_shouldLeftDataInchanged() {
        val value = AnyValueWrapper(
            UserLocation::class,
            UserLocation(1.0, 2.0)
        )
        val s = mapper.writeValueAsString(value)
        val newValue = mapper.readValue<AnyValueWrapper>(s)
        assertEquals(value, newValue)
    }

    @Test
    fun serializeAndDeserializeStateMongoWrapper_shouldLeftDataInchanged() {
        val state = DialogStateMongoWrapper(
            Intent("test"),
            mapOf(
                "role" to EntityStateValueWrapper(
                    EntityValue(
                        0,
                        1,
                        Entity(EntityType("type"), "role"),
                        "content"
                    )
                )
            ),
            emptyMap(),
            UserLocation(1.0, 2.0),
            NextUserActionState(
                listOf(NlpIntentQualifier("test")),
                ZonedDateTime.now().withZoneSameInstant(ZoneId.of("Z")),
                ZoneId.systemDefault()
            )
        )
        val s = mapper.writeValueAsString(state)
        val newValue = mapper.readValue<DialogStateMongoWrapper>(s)
        assertEquals(state, newValue)
    }

    @Test
    fun serializeAndDeserializeDialog_shouldLeftDataInchanged() {
        val dialog = Dialog(
            emptySet(),
            state = ai.tock.bot.engine.dialog.DialogState(context = mutableMapOf("a" to LocalDateTime.now()))
        )
        val playerId = PlayerId("a", PlayerType.user)
        val s = mapper.writeValueAsString(
            DialogCol(
                dialog,
                UserTimelineCol(
                    "id",
                    "namespace",
                    UserTimeline(
                        playerId
                    ),
                    null
                )
            )
        )
        val newValue = mapper.readValue<DialogCol>(s)
        assertEquals(dialog, newValue.toDialog { mockk() })
    }

    @Test
    fun `GIVEN a parameter obfuscator WHEN serializing a SendChoiceMongoWrapper instantiated from SendChoice THEN obfuscates the parameters`() {

        val testParameterObfuscator = spyk(TestParamObfuscator())
        TockObfuscatorService.registerMapObfuscator(testParameterObfuscator)
        val parameters: Map<String, String> = mapOf("key" to "value")

        val choice = SendChoice(
            PlayerId(
                UUID.randomUUID().toString()
            ),
            "",
            PlayerId(
                UUID.randomUUID().toString()
            ),
            "test",
            parameters
        )
        val stateWrapper = SendChoiceMongoWrapper(choice)

        assertTrue {
            stateWrapper.parameters.all { it.value == "" }
        }
        verify(exactly = 1) { testParameterObfuscator.obfuscate(parameters) }
        TockObfuscatorService.deregisterObfuscators()

        // test deserialization
        val json = mapper.writeValueAsString(stateWrapper)
        val stateWrapper2: ActionMongoWrapper = mapper.readValue(json)
        assertEquals(stateWrapper.toAction("id".toId()).toString(), stateWrapper2.toAction("id".toId()).toString())
    }

    @Test
    fun `StoryMongoWrapper can be serialized and deserialized correctly`() {
        val s = StoryMongoWrapper(
            "a", null, null,
            listOf(SendSentenceMongoWrapper(SendSentence(PlayerId("a"), "app", PlayerId("b"), "text")))
        )
        println(mapper.writeValueAsString(s))
        val r = mapper.readValue<StoryMongoWrapper>(mapper.writeValueAsString(s))
        assertEquals(s, r)
    }

    @Test
    fun `deserialization of unknown value is ok`() {
        val json = this::class.java.getResourceAsStream("/dialog.json")
        val col: DialogCol? = mapper.readValue(json)
        assertNotNull(col)
    }
}

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

package fr.vsct.tock.bot.engine.user

import com.fasterxml.jackson.module.kotlin.readValue
import fr.vsct.tock.bot.definition.Intent
import fr.vsct.tock.bot.engine.dialog.ContextValue
import fr.vsct.tock.bot.mongo.DialogCol.AnyValueMongoWrapper
import fr.vsct.tock.bot.mongo.DialogCol.EntityStateValueWrapper
import fr.vsct.tock.bot.mongo.DialogCol.StateMongoWrapper
import fr.vsct.tock.shared.jackson.mapper
import ft.vsct.tock.nlp.api.client.model.Entity
import ft.vsct.tock.nlp.api.client.model.EntityType
import org.junit.Test
import kotlin.test.assertEquals

/**
 *
 */
class DialogColDeserializationTest {

    @Test
    fun testAnyValueMongoWrapperSerialization() {
        val value = AnyValueMongoWrapper(
                UserLocation::class.java,
                UserLocation(1.0, 2.0))
        val s = mapper.writeValueAsString(value)
        val newValue = mapper.readValue<AnyValueMongoWrapper>(s)
        assertEquals(value, newValue)
    }

    @Test
    fun testStateMongoWrapperDeserializtion() {
        val state = StateMongoWrapper(
                Intent("test"),
                mapOf("role" to EntityStateValueWrapper(
                        ContextValue(
                                0,
                                1,
                                Entity(EntityType("type"), "role"),
                                "content"
                        ),
                        emptyList()
                )),
                emptyMap()
        )
        val s = mapper.writeValueAsString(state)
        val newValue = mapper.readValue<StateMongoWrapper>(s)
        assertEquals(state, newValue)
    }

}
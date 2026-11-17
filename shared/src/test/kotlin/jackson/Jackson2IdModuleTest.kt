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
package ai.tock.shared.jackson

import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.Id
import org.litote.kmongo.id.IdGenerator
import org.litote.kmongo.id.ObjectIdToStringGenerator
import org.litote.kmongo.toId
import kotlin.test.assertEquals

class Jackson2IdModuleTest {
    data class Ids(
        val id: Id<Ids>,
        val map: Map<Id<Ids>, String>,
    )

    private val initialGenerator = IdGenerator.defaultGenerator

    // Tock sets this generator in TockKMongoConfiguration: it must not depend on the test order
    @BeforeEach
    fun setGenerator() {
        IdGenerator.defaultGenerator = ObjectIdToStringGenerator
    }

    @AfterEach
    fun restoreGenerator() {
        IdGenerator.defaultGenerator = initialGenerator
    }

    @Test
    fun `the Tock mapper writes and reads ids and map keys as strings`() {
        val ids = Ids("id1".toId(), mapOf("id2".toId<Ids>() to "value"))

        val json = mapper.writeValueAsString(ids)

        assertEquals("""{"id":"id1","map":{"id2":"value"}}""", json)
        assertEquals(ids, mapper.readValue<Ids>(json))
    }
}

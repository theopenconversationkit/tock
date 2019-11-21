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

package ai.tock.shared

import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import org.litote.kmongo.util.KMongoConfiguration
import java.time.Period
import kotlin.test.assertEquals

/**
 *
 */
class MongoTest {

    class ThisIsACollection

    @Test
    fun collectionBuilder_shouldAddUnderscore_forEachUpperCase() {
        assertEquals("this_is_a_collection", collectionBuilder.invoke(ThisIsACollection::class))
    }

    data class TestPeriod(val p: Period)

    @Test
    fun `GIVEN serialized Period THEN KMongo mapper can deserialize it`() {
        TockKMongoConfiguration.configure()
        val test = TestPeriod(Period.ofDays(22))
        val json = KMongoConfiguration.extendedJsonMapper.writeValueAsString(test)
        assertEquals(test, KMongoConfiguration.extendedJsonMapper.readValue(json))
    }
}
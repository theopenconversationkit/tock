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

package ai.tock.bot.admin.model.evaluation

import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * Tests for evaluation model serialization/deserialization.
 * Vérifie EvaluationDialogsQuery (valeurs par défaut) et structure des réponses.
 */
class EvaluationModelTest {
    @Test
    fun `EvaluationDialogsQuery deserializes empty JSON with defaults`() {
        val query: EvaluationDialogsQuery = mapper.readValue("{}")
        assertEquals(0, query.start)
        assertEquals(10, query.size)
    }

    @Test
    fun `EvaluationDialogsQuery deserializes with explicit values`() {
        val query: EvaluationDialogsQuery = mapper.readValue("""{"start":10,"size":5}""")
        assertEquals(10, query.start)
        assertEquals(5, query.size)
    }

    @Test
    fun `EvaluationDialogsQuery serializes to JSON`() {
        val query = EvaluationDialogsQuery(start = 0, size = 20)
        val json = mapper.writeValueAsString(query)
        val back: EvaluationDialogsQuery = mapper.readValue(json)
        assertEquals(query.start, back.start)
        assertEquals(query.size, back.size)
    }
}

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

package ai.tock.nlp.core.service.entity

import ai.tock.nlp.core.DictionaryData
import ai.tock.nlp.core.EntityType
import ai.tock.nlp.core.NlpEngineType
import ai.tock.nlp.core.PredefinedValue
import ai.tock.nlp.entity.StringValue
import ai.tock.nlp.model.EntityCallContextForEntity
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PredefinedValuesEntityEvaluatorTest {

    private val context = EntityCallContextForEntity(
        EntityType("namespace:frequency", dictionary = true),
        Locale.FRENCH,
        NlpEngineType.stanford,
        "test",
        ZonedDateTime.now()
    )

    @BeforeEach
    fun fillDictionary() {
        DictionaryRepositoryService.updateData(
            listOf(
                DictionaryData(
                    "namespace",
                    "frequency",
                    listOf(
                        PredefinedValue(
                            "Annuel",
                            mapOf(
                                Pair(Locale.FRENCH, listOf("Année", "annee"))
                            )
                        ),
                        PredefinedValue(
                            "Mensuel",
                            mapOf(
                                Pair(Locale.FRENCH, listOf("Mois"))
                            )
                        ),
                        PredefinedValue(
                            "Semaine",
                            mapOf(
                                Pair(Locale.FRENCH, listOf("Hebdomadaire")),
                                Pair(Locale.ENGLISH, listOf("Week"))
                            )
                        ),
                        PredefinedValue(
                            "Jour",
                            mapOf(
                                Pair(Locale.FRENCH, listOf("Journalier", "Quotidien"))
                            )
                        )
                    )
                )
            )
        )
    }

    @AfterEach
    fun cleanupDictionary() {
        DictionaryRepositoryService.updateData(emptyList())
    }

    @Test
    fun should_evaluate_frequency_day_with_synonym_value_found() {

        val evaluationResult = DictionaryEntityTypeEvaluator.evaluate(context, "Quotidien")

        assertTrue(evaluationResult.evaluated)
        assertEquals(1.0, evaluationResult.probability)
        assertEquals("Jour", (evaluationResult.value as StringValue).value)
    }

    @Test
    fun should_evaluate_frequency_week_with_synonym_value_not_found() {

        val evaluationResult = DictionaryEntityTypeEvaluator.evaluate(context, "Week")

        assertTrue(evaluationResult.evaluated)
        assertEquals(1.0, evaluationResult.probability)
        assertNull(evaluationResult.value)
    }

    @Test
    fun should_evaluate_frequency_day_with_synonym_value_not_found_but_near() {

        val evaluationResult = DictionaryEntityTypeEvaluator.evaluate(context, "Quotidienne")

        assertTrue(evaluationResult.evaluated)
        assertEquals(0.8181818127632141, evaluationResult.probability)
        assertEquals("Jour", (evaluationResult.value as StringValue).value)
    }
}

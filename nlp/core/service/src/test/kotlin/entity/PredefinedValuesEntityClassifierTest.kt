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
import ai.tock.nlp.core.Entity
import ai.tock.nlp.core.EntityType
import ai.tock.nlp.core.Intent
import ai.tock.nlp.core.NlpEngineType
import ai.tock.nlp.core.PredefinedValue
import ai.tock.nlp.model.EntityCallContextForIntent
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import java.util.Locale

class PredefinedValuesEntityClassifierTest {
    val entityType = EntityType("namespace:pizza", dictionary = true)
    val context =
        EntityCallContextForIntent(
            Intent("eat", listOf(Entity(entityType, "pizza"))),
            Locale.FRENCH,
            NlpEngineType.stanford,
            "pizzayolo",
            ZonedDateTime.now(),
        )

    @BeforeEach
    fun fillDictionary() {
        DictionaryRepositoryService.updateData(
            listOf(
                DictionaryData(
                    "namespace",
                    "pizza",
                    listOf(
                        PredefinedValue(
                            "pizza",
                            mapOf(
                                Pair(Locale.FRENCH, listOf("4 fromages", "napolitaine", "calzone")),
                                Pair(Locale.ITALIAN, listOf("4 formaggi", "napoletana", "calzone")),
                            ),
                        ),
                    ),
                ),
            ),
        )
    }

    @AfterEach
    fun cleanupDictionary() {
        DictionaryRepositoryService.updateData(emptyList())
    }

    @Test
    fun `classifier recognized label a end of sentence`() {
        val text = "Je voudrais manger une napolitaine"

        val entityTypeRecognitions = DictionaryEntityTypeClassifier.classifyEntities(context, text)

        Assertions.assertEquals(
            listOf(
                EntityTypeRecognition(
                    EntityTypeValue(23, 34, entityType, "pizza", true),
                    1.0,
                ),
            ),
            entityTypeRecognitions,
        )
    }

    @Test
    fun `classifier recognized label a start of sentence`() {
        val text = "Napolitaine stp!"

        val entityTypeRecognitions = DictionaryEntityTypeClassifier.classifyEntities(context, text)

        Assertions.assertEquals(
            listOf(
                EntityTypeRecognition(
                    EntityTypeValue(0, 11, entityType, "pizza", true),
                    1.0,
                ),
            ),
            entityTypeRecognitions,
        )
    }

    @Test
    fun `classifier recognized label if label equals to sentence`() {
        val text = "Napolitaine"

        val entityTypeRecognitions = DictionaryEntityTypeClassifier.classifyEntities(context, text)

        Assertions.assertEquals(
            listOf(
                EntityTypeRecognition(
                    EntityTypeValue(0, 11, entityType, "pizza", true),
                    1.0,
                ),
            ),
            entityTypeRecognitions,
        )
    }

    @Test
    fun `classifier recognized label contaiones in a sentence`() {
        val text = "une Napolitaine stp"

        val entityTypeRecognitions = DictionaryEntityTypeClassifier.classifyEntities(context, text)

        Assertions.assertEquals(
            listOf(
                EntityTypeRecognition(
                    EntityTypeValue(4, 15, entityType, "pizza", true),
                    1.0,
                ),
            ),
            entityTypeRecognitions,
        )
    }

    @Test
    fun `classifier regognized only exact match`() {
        val text = "une Napolitaine Napolitaine-Man stp"

        val entityTypeRecognitions = DictionaryEntityTypeClassifier.classifyEntities(context, text)

        Assertions.assertEquals(
            listOf(
                EntityTypeRecognition(
                    EntityTypeValue(4, 15, entityType, "pizza", true),
                    1.0,
                ),
            ),
            entityTypeRecognitions,
        )
    }

    @Test
    fun `classifier regognized multi matches`() {
        val text = "une Napolitaine stp, je voudrais une napolitaine , oui une napolitaine"

        val entityTypeRecognitions = DictionaryEntityTypeClassifier.classifyEntities(context, text)

        Assertions.assertEquals(
            listOf(
                EntityTypeRecognition(EntityTypeValue(4, 15, entityType, "pizza", true), 1.0),
                EntityTypeRecognition(EntityTypeValue(37, 48, entityType, "pizza", true), 1.0),
                EntityTypeRecognition(EntityTypeValue(59, 70, entityType, "pizza", true), 1.0),
            ),
            entityTypeRecognitions,
        )
    }
}

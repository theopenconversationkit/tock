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

package ai.tock.nlp.opennlp

import ai.tock.nlp.core.Entity
import ai.tock.nlp.core.EntityRecognition
import ai.tock.nlp.core.EntityType
import ai.tock.nlp.core.EntityValue
import ai.tock.nlp.core.Intent
import ai.tock.nlp.core.NlpEngineType
import ai.tock.nlp.core.configuration.NlpApplicationConfiguration.Companion.EMPTY_CONFIGURATION
import ai.tock.nlp.model.EntityCallContextForIntent
import ai.tock.nlp.model.service.engine.EntityModelHolder
import ai.tock.shared.defaultLocale
import io.mockk.every
import io.mockk.mockk
import opennlp.tools.namefind.NameFinderME
import opennlp.tools.util.Span
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import kotlin.test.assertEquals

/**
 *
 */
class OpenNlpEntityClassifierTest {
    @Test
    fun classify_withAdjacentEntitiesOfSameRole_shouldMergeEntities() {
        val entity = Entity(EntityType("test:test"), "test")
        val context =
            EntityCallContextForIntent(
                Intent("test", listOf(entity)),
                defaultLocale,
                NlpEngineType.opennlp,
                "test",
                ZonedDateTime.now(),
            )
        val text = "a b"
        val tokens = arrayOf("a", "b")
        val model: NameFinderME = mockk()
        every { model.find(eq(tokens)) } answers { arrayOf(Span(0, 1, "test", 0.8), Span(1, 2, "test", 0.6)) }

        val classifier = OpenNlpEntityClassifier(EntityModelHolder(model, EMPTY_CONFIGURATION))

        val result = classifier.classifyEntities(context, text, tokens)

        println(result)
        assertEquals(listOf(EntityRecognition(EntityValue(0, 3, entity), 0.7)), result)
    }

    @Test
    fun classify_withAdjacentMultiTokensEntitiesOfSameRole_shouldMergeEntities() {
        val entity = Entity(EntityType("test:test"), "test")
        val context =
            EntityCallContextForIntent(
                Intent("test", listOf(entity)),
                defaultLocale,
                NlpEngineType.opennlp,
                "test",
                ZonedDateTime.now(),
            )
        val text = "a a b"
        val tokens = arrayOf("a", "a", "b")
        val model: NameFinderME = mockk()
        every { model.find(eq(tokens)) } answers { arrayOf(Span(0, 2, "test", 0.8), Span(2, 3, "test", 0.6)) }

        val classifier = OpenNlpEntityClassifier(EntityModelHolder(model, EMPTY_CONFIGURATION))

        val result = classifier.classifyEntities(context, text, tokens)

        println(result)
        assertEquals(listOf(EntityRecognition(EntityValue(0, 5, entity), 0.7)), result)
    }

    @Test
    fun classify_withNotAdjacentEntitiesOfSameRole_shouldReturnsTwoEntities() {
        val entity = Entity(EntityType("test:test"), "test")
        val context =
            EntityCallContextForIntent(
                Intent("test", listOf(entity)),
                defaultLocale,
                NlpEngineType.opennlp,
                "test",
                ZonedDateTime.now(),
            )
        val text = "a toto b"
        val tokens = arrayOf("a", "toto", "b")
        val model: NameFinderME = mockk()
        every { model.find(eq(tokens)) } answers { arrayOf(Span(0, 1, "test", 0.8), Span(2, 3, "test", 0.6)) }

        val classifier = OpenNlpEntityClassifier(EntityModelHolder(model, EMPTY_CONFIGURATION))

        val result = classifier.classifyEntities(context, text, tokens)

        println(result)
        assertEquals(
            listOf(
                EntityRecognition(EntityValue(0, 1, entity), 0.8),
                EntityRecognition(EntityValue(7, 8, entity), 0.6),
            ),
            result,
        )
    }

    @Test
    fun `classify does not throw AIOOBE even if the whole sentence is an entity`() {
        val sentence = "Je cherche un train de Lille à Paris demain"
        val tokens = arrayOf("Je", "cherche", "un", "train", "de", "Lille", "à", "Paris", "demain")
        val model: NameFinderME = mockk()
        every { model.find(eq(tokens)) } answers {
            arrayOf(
                Span(0, 1, "location", 0.55),
                Span(1, 2, "location", 0.55),
                Span(2, 3, "location", 0.55),
                Span(3, 4, "location", 0.55),
                Span(4, 5, "location", 0.55),
                Span(5, 6, "location", 0.55),
                Span(6, 7, "location", 0.55),
                Span(7, 8, "location", 0.55),
                Span(8, 9, "location", 0.55),
            )
        }

        val classifier = OpenNlpEntityClassifier(EntityModelHolder(model, EMPTY_CONFIGURATION))

        val entity = Entity(EntityType("location:location"), "location")
        val context =
            EntityCallContextForIntent(
                Intent("test", listOf(entity)),
                defaultLocale,
                NlpEngineType.opennlp,
                "test",
                ZonedDateTime.now(),
            )

        val result = classifier.classifyEntities(context, sentence, tokens)

        assertEquals(
            listOf(
                EntityRecognition(
                    value =
                        EntityValue(
                            start = 0,
                            end = 43,
                            entity =
                                Entity(
                                    entityType =
                                        EntityType(
                                            name = "location:location",
                                            subEntities = emptyList(),
                                        ),
                                    role = "location",
                                ),
                            value = null,
                            subEntities = emptyList(),
                            evaluated = false,
                        ),
                    probability = 0.5499999999999999,
                ),
            ),
            result,
        )
    }
}

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

package fr.vsct.tock.nlp.opennlp

import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import fr.vsct.tock.nlp.core.Entity
import fr.vsct.tock.nlp.core.EntityRecognition
import fr.vsct.tock.nlp.core.EntityType
import fr.vsct.tock.nlp.core.EntityValue
import fr.vsct.tock.nlp.core.Intent
import fr.vsct.tock.nlp.core.NlpEngineType
import fr.vsct.tock.nlp.model.EntityCallContextForIntent
import fr.vsct.tock.nlp.model.service.engine.EntityModelHolder
import fr.vsct.tock.shared.defaultLocale
import opennlp.tools.namefind.NameFinderME
import opennlp.tools.util.Span
import org.junit.Test
import java.time.ZonedDateTime
import kotlin.test.assertEquals

/**
 *
 */
class OpenNlpEntityClassifierTest {

    @Test
    fun classify_withAdjacentEntitiesOfSameRole_shouldMergeEntities() {
        val entity = Entity(EntityType("test:test"), "test")
        val context = EntityCallContextForIntent(
                "test",
                Intent("test", listOf(entity)),
                defaultLocale,
                NlpEngineType.opennlp,
                ZonedDateTime.now())
        val text = "a b"
        val tokens = arrayOf("a", "b")
        val model: NameFinderME = mock()
        whenever(model.find(eq(tokens))).thenReturn(arrayOf(Span(0, 1, "test", 0.8), Span(1, 2, "test", 0.6)))

        val classifier = OpenNlpEntityClassifier(EntityModelHolder(model))

        val result = classifier.classifyEntities(context, text, tokens)

        println(result)
        assertEquals(listOf(EntityRecognition(EntityValue(0, 3, entity), 0.7)), result)
    }

    @Test
    fun classify_withAdjacentMultiTokensEntitiesOfSameRole_shouldMergeEntities() {
        val entity = Entity(EntityType("test:test"), "test")
        val context = EntityCallContextForIntent(
                "test",
                Intent("test", listOf(entity)),
                defaultLocale,
                NlpEngineType.opennlp,
                ZonedDateTime.now())
        val text = "a a b"
        val tokens = arrayOf("a", "a", "b")
        val model: NameFinderME = mock()
        whenever(model.find(eq(tokens))).thenReturn(arrayOf(Span(0, 2, "test", 0.8), Span(2, 3, "test", 0.6)))

        val classifier = OpenNlpEntityClassifier(EntityModelHolder(model))

        val result = classifier.classifyEntities(context, text, tokens)

        println(result)
        assertEquals(listOf(EntityRecognition(EntityValue(0, 5, entity), 0.7)), result)
    }

    @Test
    fun classify_withNotAdjacentEntitiesOfSameRole_shouldReturnsTwoEntities() {
        val entity = Entity(EntityType("test:test"), "test")
        val context = EntityCallContextForIntent(
                "test",
                Intent("test", listOf(entity)),
                defaultLocale,
                NlpEngineType.opennlp,
                ZonedDateTime.now())
        val text = "a toto b"
        val tokens = arrayOf("a", "toto", "b")
        val model: NameFinderME = mock()
        whenever(model.find(eq(tokens))).thenReturn(arrayOf(Span(0, 1, "test", 0.8), Span(2, 3, "test", 0.6)))

        val classifier = OpenNlpEntityClassifier(EntityModelHolder(model))

        val result = classifier.classifyEntities(context, text, tokens)

        println(result)
        assertEquals(
                listOf(
                        EntityRecognition(EntityValue(0, 1, entity), 0.8),
                        EntityRecognition(EntityValue(7, 8, entity), 0.6)
                ), result)
    }
}
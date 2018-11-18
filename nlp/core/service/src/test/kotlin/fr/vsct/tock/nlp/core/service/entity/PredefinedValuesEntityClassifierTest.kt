/*
 * Copyright (C) 2018 VSCT
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

package fr.vsct.tock.nlp.core.service.entity

import fr.vsct.tock.nlp.core.Entity
import fr.vsct.tock.nlp.core.EntityType
import fr.vsct.tock.nlp.core.Intent
import fr.vsct.tock.nlp.core.NlpEngineType
import fr.vsct.tock.nlp.core.PredefinedValue
import fr.vsct.tock.nlp.model.EntityCallContextForIntent
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import java.util.Locale

class PredefinedValuesEntityClassifierTest {

    @Test
    fun qualify_predefined_values() {

        val text = "Je voudrais manger une napolitaine"

        val entityType = EntityType(
            "pizza", predefinedValues = listOf(
                PredefinedValue(
                    "pizza", mapOf(
                        Pair(Locale.FRENCH, listOf("4 fromages", "napolitaine", "calzone")),
                        Pair(Locale.ITALIAN, listOf("4 formaggi", "napoletana", "calzone"))
                    )
                )
            )
        )

        val context = EntityCallContextForIntent(
            Intent("eat", listOf(Entity(entityType, "pizza"))),
            Locale.FRENCH,
            NlpEngineType.stanford,
            "pizzayolo",
            ZonedDateTime.now())

        val entityTypeRecognitions = PredefinedValuesEntityClassifier.classifyEntities(context, text)

        Assertions.assertEquals(listOf(
            EntityTypeRecognition(
                EntityTypeValue(23, 34, entityType, "pizza", true), 1.0)),
            entityTypeRecognitions)

    }

}
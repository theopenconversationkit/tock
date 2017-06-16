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

package fr.vsct.tock.nlp.core.service.entity

import fr.vsct.tock.nlp.core.Entity
import fr.vsct.tock.nlp.core.EntityRecognition
import fr.vsct.tock.nlp.core.EntityType
import fr.vsct.tock.nlp.core.EntityValue
import fr.vsct.tock.nlp.core.Intent
import org.junit.Test

/**
 *
 */
class EntityMergeServiceTest {

    @Test
    fun mergeEntityTypes_shouldReturnsTomorrowMorning_WhenTomorrowIsDetectedByTheModelAndTomorrowMorningByTheEntityEvaluator() {
        val entityType = EntityType("test")
        val dateEntity = Entity(entityType, "test")
        val tomorrow = EntityRecognition(EntityValue(0, "Tomorrow".length, dateEntity), 0.98)
        val tomorrowMorning = EntityTypeRecognition(EntityTypeValue(0, "Tomorrow morning".length, entityType), 0.8)
        val result = EntityMergeService.mergeEntityTypes(
                Intent("test", emptyList()),
                listOf(tomorrow),
                listOf(tomorrowMorning))
        println(result)
    }
}
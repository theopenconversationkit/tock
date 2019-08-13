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

package fr.vsct.tock.nlp.front.storage.mongo

import fr.vsct.tock.nlp.core.DictionaryData
import fr.vsct.tock.nlp.core.PredefinedValue
import fr.vsct.tock.nlp.front.service.storage.EntityTypeDefinitionDAO
import fr.vsct.tock.nlp.front.shared.config.EntityTypeDefinition
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.provide
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.util.Locale.ENGLISH
import kotlin.test.assertEquals

/**
 *
 */
internal class EntityTypeDefinitionMongoDAOTest : AbstractTest() {

    val entityType = EntityTypeDefinition(
        "test:desc",
        "desc",
        dictionary = true
    )

    val dictionary = DictionaryData("test", "desc", listOf(
        PredefinedValue(
            "A",
            mapOf(ENGLISH to listOf("B", "C"))
        )
    ))

    val entityTypeDAO: EntityTypeDefinitionDAO get() = injector.provide()

    @AfterEach
    fun cleanup() {
        entityTypeDAO.deleteEntityTypeByName(entityType.name)
    }

    @Test
    fun `deletePredefinedValueByName deletes a value by name`() {
        entityTypeDAO.save(entityType)
        entityTypeDAO.save(dictionary)
        assertEquals(dictionary.values, entityTypeDAO.getDictionaryDataByEntityName(entityType.name)?.values)
        entityTypeDAO.deletePredefinedValueByName(entityType.name, "A")
        assertEquals(emptyList(), entityTypeDAO.getDictionaryDataByEntityName(entityType.name)?.values)
    }

    @Test
    fun `deletePredefinedValueLabelByName deletes a label by name`() {
        entityTypeDAO.save(entityType)
        entityTypeDAO.save(dictionary)
        assertEquals(dictionary.values, entityTypeDAO.getDictionaryDataByEntityName(entityType.name)?.values)
        entityTypeDAO.deletePredefinedValueLabelByName(entityType.name, "A", ENGLISH, "C")
        assertEquals(
            listOf("B"),
            entityTypeDAO.getDictionaryDataByEntityName(entityType.name)?.values?.first()?.labels?.get(ENGLISH)
        )
    }

}
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

package ai.tock.nlp.front.service.storage

import ai.tock.nlp.core.DictionaryData
import ai.tock.nlp.front.shared.config.EntityTypeDefinition
import java.util.Locale

/**
 *
 */
interface EntityTypeDefinitionDAO {

    /**
     * Listen changes on entity type definitions.
     */
    fun listenEntityTypeChanges(listener: () -> Unit)

    fun save(entityType: EntityTypeDefinition)

    fun getEntityTypeByName(name: String): EntityTypeDefinition?

    fun getEntityTypes(): List<EntityTypeDefinition>

    fun deleteEntityTypeByName(name: String): Boolean

    /**
     * Save [DictionaryData].
     */
    fun save(data: DictionaryData)

    /**
     * Load all dictionary data.
     */
    fun getAllDictionaryData(): List<DictionaryData>

    /**
     * Listen all dictionary data changes
     */
    fun listenDictionaryDataChanges(listener: () -> Unit)

    /**
     * Load DictionaryData for entity qualified name.
     */
    fun getDictionaryDataByEntityName(qualifiedName: String): DictionaryData?

    /**
     * Load all data of given namespace.
     */
    fun getDictionaryDataByNamespace(namespace: String): List<DictionaryData>

    fun deletePredefinedValueByName(entityTypeName: String, predefinedValue: String)

    fun deletePredefinedValueLabelByName(entityTypeName: String, predefinedValue: String, locale: Locale, label: String)
}

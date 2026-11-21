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
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.provideOrDefault
import java.util.Locale

/**
 * The part of [EntityTypeDefinitionDAO] that the NLP API uses at runtime: the parsing and the configuration it caches.
 *
 * A storage that only serves the NLP API (a serverless one) binds this interface. Otherwise, [fromInjector] adapts
 * the bound [EntityTypeDefinitionDAO]: the existing storages and the projects that replace them keep working unchanged.
 */
interface EntityTypeDefinitionRuntimeDAO {
    fun listenEntityTypeChanges(listener: () -> Unit)

    fun save(entityType: EntityTypeDefinition)

    fun getEntityTypes(): List<EntityTypeDefinition>

    fun save(data: DictionaryData)

    fun getAllDictionaryData(): List<DictionaryData>

    fun listenDictionaryDataChanges(listener: () -> Unit)

    companion object {
        /**
         * The bound [EntityTypeDefinitionRuntimeDAO], or the bound [EntityTypeDefinitionDAO] adapted to it.
         */
        fun fromInjector(): EntityTypeDefinitionRuntimeDAO = injector.provideOrDefault<EntityTypeDefinitionRuntimeDAO> { injector.provide<EntityTypeDefinitionDAO>().asRuntimeDAO() }
    }
}

/**
 * Adapts this DAO to [EntityTypeDefinitionRuntimeDAO].
 */
fun EntityTypeDefinitionDAO.asRuntimeDAO(): EntityTypeDefinitionRuntimeDAO = EntityTypeDefinitionRuntimeDAOAdapter(this)

private class EntityTypeDefinitionRuntimeDAOAdapter(
    private val dao: EntityTypeDefinitionDAO,
) : EntityTypeDefinitionRuntimeDAO {
    override fun listenEntityTypeChanges(listener: () -> Unit) = dao.listenEntityTypeChanges(listener)

    override fun save(entityType: EntityTypeDefinition) = dao.save(entityType)

    override fun getEntityTypes(): List<EntityTypeDefinition> = dao.getEntityTypes()

    override fun save(data: DictionaryData) = dao.save(data)

    override fun getAllDictionaryData(): List<DictionaryData> = dao.getAllDictionaryData()

    override fun listenDictionaryDataChanges(listener: () -> Unit) = dao.listenDictionaryDataChanges(listener)
}

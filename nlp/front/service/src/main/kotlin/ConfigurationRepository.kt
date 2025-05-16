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

package ai.tock.nlp.front.service

import ai.tock.nlp.core.Application
import ai.tock.nlp.core.DictionaryRepository
import ai.tock.nlp.core.EntitiesRegexp
import ai.tock.nlp.core.Entity
import ai.tock.nlp.core.EntityType
import ai.tock.nlp.core.Intent
import ai.tock.nlp.core.NlpCore
import ai.tock.nlp.front.service.ApplicationCodecService.config
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.EntityDefinition
import ai.tock.nlp.front.shared.config.EntityTypeDefinition
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.provide
import mu.KotlinLogging
import org.litote.kmongo.Id
import java.util.concurrent.ConcurrentHashMap

/**
 * A configuration cache to improve performance on critical path.
 */
internal object ConfigurationRepository {

    private val logger = KotlinLogging.logger {}

    private val core: NlpCore get() = injector.provide()

    @Volatile
    private var entityTypes: ConcurrentHashMap<String, EntityType?> = ConcurrentHashMap()

    @Volatile
    private var applicationsByNamespaceAndName: Map<String, Map<String, ApplicationDefinition>> = HashMap()

    @Volatile
    private var intentsById: Map<Id<IntentDefinition>, IntentDefinition> = HashMap()

    @Volatile
    private var intentsByApplicationId: Map<Id<ApplicationDefinition>, List<IntentDefinition>> = HashMap()

    @Volatile
    private var intentsSharedNamespaceByApplicationId: Map<Id<ApplicationDefinition>, List<IntentDefinition>> = HashMap()

    fun refreshEntityTypes() {
        entityTypes = loadEntityTypes()
    }

    private fun refreshApplications() {
        applicationsByNamespaceAndName =
            applicationDAO
                .getApplications()
                .groupBy { it.namespace }
                .mapValues { it.value.associateBy { it.name } }
    }

    private fun refreshIntents() {
        val byId = mutableMapOf<Id<IntentDefinition>, IntentDefinition>()
        val byApplicationId = mutableMapOf<Id<ApplicationDefinition>, List<IntentDefinition>>()

        applicationDAO
            .getApplications().forEach { app ->
                intentDAO.getIntentsByApplicationId(app._id)
                    .apply {
                        byId.putAll(associateBy { it._id })
                        byApplicationId[app._id] = this
                    }
            }

        intentsById = HashMap(byId)
        intentsByApplicationId = HashMap(byApplicationId)

        refreshIntentsSharedByApplications()
    }

    private fun refreshIntentsSharedByApplications() {
        val byApplicationId = mutableMapOf<Id<ApplicationDefinition>, List<IntentDefinition>>()

        applicationDAO
            .getApplications().forEach { app ->
                val sharedIntents = ApplicationConfigurationService.getModelSharedIntents(app.namespace)
                if(sharedIntents.isEmpty()) {
                    byApplicationId[app._id] = intentsByApplicationId[app._id] ?: emptyList()
                } else {
                    byApplicationId[app._id] =((intentsByApplicationId[app._id] ?: emptyList()) + sharedIntents).distinct()
                }
            }

        intentsSharedNamespaceByApplicationId = HashMap(byApplicationId)
    }

    private fun loadEntityTypes(): ConcurrentHashMap<String, EntityType?> {
        logger.debug { "load entity types" }
        val entityTypesDefinitionMap = entityTypeDAO.getEntityTypes().associateBy { it.name }
        // init subEntities only when all entities are known
        val entityTypesMap =
            entityTypesDefinitionMap.mapValues { (_, v) ->
                EntityType(
                    v.name, dictionary = v.dictionary, obfuscated = v.obfuscated
                )
            }

        // init subEntities
        return ConcurrentHashMap(
            entityTypesMap
                .mapValues { (_, v) ->
                    v.copy(
                        subEntities = entityTypesDefinitionMap[v.name]?.subEntities?.mapNotNull {
                            entityTypesMap[it.entityTypeName]?.let { e ->
                                Entity(e, it.role)
                            }.apply {
                                if (this == null) {
                                    logger.error { "entity ${it.entityTypeName} not found" }
                                }
                            }
                        } ?: emptyList()
                    )
                }
                .toMap()
        )
    }

    fun entityTypeExists(name: String): Boolean {
        return entityTypes.containsKey(name)
    }

    fun entityTypeByName(name: String): EntityType? {
        return entityTypes[name] ?: refreshEntityTypes().run {
            entityTypes[name] ?: null.apply { logger.error { "unknown entity $name" } }
        }
    }

    fun addNewEntityType(entityType: EntityTypeDefinition) {
        if (entityTypeByName(entityType.name) == null) {
            entityTypes[entityType.name] = EntityType(
                entityType.name,
                entityType.subEntities.mapNotNull {
                    it.toEntity()
                },
                entityType.dictionary
            )
        }
    }

    fun toEntityType(entityType: EntityTypeDefinition): EntityType? {
        return entityTypeByName(entityType.name)
    }

    fun toEntity(type: String, role: String): Entity? {
        return entityTypeByName(type)?.let { Entity(it, role) }
    }

    fun toApplication(applicationDefinition: ApplicationDefinition): Application {
        val intentDefinitions = getSharedNamespaceIntentsByApplicationId(applicationDefinition._id)
        val intents = intentDefinitions.map {
            Intent(
                it.qualifiedName,
                it.entities.mapNotNull { e -> toEntityWithEntityTypesTree(e) },
                it.entitiesRegexp.mapValues { r -> LinkedHashSet(r.value.map { v -> EntitiesRegexp(v.regexp) }) }
            )
        }
        return Application(
            applicationDefinition.qualifiedName,
            intents,
            applicationDefinition.supportedLocales,
            applicationDefinition.normalizeText,
        )
    }

    private fun toEntityWithEntityTypesTree(e: EntityDefinition): Entity? {
        var entity = toEntity(e.entityTypeName, e.role)
        if (entity?.entityType?.subEntities?.isNotEmpty() == true) {
            entity = entity.copy(entityType = loadEntityTypesTree(entity.entityType))
        }
        return entity
    }

    private fun loadEntityTypesTree(entityType: EntityType, level: Int = 0): EntityType =
        // sanity check
        if (level > 10) {
            entityType
        } else {
            entityType.copy(
                subEntities = entityType.subEntities.map { e ->
                    val t = entityTypeByName(e.entityType.name)
                    if (t != null) {
                        e.copy(
                            entityType = loadEntityTypesTree(t, level + 1)
                        )
                    } else {
                        e
                    }
                }
            )
        }

    fun getApplicationByNamespaceAndName(namespace: String, name: String): ApplicationDefinition? {
        return applicationsByNamespaceAndName[namespace]?.get(name)
            ?: config.getApplicationByNamespaceAndName(namespace, name)
    }

    fun getIntentsByApplicationId(applicationId: Id<ApplicationDefinition>): List<IntentDefinition> {
        return intentsByApplicationId[applicationId] ?: config.getIntentsByApplicationId(applicationId)
    }

    fun getSharedNamespaceIntentsByApplicationId(applicationId: Id<ApplicationDefinition>): List<IntentDefinition> {
        return intentsSharedNamespaceByApplicationId[applicationId] ?: config.getIntentsByApplicationId(applicationId)
    }

    fun getIntentById(id: Id<IntentDefinition>): IntentDefinition? {
        return intentsById[id] ?: config.getIntentById(id)
    }

    fun initRepository(): Boolean =
        try {
            refreshEntityTypes()
            core.getBuiltInEntityTypes().forEach {
                if (!entityTypeExists(it)) {
                    try {
                        logger.debug { "save built-in entity type $it" }
                        val entityType = EntityTypeDefinition(it, "built-in entity $it")
                        entityTypeDAO.save(entityType)
                    } catch (e: Exception) {
                        logger.warn("Fail to save built-in entity type $it", e)
                    }
                }
            }
            refreshEntityTypes()
            refreshApplications()
            refreshIntents()
            entityTypeDAO.listenEntityTypeChanges { refreshEntityTypes() }
            applicationDAO.listenApplicationDefinitionChanges { refreshApplications() }
            intentDAO.listenIntentDefinitionChanges { refreshIntents() }
            namespaceConfigurationDAO.listenNamespaceConfigurationChanges { refreshIntentsSharedByApplications() }

            injector.provide<DictionaryRepository>().updateData(entityTypeDAO.getAllDictionaryData())
            entityTypeDAO.listenDictionaryDataChanges {
                injector.provide<DictionaryRepository>().updateData(entityTypeDAO.getAllDictionaryData())
            }
            entityTypes.isNotEmpty()
        } catch (e: Exception) {
            logger.error(e)
            false
        }
}

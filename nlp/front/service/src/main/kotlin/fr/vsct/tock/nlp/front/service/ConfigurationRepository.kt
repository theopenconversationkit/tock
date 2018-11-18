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

package fr.vsct.tock.nlp.front.service

import fr.vsct.tock.nlp.core.Application
import fr.vsct.tock.nlp.core.EntitiesRegexp
import fr.vsct.tock.nlp.core.Entity
import fr.vsct.tock.nlp.core.EntityType
import fr.vsct.tock.nlp.core.Intent
import fr.vsct.tock.nlp.core.NlpCore
import fr.vsct.tock.nlp.front.service.ApplicationCodecService.config
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.EntityTypeDefinition
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.provide
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
    }

    private fun loadEntityTypes(): ConcurrentHashMap<String, EntityType?> {
        logger.debug { "load entity types" }
        val entityTypesDefinitionMap = entityTypeDAO.getEntityTypes().map { it.name to it }.toMap()
        //init subEntities only when all entities are known
        val entityTypesMap =
            entityTypesDefinitionMap.mapValues { (_, v) -> EntityType(v.name, predefinedValues = v.predefinedValues) }

        //init subEntities
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
                entityType.predefinedValues
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
        val intentDefinitions = getIntentsByApplicationId(applicationDefinition._id)
        val intents = intentDefinitions.map {
            Intent(it.qualifiedName,
                it.entities.mapNotNull { toEntity(it.entityTypeName, it.role) },
                it.entitiesRegexp.mapValues { LinkedHashSet(it.value.map { EntitiesRegexp(it.regexp) }) })
        }
        return Application(applicationDefinition.qualifiedName, intents, applicationDefinition.supportedLocales)
    }

    fun getApplicationByNamespaceAndName(namespace: String, name: String): ApplicationDefinition? {
        return applicationsByNamespaceAndName[namespace]?.get(name)
                ?: config.getApplicationByNamespaceAndName(namespace, name)
    }

    fun getIntentsByApplicationId(applicationId: Id<ApplicationDefinition>): List<IntentDefinition> {
        return intentsByApplicationId[applicationId] ?: config.getIntentsByApplicationId(applicationId)
    }

    fun getIntentById(id: Id<IntentDefinition>): IntentDefinition? {
        return intentsById[id] ?: config.getIntentById(id)
    }

    fun initRepository() {
        try {
            refreshEntityTypes()
            core.getEvaluableEntityTypes().forEach {
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
        } catch (e: Exception) {
            logger.error(e)
        }
    }

}
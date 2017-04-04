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
import fr.vsct.tock.nlp.core.client.NlpCoreClient
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.EntityDefinition
import fr.vsct.tock.nlp.front.shared.config.EntityTypeDefinition
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap

/**
 *
 */
internal object FrontRepository {

    private val logger = KotlinLogging.logger {}

    val core: NlpCore by lazy { NlpCoreClient }

    val config = ApplicationConfigurationService

    val entityTypes: MutableMap<String, EntityType> by lazy {
        val entityTypesDefinitionMap = config.getEntityTypes().map { it.name to it }.toMap().toMutableMap()

        //register built-in entity types
        core.getEvaluatedEntityTypes().forEach {
            if (!entityTypesDefinitionMap.containsKey(it)) {
                try {
                    logger.debug { "save built-in entity type $it" }
                    val entityType = EntityTypeDefinition(it, "built-in entity $it")
                    config.save(entityType)
                    entityTypesDefinitionMap.put(it, entityType)
                } catch(e: Exception) {
                    logger.warn("Fail to save built-in entity type $it", e)
                }
            }
        }

        val entityTypesWithoutSubEntities = entityTypesDefinitionMap
                .filterValues { it.subEntities.isEmpty() }
                .mapValues { (_, v) -> toEntityType(v) }

        val entityTypesWithSubEntities = entityTypesDefinitionMap
                .filterValues { it.subEntities.isNotEmpty() }
                .mapValues { (_, v) ->
                    EntityType(
                            v.name,
                            v.subEntities.map { Entity(entityTypesWithoutSubEntities[it.entityTypeName] ?: error("entity ${it.entityTypeName} not found"), it.role) })
                }
        ConcurrentHashMap(entityTypesWithoutSubEntities + entityTypesWithSubEntities)
    }

    fun toEntityType(entityType: EntityTypeDefinition): EntityType {
        return EntityType(entityType.name, entityType.subEntities.map { it.toEntity() })
    }

    fun toEntity(type: String, role: String): Entity {
        return Entity(entityTypes.getValue(type), role)
    }

    fun toApplication(applicationDefinition: ApplicationDefinition): Application {
        val intentDefinitions = config.getIntentsByApplicationId(applicationDefinition._id!!)
        val intents = intentDefinitions.map {
            Intent(it.qualifiedName,
                    it.entities.map { Entity(entityTypes.getValue(it.entityTypeName), it.role) },
                    it.entitiesRegexp.mapValues { it.value.map { EntitiesRegexp(it.regexp) } })
        }
        return Application(applicationDefinition.name, intents, applicationDefinition.supportedLocales)
    }

    fun EntityDefinition.toEntity(): Entity {
        return toEntity(this.entityTypeName, this.role)
    }

}
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

import com.github.salomonbrys.kodein.instance
import fr.vsct.tock.nlp.core.Entity
import fr.vsct.tock.nlp.core.Intent
import fr.vsct.tock.nlp.core.Intent.Companion.UNKNOWN_INTENT
import fr.vsct.tock.nlp.core.NlpEngineType
import fr.vsct.tock.nlp.front.service.FrontRepository.toEntityType
import fr.vsct.tock.nlp.front.service.storage.ApplicationDefinitionDAO
import fr.vsct.tock.nlp.front.service.storage.ClassifiedSentenceDAO
import fr.vsct.tock.nlp.front.service.storage.EntityTypeDefinitionDAO
import fr.vsct.tock.nlp.front.service.storage.IntentDefinitionDAO
import fr.vsct.tock.nlp.front.shared.ApplicationConfiguration
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.EntityTypeDefinition
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.namespace
import fr.vsct.tock.shared.namespaceAndName

val applicationDAO: ApplicationDefinitionDAO by injector.instance()
val entityTypeDAO: EntityTypeDefinitionDAO by injector.instance()
val intentDAO: IntentDefinitionDAO by injector.instance()
val sentenceDAO: ClassifiedSentenceDAO by injector.instance()

/**
 *
 */
object ApplicationConfigurationService :
        ApplicationDefinitionDAO by applicationDAO,
        EntityTypeDefinitionDAO by entityTypeDAO,
        IntentDefinitionDAO by intentDAO,
        ClassifiedSentenceDAO by sentenceDAO,
        ApplicationConfiguration {

    override fun deleteApplicationById(id: String) {
        sentenceDAO.deleteSentencesByApplicationId(id)
        val app = applicationDAO.getApplicationById(id)!!
        intentDAO.getIntentsByApplicationId(id).forEach {
            removeIntentFromApplication(app, it._id!!)
        }
        applicationDAO.deleteApplicationById(id)
    }

    override fun removeIntentFromApplication(
            application: ApplicationDefinition,
            intentId: String): Boolean {
        val intent = intentDAO.getIntentById(intentId)!!
        sentenceDAO.switchSentencesIntent(application._id!!, intentId, Intent.UNKNOWN_INTENT)
        applicationDAO.save(application.copy(intents = application.intents - intentId))
        val newIntent = intent.copy(applications = intent.applications - application._id!!)
        return if (newIntent.applications.isEmpty()) {
            intentDAO.deleteIntentById(intentId)
            true
        } else {
            intentDAO.save(newIntent)
            false
        }
    }

    override fun removeEntityFromIntent(
            application: ApplicationDefinition,
            intent: IntentDefinition,
            entityType: String,
            role: String): Boolean {
        sentenceDAO.removeEntityFromSentences(application._id!!, intent._id!!, entityType, role)
        intentDAO.save(intent.copy(entities = intent.entities - intent.findEntity(entityType, role)!!))
        //delete entity if same namespace and if not used by any intent
        return if (application.namespace == entityType.namespace()
                && intentDAO.getIntentsUsingEntity(entityType).isEmpty()) {
            entityTypeDAO.deleteEntityTypeByName(entityType)
            true
        } else {
            false
        }
    }

    override fun save(entityType: EntityTypeDefinition) {
        entityTypeDAO.save(entityType)
        FrontRepository.entityTypes.put(entityType.name, toEntityType(entityType))
    }

    override fun getIntentIdByQualifiedName(name: String): String? {
        return if (name == UNKNOWN_INTENT) UNKNOWN_INTENT
        else name.namespaceAndName().run { intentDAO.getIntentByNamespaceAndName(first, second)?._id }
    }

    override fun getSupportedNlpEngineTypes(): Set<NlpEngineType> {
        return FrontRepository.core.supportedNlpEngineTypes()
    }

    override fun initData() {
        FrontRepository.registerBuiltInEntities()
    }

    fun toIntent(intentId: String, cache: MutableMap<String, Intent>? = null): Intent {
        return cache?.getOrPut(intentId, { findIntent(intentId) }) ?: findIntent(intentId)
    }

    private fun findIntent(intentId: String): Intent {
        return getIntentById(intentId)?.let {
            toIntent(it)
        } ?: Intent(Intent.Companion.UNKNOWN_INTENT, emptyList())
    }

    fun toIntent(intent: IntentDefinition): Intent {
        return Intent(
                intent.qualifiedName,
                intent.entities.map { Entity(FrontRepository.entityTypeByName(it.entityTypeName), it.role) },
                intent.entitiesRegexp)
    }
}
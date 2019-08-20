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

import fr.vsct.tock.nlp.core.Intent
import fr.vsct.tock.nlp.core.Intent.Companion.UNKNOWN_INTENT_NAME
import fr.vsct.tock.nlp.core.ModelCore
import fr.vsct.tock.nlp.core.NlpCore
import fr.vsct.tock.nlp.core.NlpEngineType
import fr.vsct.tock.nlp.core.configuration.NlpApplicationConfiguration
import fr.vsct.tock.nlp.front.service.ConfigurationRepository.addNewEntityType
import fr.vsct.tock.nlp.front.service.storage.ApplicationDefinitionDAO
import fr.vsct.tock.nlp.front.service.storage.ClassifiedSentenceDAO
import fr.vsct.tock.nlp.front.service.storage.EntityTypeDefinitionDAO
import fr.vsct.tock.nlp.front.service.storage.IntentDefinitionDAO
import fr.vsct.tock.nlp.front.shared.ApplicationConfiguration
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentence
import fr.vsct.tock.nlp.front.shared.config.EntityDefinition
import fr.vsct.tock.nlp.front.shared.config.EntityTypeDefinition
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.namespace
import fr.vsct.tock.shared.namespaceAndName
import fr.vsct.tock.shared.provide
import fr.vsct.tock.shared.security.UserLogin
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.toId

val applicationDAO: ApplicationDefinitionDAO get() = injector.provide()
val entityTypeDAO: EntityTypeDefinitionDAO get() = injector.provide()
val intentDAO: IntentDefinitionDAO get() = injector.provide()
val sentenceDAO: ClassifiedSentenceDAO get() = injector.provide()

/**
 *
 */
object ApplicationConfigurationService :
    ApplicationDefinitionDAO by applicationDAO,
    EntityTypeDefinitionDAO by entityTypeDAO,
    IntentDefinitionDAO by intentDAO,
    ClassifiedSentenceDAO by sentenceDAO,
    ApplicationConfiguration {

    override fun save(sentence: ClassifiedSentence, user: UserLogin?) {
        sentenceDAO.save(sentence.copy(qualifier = user))
    }

    private val logger = KotlinLogging.logger {}

    private val core: NlpCore get() = injector.provide()
    private val modelCore: ModelCore get() = injector.provide()

    private val config: ApplicationConfiguration get() = injector.provide()

    override fun deleteApplicationById(id: Id<ApplicationDefinition>) {
        sentenceDAO.deleteSentencesByApplicationId(id)
        val app = applicationDAO.getApplicationById(id)!!
        intentDAO.getIntentsByApplicationId(id).forEach {
            removeIntentFromApplication(app, it._id)
        }
        applicationDAO.deleteApplicationById(id)
    }

    override fun removeIntentFromApplication(
        application: ApplicationDefinition,
        intentId: Id<IntentDefinition>
    ): Boolean {
        val intent = intentDAO.getIntentById(intentId)!!
        sentenceDAO.switchSentencesIntent(application._id, intentId, Intent.UNKNOWN_INTENT_NAME.toId())
        applicationDAO.save(application.copy(intents = application.intents - intentId))
        val newIntent = intent.copy(applications = intent.applications - application._id)
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
        role: String
    ): Boolean = removeEntityFromIntent(application, intent, entityType, role, true)

    private fun removeEntityFromIntent(
        application: ApplicationDefinition,
        intent: IntentDefinition,
        entityType: String,
        role: String,
        deleteEntityType: Boolean
    ): Boolean {
        sentenceDAO.removeEntityFromSentences(application._id, intent._id, entityType, role)
        val loadedIntent = getIntentById(intent._id)
        if (loadedIntent != null) {
            intentDAO.save(
                loadedIntent.copy(
                    entities = loadedIntent.entities - listOfNotNull(
                        loadedIntent.findEntity(
                            entityType,
                            role
                        )
                    )
                )
            )
        }
        //delete entity if same namespace and if not used by any intent
        return if (
            deleteEntityType
            && application.namespace == entityType.namespace()
            && intentDAO.getIntentsUsingEntity(entityType).isEmpty()
        ) {
            entityTypeDAO.deleteEntityTypeByName(entityType)
            true
        } else {
            false
        }
    }

    override fun removeSubEntityFromEntity(
        application: ApplicationDefinition,
        entityType: EntityTypeDefinition,
        role: String
    ): Boolean {
        sentenceDAO.removeSubEntityFromSentences(application._id, entityType.name, role)
        config.save(entityType.copy(subEntities = entityType.subEntities.filterNot { it.role == role }))
        //TODO
        //delete sub entity if same namespace and if not used by any intent
        return true
    }

    override fun save(entityType: EntityTypeDefinition) {
        entityTypeDAO.save(entityType)
        addNewEntityType(entityType)
    }

    override fun getIntentIdByQualifiedName(name: String): Id<IntentDefinition>? {
        return if (name == UNKNOWN_INTENT_NAME) UNKNOWN_INTENT_NAME.toId()
        else name.namespaceAndName().run { intentDAO.getIntentByNamespaceAndName(first, second)?._id }
    }

    override fun getSupportedNlpEngineTypes(): Set<NlpEngineType> {
        return core.supportedNlpEngineTypes()
    }

    override fun deleteEntityTypeByName(name: String): Boolean {
        getIntentsUsingEntity(name).forEach { intent ->
            intent.applications.forEach { id ->
                val app = getApplicationById(id)
                if (app != null) {
                    intent.entities.filter { it.entityTypeName == name }.forEach {
                        removeEntityFromIntent(app, intent, name, it.role, false)
                    }
                }
            }
        }

        return entityTypeDAO.deleteEntityTypeByName(name).apply {
            ConfigurationRepository.refreshEntityTypes()
        }
    }

    fun toIntent(intentId: Id<IntentDefinition>, cache: MutableMap<Id<IntentDefinition>, Intent>? = null): Intent {
        return cache?.getOrPut(intentId, { findIntent(intentId) }) ?: findIntent(intentId)
    }

    private fun findIntent(intentId: Id<IntentDefinition>): Intent {
        return getIntentById(intentId)?.let {
            toIntent(it)
        } ?: Intent(Intent.UNKNOWN_INTENT_NAME, emptyList())
    }

    fun toIntent(intent: IntentDefinition): Intent {
        return Intent(
            intent.qualifiedName,
            intent.entities.mapNotNull { ConfigurationRepository.toEntity(it.entityTypeName, it.role) },
            intent.entitiesRegexp
        )
    }

    override fun switchSentencesIntent(
        sentences: List<ClassifiedSentence>,
        targetApplication: ApplicationDefinition,
        targetIntentId: Id<IntentDefinition>
    ): Int {

        val s = sentences.filter { it.classification.intentId != targetIntentId }

        //1 collect entities
        val entities = s
            .flatMap { sentence ->
                sentence.classification.entities.mapNotNull { it.toEntity(ConfigurationRepository::toEntity) }
            }
            .distinct()

        //2 create entities where there are not present in the new intent (except if it's the unknown intent)
        if (targetIntentId.toString() != UNKNOWN_INTENT_NAME) {
            val intent = getIntentById(targetIntentId)!!
            entities.filterNot { intent.hasEntity(it) }.apply {
                if (isNotEmpty()) {
                    save(intent.copy(entities = intent.entities + map { EntityDefinition(it) }))
                }
            }
        }

        //3 switch intents
        sentenceDAO.switchSentencesIntent(s, targetIntentId)

        return sentences.size
    }

    override fun switchSentencesEntity(
        sentences: List<ClassifiedSentence>,
        targetApplication: ApplicationDefinition,
        oldEntity: EntityDefinition,
        newEntity: EntityDefinition
    ): Int {
        //0 check new entity is known
        val entity = newEntity.toEntity()
        if (entity == null) {
            logger.warn { "unknown entity $newEntity" }
            return 0
        }

        //1 create entities where there are not present in the intents
        val intents =
            sentences.map { it.classification.intentId }.distinct().filter { it.toString() != UNKNOWN_INTENT_NAME }
        intents.forEach {
            val intent = getIntentById(it)
            if (intent != null) {
                if (!intent.hasEntity(entity)) {
                    save(intent.copy(entities = intent.entities + newEntity))
                }
            } else {
                logger.warn { "unknown intent $it" }
            }
        }

        //switch entity
        sentenceDAO.switchSentencesEntity(sentences, oldEntity, newEntity)

        return sentences.size
    }

    override fun updateEntityDefinition(namespace: String, applicationName: String, entity: EntityDefinition) {
        val app = getApplicationByNamespaceAndName(namespace, applicationName)!!
        val intents = getIntentsByApplicationId(app._id)
        intents.forEach {
            it.findEntity(entity.entityTypeName, entity.role)
                ?.apply {
                    save(
                        it.copy(
                            entities = it.entities - this + entity
                        )
                    )
                }
        }
    }

    override fun initializeConfiguration() {
        ConfigurationRepository.initRepository()
    }

    override fun getCurrentModelConfiguration(
        applicationName: String,
        nlpEngineType: NlpEngineType
    ): NlpApplicationConfiguration = modelCore.getCurrentModelConfiguration(applicationName, nlpEngineType)

    override fun updateModelConfiguration(
        applicationName: String,
        engineType: NlpEngineType,
        configuration: NlpApplicationConfiguration
    ) =
        modelCore.updateModelConfiguration(applicationName, engineType, configuration)
}
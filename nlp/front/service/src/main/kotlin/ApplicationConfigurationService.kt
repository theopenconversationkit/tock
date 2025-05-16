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

import ai.tock.nlp.core.Intent
import ai.tock.nlp.core.Intent.Companion.RAG_EXCLUDED_INTENT_NAME
import ai.tock.nlp.core.Intent.Companion.UNKNOWN_INTENT_NAME
import ai.tock.nlp.core.ModelCore
import ai.tock.nlp.core.NlpCore
import ai.tock.nlp.core.NlpEngineType
import ai.tock.nlp.core.configuration.NlpApplicationConfiguration
import ai.tock.nlp.front.service.ConfigurationRepository.addNewEntityType
import ai.tock.nlp.front.service.storage.ApplicationDefinitionDAO
import ai.tock.nlp.front.service.storage.ClassifiedSentenceDAO
import ai.tock.nlp.front.service.storage.EntityTypeDefinitionDAO
import ai.tock.nlp.front.service.storage.FaqDefinitionDAO
import ai.tock.nlp.front.service.storage.FaqSettingsDAO
import ai.tock.nlp.front.service.storage.IntentDefinitionDAO
import ai.tock.nlp.front.service.storage.NamespaceConfigurationDAO
import ai.tock.nlp.front.service.storage.UserNamespaceDAO
import ai.tock.nlp.front.shared.ApplicationConfiguration
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import ai.tock.nlp.front.shared.config.EntityDefinition
import ai.tock.nlp.front.shared.config.EntityTypeDefinition
import ai.tock.nlp.front.shared.config.FaqDefinition
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.shared.injector
import ai.tock.shared.namespace
import ai.tock.shared.namespaceAndName
import ai.tock.shared.provide
import ai.tock.shared.security.UserLogin
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.toId
import java.util.Locale

val applicationDAO: ApplicationDefinitionDAO get() = injector.provide()
val entityTypeDAO: EntityTypeDefinitionDAO get() = injector.provide()
val intentDAO: IntentDefinitionDAO get() = injector.provide()
val sentenceDAO: ClassifiedSentenceDAO get() = injector.provide()
val userNamespaceDAO: UserNamespaceDAO get() = injector.provide()
val faqDefinitionDAO: FaqDefinitionDAO get() = injector.provide()
val faqSettingsDAO: FaqSettingsDAO get() = injector.provide()
val namespaceConfigurationDAO: NamespaceConfigurationDAO get() = injector.provide()

/**
 *
 */
object ApplicationConfigurationService :
    ApplicationDefinitionDAO by applicationDAO,
    EntityTypeDefinitionDAO by entityTypeDAO,
    IntentDefinitionDAO by intentDAO,
    ClassifiedSentenceDAO by sentenceDAO,
    UserNamespaceDAO by userNamespaceDAO,
    FaqDefinitionDAO by faqDefinitionDAO,
    NamespaceConfigurationDAO by namespaceConfigurationDAO,
    ApplicationConfiguration {

    private val logger = KotlinLogging.logger {}

    private val core: NlpCore get() = injector.provide()
    private val modelCore: ModelCore get() = injector.provide()

    private val config: ApplicationConfiguration get() = injector.provide()

    override fun save(application: ApplicationDefinition): ApplicationDefinition {
        if (application.normalizeText) {
            sentenceDAO.updateFormattedSentences(application._id)
        }
        return applicationDAO.save(application)
    }

    override fun save(faqDefinition: FaqDefinition) {
        faqDefinitionDAO.save(faqDefinition)
    }

    override fun save(sentence: ClassifiedSentence, user: UserLogin?) {
        sentenceDAO.save(sentence.copy(qualifier = user))
    }

    override fun deleteApplicationById(id: Id<ApplicationDefinition>) {
        sentenceDAO.deleteSentencesByApplicationId(id)
        val app = applicationDAO.getApplicationById(id)!!
        intentDAO.getIntentsByApplicationId(id).forEach { intent ->
            removeIntentFromApplication(app, intent._id)
        }

        faqDefinitionDAO.deleteFaqDefinitionByBotIdAndNamespace(app.name, app.namespace)
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
                    entities = loadedIntent.entities - setOfNotNull(
                        loadedIntent.findEntity(
                            entityType,
                            role
                        )
                    )
                )
            )
        }
        // delete entity if same namespace and if not used by any intent
        return if (
            deleteEntityType &&
            application.namespace == entityType.namespace() &&
            intentDAO.getIntentsUsingEntity(entityType).isEmpty()
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
        // TODO
        // delete sub entity if same namespace and if not used by any intent
        return true
    }

    override fun save(entityType: EntityTypeDefinition) {
        entityTypeDAO.save(entityType)
        addNewEntityType(entityType)
    }

    override fun getIntentIdByQualifiedName(name: String): Id<IntentDefinition>? {
        return if (name == UNKNOWN_INTENT_NAME) UNKNOWN_INTENT_NAME.toId()
        else if (name == RAG_EXCLUDED_INTENT_NAME) RAG_EXCLUDED_INTENT_NAME.toId()
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
        return cache?.getOrPut(intentId) { findIntent(intentId) } ?: findIntent(intentId)
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

        // 1 collect entities
        val entities = s
            .flatMap { sentence ->
                sentence.classification.entities.mapNotNull { it.toEntity(ConfigurationRepository::toEntity) }
            }
            .distinct()

        // 2 create entities where there are not present in the new intent (except if it's the unknown or ragexcluded intent)
        if (targetIntentId.toString() != UNKNOWN_INTENT_NAME
            && targetIntentId.toString() != RAG_EXCLUDED_INTENT_NAME) {
            val intent = getIntentById(targetIntentId)!!
            entities.filterNot { intent.hasEntity(it) }.apply {
                if (isNotEmpty()) {
                    save(intent.copy(entities = intent.entities + map { EntityDefinition(it) }))
                }
            }
        }

        // 3 switch intents
        sentenceDAO.switchSentencesIntent(s, targetIntentId)

        return sentences.size
    }

    override fun switchSentencesEntity(
        sentences: List<ClassifiedSentence>,
        targetApplication: ApplicationDefinition,
        oldEntity: EntityDefinition,
        newEntity: EntityDefinition
    ): Int {
        // 0 check new entity is known
        val entity = newEntity.toEntity()
        if (entity == null) {
            logger.warn { "unknown entity $newEntity" }
            return 0
        }

        // 1 create entities where there are not present in the intents
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

        // switch entity
        sentenceDAO.switchSentencesEntity(targetApplication.namespace, sentences, oldEntity, newEntity)

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

    override fun initializeConfiguration(): Boolean = ConfigurationRepository.initRepository()

    override fun getCurrentModelConfiguration(
        applicationName: String,
        nlpEngineType: NlpEngineType
    ): NlpApplicationConfiguration = modelCore.getCurrentModelConfiguration(applicationName, nlpEngineType)

    override fun updateModelConfiguration(
        applicationName: String,
        engineType: NlpEngineType,
        configuration: NlpApplicationConfiguration
    ) = modelCore.updateModelConfiguration(applicationName, engineType, configuration)

    override fun getEntityTypesByNamespaceAndSharedEntityTypes(namespace: String): List<EntityTypeDefinition> {
        val builtin = core.getBuiltInEntityTypes()
        return getEntityTypes().filter { it.name.namespace() == namespace || builtin.contains(it.name) }
    }

    override fun isEntityTypeObfuscated(name: String): Boolean =
        ConfigurationRepository.entityTypeByName(name)?.obfuscated ?: true

    override fun getFaqsDefinitionByApplicationId(id: Id<ApplicationDefinition>): List<FaqDefinition> =
        getApplicationById(id)?.let { faqDefinitionDAO.getFaqDefinitionByBotIdAndNamespace(it.name, it.namespace) } ?: arrayListOf()

    override fun getFaqDefinitionByIntentId(id: Id<IntentDefinition>): FaqDefinition? =
        faqDefinitionDAO.getFaqDefinitionByIntentId(id)

    override fun getModelSharedIntents(namespace: String): List<IntentDefinition> =
        getNamespaceConfiguration(namespace)
            ?.namespaceImportConfiguration
            ?.filterValues { it.model }
            ?.map { getIntentsByNamespace(it.key) }
            ?.fold(emptyList()) { result, intents -> result + intents }
            ?: emptyList()

    override fun getSentencesForModel(
        application: ApplicationDefinition,
        language: Locale
    ): List<ClassifiedSentence> =
        getSentences(
            application.intents + getModelSharedIntents(application.namespace).map { it._id },
            language,
            ClassifiedSentenceStatus.model
        )

}

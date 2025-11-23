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

package ai.tock.nlp.front.shared

import ai.tock.nlp.core.DictionaryData
import ai.tock.nlp.core.NlpEngineType
import ai.tock.nlp.core.configuration.NlpApplicationConfiguration
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import ai.tock.nlp.front.shared.config.EntityDefinition
import ai.tock.nlp.front.shared.config.EntityTypeDefinition
import ai.tock.nlp.front.shared.config.FaqDefinition
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.nlp.front.shared.config.SentencesQuery
import ai.tock.nlp.front.shared.config.SentencesQueryResult
import ai.tock.nlp.front.shared.namespace.NamespaceConfiguration
import ai.tock.nlp.front.shared.user.UserNamespace
import ai.tock.shared.security.UserLogin
import org.litote.kmongo.Id
import java.util.Locale

/**
 *
 */
interface ApplicationConfiguration {
    fun save(application: ApplicationDefinition): ApplicationDefinition

    fun deleteApplicationById(id: Id<ApplicationDefinition>)

    fun getApplicationByNamespaceAndName(
        namespace: String,
        name: String,
    ): ApplicationDefinition?

    fun getApplicationById(id: Id<ApplicationDefinition>): ApplicationDefinition?

    fun getApplications(): List<ApplicationDefinition>

    /**
     * Remove intent from application.
     *
     * @return true if intent is also deleted, false either
     */
    fun removeIntentFromApplication(
        application: ApplicationDefinition,
        intentId: Id<IntentDefinition>,
    ): Boolean

    /**
     * Get the sentences with the specified criteria.
     *
     * @throws error if all parameters are null
     */
    fun getSentences(
        intents: Set<Id<IntentDefinition>>? = null,
        language: Locale? = null,
        status: ClassifiedSentenceStatus? = null,
    ): List<ClassifiedSentence>

    fun deleteSentencesByStatus(status: ClassifiedSentenceStatus)

    fun save(
        sentence: ClassifiedSentence,
        user: UserLogin? = sentence.qualifier,
    )

    fun search(query: SentencesQuery): SentencesQueryResult

    fun switchSentencesStatus(
        sentences: List<ClassifiedSentence>,
        newStatus: ClassifiedSentenceStatus,
    )

    /**
     * Returns sentence validator users.
     */
    fun users(applicationId: Id<ApplicationDefinition>): List<String>

    /**
     * Returns sentence configurations source.
     */
    fun configurations(applicationId: Id<ApplicationDefinition>): List<String>

    /**
     * Switch specified sentences to a new intent.
     *
     * @return the number of sentences updated
     */
    fun switchSentencesIntent(
        sentences: List<ClassifiedSentence>,
        targetApplication: ApplicationDefinition,
        targetIntentId: Id<IntentDefinition>,
    ): Int

    /**
     * Switch old entity to new entity.
     *
     * @return the number of sentences updated
     */
    fun switchSentencesEntity(
        sentences: List<ClassifiedSentence>,
        targetApplication: ApplicationDefinition,
        oldEntity: EntityDefinition,
        newEntity: EntityDefinition,
    ): Int

    fun save(entityType: EntityTypeDefinition)

    fun getEntityTypesByNamespaceAndSharedEntityTypes(namespace: String): List<EntityTypeDefinition>

    fun getEntityTypes(): List<EntityTypeDefinition>

    fun getEntityTypeByName(name: String): EntityTypeDefinition?

    fun isEntityTypeObfuscated(name: String): Boolean

    /**
     * Load DictionaryData for entity qualified name.
     */
    fun getDictionaryDataByEntityName(qualifiedName: String): DictionaryData?

    /**
     * Save [DictionaryData].
     */
    fun save(data: DictionaryData)

    /**
     * Delete the [EntityTypeDefinition] and cleanup all sentences from this entity type.
     */
    fun deleteEntityTypeByName(name: String): Boolean

    /**
     * Update matching entity definition of all intents of the specified application.
     */
    fun updateEntityDefinition(
        namespace: String,
        applicationName: String,
        entity: EntityDefinition,
    )

    fun getIntentsByApplicationId(applicationId: Id<ApplicationDefinition>): List<IntentDefinition>

    fun getIntentById(id: Id<IntentDefinition>): IntentDefinition?

    fun getIntentByNamespaceAndName(
        namespace: String,
        name: String,
    ): IntentDefinition?

    fun save(intent: IntentDefinition)

    fun save(faq: FaqDefinition)

    fun getIntentIdByQualifiedName(name: String): Id<IntentDefinition>?

    /**
     * Remove entity from intent.
     *
     * @return true if the entity type is also deleted, false either.
     */
    fun removeEntityFromIntent(
        application: ApplicationDefinition,
        intent: IntentDefinition,
        entityType: String,
        role: String,
    ): Boolean

    /**
     * Remove a sub entity from an entity.
     *
     * @return true if the entity type is also deleted, false either.
     */
    fun removeSubEntityFromEntity(
        application: ApplicationDefinition,
        entityType: EntityTypeDefinition,
        role: String,
    ): Boolean

    /**
     * Returns supported NLP engines.
     */
    fun getSupportedNlpEngineTypes(): Set<NlpEngineType>

    fun deletePredefinedValueByName(
        entityTypeName: String,
        predefinedValue: String,
    )

    fun deletePredefinedValueLabelByName(
        entityTypeName: String,
        predefinedValue: String,
        locale: Locale,
        label: String,
    )

    /**
     * Load the configuration cache.
     * @return true if configuration is correctly initialized
     */
    fun initializeConfiguration(): Boolean

    /**
     * Returns the current model configuration.
     */
    fun getCurrentModelConfiguration(
        applicationName: String,
        nlpEngineType: NlpEngineType,
    ): NlpApplicationConfiguration

    /**
     * Updates the model configuration for the given application name.
     */
    fun updateModelConfiguration(
        applicationName: String,
        engineType: NlpEngineType,
        configuration: NlpApplicationConfiguration,
    )

    /**
     * Returns all the namespaces of a user.
     */
    fun getNamespaces(user: String): List<UserNamespace>

    /**
     * Returns all the users of a namespace.
     */
    fun getUsers(namespace: String): List<UserNamespace>

    /**
     * Persists namespace.
     */
    fun saveNamespace(namespace: UserNamespace)

    /**
     * Delete namespace.
     */
    fun deleteNamespace(
        user: String,
        namespace: String,
    )

    /**
     * Set current namespace for selected user.
     */
    fun setCurrentNamespace(
        user: String,
        namespace: String,
    )

    /**
     * Is it the namespace owner ?
     */
    fun isNamespaceOwner(
        user: String,
        namespace: String,
    ): Boolean

    /**
     * Is this user has the namespace ?
     */
    fun hasNamespace(
        user: String,
        namespace: String,
    ): Boolean

    /**
     * Is this namespace exists ?
     */
    fun isExistingNamespace(namespace: String): Boolean

    /**
     * Returns all application FAQs
     */
    fun getFaqsDefinitionByApplicationId(id: Id<ApplicationDefinition>): List<FaqDefinition>

    /**
     * Returns FaqDefinition by Intent
     */
    fun getFaqDefinitionByIntentId(id: Id<IntentDefinition>): FaqDefinition?

    fun saveNamespaceConfiguration(configuration: NamespaceConfiguration)

    fun getNamespaceConfiguration(namespace: String): NamespaceConfiguration?

    fun getSharableNamespaceConfiguration(): List<NamespaceConfiguration>

    fun getModelSharedIntents(namespace: String): List<IntentDefinition>

    fun getSentencesForModel(
        application: ApplicationDefinition,
        language: Locale,
    ): List<ClassifiedSentence>
}

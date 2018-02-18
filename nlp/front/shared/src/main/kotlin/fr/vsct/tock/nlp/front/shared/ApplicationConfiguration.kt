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

package fr.vsct.tock.nlp.front.shared

import fr.vsct.tock.nlp.core.NlpEngineType
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentence
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import fr.vsct.tock.nlp.front.shared.config.EntityDefinition
import fr.vsct.tock.nlp.front.shared.config.EntityTypeDefinition
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition
import fr.vsct.tock.nlp.front.shared.config.SentencesQuery
import fr.vsct.tock.nlp.front.shared.config.SentencesQueryResult
import org.litote.kmongo.Id
import java.util.Locale

/**
 *
 */
interface ApplicationConfiguration {

    fun save(application: ApplicationDefinition): ApplicationDefinition

    fun deleteApplicationById(id: Id<ApplicationDefinition>)

    fun getApplicationByNamespaceAndName(namespace: String, name: String): ApplicationDefinition?

    fun getApplicationById(id: Id<ApplicationDefinition>): ApplicationDefinition?

    fun getApplications(): List<ApplicationDefinition>

    /**
     * Remove intent from application.
     *
     * @return true if intent is also deleted, false either
     */
    fun removeIntentFromApplication(
        application: ApplicationDefinition,
        intentId: Id<IntentDefinition>
    ): Boolean

    /**
     * Get the sentences with the specified criteria.
     *
     * @throws error if all parameters are null
     */
    fun getSentences(
        intents: Set<Id<IntentDefinition>>? = null,
        language: Locale? = null,
        status: ClassifiedSentenceStatus? = null
    ): List<ClassifiedSentence>

    fun deleteSentencesByStatus(status: ClassifiedSentenceStatus)

    fun save(sentence: ClassifiedSentence)

    fun search(query: SentencesQuery): SentencesQueryResult

    fun switchSentencesStatus(sentences: List<ClassifiedSentence>, newStatus: ClassifiedSentenceStatus)

    /**
     * Switch specified sentences to a new intent.
     *
     * @return the number of sentences updated
     */
    fun switchSentencesIntent(
        sentences: List<ClassifiedSentence>,
        targetApplication: ApplicationDefinition,
        targetIntentId: Id<IntentDefinition>
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
        newEntity: EntityDefinition
    ): Int

    fun save(entityType: EntityTypeDefinition)

    fun getEntityTypes(): List<EntityTypeDefinition>

    fun getEntityTypeByName(name: String): EntityTypeDefinition?

    /**
     * Delete the [EntityTypeDefinition] and cleanup all sentences from this entity type.
     */
    fun deleteEntityTypeByName(name: String): Boolean

    /**
     * Update matching entity definition of all intents of the specified application.
     */
    fun updateEntityDefinition(namespace: String, applicationName: String, entity: EntityDefinition)


    fun getIntentsByApplicationId(applicationId: Id<ApplicationDefinition>): List<IntentDefinition>

    fun getIntentById(id: Id<IntentDefinition>): IntentDefinition?

    fun getIntentByNamespaceAndName(namespace: String, name: String): IntentDefinition?

    fun save(intent: IntentDefinition)

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
        role: String
    ): Boolean

    /**
     * Remove a sub entity from an entity.
     *
     * @return true if the entity type is also deleted, false either.
     */
    fun removeSubEntityFromEntity(
        application: ApplicationDefinition,
        entityType: EntityTypeDefinition,
        role: String
    ): Boolean

    /**
     * Returns supported NLP engines.
     */
    fun getSupportedNlpEngineTypes(): Set<NlpEngineType>

    /**
     * Load data in storage engine at startup.
     */
    fun initData()

}
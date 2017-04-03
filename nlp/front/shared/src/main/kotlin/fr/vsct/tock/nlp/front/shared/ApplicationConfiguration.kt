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

import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentence
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import fr.vsct.tock.nlp.front.shared.config.EntityTypeDefinition
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition
import fr.vsct.tock.nlp.front.shared.config.SentencesQuery
import fr.vsct.tock.nlp.front.shared.config.SentencesQueryResult
import java.util.Locale

/**
 *
 */
interface ApplicationConfiguration {

    fun save(application: ApplicationDefinition): ApplicationDefinition

    fun deleteApplicationById(id: String)

    fun getApplicationByNamespaceAndName(namespace: String, name: String): ApplicationDefinition?

    fun getApplicationById(id: String): ApplicationDefinition?

    fun getApplications(): List<ApplicationDefinition>


    fun getSentences(intents: Set<String>, language: Locale, status: ClassifiedSentenceStatus): List<ClassifiedSentence>

    fun getSentences(status: ClassifiedSentenceStatus): List<ClassifiedSentence>

    fun deleteSentencesByStatus(status: ClassifiedSentenceStatus)

    fun save(sentence: ClassifiedSentence)

    fun search(query : SentencesQuery) : SentencesQueryResult

    fun switchIntent(applicationId:String, oldIntentId: String, newIntentId:String)

    fun switchStatus(sentences:List<ClassifiedSentence>, newStatus: ClassifiedSentenceStatus)

    fun removeEntity(applicationId:String, intentId: String, entityType:String, role:String)



    fun save(entityType: EntityTypeDefinition)

    fun getEntityTypes(): List<EntityTypeDefinition>

    fun getEntityTypeByName(name: String): EntityTypeDefinition?


    fun getIntentsByApplicationId(applicationId: String): List<IntentDefinition>

    fun getIntentByNamespaceAndName(namespace:String, name: String): IntentDefinition?

    fun getIntentById(id: String): IntentDefinition?

    fun save(intent: IntentDefinition)



    fun getIntentIdByQualifiedName(name: String): String


}
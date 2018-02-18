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

package fr.vsct.tock.nlp.front.service.storage

import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentence
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import fr.vsct.tock.nlp.front.shared.config.EntityDefinition
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition
import fr.vsct.tock.nlp.front.shared.config.SentencesQuery
import fr.vsct.tock.nlp.front.shared.config.SentencesQueryResult
import org.litote.kmongo.Id
import java.util.Locale

/**
 * Manage sentences of the NLP model.
 */
interface ClassifiedSentenceDAO {

    fun getSentences(
        intents: Set<Id<IntentDefinition>>?,
        language: Locale?,
        status: ClassifiedSentenceStatus?
    ): List<ClassifiedSentence>

    fun deleteSentencesByStatus(status: ClassifiedSentenceStatus)

    fun deleteSentencesByApplicationId(applicationId: Id<ApplicationDefinition>)

    fun save(sentence: ClassifiedSentence)

    fun search(query: SentencesQuery): SentencesQueryResult

    fun switchSentencesIntent(
        applicationId: Id<ApplicationDefinition>,
        oldIntentId: Id<IntentDefinition>,
        newIntentId: Id<IntentDefinition>
    )

    fun switchSentencesIntent(sentences: List<ClassifiedSentence>, newIntentId: Id<IntentDefinition>)

    fun switchSentencesEntity(
        sentences: List<ClassifiedSentence>,
        oldEntity: EntityDefinition,
        newEntity: EntityDefinition
    )

    fun switchSentencesStatus(sentences: List<ClassifiedSentence>, newStatus: ClassifiedSentenceStatus)

    fun removeEntityFromSentences(
        applicationId: Id<ApplicationDefinition>,
        intentId: Id<IntentDefinition>,
        entityType: String,
        role: String
    )

    fun removeSubEntityFromSentences(applicationId: Id<ApplicationDefinition>, entityType: String, role: String)
}
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

package fr.vsct.tock.nlp.admin.model

import fr.vsct.tock.nlp.core.Intent
import fr.vsct.tock.nlp.front.shared.config.Classification
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentence
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition
import fr.vsct.tock.nlp.front.shared.parser.ParseResult
import fr.vsct.tock.nlp.front.shared.test.EntityTestError
import fr.vsct.tock.nlp.front.shared.test.IntentTestError
import org.litote.kmongo.Id
import org.litote.kmongo.toId

/**
 *
 */
data class ClassificationReport(
        val intentId: Id<IntentDefinition>?,
        val entities: List<ClassifiedEntityReport>,
        val intentProbability: Double?,
        val entitiesProbability: Double?,
        val otherIntentsProbabilities: Map<String, Double>) {

    constructor(query: ParseResult, intentId: Id<IntentDefinition>?) : this(
            intentId,
            query.entities.map { ClassifiedEntityReport(it) },
            query.intentProbability,
            query.entitiesProbability,
            query.otherIntentsProbabilities
    )

    constructor(sentence: ClassifiedSentence) : this(
            sentence.classification.intentId,
            sentence.classification.entities.map { ClassifiedEntityReport(it) },
            sentence.lastIntentProbability,
            sentence.lastEntityProbability,
            emptyMap()
    )

    constructor(error: IntentTestError) : this(
            null,
            emptyList(),
            error.averageErrorProbability,
            1.0,
            emptyMap())

    constructor(error: EntityTestError) : this(
            error.intentId,
            error.lastAnalyse.map { ClassifiedEntityReport(it) },
            1.0,
            error.averageErrorProbability,
            emptyMap())

    fun toClassification(): Classification {
        return Classification(intentId ?: Intent.UNKNOWN_INTENT.toId(), entities.map { it.toClassifiedEntity() })
    }
}
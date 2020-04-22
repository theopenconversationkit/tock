/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
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

package ai.tock.nlp.admin.model

import ai.tock.nlp.admin.AdminService.obfuscatedEntityRanges
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.nlp.front.shared.parser.ParseResult
import ai.tock.nlp.front.shared.test.EntityTestError
import ai.tock.nlp.front.shared.test.IntentTestError
import ai.tock.shared.security.TockObfuscatorService.obfuscate
import ai.tock.shared.security.UserLogin
import ai.tock.shared.security.decrypt
import ai.tock.shared.security.encrypt
import org.litote.kmongo.Id
import java.time.Instant
import java.time.Instant.now
import java.util.Locale

/**
 *
 */
data class SentenceReport(
    val text: String,
    val language: Locale,
    val applicationId: Id<ApplicationDefinition>,
    val creationDate: Instant,
    val updateDate: Instant,
    val status: ClassifiedSentenceStatus,
    val classification: ClassificationReport,
    var key: String? = null,
    val forReview: Boolean = false,
    val reviewComment: String? = null,
    val qualifier: UserLogin? = null
) {

    constructor(
        query: ParseResult,
        language: Locale,
        applicationId: Id<ApplicationDefinition>,
        intentId: Id<IntentDefinition>?
    ) :
        this(
            obfuscate(
                text = query.retainedQuery,
                obfuscatedRanges = query.entities.filter { it.entity.entityType.obfuscated }.map { it.toClosedRange() }
            ) ?: "",
            language,
            applicationId,
            now(),
            now(),
            ClassifiedSentenceStatus.inbox,
            ClassificationReport(query, intentId)
        ) {
        if (text != query.retainedQuery) {
            key = encrypt(query.retainedQuery)
        }
    }

    constructor(sentence: ClassifiedSentence, obfuscateSentences: Boolean = true) :
        this(
            if (obfuscateSentences)
                obfuscate(text = sentence.text, obfuscatedRanges = sentence.obfuscatedEntityRanges()) ?: ""
            else sentence.text,
            sentence.language,
            sentence.applicationId,
            sentence.creationDate,
            sentence.updateDate,
            sentence.status,
            ClassificationReport(sentence),
            forReview = sentence.forReview,
            reviewComment = sentence.reviewComment,
            qualifier = sentence.qualifier
        ) {
        if (text != sentence.text) {
            key = encrypt(sentence.text)
        }
    }

    constructor(error: IntentTestError, obfuscatedSentences: Boolean, obfuscatedRanges: List<IntRange>) : this(
        if (obfuscatedSentences) obfuscate(text = error.text, obfuscatedRanges = obfuscatedRanges) ?: ""
        else error.text,
        error.language,
        error.applicationId,
        error.firstDetectionDate,
        error.firstDetectionDate,
        ClassifiedSentenceStatus.model,
        ClassificationReport(error)
    )

    constructor(error: EntityTestError, obfuscatedSentences: Boolean, obfuscatedRanges: List<IntRange>) : this(
        if (obfuscatedSentences) obfuscate(text = error.text, obfuscatedRanges = obfuscatedRanges) ?: ""
        else error.text,
        error.language,
        error.applicationId,
        error.firstDetectionDate,
        error.firstDetectionDate,
        ClassifiedSentenceStatus.model,
        ClassificationReport(error)
    )

    fun toClassifiedSentence(): ClassifiedSentence {
        return ClassifiedSentence(
            if (key == null) text else decrypt(key!!),
            language,
            applicationId,
            creationDate,
            updateDate,
            status,
            classification.toClassification(),
            1.0,
            1.0,
            forReview = forReview,
            reviewComment = reviewComment,
            qualifier = qualifier
        )
    }
}
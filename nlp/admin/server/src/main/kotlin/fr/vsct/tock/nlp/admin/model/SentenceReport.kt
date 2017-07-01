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

import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentence
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import fr.vsct.tock.nlp.front.shared.parser.ParseResult
import java.time.Instant
import java.time.Instant.now
import java.util.Locale

/**
 *
 */
data class SentenceReport(val text: String,
                          val language: Locale,
                          val applicationId: String,
                          val creationDate: Instant,
                          val updateDate: Instant,
                          val status: ClassifiedSentenceStatus,
                          val classification: ClassificationReport) {

    constructor(query: ParseResult, language: Locale, applicationId: String, intentId: String)
            : this(
            query.retainedQuery,
            language,
            applicationId,
            now(),
            now(),
            ClassifiedSentenceStatus.inbox,
            ClassificationReport(query, intentId))

    constructor(sentence: ClassifiedSentence) : this(
            sentence.text,
            sentence.language,
            sentence.applicationId,
            sentence.creationDate,
            sentence.updateDate,
            sentence.status,
            ClassificationReport(sentence)
    )

    fun toClassifiedSentence(): ClassifiedSentence {
        return ClassifiedSentence(
                text,
                language,
                applicationId,
                creationDate,
                updateDate,
                status,
                classification.toClassification(),
                1.0,
                1.0
        )
    }
}
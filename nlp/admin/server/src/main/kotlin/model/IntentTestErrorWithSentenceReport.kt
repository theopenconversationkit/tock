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

package ai.tock.nlp.admin.model

import ai.tock.nlp.admin.AdminService.obfuscatedEntityRanges
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.test.IntentTestError
import java.time.Instant

/**
 *
 */
class IntentTestErrorWithSentenceReport(
    val sentence: SentenceReport,
    val currentIntent: String,
    val wrongIntent: String,
    val averageErrorProbability: Double,
    val count: Int = 1,
    val total: Int = 1,
    val firstDetectionDate: Instant = Instant.now()
) {

    constructor(originalSentence: ClassifiedSentence, error: IntentTestError) : this(
        SentenceReport(error, originalSentence.obfuscatedEntityRanges()),
        error.currentIntent,
        error.wrongIntent,
        error.averageErrorProbability,
        error.count,
        error.total,
        error.firstDetectionDate
    )
}

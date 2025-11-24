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

import ai.tock.nlp.front.shared.config.SentencesQueryResult

/**
 *
 */
data class SentencesReport(
    val sentences: List<SentenceReport>,
    val total: Long,
    val start: Long,
    val end: Long,
) {
    constructor(start: Long, result: SentencesQueryResult) :
        this(
            result.sentences.map { SentenceReport(it) },
            result.total,
            start,
            start + result.sentences.size,
        )
}

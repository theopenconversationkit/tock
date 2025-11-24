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

package ai.tock.nlp.front.shared.codec

import ai.tock.nlp.front.shared.config.ClassifiedEntity
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * A classified sentences dump.
 */
data class SentencesDump(
    val applicationName: String,
    val language: Locale? = null,
    val sentences: List<SentenceDump>,
)

data class SentenceDump(
    val text: String,
    val intent: String,
    val creationDate: Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS),
    val entities: List<SentenceEntityDump> = emptyList(),
    val language: Locale? = null,
    val status: ClassifiedSentenceStatus = ClassifiedSentenceStatus.model,
    val forReview: Boolean = false,
    val reviewComment: String? = null,
)

data class SentenceEntityDump(
    val entity: String,
    val role: String,
    val subEntities: List<SentenceEntityDump> = emptyList(),
    val start: Int,
    val end: Int,
) {
    constructor(entity: ClassifiedEntity) :
        this(
            entity.type,
            entity.role,
            entity.subEntities.map { SentenceEntityDump(it) },
            entity.start,
            entity.end,
        )

    fun toClassifiedEntity(): ClassifiedEntity {
        return ClassifiedEntity(
            entity,
            role,
            start,
            end,
            subEntities.map { it.toClassifiedEntity() },
        )
    }
}

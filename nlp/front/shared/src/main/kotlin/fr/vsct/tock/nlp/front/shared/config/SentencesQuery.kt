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

package fr.vsct.tock.nlp.front.shared.config

import org.litote.kmongo.Id
import java.time.ZonedDateTime
import java.util.Locale

/**
 * Complex query to search classified sentences.
 */
data class SentencesQuery(
    val applicationId: Id<ApplicationDefinition>,
    val language: Locale? = null,
    val start: Long = 0,
    val size: Int = 1,
    val search: String? = null,
    val intentId: Id<IntentDefinition>? = null,
    val status: Set<ClassifiedSentenceStatus> = emptySet(),
    val notStatus: ClassifiedSentenceStatus? = ClassifiedSentenceStatus.deleted,
    val onlyExactMatch: Boolean = false,
    val entityType: String? = null,
    val entityRole: String? = null,
    val modifiedAfter: ZonedDateTime? = null,
    val searchMark: SearchMark? = null,
    val onlyToReview: Boolean = false,
    /**
     * The optional sort parameters.
     */
    val sort: List<Pair<String, Boolean>> = emptyList()
)
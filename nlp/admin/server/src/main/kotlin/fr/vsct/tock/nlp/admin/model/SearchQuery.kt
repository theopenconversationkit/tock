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

import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition
import fr.vsct.tock.nlp.front.shared.config.SentencesQuery
import org.litote.kmongo.Id
import java.time.ZonedDateTime

/**
 *
 */
data class SearchQuery(
    val search: String?,
    val intentId: Id<IntentDefinition>?,
    val status: Set<ClassifiedSentenceStatus> = emptySet(),
    val entityType: String? = null,
    val entityRole: String? = null,
    val modifiedAfter: ZonedDateTime? = null,
    val onlyToReview: Boolean = false,
    /**
     * Search the sub entities in the whole entity tree.
     */
    val searchSubEntities: Boolean = false
) : PaginatedQuery() {

    fun toSentencesQuery(applicationId: Id<ApplicationDefinition>): SentencesQuery {
        return SentencesQuery(
            applicationId,
            language,
            start,
            size,
            search,
            intentId,
            status,
            entityType = entityType,
            entityRole = entityRole,
            modifiedAfter = modifiedAfter,
            searchMark = searchMark,
            sort = sort ?: emptyList(),
            onlyToReview = onlyToReview,
            searchSubEntities = searchSubEntities
        )
    }
}
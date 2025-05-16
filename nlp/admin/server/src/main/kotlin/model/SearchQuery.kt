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

import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.nlp.front.shared.config.SentencesQuery
import ai.tock.shared.allowDiacriticsInRegexp
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
    val entityRolesToInclude: List<String> = emptyList(),
    val entityRolesToExclude: List<String> = emptyList(),
    val modifiedAfter: ZonedDateTime? = null,
    val modifiedBefore: ZonedDateTime? = null,
    val onlyToReview: Boolean = false,
    /**
     * Search the sub entities in the whole entity tree.
     */
    val searchSubEntities: Boolean = false,
    /**
     * Sentences validated by user.
     */
    val user: String? = null,
    /**
     * Sentences not validated by user.
     */
    val allButUser: String? = null,
    val maxIntentProbability: Float = 1f,
    val minIntentProbability: Float = 0f,
    val configuration : String? = null
) : PaginatedQuery() {

    fun toSentencesQuery(applicationId: Id<ApplicationDefinition>): SentencesQuery {
        return SentencesQuery(
            applicationId,
            language,
            start,
            size,
            search?.let { allowDiacriticsInRegexp(it.trim()) },
            intentId,
            status,
            entityType = entityType,
            entityRolesToInclude = entityRolesToInclude,
            entityRolesToExclude = entityRolesToExclude,
            modifiedAfter = modifiedAfter,
            modifiedBefore = modifiedBefore,
            searchMark = searchMark,
            sort = sort ?: emptyList(),
            onlyToReview = onlyToReview,
            searchSubEntities = searchSubEntities,
            user = user?.takeUnless { it.isBlank() },
            allButUser = allButUser?.takeUnless { it.isBlank() },
            maxIntentProbability = maxIntentProbability,
            minIntentProbability = minIntentProbability,
            configuration = configuration
        )
    }
}

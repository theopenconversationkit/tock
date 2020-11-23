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

import ai.tock.nlp.core.Intent.Companion.UNKNOWN_INTENT_NAME
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import ai.tock.nlp.front.shared.config.EntityDefinition
import ai.tock.nlp.front.shared.config.IntentDefinition
import org.litote.kmongo.Id

/**
 *
 */
data class UpdateSentencesQuery(
    val newIntentId: Id<IntentDefinition>?,
    val oldEntity: EntityDefinition?,
    val newEntity: EntityDefinition?,
    val searchQuery: SearchQuery?,
    val selectedSentences: List<SentenceReport> = emptyList(),
    val newStatus: ClassifiedSentenceStatus? = null
) : ApplicationScopedQuery() {

    val unknownNewIntent: Boolean get() = UNKNOWN_INTENT_NAME == newIntentId?.toString()
}

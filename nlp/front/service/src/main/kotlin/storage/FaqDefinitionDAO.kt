/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.nlp.front.service.storage

import ai.tock.nlp.front.shared.config.FaqDefinition
import ai.tock.nlp.front.shared.config.FaqQuery
import ai.tock.nlp.front.shared.config.FaqQueryResult
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.translator.I18nLabel
import org.litote.kmongo.Id

interface FaqDefinitionDAO {

    fun deleteFaqDefinitionById(id: Id<FaqDefinition>)

    fun save(faqDefinition: FaqDefinition)

    fun getFaqDefinitionById(id: Id<FaqDefinition>): FaqDefinition?

    fun listenFaqDefinitionChanges(listener: () -> Unit)

    fun getFaqDefinitionByI18nId(id: Id<I18nLabel>): FaqDefinition?

    fun getFaqDefinitionByI18nIds(ids: Set<Id<I18nLabel>>): List<FaqDefinition>?

    fun getFaqDefinitionByIntentId(id: Id<IntentDefinition>): FaqDefinition?

    fun getFaqDefinitionByIntentIds(intentIds: Set<Id<IntentDefinition>>): List<FaqDefinition>?

    fun getFaqDefinitionByTags(tags: Set<String>): List<FaqDefinition>

    /**
     * Get the aggregated Faq and total count
     */
    fun getFaqDetailsWithCount(
        query: FaqQuery,
        applicationId: String,
        i18nIds: List<Id<I18nLabel>>? = null
    ): Pair<List<FaqQueryResult>, Long>

    fun getTags(applicationId: String): List<String>


}
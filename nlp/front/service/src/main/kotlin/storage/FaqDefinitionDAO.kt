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

package ai.tock.nlp.front.service.storage

import ai.tock.nlp.front.shared.config.FaqDefinition
import ai.tock.nlp.front.shared.config.FaqQuery
import ai.tock.nlp.front.shared.config.FaqQueryResult
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.translator.I18nLabel
import org.litote.kmongo.Id

interface FaqDefinitionDAO {

    fun deleteFaqDefinitionById(id: Id<FaqDefinition>)

    /**
     * Delete the FaqDefinition by filtering on the application [id][ApplicationDefinition]
     * @param id the application name [ApplicationDefinition]
     */
    fun deleteFaqDefinitionByBotIdAndNamespace(id: String, namespace: String)

    fun save(faqDefinition: FaqDefinition)

    fun getFaqDefinitionById(id: Id<FaqDefinition>): FaqDefinition?

    /**
     * Retrieve faqDefinition by filtering on the application name [id][ApplicationDefinition]
     * @param botId the application name
     * @param namespace the namespace
     */
    fun getFaqDefinitionByBotIdAndNamespace(botId: String, namespace: String): List<FaqDefinition>

    fun listenFaqDefinitionChanges(listener: () -> Unit)

    fun getFaqDefinitionByI18nId(id: Id<I18nLabel>): FaqDefinition?

    fun getFaqDefinitionByI18nIds(ids: Set<Id<I18nLabel>>): List<FaqDefinition>?

    fun getFaqDefinitionByIntentId(id: Id<IntentDefinition>): FaqDefinition?

    fun getFaqDefinitionByIntentIds(intentIds: Set<Id<IntentDefinition>>): List<FaqDefinition>?

    fun getFaqDefinitionByTags(tags: Set<String>): List<FaqDefinition>

    /**
     * Retrieve faqDefinition by filtering on intent id [intentId][IntentDefinition] and the application name[botId][ApplicationDefinition]
     * @param intentId intent id
     * @param botId the application name
     * @param namespace the namespace
     */
    fun getFaqDefinitionByIntentIdAndBotIdAndNamespace(intentId: Id<IntentDefinition>, botId: String, namespace: String): FaqDefinition?

    /**
     * Retrieve faq details with total count numbers according to the filter present un [FaqQuery]
     * @param query [FaqQuery] the query search
     * @param applicationDefinition the current [ApplicationDefinition]
     * @param i18nIds optional to request eventually on i18nIds
     */
    fun getFaqDetailsWithCount(
        query: FaqQuery,
        applicationDefinition: ApplicationDefinition,
        i18nIds: List<Id<I18nLabel>>? = null
    ): Pair<List<FaqQueryResult>, Long>

    fun getTags(botId: String, namespace: String): List<String>

    /**
     * Make migration
     * @param intentIdSupplier : function that return a namespace with a given Id<>
     */
    fun makeMigration(intentIdSupplier: (Id<IntentDefinition>) -> String?)
}

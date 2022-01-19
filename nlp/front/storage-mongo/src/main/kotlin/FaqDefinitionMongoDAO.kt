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

package ai.tock.nlp.front.storage.mongo

import ai.tock.nlp.front.service.storage.FaqDefinitionDAO
import ai.tock.nlp.front.shared.config.*
import ai.tock.translator.I18nLabel
import com.mongodb.client.MongoCollection
import mu.KotlinLogging
import org.litote.kmongo.*
import org.litote.kmongo.reactivestreams.getCollection
import ai.tock.shared.watch
import ai.tock.shared.ensureUniqueIndex
import ai.tock.translator.I18nLabel_
import com.mongodb.client.model.ReplaceOptions
import java.util.*


object FaqDefinitionMongoDAO : FaqDefinitionDAO {

    private val logger = KotlinLogging.logger {}

    private val col: MongoCollection<FaqDefinition> by lazy {

        val c = MongoFrontConfiguration.database.getCollection<FaqDefinition>().apply {
            ensureUniqueIndex(FaqDefinition::i18nId, FaqDefinition::intentId)
        }
        c
    }

    private val asyncCol by lazy {
        MongoFrontConfiguration.asyncDatabase.getCollection<FaqDefinition>()
    }

    override fun listenQAItemChanges(listener: () -> Unit) {
        asyncCol.watch { listener() }
    }

    override fun deleteQAItemById(id: Id<FaqDefinition>) {
        col.deleteOneById(FaqDefinition::_id eq id)
    }

    override fun getQAItemById(id: Id<FaqDefinition>): FaqDefinition? {
        return col.findOneById(FaqDefinition::_id eq id)
    }


    override fun getQAItemByIntentId(id: Id<IntentDefinition>): FaqDefinition? {
        return col.findOneById(FaqDefinition::intentId eq id)
    }

    override fun getQAItemByIntentIds(intentIds: Set<Id<IntentDefinition>>): List<FaqDefinition> {
        return col.find(FaqDefinition::intentId `in` intentIds).into(ArrayList())
    }

    override fun getQAItemByTags(tags: Set<String>): List<FaqDefinition> {
        return col.find(FaqDefinition::tags `in` tags).into(ArrayList())
    }

    override fun getQAItemByI18nId(id: Id<I18nLabel>): FaqDefinition? {
        return col.findOne(FaqDefinition::i18nId eq id)
    }

    override fun getQAItemByI18nIds(ids: Set<Id<I18nLabel>>): List<FaqDefinition>? {
        return col.find(FaqDefinition::i18nId `in` ids).into(ArrayList())
    }


    override fun deleteByNamespaceAndId(namespace: String, id: Id<I18nLabel>) {
        col.deleteOne(I18nLabel_.Namespace eq namespace, I18nLabel_._id eq id)
    }

    override fun save(faqDefinition: FaqDefinition) {
        col.replaceOneWithFilter(
            and(
                FaqDefinition::intentId eq faqDefinition.intentId,
                FaqDefinition::i18nId eq faqDefinition.i18nId,
                FaqDefinition::_id eq faqDefinition._id,
                FaqDefinition::tags eq faqDefinition.tags
            ),
            faqDefinition,
            ReplaceOptions().upsert(true)
        )
    }

    private const val CLASSIFIED_SENTENCE_COLLECTION = "classified_sentence"

    private const val INTENT_DEFINTION_COLLECTON = "intent_definition"

    fun getFaqDetails(query:FaqQuery) {
        col.aggregate<FaqQueryResult>(
            lookup(
                CLASSIFIED_SENTENCE_COLLECTION,
                FaqDefinition::intentId.name,
                ClassifiedSentence::classification.name + ".intentId",
                "uterrances"
            ),
            lookup(INTENT_DEFINTION_COLLECTON, FaqDefinition::intentId.name, "_id", "faq"),
            unwind("faq"),
            match(
//                and(
//                    listOfNotNull(
//                        FAQQueryResult::faqDefinition
//                        DialogFlowStateTransitionStatCol_.TransitionId `in` transitionIds,
//                        if (botAppConfIds.isEmpty()) null else DialogFlowStateTransitionStatCol_.ApplicationId `in` botAppConfIds,
//                        if (from == null) null else DialogFlowStateTransitionStatCol_.Date gt from.toInstant(),
//                        if (to == null) null else DialogFlowStateTransitionStatCol_.Date lt to.toInstant()
//                    )
//                )
            ),

        ).map {
//            it.utterances =
        }
    }


}
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
import ai.tock.shared.watch
import ai.tock.translator.I18nLabel
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.model.UnwindOptions
import config.FaqDefinitionTag
import mu.KotlinLogging
import org.litote.kmongo.*
import org.litote.kmongo.reactivestreams.getCollection


object FaqDefinitionMongoDAO : FaqDefinitionDAO {

    private val logger = KotlinLogging.logger {}

    internal val col: MongoCollection<FaqDefinition> by lazy {

        val c = MongoFrontConfiguration.database.getCollection<FaqDefinition>().apply {
            ensureUniqueIndex(
                FaqDefinition::intentId,
                FaqDefinition::i18nId,
                FaqDefinition::tags,
                FaqDefinition::updateDate
            )
        }
        c
    }

    private val asyncCol by lazy {
        MongoFrontConfiguration.asyncDatabase.getCollection<FaqDefinition>()
    }

    override fun listenFaqDefinitionChanges(listener: () -> Unit) {
        asyncCol.watch { listener() }
    }

    override fun deleteFaqDefinitionById(id: Id<FaqDefinition>) {
        col.deleteOneById(FaqDefinition::_id eq id)
    }

    override fun getFaqDefinitionById(id: Id<FaqDefinition>): FaqDefinition? {
        return col.findOneById(id)
    }

    override fun getFaqDefinitionByIntentId(id: Id<IntentDefinition>): FaqDefinition? {
        return col.findOne(FaqDefinition::intentId eq id)
    }

    override fun getFaqDefinitionByIntentIds(intentIds: Set<Id<IntentDefinition>>): List<FaqDefinition> {
        return col.find(FaqDefinition::intentId `in` intentIds).into(ArrayList())
    }

    override fun getFaqDefinitionByTags(tags: Set<String>): List<FaqDefinition> {
        return col.find(FaqDefinition::tags `in` tags).into(ArrayList())
    }

    override fun getFaqDefinitionByI18nId(id: Id<I18nLabel>): FaqDefinition? {
        return col.findOne(FaqDefinition::i18nId eq id)
    }

    override fun getFaqDefinitionByI18nIds(ids: Set<Id<I18nLabel>>): List<FaqDefinition>? {
        return col.find(FaqDefinition::i18nId `in` ids).into(ArrayList())
    }

    override fun save(faqDefinition: FaqDefinition) {
        col.replaceOneWithFilter(
            and(
                FaqDefinition::intentId eq faqDefinition.intentId,
                FaqDefinition::i18nId eq faqDefinition.i18nId,
                FaqDefinition::_id eq faqDefinition._id,
                FaqDefinition::tags eq faqDefinition.tags,
            ),
            faqDefinition,
            ReplaceOptions().upsert(true)
        )
    }

    private const val CLASSIFIED_SENTENCE_COLLECTION = "classified_sentence"

    private const val INTENT_DEFINITION_COLLECTION = "intent_definition"

    override fun getFaqDetailsWithCount(
        query: FaqQuery,
        applicationId: String,
        i18nIds: List<Id<I18nLabel>>?
    ): Pair<List<FaqQueryResult>, Long> {
        with(query) {
            val baseAggregation = arrayListOf(
                sort(ascending(FaqDefinition::i18nId)),
                lookup(
                    INTENT_DEFINITION_COLLECTION,
                    FaqDefinition::intentId.name,
                    IntentDefinition::_id.name,
                    FaqQueryResult::faq.name
                ),
                lookup(
                    CLASSIFIED_SENTENCE_COLLECTION,
                    FaqDefinition::intentId.name,
                    ClassifiedSentence::classification.name + "." + Classification::intentId.name, //classification.intentId
                    FaqQueryResult::utterances.name,
                ),
                FaqQueryResult::faq.unwind(),
                match(
                    or(
                        listOfNotNull(
                            //regex is use like contains because not accessible with this writing
                            if (search == null) null else FaqQueryResult::faq / IntentDefinition::name regex search!!,
                            if (search == null) null else FaqQueryResult::faq / IntentDefinition::description regex search!!,
                            if (search == null) null else FaqQueryResult::utterances.allPosOp / ClassifiedSentence::text regex search!!,
                            //i18nIds are optional and can be used if the request has i18nsIds
                            if (i18nIds == null) null else FaqQueryResult::i18nId `in` i18nIds,
                        )
                    ),
                    and(
                        listOfNotNull(
                            FaqQueryResult::faq / IntentDefinition::applications `in` applicationId,
                            if (tags.isEmpty()) null else FaqQueryResult::tags eq tags,
                        )
                    )
                ),
                sort(
                    ascending(
                        FaqQueryResult::faq / IntentDefinition::name
                    )
                ),
            )
            logger.debug { baseAggregation.map { it.json } }
            //counting total aggregation without skip and limit
            val count = col.aggregate(baseAggregation, FaqQueryResult::class.java).count()
            logger.debug { "count : $count" }
            var aggregationWithSkipAndLimit =
                if (start.toInt() > 0) baseAggregation.plusElement(skip(start.toInt())) else baseAggregation
            aggregationWithSkipAndLimit.plusElement(limit(size))
            return if (count > start) {
                val res = col.aggregate(aggregationWithSkipAndLimit, FaqQueryResult::class.java)
                Pair(
                    res.mapNotNull { it }.sortedBy { it.faq._id.toString() },
                    count.toLong()
                )
            } else {
                Pair(emptyList(), 0)
            }
        }
    }

    override fun getTags(applicationId: String): List<String> {
        return col.aggregate<FaqDefinitionTag>(
            lookup(
                INTENT_DEFINITION_COLLECTION,
                FaqDefinition::intentId.name,
                IntentDefinition::_id.name,
                FaqDefinitionDetailed::faq.name
            ),
            match(
                and(
                    listOfNotNull(
                        FaqDefinitionDetailed::faq / IntentDefinition::applications `in` applicationId,
                    )
                )
            ),
            FaqDefinition::tags.unwind(unwindOptions = UnwindOptions().preserveNullAndEmptyArrays(false)),
            group(
                FaqDefinition::tags,
                FaqDefinition::tags.first(FaqQueryResult::tags)
            ),
            project(
                excludeId(),
                document(FaqDefinitionTag::tag from FaqDefinition::tags)
            ),
            sort(ascending(FaqDefinitionTag::tag))
        ).map { it.tag }.toList()
    }

}


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
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.Classification
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import ai.tock.nlp.front.shared.config.FaqDefinition
import ai.tock.nlp.front.shared.config.FaqDefinitionTag
import ai.tock.nlp.front.shared.config.FaqQuery
import ai.tock.nlp.front.shared.config.FaqQueryResult
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.shared.ensureIndex
import ai.tock.shared.isDocumentDB
import ai.tock.shared.watch
import ai.tock.translator.I18nLabel
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.model.Variable
import mu.KotlinLogging
import org.bson.conversions.Bson
import org.litote.kmongo.Id
import org.litote.kmongo.MongoOperator.and
import org.litote.kmongo.MongoOperator.eq
import org.litote.kmongo.MongoOperator.ne
import org.litote.kmongo.aggregate
import org.litote.kmongo.and
import org.litote.kmongo.ascending
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.descending
import org.litote.kmongo.div
import org.litote.kmongo.document
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.excludeId
import org.litote.kmongo.exists
import org.litote.kmongo.expr
import org.litote.kmongo.findOne
import org.litote.kmongo.findOneById
import org.litote.kmongo.first
import org.litote.kmongo.from
import org.litote.kmongo.getCollection
import org.litote.kmongo.group
import org.litote.kmongo.`in`
import org.litote.kmongo.json
import org.litote.kmongo.limit
import org.litote.kmongo.lookup
import org.litote.kmongo.match
import org.litote.kmongo.ne
import org.litote.kmongo.newId
import org.litote.kmongo.or
import org.litote.kmongo.project
import org.litote.kmongo.reactivestreams.getCollection
import org.litote.kmongo.regex
import org.litote.kmongo.replaceOneWithFilter
import org.litote.kmongo.save
import org.litote.kmongo.skip
import org.litote.kmongo.sort
import org.litote.kmongo.unwind
import java.time.Instant
import kotlin.concurrent.thread
import kotlin.reflect.KProperty

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
            ensureIndex(FaqDefinition::intentId, FaqDefinition::i18nId, FaqDefinition::botId)
            ensureIndex(FaqDefinition::intentId, FaqDefinition::creationDate, FaqDefinition::botId)
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
        col.deleteOneById(id)
    }

    override fun deleteFaqDefinitionByBotIdAndNamespace(id: String, namespace: String) {
        col.deleteMany(and(FaqDefinition::botId eq id, FaqDefinition::namespace eq namespace))
    }

    override fun getFaqDefinitionById(id: Id<FaqDefinition>): FaqDefinition? {
        return col.findOneById(id)
    }

    override fun getFaqDefinitionByBotIdAndNamespace(botId: String, namespace: String): List<FaqDefinition> {
        return col.find(
            and(
                FaqDefinition::botId eq botId,
                FaqDefinition::namespace eq namespace
            )
        ).into(ArrayList())
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

    override fun getFaqDefinitionByIntentIdAndBotIdAndNamespace(intentId: Id<IntentDefinition>, botId: String, namespace: String): FaqDefinition? {
        return col.findOne(
            FaqDefinition::intentId eq intentId,
            FaqDefinition::namespace eq namespace,
            FaqDefinition::botId eq botId
        )
    }

    override fun save(faqDefinition: FaqDefinition) {
        col.replaceOneWithFilter(
            and(
                FaqDefinition::_id eq faqDefinition._id,
                FaqDefinition::intentId eq faqDefinition.intentId,
                FaqDefinition::i18nId eq faqDefinition.i18nId,
            ),
            faqDefinition,
            ReplaceOptions().upsert(true)
        )
    }

    override fun getFaqDetailsWithCount(
        query: FaqQuery,
        applicationDefinition: ApplicationDefinition,
        i18nIds: List<Id<I18nLabel>>?
    ): Pair<List<FaqQueryResult>, Long> {
        with(query) {
            //prepare aggregation without skip and limit to know the total number of faq available
            val baseAggregation = if (isDocumentDB()) {
                prepareFaqDetailBaseAggregationDocumentDb(query, applicationDefinition, i18nIds)
            } else {
                prepareFaqDetailBaseAggregation(query, applicationDefinition, i18nIds)
            }

            logger.debug { baseAggregation.map { it.json } }
            //counting total
            val count = col.aggregate(baseAggregation, FaqQueryResult::class.java).count()
            logger.debug { "count : $count" }

            //add skip and limit on the baseAggregation
            var aggregationWithSkipAndLimit =
                if (start.toInt() > 0) baseAggregation.plusElement(skip(start.toInt())) else baseAggregation
            aggregationWithSkipAndLimit = aggregationWithSkipAndLimit.plusElement(limit(size))

            return if (count > start) {
                val res = col.aggregate(aggregationWithSkipAndLimit, FaqQueryResult::class.java)
                Pair(
                    res.mapNotNull { it },
                    count.toLong()
                )
            } else {
                Pair(emptyList(), 0)
            }
        }
    }

    /**
     * Retrieve tags according to the applicationId present in IntentDefinition with aggregation
     * @param botId : the botId
     * @param namespace: the namespace
     * @return a string list of tags
     */
    override fun getTags(botId: String, namespace: String): List<String> {
        return col.aggregate<FaqDefinitionTag>(
            joinOnIntentDefinition(),
            match(
                andNotNull(
                    filterOnNamespaceAndBotId(botId, namespace)
                )
            ),
            // unwind : to flat tags array into an object
            FaqDefinition::tags.unwind(),
            groupByTag(),
            projectByTag(),
            sortAscending(FaqDefinitionTag::tag),
        ).map { it.tag }.toList()
    }

    /**
     * @see FaqDefinitionDAO.makeMigration
     */
    override fun makeMigration(intentIdSupplier: (Id<IntentDefinition>) -> String?) {

        // Faq projection represents the old structure of FaqDefinition
        // May 2023
        data class FaqProjection(
            val _id: Id<FaqDefinition> = newId(),
            val botId: String,
            val intentId: Id<IntentDefinition>,
            val i18nId: Id<I18nLabel>,
            val tags: List<String>,
            val enabled: Boolean,
            val creationDate: Instant,
            val updateDate: Instant,
        )

        col.aggregate<FaqProjection>(
            match(
                FaqDefinition::namespace exists false,
                // to ensure old faq migration if not done (see Tock version 23.3.0)
                FaqDefinition::botId exists true
            )
        ).forEach { projection ->
            thread(true) {

                with(projection) {


                    val namespace = intentIdSupplier.invoke(intentId)
                        ?: throw Exception("Fail to migrate Faq with intent $intentId  due to namespace not found with id $intentId")

                    logger.info { "Migrate FaqDefinition ${projection._id} with namespace $namespace" }

                    FaqDefinition(
                        _id,
                        botId,
                        namespace,
                        intentId,
                        i18nId,
                        tags,
                        enabled,
                        creationDate,
                        updateDate
                    )
                }.let { faq -> col.save(faq) }
            }
        }
    }

    private const val CLASSIFIED_SENTENCE_COLLECTION = "classified_sentence"

    private const val INTENT_DEFINITION_COLLECTION = "intent_definition"

    //create a variable for let parameter in lookup in order to join two collection
    private const val FAQ_INTENTID = "faq_intentId"

    /**
     * Prepare the faq detail aggregation without skip and limit
     * @param query [FaqQuery] the query search
     * @param applicationDefinition the current [ApplicationDefinition]
     * @param i18nIds optional to request eventually on i18nIds
     */
    private fun prepareFaqDetailBaseAggregation(
        query: FaqQuery,
        applicationDefinition: ApplicationDefinition,
        i18nIds: List<Id<I18nLabel>>?
    ): ArrayList<Bson> {
        with(query) {
            return arrayListOf(
                // sort the i18n by ids
                sortAscending(FaqDefinition::i18nId),
                // join FaqDefinition with IntentDefinition
                joinOnIntentDefinition(),
                // join FaqDefinition with ClassifiedSentence
                joinOnClassifiedSentenceStatusNotDeletedAndNotInboxed(applicationDefinition._id),
                // unwind : to flat faq array into an object
                FaqQueryResult::faq.unwind(),
                match(
                    andNotNull(
                        orNotNull(
                            filterTextSearchOnFaqTitle(),
                            filterTextSearchOnFaqDescription(),
                            filterTextSearchOnClassifiedSentence(),
                            filterI18nIds(i18nIds)
                        ),
                        andNotNull(
                            filterOnNamespaceAndBotId(applicationDefinition.name, applicationDefinition.namespace),
                            filterTags(),
                            filterEnabled(),
                        )
                    )
                ),
                sortDescending(FaqQueryResult::creationDate)
            )
        }
    }

    /**
     * Prepare the faq detail aggregation without skip and limit and without lookup let pipeline and two unwind
     * to avoid unavailable aggregation let pipeline exp with documentDB
     * @param query [FaqQuery] the query search
     * @param applicationDefinition the current [ApplicationDefinition]
     * @param i18nIds optional to request eventually on i18nIds
     */
    private fun prepareFaqDetailBaseAggregationDocumentDb(
        query: FaqQuery,
        applicationDefinition: ApplicationDefinition,
        i18nIds: List<Id<I18nLabel>>?
    ): ArrayList<Bson> {
        with(query) {
            return arrayListOf(
                // sort the i18n by ids
                sortAscending(FaqDefinition::i18nId),
                // join FaqDefinition with IntentDefinition
                joinOnIntentDefinition(),
                // join FaqDefinition with ClassifiedSentence, cannot use aggregation let pipeline with documentDB so won't filter here on not deleted status
                joinOnClassifiedSentence(),
                // unwind : to flat faq array into an object
                FaqQueryResult::faq.unwind(),
                match(
                    andNotNull(
                        andNotNull(
                            filterOnNamespaceAndBotId(applicationDefinition.name, applicationDefinition.namespace),
                            filterTags(),
                            filterEnabled(),
                            filterCurrentApplicationClassifiedSentence(applicationDefinition._id),
                            filterNotDeletedClassifiedSentencesStatus()
                        ),
                        orNotNull(
                            filterTextSearchOnFaqTitle(),
                            filterTextSearchOnFaqDescription(),
                            filterTextSearchOnClassifiedSentence(),
                            filterI18nIds(i18nIds)
                        ),
                    )
                ),
                groupFaqDefinitionDetailedData(),
                sortDescending(FaqQueryResult::creationDate)
            )
        }
    }

    private fun FaqQuery.filterTextSearchOnFaqTitle() =
        if (search == null) null else (FaqQueryResult::faq / IntentDefinition::label).regex(
            pattern = search!!,
            options = "i"
        )

    private fun FaqQuery.filterTextSearchOnFaqDescription() =
        if (search == null) null else (FaqQueryResult::faq / IntentDefinition::description).regex(
            pattern = search!!,
            options = "i"
        )

    private fun FaqQuery.filterTextSearchOnClassifiedSentence() =
        if (search == null) null else (FaqQueryResult::utterances / ClassifiedSentence::text).regex(
            pattern = search!!,
            options = "i"
        )

    private fun FaqQuery.filterCurrentApplicationClassifiedSentence(currentApplicationId: Id<ApplicationDefinition>) =
        FaqQueryResult::utterances / ClassifiedSentence::applicationId eq currentApplicationId

    /**
     *  add the filter on deleted Classified Sentences if cannot use aggegration pipeline with documentDB
     */
    private fun FaqQuery.filterNotDeletedClassifiedSentencesStatus() =
        FaqQueryResult::utterances / ClassifiedSentence::status ne ClassifiedSentenceStatus.deleted

    /**
     * Filter on i18nIds
     * @param i18nIds: List<Id<I18nLabel>>?
     */
    private fun FaqQuery.filterI18nIds(i18nIds: List<Id<I18nLabel>>?): Bson? =
        //i18nIds are optional and can be used if the request has i18nIds
        if (i18nIds == null) null else FaqQueryResult::i18nId `in` i18nIds

    /**
     * Filter on the tags
     */
    private fun FaqQuery.filterTags(): Bson? = if (tags.isEmpty()) null else FaqQueryResult::tags `in` tags

    /**
     * Filter if the faq is activated/enabled
     */
    private fun FaqQuery.filterEnabled(): Bson? = if (enabled == null) null else FaqQueryResult::enabled eq enabled

    /**
     * Filter on the botId and the namespace
     */
    private fun filterOnNamespaceAndBotId(botId: String, namespace: String): Bson =
        and(FaqDefinition::botId eq botId, FaqDefinition::namespace eq namespace)

    /**
     * Group aggregation pipeline to recompose and group data after multiple unwind especially due to utterances unwind
     */
    private fun FaqQuery.groupFaqDefinitionDetailedData(): Bson =
        group(
            FaqQueryResult::_id,
            listOf(
                FaqQueryResult::botId first FaqQueryResult::botId,
                FaqQueryResult::intentId first FaqQueryResult::intentId,
                FaqQueryResult::namespace first FaqQueryResult::namespace,
                FaqQueryResult::i18nId first FaqQueryResult::i18nId,
                FaqQueryResult::tags first FaqQueryResult::tags,
                FaqQueryResult::enabled first FaqQueryResult::enabled,
                FaqQueryResult::creationDate first FaqQueryResult::creationDate,
                FaqQueryResult::updateDate first FaqQueryResult::updateDate,
                FaqQueryResult::utterances first FaqQueryResult::utterances,
                FaqQueryResult::faq first FaqQueryResult::faq
            )
        )

    /**
     * Perform a lookup join from the FaqDefinition.intentId on IntentDefinition._id
     */
    private fun joinOnIntentDefinition() = lookup(
        INTENT_DEFINITION_COLLECTION,
        FaqDefinition::intentId.name,
        IntentDefinition::_id.name,
        FaqQueryResult::faq.name
    )

    /**
     * Perform a lookup join from the FaqDefinition.intentId on ClassifiedSentence.classification.intentId and avoid returning deleted sentences
     */
    private fun FaqQuery.joinOnClassifiedSentence() =
        lookup(
            CLASSIFIED_SENTENCE_COLLECTION,
            FaqDefinition::intentId.name,
            (ClassifiedSentence::classification / Classification::intentId).name,  //classification.intentId
            FaqQueryResult::utterances.name,
        )

    /**
     * Perform a lookup join from the FaqDefinition.intentId on ClassifiedSentence.classification.intentId and avoid returning deleted and inboxed sentences
     */
    private fun FaqQuery.joinOnClassifiedSentenceStatusNotDeletedAndNotInboxed(applicationId: Id<ApplicationDefinition>) =
        //inspired from https://github.com/Litote/kmongo/blob/master/kmongo-core-tests/src/main/kotlin/org/litote/kmongo/AggregateTypedTest.kt#L322
        lookup(
            CLASSIFIED_SENTENCE_COLLECTION,
            // declare a variable FAQ_INTENTID to call it in the pipeline exp below
            listOf(
                Variable(FAQ_INTENTID, FaqDefinition::intentId)
            ),
            FaqQueryResult::utterances,
            match(
                expr(
                    and from
                            listOf(
                                // join classifiedSentence intentId and FaqDefinition intentId
                                eq from listOf(
                                    ClassifiedSentence::classification / Classification::intentId,
                                    "\$\$$FAQ_INTENTID"
                                ),
                                //filter on current applicationId
                                eq from listOf(ClassifiedSentence::applicationId, applicationId),
                                // do not take classified sentences with deleted status because of the BuildWorker scheduled delay (1 second)
                                // needed to check and erase the ones with deleted status each
                                ne from listOf(
                                    ClassifiedSentence::status,
                                    ClassifiedSentenceStatus.deleted
                                ),
                                ne from listOf(
                                    ClassifiedSentence::status,
                                    ClassifiedSentenceStatus.inbox
                                )
                            ),
                )
            )
        )


    /**
     * Create group by tag name
     * {
     * _id: "tag"
     * tags: "tag"
     * }
     */
    private fun groupByTag(): Bson = group(
        FaqDefinition::tags,
        FaqDefinition::tags.first(FaqQueryResult::tags)
    )

    /**
     * Exclude the _id
     * Just keep the tag element
     */
    private fun projectByTag(): Bson = project(
        excludeId(),
        document(FaqDefinitionTag::tag from FaqDefinition::tags)
    )

    /**
     * Util method to do an union on the not null filter predicates
     */
    private fun orNotNull(vararg predicates: Bson?): Bson {
        return or(
            predicates.filterNotNull()
        )
    }

    /**
     * Util method to do an intersection on the not null filter predicates
     */
    private fun andNotNull(vararg predicates: Bson?): Bson {
        return and(
            predicates.filterNotNull()
        )
    }

    /**
     * Util method to sort ascending the properties given
     */
    private fun sortAscending(vararg properties: KProperty<*>): Bson {
        return sort(ascending(properties.asList()))
    }

    /**
     * Util method to sort descending the properties given
     */
    private fun sortDescending(vararg properties: KProperty<*>): Bson {
        return sort(descending(properties.asList()))
    }

}

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

package ai.tock.nlp.front.storage.mongo

import ai.tock.nlp.front.service.storage.ParseRequestLogDAO
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.monitoring.ParseRequestExportLog
import ai.tock.nlp.front.shared.monitoring.ParseRequestLog
import ai.tock.nlp.front.shared.monitoring.ParseRequestLogCount
import ai.tock.nlp.front.shared.monitoring.ParseRequestLogCountQuery
import ai.tock.nlp.front.shared.monitoring.ParseRequestLogCountQueryResult
import ai.tock.nlp.front.shared.monitoring.ParseRequestLogIntentStat
import ai.tock.nlp.front.shared.monitoring.ParseRequestLogQuery
import ai.tock.nlp.front.shared.monitoring.ParseRequestLogQueryResult
import ai.tock.nlp.front.shared.monitoring.ParseRequestLogStat
import ai.tock.nlp.front.shared.monitoring.ParseRequestLogStatQuery
import ai.tock.nlp.front.shared.parser.ParseQuery
import ai.tock.nlp.front.shared.parser.ParseResult
import ai.tock.nlp.front.storage.mongo.DayAndYear_.Companion.DayOfYear
import ai.tock.nlp.front.storage.mongo.DayAndYear_.Companion.Year
import ai.tock.nlp.front.storage.mongo.MongoFrontConfiguration.database
import ai.tock.nlp.front.storage.mongo.ParseRequestLogCol_.Companion.ApplicationId
import ai.tock.nlp.front.storage.mongo.ParseRequestLogCol_.Companion.Date
import ai.tock.nlp.front.storage.mongo.ParseRequestLogCol_.Companion.DurationInMS
import ai.tock.nlp.front.storage.mongo.ParseRequestLogCol_.Companion.Error
import ai.tock.nlp.front.storage.mongo.ParseRequestLogCol_.Companion.Query
import ai.tock.nlp.front.storage.mongo.ParseRequestLogCol_.Companion.Result
import ai.tock.nlp.front.storage.mongo.ParseRequestLogCol_.Companion.Text
import ai.tock.nlp.front.storage.mongo.ParseRequestLogIntentStatCol_.Companion.Intent1
import ai.tock.nlp.front.storage.mongo.ParseRequestLogIntentStatCol_.Companion.Intent2
import ai.tock.nlp.front.storage.mongo.ParseRequestLogStatCol_.Companion.Intent
import ai.tock.nlp.front.storage.mongo.ParseRequestLogStatCol_.Companion.Language
import ai.tock.nlp.front.storage.mongo.ParseRequestLogStatCol_.Companion.LastUsage
import ai.tock.nlp.front.storage.mongo.ParseRequestLogStatResult_.Companion.Count
import ai.tock.nlp.front.storage.mongo.ParseRequestLogStatResult_.Companion.Duration
import ai.tock.nlp.front.storage.mongo.ParseRequestLogStatResult_.Companion.EntitiesProbability
import ai.tock.nlp.front.storage.mongo.ParseRequestLogStatResult_.Companion.IntentProbability
import ai.tock.nlp.front.storage.mongo.ParseRequestLogStatResult_.Companion._id
import ai.tock.shared.defaultCountOptions
import ai.tock.shared.ensureIndex
import ai.tock.shared.ensureUniqueIndex
import ai.tock.shared.error
import ai.tock.shared.longProperty
import ai.tock.shared.name
import ai.tock.shared.withNamespace
import com.mongodb.ReadPreference.secondaryPreferred
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.ReturnDocument.AFTER
import mu.KotlinLogging
import org.bson.Document
import org.litote.jackson.data.JacksonData
import org.litote.kmongo.Data
import org.litote.kmongo.Id
import org.litote.kmongo.aggregate
import org.litote.kmongo.and
import org.litote.kmongo.ascending
import org.litote.kmongo.avg
import org.litote.kmongo.combine
import org.litote.kmongo.cond
import org.litote.kmongo.dayOfYear
import org.litote.kmongo.descendingSort
import org.litote.kmongo.document
import org.litote.kmongo.eq
import org.litote.kmongo.excludeId
import org.litote.kmongo.fields
import org.litote.kmongo.findOne
import org.litote.kmongo.from
import org.litote.kmongo.getCollection
import org.litote.kmongo.group
import org.litote.kmongo.gte
import org.litote.kmongo.inc
import org.litote.kmongo.include
import org.litote.kmongo.lte
import org.litote.kmongo.match
import org.litote.kmongo.newId
import org.litote.kmongo.project
import org.litote.kmongo.regex
import org.litote.kmongo.save
import org.litote.kmongo.setValue
import org.litote.kmongo.sort
import org.litote.kmongo.sum
import org.litote.kmongo.withDocumentClass
import org.litote.kmongo.year
import java.time.Instant
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit.DAYS

/**
 *
 */
internal object ParseRequestLogMongoDAO : ParseRequestLogDAO {
    private val logger = KotlinLogging.logger {}
    private val logObfuscationService = LogObfuscationService()

    @Data(internal = true)
    @JacksonData(internal = true)
    data class ParseRequestLogCol(
        val text: String,
        val applicationId: Id<ApplicationDefinition>,
        val query: ParseQuery,
        val result: ParseResult? = null,
        val durationInMS: Long = 0,
        val error: Boolean = false,
        val date: Instant = Instant.now(),
    ) {
        constructor(request: ParseRequestLog) :
            this(
                textKey(request.result?.retainedQuery ?: request.query.queries.firstOrNull() ?: ""),
                request.applicationId,
                request.query,
                request.result,
                request.durationInMS,
                request.error,
                request.date,
            )

        fun toRequest(): ParseRequestLog =
            ParseRequestLog(
                applicationId,
                query,
                result,
                durationInMS,
                error,
                date,
            )
    }

    @Data(internal = true)
    @JacksonData(internal = true)
    data class ParseRequestLogStatCol(
        val text: String,
        val applicationId: Id<ApplicationDefinition>,
        val language: Locale,
        val intent: String? = null,
        val intentProbability: Double? = null,
        val entitiesProbability: Double? = null,
        val lastUsage: Instant = Instant.now(),
        val count: Long = 1,
        val validated: Boolean = false,
    ) {
        constructor(request: ParseRequestLog) :
            this(
                textKey(request.result?.retainedQuery ?: request.query.queries.firstOrNull() ?: ""),
                request.applicationId,
                request.query.context.language,
                request.result?.intentNamespace?.let { namespace ->
                    request.result?.intent?.withNamespace(namespace)
                },
                request.result?.intentProbability,
                request.result?.entitiesProbability,
                request.date,
            )

        fun toRequestLogStat(): ParseRequestLogCount =
            ParseRequestLogCount(
                text = text,
                intent = intent,
                intentProbability = intentProbability,
                entitiesProbability = entitiesProbability,
                lastUsage = lastUsage,
                count = count,
                validated = validated,
            )
    }

    @Data(internal = true)
    @JacksonData(internal = true)
    data class ParseRequestLogIntentStatCol(
        val applicationId: Id<ApplicationDefinition>,
        val language: Locale,
        val intent1: String,
        val intent2: String,
        val averageDiff: Double,
        val count: Long = 1,
        val _id: Id<ParseRequestLogIntentStatCol> = newId(),
    )

    @Data(internal = true)
    @JacksonData(internal = true)
    data class DayAndYear(val dayOfYear: Int, val year: Int)

    @Data(internal = true)
    @JacksonData(internal = true)
    data class ParseRequestLogStatResult(
        val _id: DayAndYear,
        val error: Int,
        val count: Int,
        val duration: Double,
        val intentProbability: Double,
        val entitiesProbability: Double,
    ) {
        fun toStat(): ParseRequestLogStat =
            ParseRequestLogStat(
                LocalDate.ofYearDay(_id.year, _id.dayOfYear),
                error,
                count,
                duration,
                intentProbability,
                entitiesProbability,
            )
    }

    internal val col: MongoCollection<ParseRequestLogCol> by lazy {
        val c = database.getCollection<ParseRequestLogCol>("parse_request_log")
        try {
            c.ensureIndex(Query.context.language, ApplicationId, Query.context.test)
            c.ensureIndex(Query.context.language, ApplicationId, Text)
            c.ensureIndex(
                Date,
                indexOptions = IndexOptions().expireAfter(longProperty("tock_nlp_log_index_ttl_days", 7), DAYS),
            )
        } catch (e: Exception) {
            logger.error(e)
        }
        c.withReadPreference(secondaryPreferred())
    }

    internal val statsCol: MongoCollection<ParseRequestLogStatCol> by lazy {
        val c = database.getCollection<ParseRequestLogStatCol>("parse_request_log_stats")
        try {
            c.ensureIndex(Language, ApplicationId, Text)
            c.ensureIndex(Count, Language, ApplicationId, Text, Intent)
            c.ensureIndex(
                LastUsage,
                indexOptions =
                    IndexOptions()
                        .expireAfter(longProperty("tock_nlp_log_stats_index_ttl_days", 365), DAYS),
            )
        } catch (e: Exception) {
            logger.error(e)
        }
        c
    }

    internal val intentStatsCol: MongoCollection<ParseRequestLogIntentStatCol> by lazy {
        val c = database.getCollection<ParseRequestLogIntentStatCol>("parse_request_log_intent_stats")
        c.ensureUniqueIndex(Language, ApplicationId, Intent1, Intent2)
        c
    }

    override fun save(log: ParseRequestLog) {
        val savedLog = logObfuscationService.obfuscate(log)
        col.insertOne(ParseRequestLogCol(savedLog))
        if (log.query.context.increaseQueryCounter) {
            val stat = ParseRequestLogStatCol(log)
            val updatedStat =
                statsCol.findOneAndUpdate(
                    and(
                        Language eq stat.language,
                        ApplicationId eq stat.applicationId,
                        Text eq stat.text,
                    ),
                    combine(
                        listOfNotNull(
                            stat.intentProbability?.let { setValue(IntentProbability, it) },
                            stat.entitiesProbability?.let { setValue(EntitiesProbability, it) },
                            setValue(LastUsage, stat.lastUsage),
                            stat.intent?.let { setValue(Intent, it) },
                            inc(Count, 1),
                        ),
                    ),
                    FindOneAndUpdateOptions().upsert(true).returnDocument(AFTER),
                )
            if (updatedStat != null) {
                ClassifiedSentenceMongoDAO.updateSentenceState(updatedStat)
            }

            var intent1 = log.result?.intent
            val nextIntent = log.result?.otherIntentsProbabilities?.asSequence()?.firstOrNull()
            var intent2 = nextIntent?.key?.name()
            if (intent1 != null && intent2 != null && intent2 != intent1) {
                // order by string intent
                if (intent1 > intent2) {
                    val tmp = intent1
                    intent1 = intent2
                    intent2 = tmp
                }
                val diff = log.result!!.intentProbability - nextIntent!!.value
                val s =
                    intentStatsCol.findOne(
                        and(
                            Language eq stat.language,
                            ApplicationId eq stat.applicationId,
                            Intent1 eq intent1,
                            Intent2 eq intent2,
                        ),
                    )?.run {
                        copy(
                            count = count + 1,
                            averageDiff = ((averageDiff * count) + diff) / (count + 1),
                        )
                    }
                        ?: ParseRequestLogIntentStatCol(
                            log.applicationId,
                            log.query.context.language,
                            intent1,
                            intent2,
                            diff,
                            1,
                        )
                intentStatsCol.save(s)
            }
        }
    }

    override fun search(query: ParseRequestLogQuery): ParseRequestLogQueryResult {
        with(query) {
            val baseFilter =
                and(
                    ApplicationId eq applicationId,
                    if (language == null) null else Query.context.language eq language,
                    if (query.displayTests) null else Query.context.test eq false,
                    when {
                        search.isNullOrBlank() -> null
                        query.onlyExactMatch -> Text eq search
                        else -> Text.regex(search!!.trim(), "i")
                    },
                    if (searchMark == null) null else Date lte searchMark!!.date,
                    if (sinceDate == null) null else Date gte sinceDate,
                    if (clientDevice.isNullOrBlank()) null else Query.context.clientDevice eq clientDevice,
                    if (clientId.isNullOrBlank()) null else Query.context.clientId eq clientId,
                )
            val count = col.countDocuments(baseFilter, defaultCountOptions)
            return if (count > start) {
                val list =
                    col.find(baseFilter)
                        .descendingSort(Date)
                        .skip(start.toInt())
                        .limit(size)

                ParseRequestLogQueryResult(count, list.map { it.toRequest() }.toList())
            } else {
                ParseRequestLogQueryResult(0, emptyList())
            }
        }
    }

    override fun search(query: ParseRequestLogCountQuery): ParseRequestLogCountQueryResult {
        with(query) {
            val baseFilter =
                and(
                    ApplicationId eq applicationId,
                    Language eq language,
                    Count gte query.minCount,
                    if (intent == null) null else Intent eq intent,
                )
            val count = statsCol.countDocuments(baseFilter, defaultCountOptions)
            return if (count > start) {
                val list =
                    statsCol.find(baseFilter)
                        .descendingSort(Count)
                        .skip(start.toInt())
                        .limit(size)

                ParseRequestLogCountQueryResult(count, list.map { it.toRequestLogStat() }.toList())
            } else {
                ParseRequestLogCountQueryResult(0, emptyList())
            }
        }
    }

    override fun export(
        applicationId: Id<ApplicationDefinition>,
        language: Locale,
    ): List<ParseRequestExportLog> =
        col.withDocumentClass<Document>()
            .find(and(ApplicationId eq applicationId, Query.context.language eq language, Query.context.test eq false))
            .descendingSort(Date)
            .projection(fields(include(Query.queries, Result.intent, Result.intentNamespace, Date), excludeId()))
            .map {
                @Suppress("UNCHECKED_CAST")
                ParseRequestExportLog(
                    ((it["query"] as Document)["queries"] as List<String>).first(),
                    (it["result"] as? Document)?.getString("intent")
                        ?.let { intent -> (it["result"] as Document).getString("intentNamespace") + ":" + intent },
                    (it["date"] as Date).toInstant(),
                )
            }
            .toList()

    override fun stats(query: ParseRequestLogStatQuery): List<ParseRequestLogStat> {
        return with(query) {
            col.aggregate<ParseRequestLogStatResult>(
                match(
                    and(
                        listOfNotNull(
                            ApplicationId eq applicationId,
                            if (language == null) null else Query.context.language eq language,
                            Query.context.test eq false,
                            if (intent == null) null else Result.intent eq intent,
                        ),
                    ),
                ),
                project(
                    Error from cond(Error, 1, 0),
                    DayOfYear from dayOfYear(Date),
                    Year from year(Date),
                    Duration from DurationInMS,
                    IntentProbability from Result.intentProbability,
                    EntitiesProbability from Result.entitiesProbability,
                ),
                group(
                    document(
                        DayOfYear from DayOfYear,
                        Year from Year,
                    ),
                    Error sum Error,
                    Count sum 1,
                    Duration avg Duration,
                    IntentProbability avg IntentProbability,
                    EntitiesProbability avg EntitiesProbability,
                ),
                sort(
                    ascending(
                        _id.year,
                        _id.dayOfYear,
                    ),
                ),
            )
                .toList().map { it.toStat() }
        }
    }

    override fun intentStats(query: ParseRequestLogStatQuery): List<ParseRequestLogIntentStat> {
        return intentStatsCol
            .find(
                and(
                    ApplicationId eq query.applicationId,
                    Count gte query.minOccurrences,
                ),
            )
            .descendingSort(Count)
            .map {
                ParseRequestLogIntentStat(
                    it.intent1,
                    it.intent2,
                    it.count,
                    it.averageDiff,
                )
            }.toList()
    }
}

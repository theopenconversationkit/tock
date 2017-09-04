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

package fr.vsct.tock.nlp.front.storage.mongo

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Sorts
import fr.vsct.tock.nlp.front.service.storage.ParseRequestLogDAO
import fr.vsct.tock.nlp.front.shared.monitoring.ParseRequestLog
import fr.vsct.tock.nlp.front.shared.monitoring.ParseRequestLogQuery
import fr.vsct.tock.nlp.front.shared.monitoring.ParseRequestLogQueryResult
import fr.vsct.tock.nlp.front.shared.monitoring.ParseRequestLogStat
import fr.vsct.tock.nlp.front.shared.monitoring.ParseRequestLogStatQuery
import fr.vsct.tock.nlp.front.shared.parser.ParseQuery
import fr.vsct.tock.nlp.front.shared.parser.ParseResult
import fr.vsct.tock.nlp.front.storage.mongo.MongoFrontConfiguration.database
import fr.vsct.tock.shared.longProperty
import fr.vsct.tock.shared.security.StringObfuscatorService.obfuscate
import org.litote.kmongo.MongoOperator.avg
import org.litote.kmongo.MongoOperator.cond
import org.litote.kmongo.MongoOperator.dayOfYear
import org.litote.kmongo.MongoOperator.group
import org.litote.kmongo.MongoOperator.match
import org.litote.kmongo.MongoOperator.project
import org.litote.kmongo.MongoOperator.sort
import org.litote.kmongo.MongoOperator.sum
import org.litote.kmongo.MongoOperator.year
import org.litote.kmongo.aggregate
import org.litote.kmongo.count
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.find
import org.litote.kmongo.getCollection
import org.litote.kmongo.json
import org.litote.kmongo.toList
import java.time.Instant
import java.time.LocalDate
import java.util.concurrent.TimeUnit

/**
 *
 */
object ParseRequestLogMongoDAO : ParseRequestLogDAO {

    private data class ParseRequestLogCol(val text: String,
                                          val applicationId: String,
                                          val query: ParseQuery,
                                          val result: ParseResult?,
                                          val durationInMS: Long = 0,
                                          val error: Boolean = false,
                                          val date: Instant = Instant.now()) {

        constructor(request: ParseRequestLog) :
                this(
                        textKey(request.result?.retainedQuery ?: request.query.queries.firstOrNull() ?: ""),
                        request.applicationId,
                        request.query,
                        request.result,
                        request.durationInMS,
                        request.error,
                        request.date
                )

        fun toRequest(): ParseRequestLog =
                ParseRequestLog(
                        applicationId,
                        query,
                        result,
                        durationInMS,
                        error,
                        date)
    }

    private data class DayAndYear(val dayOfYear: Int, val year: Int)

    private data class ParseRequestLogStatResult(
            val _id: DayAndYear,
            val error: Int,
            val count: Int,
            val duration: Double,
            val intentProbability: Double,
            val entitiesProbability: Double) {

        fun toStat(): ParseRequestLogStat
                = ParseRequestLogStat(
                LocalDate.ofYearDay(_id.year, _id.dayOfYear),
                error,
                count,
                duration,
                intentProbability,
                entitiesProbability
        )
    }

    private val col: MongoCollection<ParseRequestLogCol> by lazy {
        val c = database.getCollection<ParseRequestLogCol>("parse_request_log")
        c.ensureIndex("{'query.context.language':1,'applicationId':1}")
        c.ensureIndex("{'query.context.language':1,'applicationId':1, 'text':1}")
        c.ensureIndex("{'date':1}", IndexOptions().expireAfter(longProperty("tock_nlp_log_index_ttl_days", 7), TimeUnit.DAYS))
        c
    }

    override fun save(log: ParseRequestLog) {
        val savedLog = log.copy(
                query = log.query.copy(queries = obfuscate(log.query.queries)),
                result = log.result?.copy(retainedQuery = obfuscate(log.result?.retainedQuery) ?: "")
        )
        col.insertOne(ParseRequestLogCol(savedLog))
    }

    override fun search(query: ParseRequestLogQuery): ParseRequestLogQueryResult {
        with(query) {
            val filter =
                    listOfNotNull(
                            "'applicationId':${applicationId.json}",
                            "'query.context.language':${language.json}",
                            if (search.isNullOrBlank()) null else if (query.onlyExactMatch) "'text':${search!!.json}" else "'text':/${search!!.trim()}/i"
                    ).joinToString(",", "{", "}")
            val count = col.count(filter)
            if (count > start) {
                val list = col.find(filter)
                        .skip(start.toInt()).limit(size).sort(Sorts.descending("_id")).toList()
                return ParseRequestLogQueryResult(count, list.map { it.toRequest() })
            } else {
                return ParseRequestLogQueryResult(0, emptyList())
            }
        }
    }

    override fun stats(query: ParseRequestLogStatQuery): List<ParseRequestLogStat> {
        return with(query) {
            val filter =
                    listOfNotNull(
                            "'applicationId':${applicationId.json}",
                            "'query.context.language':${language.json}",
                            if (intent == null) null else "'result.intent':${intent!!.json}"
                    ).joinToString(",", "{", "}")

            val match = "{$match:$filter}"
            val project = "{$project:{error:{$cond:['\$error',1,0]},dayOfYear:{$dayOfYear:'\$date'},year:{$year:'\$date'},duration:'\$durationInMS',intentProbability:'\$result.intentProbability',entitiesProbability:'\$result.entitiesProbability'}}"
            val group = "{$group:{ _id:{dayOfYear:'\$dayOfYear',year:'\$year'},error:{$sum:'\$error'},count:{$sum:1},duration:{$avg:'\$duration'},intentProbability:{$avg:'\$intentProbability'},entitiesProbability:{$avg:'\$entitiesProbability'}}}"
            val sort = "{$sort:{'_id.year':1,'_id.dayOfYear':1}}"
            col.aggregate<ParseRequestLogStatResult>(match, project, group, sort)
                    .toList().map { it.toStat() }

        }
    }
}
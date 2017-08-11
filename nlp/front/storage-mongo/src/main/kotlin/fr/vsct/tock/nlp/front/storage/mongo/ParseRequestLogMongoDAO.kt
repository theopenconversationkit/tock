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
import com.mongodb.client.model.Sorts
import fr.vsct.tock.nlp.front.service.storage.ParseRequestLogDAO
import fr.vsct.tock.nlp.front.shared.monitoring.ParseRequestLog
import fr.vsct.tock.nlp.front.shared.monitoring.ParseRequestLogQuery
import fr.vsct.tock.nlp.front.shared.monitoring.ParseRequestLogQueryResult
import fr.vsct.tock.nlp.front.shared.parser.ParseQuery
import fr.vsct.tock.nlp.front.shared.parser.ParseResult
import fr.vsct.tock.nlp.front.storage.mongo.MongoFrontConfiguration.database
import org.litote.kmongo.count
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.find
import org.litote.kmongo.getCollection
import org.litote.kmongo.json
import java.time.Instant

/**
 *
 */
object ParseRequestLogMongoDAO : ParseRequestLogDAO {

    //wrapper to workaround the 1024 chars limit for String indexes
    private fun textKey(text: String): String
            = if (text.length > 512) text.substring(0, Math.min(512, text.length)) else text

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

    private val col: MongoCollection<ParseRequestLogCol> by lazy {
        val c = database.getCollection<ParseRequestLogCol>("parse_request_log")
        c.ensureIndex("{'query.context.language':1,'applicationId':1}")
        c.ensureIndex("{'query.context.language':1,'applicationId':1, 'text':1}")
        c
    }

    override fun save(log: ParseRequestLog) {
        col.insertOne(ParseRequestLogCol(log))
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
}
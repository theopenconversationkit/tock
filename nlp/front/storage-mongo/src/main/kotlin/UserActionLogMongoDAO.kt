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

import ai.tock.nlp.front.service.storage.UserActionLogDAO
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.monitoring.UserActionLog
import ai.tock.nlp.front.shared.monitoring.UserActionLogQuery
import ai.tock.nlp.front.shared.monitoring.UserActionLogQueryResult
import ai.tock.nlp.front.storage.mongo.UserActionLogCol_.Companion.Date
import ai.tock.nlp.front.storage.mongo.UserActionLogCol_.Companion.Namespace
import ai.tock.shared.defaultCountOptions
import ai.tock.shared.error
import ai.tock.shared.jackson.AnyValueWrapper
import ai.tock.shared.longProperty
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.IndexOptions
import mu.KotlinLogging
import org.litote.jackson.data.JacksonData
import org.litote.kmongo.Data
import org.litote.kmongo.Id
import org.litote.kmongo.and
import org.litote.kmongo.descendingSort
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.eq
import org.litote.kmongo.getCollection
import java.time.Instant
import java.util.concurrent.TimeUnit.DAYS

internal object UserActionLogMongoDAO : UserActionLogDAO {
    private val logger = KotlinLogging.logger {}

    @Data(internal = true)
    @JacksonData(internal = true)
    data class UserActionLogCol(
        /**
         * The namespace of the application.
         */
        val namespace: String,
        /**
         * The application identifier.
         */
        val applicationId: Id<ApplicationDefinition>?,
        /**
         * The user login.
         */
        val login: String,
        /**
         * The action type.
         */
        val actionType: String,
        /**
         * New Data of any.
         */
        val newData: AnyValueWrapper?,
        /**
         * Is there a technical error?
         */
        val error: Boolean = false,
        /**
         * Date of the action.
         */
        val date: Instant = Instant.now(),
    ) {
        constructor(log: UserActionLog) :
            this(
                log.namespace,
                log.applicationId,
                log.login,
                log.actionType,
                log.newData?.let { AnyValueWrapper(it) },
                log.error,
                log.date,
            )

        fun toLog(): UserActionLog = UserActionLog(namespace, applicationId, login, actionType, newData?.value, error, date)
    }

    private val col: MongoCollection<UserActionLogCol> by lazy {
        val c = MongoFrontConfiguration.database.getCollection<UserActionLogCol>("user_action_log")
        try {
            c.ensureIndex(Namespace, Date)
            c.ensureIndex(
                Date,
                indexOptions = IndexOptions().expireAfter(longProperty("tock_user_log_index_ttl_days", 365), DAYS),
            )
        } catch (e: Exception) {
            logger.error(e)
        }
        c
    }

    override fun save(log: UserActionLog) {
        col.insertOne(UserActionLogCol(log))
    }

    override fun search(query: UserActionLogQuery): UserActionLogQueryResult {
        with(query) {
            val baseFilter =
                and(
                    Namespace eq namespace,
                )
            val count = col.countDocuments(baseFilter, defaultCountOptions)
            return if (count > start) {
                val list =
                    col.find(baseFilter)
                        .descendingSort(Date)
                        .skip(start.toInt())
                        .limit(size)

                UserActionLogQueryResult(count, list.map { it.toLog() }.toList())
            } else {
                UserActionLogQueryResult(0, emptyList())
            }
        }
    }
}

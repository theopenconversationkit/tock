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

package ai.tock.bot.mongo

import ai.tock.bot.engine.user.UserLock
import ai.tock.bot.mongo.MongoBotConfiguration.asyncDatabase
import ai.tock.bot.mongo.UserLock_.Companion.Date
import ai.tock.bot.mongo.UserLock_.Companion.Locked
import ai.tock.bot.mongo.UserLock_.Companion._id
import ai.tock.shared.error
import ai.tock.shared.longProperty
import com.mongodb.MongoWriteException
import com.mongodb.client.model.IndexOptions
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.litote.jackson.data.JacksonData
import org.litote.kmongo.Data
import org.litote.kmongo.Id
import org.litote.kmongo.and
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.lt
import org.litote.kmongo.or
import org.litote.kmongo.reactivestreams.getCollection
import org.litote.kmongo.toId
import org.litote.kmongo.upsert
import java.time.Instant
import java.time.Instant.now
import java.util.concurrent.TimeUnit.HOURS

/**
 *
 */
internal object MongoUserLock : UserLock {
    @Data(internal = true)
    @JacksonData(internal = true)
    data class UserLock(val _id: Id<UserLock>, val locked: Boolean = true, val date: Instant = now())

    private val logger = KotlinLogging.logger {}

    private val col = asyncDatabase.getCollection<UserLock>().coroutine

    private val lockTimeout = longProperty("tock_bot_lock_timeout_in_ms", 5000)

    init {
        try {
            runBlocking {
                col.ensureIndex(
                    Date,
                    indexOptions =
                        IndexOptions()
                            .expireAfter(
                                longProperty("mongo_user_ttl_hours", 6),
                                HOURS,
                            ),
                )
            }
        } catch (e: Exception) {
            logger.error(e)
        }
    }

    override suspend fun lock(userId: String): Boolean {
        val lock = UserLock(userId.toId())
        val validLockDatesLimit = now().minusMillis(lockTimeout)

        try {
            // Only for logging.
            if (logger.isDebugEnabled) {
                // Try to find existing user lock (for logging purpose only)
                val existingLock = col.findOneById(userId)
                logger.debug("lock user : $userId")
                if (existingLock != null && existingLock.locked && existingLock.date.isBefore(validLockDatesLimit)) {
                    logger.debug("previous lock date is too old")
                }
            }

            // This query finds unlocked UserLock objects, either because
            // their locked property is false or because their lock date
            // is too old
            val query =
                and(
                    _id eq lock._id,
                    or(
                        Locked eq false,
                        Date lt validLockDatesLimit,
                    ),
                )

            // Atomically take lock if it's unlocked
            //
            // upsert option will ensure we create the lock document if it doesn't
            // already exist. It will also trigger a duplicate key exception that
            // we'll capture to indicate lock is already taken
            col.updateOne(query, lock, upsert())

            return true
        } catch (e: Exception) {
            // lock could not be acquired
            if (e is MongoWriteException && e.code == 11000) {
                // duplicate key exception triggered by upsert
                logger.debug { "lock for user $userId already taken" }
            } else {
                logger.error(e)
            }
            return false
        }
    }

    override suspend fun releaseLock(userId: String) {
        try {
            logger.debug { "release lock for user : $userId" }
            val r = col.updateOneById(userId, org.litote.kmongo.setValue(Locked, false))
            if (r.modifiedCount == 0L) {
                logger.warn { "lock deleted or updated??? : $userId" }
            }
        } catch (e: Exception) {
            logger.error(e)
        }
    }

    suspend fun deleteLock(userId: String) {
        try {
            col.deleteOneById(userId)
        } catch (e: Exception) {
            logger.error(e)
        }
    }
}

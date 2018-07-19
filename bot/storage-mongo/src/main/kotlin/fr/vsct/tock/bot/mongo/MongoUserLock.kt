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

package fr.vsct.tock.bot.mongo

import com.mongodb.MongoWriteException
import com.mongodb.client.model.UpdateOptions
import fr.vsct.tock.bot.engine.user.UserLock
import fr.vsct.tock.bot.mongo.MongoBotConfiguration.database
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.longProperty
import mu.KotlinLogging
import org.litote.kmongo.*
import java.lang.Exception
import java.time.Instant
import java.time.Instant.now

/**
 *
 */
internal object MongoUserLock : UserLock {

    @Data(internal = true)
    data class UserLock(val _id: Id<UserLock>, val locked: Boolean = true, val date: Instant = now())

    private val logger = KotlinLogging.logger {}

    private val col = database.getCollection<UserLock>()

    private val lockTimeout = longProperty("tock_bot_lock_timeout_in_ms", 5000)

    override fun lock(userId: String): Boolean {
        val lock = UserLock(userId.toId())
        val validLockDatesLimit = now().minusMillis(lockTimeout)

        // This query finds unlocked UserLock objects, either because
        // their locked property is false or because their lock date
        // is too old
        val query = """
            {
                _id: "${lock._id}",
                ${MongoOperator.or}: [
                    { locked: false },
                    { date: { ${MongoOperator.lt} : ISODate("${validLockDatesLimit}") } }
                ]
            }
        """

        try {
            // Try to find existing user lock (for logging purpose only)
            val existingLock = col.findOneById(userId)

            // Atomically take lock if it's unlocked
            //
            // upsert option will ensure we create the lock document if it doesn't
            // already exist. It will also trigger a duplicate key exception that
            // we'll capture to indicate lock is already taken
            // (without it we would check update result)
            col.updateOne(query, lock, UpdateOptions().upsert(true))

            // at this point, lock has been acquired. A bit of logging.
            logger.debug { "lock user : $userId" }
            if (existingLock != null) {
                if (existingLock.locked == true && existingLock.date.isBefore(validLockDatesLimit)) {
                    logger.debug { "(previous lock date was too old" }
                }
            }

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


    override fun releaseLock(userId: String) {
        try {
            logger.debug { "release lock for user : $userId" }
            val lock = col.findOneById(userId)
            if (lock != null && lock.locked) {
                col.updateOneById(userId, UserLock(userId.toId(), false))
            } else {
                logger.warn { "lock deleted or updated??? : $userId" }
            }
        } catch (e: Exception) {
            logger.error(e)
        }
    }

    fun deleteLock(userId: String) {
        try {
            col.deleteOneById(userId)
        } catch (e: Exception) {
            logger.error(e)
        }
    }
}
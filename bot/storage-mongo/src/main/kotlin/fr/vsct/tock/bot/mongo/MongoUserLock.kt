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

import fr.vsct.tock.bot.engine.user.UserLock
import fr.vsct.tock.bot.mongo.MongoBotConfiguration.database
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.longProperty
import mu.KotlinLogging
import org.litote.kmongo.Data
import org.litote.kmongo.Id
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollection
import org.litote.kmongo.toId
import org.litote.kmongo.updateOneById
import java.lang.Exception
import java.time.Instant
import java.time.Instant.now

/**
 *
 */
object MongoUserLock : UserLock {

    @Data
    data class UserLock(val _id: Id<UserLock>, val locked: Boolean = true, val date: Instant = now())

    private val logger = KotlinLogging.logger {}

    private val col = database.getCollection<UserLock>()

    private val lockTimeout = longProperty("tock_bot_lock_timeout_in_ms", 5000)

    override fun lock(userId: String): Boolean {
        try {
            var lock = col.findOneById(userId)
            return if (lock == null) {
                lock = UserLock(userId.toId())
                col.insertOne(lock)
                logger.debug { "lock user : $userId" }
                true
            } else {
                if (!lock.locked) {
                    logger.debug { "lock user : $userId" }
                    col.updateOneById(lock._id, UserLock(userId.toId(), true))
                    true
                } else {
                    if (lock.date.plusMillis(lockTimeout).isBefore(now())) {
                        logger.warn { "lock user : $userId because lock date is too old" }
                        col.updateOneById(lock._id, UserLock(userId.toId(), true))
                        true
                    } else {
                        false
                    }
                }
            }
        } catch (e: Exception) {
            logger.error(e)
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
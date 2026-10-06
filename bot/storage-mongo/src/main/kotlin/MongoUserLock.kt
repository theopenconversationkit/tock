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

import ai.tock.bot.engine.user.LockAcquisitionException
import ai.tock.bot.engine.user.LockLostException
import ai.tock.bot.engine.user.UserLock
import ai.tock.bot.mongo.MongoBotConfiguration.asyncDatabase
import ai.tock.bot.mongo.UserLock_.Companion.Date
import ai.tock.bot.mongo.UserLock_.Companion.Locked
import ai.tock.bot.mongo.UserLock_.Companion._id
import ai.tock.shared.error
import ai.tock.shared.intProperty
import ai.tock.shared.longProperty
import com.mongodb.MongoWriteException
import com.mongodb.client.model.IndexOptions
import io.github.resilience4j.core.IntervalFunction
import io.github.resilience4j.kotlin.retry.RetryConfig
import io.github.resilience4j.kotlin.retry.executeSuspendFunction
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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
import org.litote.kmongo.setValue
import org.litote.kmongo.toId
import org.litote.kmongo.upsert
import java.time.Instant
import java.time.Instant.now
import java.util.UUID
import java.util.concurrent.TimeUnit.HOURS
import kotlin.random.Random

/**
 *
 */
internal object MongoUserLock : UserLock {
    @Data(internal = true)
    @JacksonData(internal = true)
    data class UserLock(
        val _id: Id<UserLock>,
        val locked: Boolean = true,
        val date: Instant = now(),
        val lockId: String? = null,
    )

    private val logger = KotlinLogging.logger {}

    private val col = asyncDatabase.getCollection<UserLock>().coroutine

    private val lockTimeout = longProperty("tock_bot_lock_timeout_in_ms", 5000)

    /** Interval at which to refresh a lease for an ongoing operation;
     short enough to never expire under normal conditions. */
    private val lockHeartbeatInterval = lockTimeout / 2

    private val lockRetryDelay = longProperty("tock_bot_locked_attempts_wait_in_ms", 500)

    private val lockRetryJitter = longProperty("tock_bot_lock_retry_jitter_in_ms", 20)

    private val lockRetryIntervalFunction =
        IntervalFunction { lockRetryDelay + Random.nextLong(0, lockRetryJitter + 1) }

    /** withLock() fails with an exception past this number of acquisition attempts */
    private val lockMaxAcquireAttempts = intProperty("tock_bot_max_locked_attempts", 10)

    /** Log a single warning once a caller has retried this many times without acquiring
     the lock, to surface potentially stuck locks. */
    private val lockAcquireWarnAttempts = longProperty("tock_bot_warn_after_locked_attempts", 5)

    private val lockRetryConfig: RetryConfig =
        RetryConfig<Boolean> {
            maxAttempts(lockMaxAcquireAttempts)
            retryOnResult { acquired -> !acquired }
            intervalFunction(lockRetryIntervalFunction)
            failAfterMaxAttempts(true)
        }
    private val retry =
        Retry.of("mongo-user-lock", lockRetryConfig).apply {
            eventPublisher
                .onRetry { event ->
                    if (event.numberOfRetryAttempts.toLong() == lockAcquireWarnAttempts) {
                        logger.warn { "still waiting for lock after ${event.numberOfRetryAttempts} attempts - possible stuck lock" }
                    }
                }.onError {
                    throw LockAcquisitionException("failed to acquire lock after ${it.numberOfRetryAttempts} attempts")
                }
        }

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

    override suspend fun tryLock(userId: String): Boolean = lock(userId, lockId = null)

    private suspend fun lock(
        userId: String,
        lockId: UUID?,
    ): Boolean {
        val lock = UserLock(userId.toId(), lockId = lockId?.toString())
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
        releaseLock(userId, null)
    }

    private suspend fun releaseLock(
        userId: String,
        lockId: UUID?,
    ) {
        try {
            logger.debug { "release lock for user : $userId" }
            // Only release the lock if it is still owned by lockId, so that we never release
            // a lock that has since been taken over by another owner (e.g. after expiration).
            val query = and(_id eq userId.toId(), UserLock::lockId eq lockId?.toString())
            val r = col.updateOne(query, setValue(Locked, false))
            if (r.modifiedCount == 0L) {
                logger.warn { "lock already released or taken over by another owner : $userId" }
            }
        } catch (e: Exception) {
            logger.error(e)
        }
    }

    /**
     * Touches the lock lease (resets its date) as long as [lockId] still owns it.
     * Returns false if the lease has already been taken over by another owner,
     * meaning the caller no longer holds a valid lock.
     */
    private suspend fun renewLock(
        userId: String,
        lockId: UUID,
    ): Boolean =
        try {
            val query = and(_id eq userId.toId(), UserLock::lockId eq lockId.toString())
            col.updateOne(query, setValue(Date, now())).modifiedCount > 0
        } catch (e: Exception) {
            logger.error(e)
            false
        }

    /**
     * @throws LockAcquisitionException if the lock could not be acquired after [lockMaxAcquireAttempts] attempts.
     */
    private suspend fun acquireLock(
        userId: String,
        lockId: UUID,
    ) {
        retry.executeSuspendFunction { lock(userId, lockId) }
    }

    override suspend fun <T> withLock(
        userId: String,
        abortOnLockLoss: Boolean,
        postLockRelease: (() -> Unit)?,
        op: suspend () -> T,
    ): T {
        val lockId = UUID.randomUUID()
        acquireLock(userId, lockId)

        return try {
            coroutineScope {
                val opJob = async { op() }
                // Periodically renews the lease while op() runs. If renewal ever fails,
                // another owner has genuinely taken over the lock: cancel op() when
                // abortOnLockLoss is requested, otherwise just log and let it continue.
                val heartbeatJob =
                    launch {
                        while (isActive) {
                            delay(lockHeartbeatInterval)
                            if (!renewLock(userId, lockId)) {
                                logger.warn { "lock for user $userId was taken over by another owner while running" }
                                if (abortOnLockLoss) {
                                    opJob.cancel(
                                        CancellationException(
                                            "lock for user $userId expired while op() was running",
                                            LockLostException("lock for user $userId expired while op() was running"),
                                        ),
                                    )
                                }
                                break
                            }
                        }
                    }
                try {
                    opJob.await()
                } catch (e: CancellationException) {
                    // unwrap our own LockLostException, leave other cancellation causes as-is
                    throw e.cause as? LockLostException ?: e
                } finally {
                    heartbeatJob.cancel()
                }
            }
        } finally {
            releaseLock(userId, lockId)
            postLockRelease?.invoke()
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

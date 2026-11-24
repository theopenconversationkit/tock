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

package ai.tock.bot.engine.user

import ai.tock.shared.intProperty
import ai.tock.shared.longProperty
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.util.UUID
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

/**
 * A [UserLock] made of leases stored in a database: a lease is taken for [lockTimeout], renewed while the
 * operation of [withLock] runs, and taken over by another owner once it has expired.
 *
 * The storages implement the four primitives; the acquisition retries, the renewal and the detection of a lost
 * lock are the same for all of them.
 *
 * Properties: `tock_bot_lock_timeout_in_ms` (5000), `tock_bot_locked_attempts_wait_in_ms` (500),
 * `tock_bot_lock_retry_jitter_in_ms` (20), `tock_bot_max_locked_attempts` (10),
 * `tock_bot_warn_after_locked_attempts` (5).
 */
abstract class LeasedUserLock : UserLock {
    private val logger = KotlinLogging.logger {}

    /** The duration of a lease, in milliseconds: an older lease is free. */
    protected val lockTimeout: Long = longProperty("tock_bot_lock_timeout_in_ms", 5000)

    /** Interval at which to refresh a lease for an ongoing operation; short enough to never expire under normal conditions. */
    private val lockHeartbeatInterval = lockTimeout / 2

    private val lockRetryDelay = longProperty("tock_bot_locked_attempts_wait_in_ms", 500)

    private val lockRetryJitter = longProperty("tock_bot_lock_retry_jitter_in_ms", 20)

    /** withLock() fails with an exception past this number of acquisition attempts */
    private val lockMaxAcquireAttempts = intProperty("tock_bot_max_locked_attempts", 10).coerceAtLeast(1)

    /** Log a single warning once a caller has retried this many times without acquiring the lock, to surface potentially stuck locks. */
    private val lockAcquireWarnAttempts = longProperty("tock_bot_warn_after_locked_attempts", 5)

    /**
     * Takes the lease of [userId] for [lockId] if it is free: never taken, released, or older than [lockTimeout].
     * A null [lockId] is the lease of [tryLock], released by [releaseLock].
     *
     * @return false if another owner holds the lease, or if it could not be taken (database error)
     */
    protected abstract suspend fun acquire(
        userId: String,
        lockId: String?,
    ): Boolean

    /**
     * Restarts the lease of [userId] as long as [lockId] still owns it.
     *
     * @return false if another owner has taken over the lease, null if this could not be determined (database error)
     */
    protected abstract suspend fun renew(
        userId: String,
        lockId: String,
    ): Boolean?

    /**
     * Checks, without restarting the lease, whether [lockId] still owns the lock of [userId].
     *
     * @return false if another owner has taken over the lease, null if this could not be determined (database error)
     */
    protected abstract suspend fun isOwner(
        userId: String,
        lockId: String,
    ): Boolean?

    /**
     * Frees the lease of [userId] if [lockId] still owns it, so that a lease taken over by another owner is never freed.
     *
     * @return false if the lease was already free or owned by another owner
     */
    protected abstract suspend fun release(
        userId: String,
        lockId: String?,
    ): Boolean

    override suspend fun tryLock(userId: String): Boolean = acquire(userId, lockId = null)

    override suspend fun releaseLock(userId: String) {
        releaseLock(userId, null)
    }

    private suspend fun releaseLock(
        userId: String,
        lockId: String?,
    ) {
        logger.debug { "release lock for user : $userId" }
        try {
            if (!release(userId, lockId)) {
                logger.warn { "lock already released or taken over by another owner : $userId" }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "unable to release the lock of user $userId" }
        }
    }

    /**
     * @throws LockAcquisitionException if the lock could not be acquired after [lockMaxAcquireAttempts] attempts.
     */
    private suspend fun acquireLock(
        userId: String,
        lockId: String,
    ) {
        var attempt = 1
        while (!acquire(userId, lockId)) {
            if (attempt >= lockMaxAcquireAttempts) {
                throw LockAcquisitionException("failed to acquire lock after $attempt attempts")
            }
            if (attempt.toLong() == lockAcquireWarnAttempts) {
                logger.warn { "still waiting for lock after $attempt attempts - possible stuck lock" }
            }
            delay(lockRetryDelay + Random.nextLong(0, lockRetryJitter + 1))
            attempt++
        }
    }

    override suspend fun <T> withLock(
        userId: String,
        abortOnLockLoss: Boolean,
        postLockRelease: (() -> Unit)?,
        maxLeaseRenewal: Duration,
        op: suspend () -> T,
    ): T {
        val lockId = UUID.randomUUID().toString()
        acquireLock(userId, lockId)
        // computed before launching op(), so that a delayed heartbeat cannot extend the renewal budget
        val renewalDeadline = if (maxLeaseRenewal.isInfinite()) null else TimeSource.Monotonic.markNow() + maxLeaseRenewal

        return try {
            coroutineScope {
                val opJob = async { op() }
                // The lease is renewed during at most maxLeaseRenewal, then left to expire on its own,
                // so that a stuck op() cannot hold the lock forever (see #2119). When abortOnLockLoss is requested,
                // ownership keeps being checked after renewal stops, so that op() is aborted once the lock is taken over.
                // The heartbeat runs on its own dispatcher, so that a blocking op() or a saturated op() dispatcher
                // (e.g. worker pool full of blocking turns) cannot delay the renewal.
                val heartbeatJob =
                    launch(Dispatchers.Default) {
                        monitorLockOwnership(userId, lockId, abortOnLockLoss, maxLeaseRenewal, renewalDeadline, opJob)
                    }
                try {
                    opJob.await()
                } catch (e: CancellationException) {
                    // unwrap our own LockLostException, leave other cancellation causes as-is
                    // (the whole cause chain is searched, as coroutine stack trace recovery may wrap the exception)
                    throw generateSequence<Throwable>(e) { it.cause }.filterIsInstance<LockLostException>().firstOrNull() ?: e
                } finally {
                    heartbeatJob.cancel()
                }
            }
        } finally {
            // the caller may have been cancelled: release the lock anyway
            withContext(NonCancellable) {
                releaseLock(userId, lockId)
            }
            postLockRelease?.invoke()
        }
    }

    private suspend fun <T> CoroutineScope.monitorLockOwnership(
        userId: String,
        lockId: String,
        abortOnLockLoss: Boolean,
        maxLeaseRenewal: Duration,
        renewalDeadline: TimeSource.Monotonic.ValueTimeMark?,
        opJob: Deferred<T>,
    ) {
        var renewalStopped = false
        while (isActive) {
            delay(lockHeartbeatInterval.milliseconds)
            val renew = renewalDeadline?.hasNotPassedNow() ?: true
            if (!renew && !renewalStopped) {
                renewalStopped = true
                if (maxLeaseRenewal.isPositive()) {
                    logger.warn { "operation still running after $maxLeaseRenewal for user $userId - lock lease is no longer renewed" }
                }
                if (!abortOnLockLoss) {
                    // nothing more to do: the lease will expire on its own, and op() is never aborted
                    break
                }
            }
            val stillOwned = if (renew) renew(userId, lockId) else isOwner(userId, lockId)
            // null: unknown (e.g. transient database error), try again at the next interval
            if (stillOwned == false) {
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
}

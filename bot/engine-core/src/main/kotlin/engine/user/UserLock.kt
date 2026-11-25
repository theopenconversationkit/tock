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

import kotlin.time.Duration

/**
 *
 */
interface UserLock {
    @Deprecated(
        message = "Use the more aptly named tryLock method",
        replaceWith = ReplaceWith("tryLock(userId)"),
    )
    suspend fun lock(userId: String): Boolean = tryLock(userId)

    /**
     * Acquires the user lock only if it is free at the time of invocation
     *
     * Acquires the lock for the given [userId] if it is available and returns immediately
     * with the value `true`.
     * If the lock is not available then this method will return
     * immediately with the value `false`.
     */
    suspend fun tryLock(userId: String): Boolean

    /**
     * Runs [op] while holding the lock for [userId].
     *
     * The lock is acquired (retrying until available). While [op] is running, the lease is periodically
     * renewed, so that long-running work is not pre-empted once the configured lease duration elapses.
     * Renewal stops once [op] has been running for [maxLeaseRenewal]: from then on the lease is left to
     * expire on its own, so that an [op] which never completes cannot hold the lock forever.
     *
     * If [abortOnLockLoss] is `true`, ownership of the lock is checked at each renewal interval (whether the
     * lease is renewed or not), and if the lock is found to have been taken over by another owner
     * (e.g. because the lease expired), [op] is aborted and this method throws [LockLostException].
     * Otherwise [op] keeps running despite the lost lock.
     *
     * The lock is always released once [op] completes, is canceled, or throws
     * (unless it has been taken over by another owner in the meantime).
     *
     * @param userId the user for which to lease a lock
     * @param abortOnLockLoss if `true`, aborts [op] when the lock is found to have been lost prematurely
     * @param postLockRelease optional listener to invoke after the lock is released (normally or after an exception)
     * @param maxLeaseRenewal the maximum duration during which the lease is renewed while [op] runs
     * ([Duration.INFINITE] by default, [Duration.ZERO] to never renew the lease)
     * @throws LockAcquisitionException when the lock cannot be acquired after a configurable amount of attempts
     * @throws LockLostException if the lock is lost mid-operation
     */
    suspend fun <T> withLock(
        userId: String,
        abortOnLockLoss: Boolean = true,
        postLockRelease: (() -> Unit)? = null,
        maxLeaseRenewal: Duration = Duration.INFINITE,
        op: suspend () -> T,
    ): T

    suspend fun releaseLock(userId: String)
}

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
     * The lock is acquired (retrying until available). Ownership of the lock is always
     * periodically checked for as long as [op] is running, independently of [renewLease]: when
     * [renewLease] is `true`, each check also extends the lease so long-running work is never
     * pre-empted once the configured lease duration elapses; when `false`, the lease is left
     * untouched but ownership is still verified. Either way, if [abortOnLockLoss] is `true` and
     * the lock is ever found to have been taken over by another owner (e.g. because [op] ran
     * long enough for the lease to expire), [op] is aborted and this method throws
     * [LockLostException]; otherwise [op] keeps running despite the lost lock.
     *
     * The lock is always released once [op] completes, is canceled, or throws.
     *
     * @param userId the user for which to lease a lock
     * @param abortOnLockLoss if `true`, aborts [op] when the lock is found to have been lost prematurely
     * @param renewLease if `true` (the default), periodically extends the lease while [op] runs, so that
     * long-running work is never pre-empted by another owner once the configured lease duration elapses.
     * Set to `false` only for callers whose [op] is known to always complete well within the lease duration,
     * where extending it would add overhead without benefit; ownership is still monitored either way.
     * @param postLockRelease optional listener to invoke after the lock is released (normally or after an exception)
     * @throws LockAcquisitionException when the lock cannot be acquired after a configurable amount of attempts
     * @throws LockLostException if the lock is lost mid-operation
     */
    suspend fun <T> withLock(
        userId: String,
        abortOnLockLoss: Boolean = true,
        renewLease: Boolean = true,
        postLockRelease: (() -> Unit)? = null,
        op: suspend () -> T,
    ): T

    suspend fun releaseLock(userId: String)
}

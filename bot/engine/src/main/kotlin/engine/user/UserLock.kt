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

    suspend fun lock(userId: String): Boolean

    /**
     * Acquires the user lock only if it is free at the time of invocation
     *
     * Acquires the lock for the given [userId] if it is available and returns immediately
     * with the value `true`.
     * If the lock is not available then this method will return
     * immediately with the value `false`.
     */
    suspend fun tryLock(userId: String): Boolean = lock(userId)

    suspend fun releaseLock(userId: String)
}

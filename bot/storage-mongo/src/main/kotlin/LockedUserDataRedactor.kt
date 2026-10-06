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
import ai.tock.shared.service.RedactionResult
import ai.tock.shared.service.UserDataRedactor

class LockedUserDataRedactor(
    val delegate: UserDataRedactor,
    val userLock: UserLock,
) : UserDataRedactor {
    override suspend fun migrateUserId(
        namespace: String,
        oldUserId: String,
        newUserId: String,
    ): RedactionResult {
        if (oldUserId == newUserId) return RedactionResult(recordsAffected = emptyMap())

        // Sort our identifiers to avoid concurrent opposite migrations causing a deadlock (unlikely but cheap to avoid)
        val firstId = minOf(oldUserId, newUserId)
        val secondId = maxOf(oldUserId, newUserId)
        return userLock.withLock(firstId) {
            userLock.withLock(secondId) {
                delegate.migrateUserId(namespace, oldUserId, newUserId)
            }
        }
    }

    override suspend fun deleteByUserId(
        namespace: String,
        userId: String,
    ): RedactionResult =
        userLock.withLock(userId) {
            delegate.deleteByUserId(namespace, userId)
        }
}

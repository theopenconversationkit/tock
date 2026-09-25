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

package ai.tock.shared.service

/**
 * Called by [UserDataRedactor]
 *
 * Implementations should be made available through a `META-INF/services/ai.tock.shared.service.UserDataRedactionProvider` file.
 */
interface UserDataRedactionProvider {
    /**
     * a short descriptive name for the type of data affected by this provider
     */
    val name: String

    /**
     * Migrates all data linked to a userId
     *
     * It is the caller's responsibility to ensure no concurrent access is made during the migration
     *
     * @return the number of records affected by this operation
     */
    suspend fun migrateUserId(
        namespace: String,
        oldUserId: String,
        newUserId: String,
    ): RedactionResult

    /**
     * Deletes all personal data linked to a userId
     *
     * It is the caller's responsibility to ensure no concurrent access is made during the deletion
     *
     * @return the number of records affected by this operation
     */
    suspend fun deleteByUserId(
        namespace: String,
        userId: String,
    ): RedactionResult
}

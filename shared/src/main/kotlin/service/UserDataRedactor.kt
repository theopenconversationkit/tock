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
 * Class used to redact out or otherwise update personally identifying information in the system
 *
 * The default implementation delegates to loaded [UserDataRedactionProvider], guarding each operation
 * with a lock based on `userId`
 */
interface UserDataRedactor {
    /**
     * Migrates all data linked to a userId
     *
     * @return the outcome of the operation, see [RedactionResult]
     */
    suspend fun migrateUserId(
        namespace: String,
        oldUserId: String,
        newUserId: String,
    ): RedactionResult

    /**
     * Deletes all personal data linked to a userId
     *
     * @return the outcome of the operation, see [RedactionResult]
     */
    suspend fun deleteByUserId(
        namespace: String,
        userId: String,
    ): RedactionResult
}

/**
 * Outcome of a redaction operation
 *
 * @property recordsAffected a summary of records successfully affected by this operation, summed
 * across every provider that completed without error
 * @property failures the failures raised by individual providers, if any. An empty list means every
 * provider completed successfully (though possibly affecting zero records)
 * @see UserDataRedactor.migrateUserId
 * @see UserDataRedactor.deleteByUserId
 */
data class RedactionResult(
    val recordsAffected: Map<String, Long>,
    val failures: List<RedactionFailure> = emptyList(),
) {
    /**
     * @param recordType a short descriptive name for the type of record affected
     * @param affectedCount the number of records successfully affected by this operation
     */
    constructor(recordType: String, affectedCount: Long) : this(mapOf(recordType to affectedCount))

    /** true if every provider completed without error. */
    val isSuccess: Boolean get() = failures.isEmpty()

    val totalRecordsAffected get() = recordsAffected.values.sum()

    companion object {
        context(provider: UserDataRedactionProvider)
        operator fun invoke(affectedCount: Long) = RedactionResult(provider.name, affectedCount)
    }
}

/**
 * A failure raised by a [UserDataRedactionProvider] while performing a redaction operation.
 *
 * @property providerName identifies the provider that failed, typically its class name
 * @property error the exception raised by the provider
 */
data class RedactionFailure(
    val providerName: String,
    val error: Throwable,
)

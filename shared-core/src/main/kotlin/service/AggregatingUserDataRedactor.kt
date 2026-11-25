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

import ai.tock.shared.Loader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import mu.KotlinLogging

/**
 * Aggregates every discovered [UserDataRedactionProvider]
 *
 * A provider throwing does not prevent the other providers from running. Each provider's
 * failure is caught, logged, and reported in the returned [RedactionResult.failures].
 *
 * @param delegates the providers to aggregate; defaults to every [UserDataRedactionProvider]
 * discovered via [Loader]
 */
class AggregatingUserDataRedactor(
    private val delegates: List<UserDataRedactionProvider> = Loader.loadServices(),
) : UserDataRedactor {
    private val logger = KotlinLogging.logger {}

    override suspend fun migrateUserId(
        namespace: String,
        oldUserId: String,
        newUserId: String,
    ): RedactionResult = runOnEachProvider { it.migrateUserId(namespace, oldUserId, newUserId) }

    override suspend fun deleteByUserId(
        namespace: String,
        userId: String,
    ): RedactionResult = runOnEachProvider { it.deleteByUserId(namespace, userId) }

    private suspend fun runOnEachProvider(operation: suspend (UserDataRedactionProvider) -> RedactionResult): RedactionResult {
        val recordsAffected = mutableMapOf<String, Long>()
        val failures = mutableListOf<RedactionFailure>()
        for (delegate in delegates) {
            currentCoroutineContext().ensureActive()
            try {
                val result = operation(delegate)
                for ((key, value) in result.recordsAffected) {
                    recordsAffected.merge(key, value, Long::plus)
                }
                failures += result.failures
            } catch (e: CancellationException) {
                throw e // cancellation should never be caught
            } catch (e: Exception) {
                val providerName = "${delegate.name} (${delegate::class.qualifiedName ?: delegate::class.toString()})"
                logger.error(e) { "redaction provider $providerName failed" }
                failures += RedactionFailure(providerName, e)
            }
        }
        return RedactionResult(recordsAffected, failures)
    }
}

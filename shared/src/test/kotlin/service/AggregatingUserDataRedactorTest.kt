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

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AggregatingUserDataRedactorTest {
    private val namespace = "namespace"
    private val oldUserId = "old"
    private val newUserId = "new"
    private val userId = "user"

    private fun result(count: Long) = RedactionResult("test_data", count)

    @Test
    fun `migrateUserId sums records affected across every provider`() {
        val provider1 =
            mockk<UserDataRedactionProvider> {
                coEvery { migrateUserId(namespace, oldUserId, newUserId) } returns result(2)
            }
        val provider2 =
            mockk<UserDataRedactionProvider> {
                coEvery { migrateUserId(namespace, oldUserId, newUserId) } returns result(3)
            }
        val redactor = AggregatingUserDataRedactor(listOf(provider1, provider2))

        val result = runBlocking { redactor.migrateUserId(namespace, oldUserId, newUserId) }

        assertEquals(5L, result.totalRecordsAffected)
        assertTrue(result.isSuccess)
        assertTrue(result.failures.isEmpty())
    }

    @Test
    fun `deleteByUserId sums records affected across every provider`() {
        val provider1 =
            mockk<UserDataRedactionProvider> {
                coEvery { deleteByUserId(namespace, userId) } returns result(1)
            }
        val provider2 =
            mockk<UserDataRedactionProvider> {
                coEvery { deleteByUserId(namespace, userId) } returns result(4)
            }
        val redactor = AggregatingUserDataRedactor(listOf(provider1, provider2))

        val result = runBlocking { redactor.deleteByUserId(namespace, userId) }

        assertEquals(5L, result.totalRecordsAffected)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `a failing provider does not prevent other providers from running`() {
        val failure = IllegalStateException("boom")
        val failingProvider =
            mockk<UserDataRedactionProvider> {
                every { name } returns "failing_provider"
                coEvery { deleteByUserId(namespace, userId) } throws failure
            }
        val succeedingProvider =
            mockk<UserDataRedactionProvider> {
                coEvery { deleteByUserId(namespace, userId) } returns result(7)
            }
        val redactor = AggregatingUserDataRedactor(listOf(failingProvider, succeedingProvider))

        val result = runBlocking { redactor.deleteByUserId(namespace, userId) }

        // the succeeding provider's effect is still counted...
        assertEquals(7L, result.totalRecordsAffected)
        // ...and the failure is reported, not swallowed
        assertFalse(result.isSuccess)
        assertEquals(1, result.failures.size)
        assertEquals(failure, result.failures.single().error)
    }

    @Test
    fun `every provider failing is reported without throwing`() {
        val failure1 = IllegalStateException("boom 1")
        val failure2 = IllegalStateException("boom 2")
        val provider1 =
            mockk<UserDataRedactionProvider> {
                every { name } returns "failing_1"
                coEvery { deleteByUserId(namespace, userId) } throws failure1
            }
        val provider2 =
            mockk<UserDataRedactionProvider> {
                every { name } returns "failing_2"
                coEvery { deleteByUserId(namespace, userId) } throws failure2
            }
        val redactor = AggregatingUserDataRedactor(listOf(provider1, provider2))

        val result = runBlocking { redactor.deleteByUserId(namespace, userId) }

        assertEquals(0L, result.totalRecordsAffected)
        assertFalse(result.isSuccess)
        assertEquals(listOf(failure1, failure2), result.failures.map { it.error })
    }

    @Test
    fun `no providers registered results in a successful empty result`() {
        val redactor = AggregatingUserDataRedactor(emptyList())

        val result = runBlocking { redactor.deleteByUserId(namespace, userId) }

        assertEquals(0L, result.totalRecordsAffected)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `cancellation is never caught and propagates immediately`() {
        val cancelledProvider =
            mockk<UserDataRedactionProvider> {
                coEvery { deleteByUserId(namespace, userId) } throws CancellationException("cancelled")
            }
        // this provider should never run: the cancellation from the first provider must
        // abort the aggregation instead of being treated as a regular failure
        val neverCalledProvider = mockk<UserDataRedactionProvider>()
        val redactor = AggregatingUserDataRedactor(listOf(cancelledProvider, neverCalledProvider))

        runBlocking {
            val job = async { redactor.deleteByUserId(namespace, userId) }
            assertFailsWith<CancellationException> { job.await() }
        }
    }
}

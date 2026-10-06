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

import ai.tock.bot.engine.user.LockLostException
import ai.tock.bot.engine.user.UserLock
import ai.tock.shared.service.RedactionResult
import ai.tock.shared.service.UserDataRedactor
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LockedUserDataRedactorTest {
    private val namespace = "namespace"
    private val oldUserId = "old"
    private val newUserId = "new"
    private val userId = "user"

    private val delegate = mockk<UserDataRedactor>()
    private val userLock = mockk<UserLock>()
    private val redactor = LockedUserDataRedactor(delegate, userLock)

    @BeforeEach
    fun setup() {
        // executes op() directly, as if the lock was instantly acquired
        coEvery { userLock.withLock(any(), any(), postLockRelease = any(), op = any<suspend () -> Any?>()) } coAnswers {
            arg<suspend () -> Any?>(3).invoke()
        }
    }

    @Test
    fun `migrateUserId locks both old and new userId before delegating`() {
        val expected = RedactionResult("test", 3L)
        coEvery { delegate.migrateUserId(namespace, oldUserId, newUserId) } returns expected

        val result = runBlocking { redactor.migrateUserId(namespace, oldUserId, newUserId) }

        assertEquals(expected, result)
        coVerifyOrder {
            userLock.withLock<RedactionResult>(newUserId, any(), any(), any())
            userLock.withLock<RedactionResult>(oldUserId, any(), any(), any())
            delegate.migrateUserId(namespace, oldUserId, newUserId)
        }
    }

    @Test
    fun `migrateUserId locks user ids in a deterministic order`() {
        val expected = RedactionResult("test", 3L)
        coEvery { delegate.migrateUserId(namespace, "z-user", "a-user") } returns expected

        val result = runBlocking { redactor.migrateUserId(namespace, "z-user", "a-user") }

        assertEquals(expected, result)
        coVerifyOrder {
            userLock.withLock<RedactionResult>("a-user", any(), any(), any())
            userLock.withLock<RedactionResult>("z-user", any(), any(), any())
            delegate.migrateUserId(namespace, "z-user", "a-user")
        }
    }

    @Test
    fun `migrateUserId is a no-op when both user ids are identical`() {
        val result = runBlocking { redactor.migrateUserId(namespace, userId, userId) }

        assertEquals(RedactionResult(emptyMap()), result)
        coVerify(exactly = 0) { userLock.withLock<RedactionResult>(any(), any(), any(), any()) }
        coVerify(exactly = 0) { delegate.migrateUserId(any(), any(), any()) }
    }

    @Test
    fun `deleteByUserId locks the userId before delegating`() {
        val expected = RedactionResult("test", 1L)
        coEvery { delegate.deleteByUserId(namespace, userId) } returns expected

        val result = runBlocking { redactor.deleteByUserId(namespace, userId) }

        assertEquals(expected, result)
        coVerify { userLock.withLock<RedactionResult>(userId, any(), any(), any()) }
        coVerify { delegate.deleteByUserId(namespace, userId) }
    }

    @Test
    fun `a delegate failure propagates to the caller`() {
        val failure = IllegalStateException("boom")
        coEvery { delegate.deleteByUserId(namespace, userId) } throws failure

        val thrown =
            assertFailsWith<IllegalStateException> {
                runBlocking { redactor.deleteByUserId(namespace, userId) }
            }
        assertEquals(failure, thrown)
    }

    @Test
    fun `a lost lock propagates as LockLostException, not swallowed`() {
        coEvery {
            userLock.withLock(userId, any(), any(), any<suspend () -> Any?>())
        } throws LockLostException("lock for user $userId expired while op() was running")

        assertFailsWith<LockLostException> {
            runBlocking { redactor.deleteByUserId(namespace, userId) }
        }
    }
}

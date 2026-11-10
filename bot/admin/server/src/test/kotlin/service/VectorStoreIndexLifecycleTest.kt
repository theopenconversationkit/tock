/*
 * Copyright (C) 2017/2026 SNCF Connect & Tech
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

package ai.tock.bot.admin.service

import ai.tock.bot.engine.user.LockAcquisitionException
import ai.tock.bot.engine.user.UserLock
import ai.tock.shared.exception.rest.RestException
import ai.tock.shared.tockInternalInjector
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.singleton
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class VectorStoreIndexLifecycleTest {
    private val previousInjector = tockInternalInjector
    private val lock = mockk<UserLock>()

    @BeforeEach
    fun setup() {
        tockInternalInjector = KodeinInjector().apply { inject(Kodein { bind<UserLock>() with singleton { lock } }) }
    }

    @AfterEach
    fun cleanup() {
        tockInternalInjector = previousInjector
    }

    @Test
    fun `direct deletion holds the worker lock before acquiring the lifecycle lock`() {
        val held = mutableListOf<String>()
        coEvery { lock.withLock<Any?>(any(), any(), any(), any(), any()) } coAnswers {
            held.add(firstArg())
            try {
                arg<suspend () -> Any?>(4).invoke()
            } finally {
                held.removeAt(held.lastIndex)
            }
        }
        VectorStoreIndexLifecycle.withProjectionLock("ns", "bot") {
            assertEquals(listOf("knowledge-base-projection-worker", "vector-store-lifecycle:${KnowledgeBaseService.hash("ns/bot")}"), held)
        }
        assertEquals(emptyList(), held)
    }

    @Test
    fun `a busy worker prevents deletion and returns an actionable conflict`() {
        coEvery { lock.withLock<Any?>(any(), any(), any(), any(), any()) } throws LockAcquisitionException("busy")
        var deleted = false
        val error =
            assertFailsWith<RestException> {
                VectorStoreIndexLifecycle.withProjectionLock("ns", "bot") { deleted = true }
            }
        assertEquals(409, error.httpResponseStatus.code())
        assertFalse(deleted)
    }
}

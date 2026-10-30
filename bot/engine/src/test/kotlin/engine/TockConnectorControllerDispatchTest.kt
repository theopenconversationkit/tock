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

package ai.tock.bot.engine

import ai.tock.shared.Executor
import ai.tock.shared.SimpleExecutor
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import io.mockk.coEvery
import io.mockk.every
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class TockConnectorControllerDispatchTest : BotEngineTest() {
    private val workerPoolName = "worker-pool"
    private val executor = SimpleExecutor(2, workerPoolName)

    override fun baseModule(): Kodein.Module =
        Kodein.Module {
            import(super.baseModule())
            bind<Executor>(overrides = true) with provider { executor }
        }

    @Test
    fun `GIVEN a call from an event loop thread WHEN handleUserEvent THEN blocking processing is done on the worker pool`() {
        coEvery { userLock.withLock<Any?>(any(), any(), any(), any(), any()) } coAnswers {
            @Suppress("UNCHECKED_CAST")
            (args[4] as suspend () -> Any?).invoke()
        }
        coEvery { userTimelineDAO.loadWithLastValidDialog(any(), any(), any(), any(), any()) } returns userTimeline
        var profileLoadingThread: String? = null
        every { connector.loadProfile(any(), any()) } answers {
            profileLoadingThread = Thread.currentThread().name
            null
        }

        val vertx = Vertx.vertx()
        try {
            runBlocking {
                withContext(vertx.orCreateContext.dispatcher()) {
                    connectorController.handleUserEvent(userAction, connectorData)
                }
            }
        } finally {
            runBlocking { vertx.close().coAwait() }
        }

        val thread = assertNotNull(profileLoadingThread)
        assertTrue(thread.startsWith(workerPoolName), "profile loaded on $thread")
    }
}

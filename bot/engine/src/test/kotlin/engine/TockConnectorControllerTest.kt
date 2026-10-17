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

import ai.tock.bot.connector.Connector
import ai.tock.bot.connector.ConnectorCallback
import ai.tock.bot.connector.ConnectorData
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.config.BotDefinitionWrapper
import ai.tock.bot.engine.user.LockAcquisitionException
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.bot.engine.user.UserLock
import ai.tock.bot.engine.user.UserTimeline
import ai.tock.bot.engine.user.UserTimelineDAO
import ai.tock.shared.injector
import ai.tock.shared.tockInternalInjector
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.invoke
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.CoroutineRouterSupport
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 *
 */
@OptIn(ai.tock.shared.coroutines.ExperimentalTockCoroutines::class)
class TockConnectorControllerTest {
    private val userLock: UserLock = mockk(relaxed = true)
    private val userTimelineDAO: UserTimelineDAO = mockk(relaxed = true)
    private val userTimeline: UserTimeline = mockk(relaxed = true)
    private val callback: ConnectorCallback = mockk(relaxed = true)
    private val connector: Connector = mockk(relaxed = true)
    private val bot: Bot = mockk(relaxed = true)
    private val botDefinition: BotDefinitionWrapper = mockk(relaxed = true)
    private val data = ConnectorData(callback)
    private val action = SendSentence(PlayerId("user"), "connector", PlayerId("bot", PlayerType.bot), "hello")

    @BeforeEach
    fun setUp() {
        tockInternalInjector = KodeinInjector()
        injector.inject(
            Kodein {
                bind<UserLock>() with provider { userLock }
                bind<UserTimelineDAO>() with provider { userTimelineDAO }
            },
        )
        System.setProperty("tock_bot_locked_attempts_wait_in_ms", "1")
        System.setProperty("tock_bot_max_locked_attempts", "3")

        every { bot.configuration } returns mockk()
        every { bot.botDefinition } returns botDefinition
        every { connector.connectorType } returns ConnectorType("test")
        every { botDefinition.eventListener.listenEvent(any(), any(), any()) } returns false
        every { botDefinition.errorAction(any(), any(), any()) } answers {
            SendSentence(PlayerId("bot", PlayerType.bot), "connector", PlayerId("user"), "error")
        }
        coEvery { userTimelineDAO.loadWithLastValidDialog(any(), any(), any(), any(), any()) } returns userTimeline
        coEvery { bot.handleAction(any(), any(), any(), any()) } returns Unit
        // simulates the real withLock: runs op, then notifies the release listener
        // NB: op is the 4th parameter (index 3); lastArg() would pick up the hidden
        // Continuation parameter added by the Kotlin compiler for suspend lambdas.
        coEvery { userLock.withLock<Any?>(any(), any(), any(), any()) } coAnswers {
            try {
                arg<suspend () -> Any?>(3).invoke()
            } finally {
                thirdArg<(() -> Unit)?>()?.invoke()
            }
        }
    }

    @AfterEach
    fun tearDown() {
        synchronousMode = true
        asynchronousMode = false
        System.clearProperty("tock_bot_locked_attempts_wait_in_ms")
        System.clearProperty("tock_bot_max_locked_attempts")
        tockInternalInjector = KodeinInjector()
    }

    private fun controller() = TockConnectorController(bot, connector, mockk(relaxed = true), botDefinition, mockk(relaxed = true))

    private fun lastAnswer() =
        SendSentence(PlayerId("bot", PlayerType.bot), "connector", PlayerId("user"), "answer").apply { metadata.lastAnswer = true }

    @Test
    fun `handleAction in synchronous mode saves the timeline and releases the lock via withLock`() {
        runBlocking { controller().handleUserEvent(action, data) }

        coVerify(exactly = 1) { userLock.withLock<Any?>("user", false, any(), any()) }
        coVerify(exactly = 0) { userLock.tryLock(any()) }
        coVerify(exactly = 0) { userLock.releaseLock(any()) }
        coVerify(exactly = 1) { userTimelineDAO.save(userTimeline, botDefinition) }
        verify(exactly = 1) { callback.userLocked(action) }
        verify(exactly = 1) { callback.userLockReleased(action) }
    }

    @Test
    fun `handleAction in synchronous mode skips the event when the lock cannot be acquired`() {
        coEvery { userLock.withLock<Any?>(any(), any(), any(), any()) } throws LockAcquisitionException("busy")

        runBlocking { controller().handleUserEvent(action, data) }

        verify(exactly = 1) { callback.eventSkipped(action) }
        coVerify(exactly = 0) { bot.handleAction(any(), any(), any(), any()) }
    }

    @Test
    fun `handleAction in asynchronous mode keeps the lock until the last answer is sent`() {
        asynchronousMode = true
        coEvery { userLock.tryLock("user") } returns true
        val controller = controller()

        runBlocking { controller.handleUserEvent(action, data) }

        coVerify(exactly = 1) { bot.handleAction(any(), userTimeline, any(), data) }
        coVerify(exactly = 0) { userLock.withLock<Any?>(any(), any(), any(), any()) }
        coVerify(exactly = 0) { userLock.releaseLock(any()) }
        coVerify(exactly = 0) { userTimelineDAO.save(any(), any(), any()) }
        verify(exactly = 0) { callback.userLockReleased(any()) }
    }

    @Test
    fun `send in asynchronous mode saves the timeline then releases the lock on last answer`() {
        asynchronousMode = true
        val controller = controller()

        controller.send(userTimeline, data, action, lastAnswer())

        coVerifyOrder {
            userTimelineDAO.save(userTimeline, botDefinition)
            callback.eventAnswered(action)
            userLock.releaseLock("user")
            callback.userLockReleased(action)
        }
    }

    @Test
    fun `send in asynchronous mode does not release the lock when it is not the last answer`() {
        asynchronousMode = true
        val notLast = SendSentence(PlayerId("bot", PlayerType.bot), "connector", PlayerId("user"), "partial")

        controller().send(userTimeline, data, action, notLast)

        coVerify(exactly = 0) { userLock.releaseLock(any()) }
        coVerify(exactly = 0) { userTimelineDAO.save(any(), any(), any()) }
        verify(exactly = 0) { callback.userLockReleased(any()) }
    }

    @Test
    fun `send in synchronous mode never releases the lock`() {
        controller().send(userTimeline, data, action, lastAnswer())

        coVerify(exactly = 0) { userLock.releaseLock(any()) }
        coVerify(exactly = 0) { userTimelineDAO.save(any(), any(), any()) }
        verify(exactly = 1) { callback.eventAnswered(action) }
    }

    @Test
    fun `handleAction in asynchronous mode retries until the lock is acquired`() {
        asynchronousMode = true
        coEvery { userLock.tryLock("user") } returnsMany listOf(false, false, true)

        runBlocking { controller().handleUserEvent(action, data) }

        coVerify(exactly = 3) { userLock.tryLock("user") }
        coVerify(exactly = 1) { bot.handleAction(any(), any(), any(), any()) }
        verify(exactly = 0) { callback.eventSkipped(any()) }
    }

    @Test
    fun `handleAction in asynchronous mode skips the event after max lock attempts`() {
        asynchronousMode = true
        coEvery { userLock.tryLock(any()) } returns false

        runBlocking { controller().handleUserEvent(action, data) }

        coVerify(exactly = 3) { userLock.tryLock("user") }
        coVerify(exactly = 0) { bot.handleAction(any(), any(), any(), any()) }
        verify(exactly = 1) { callback.eventSkipped(action) }
        coVerify(exactly = 0) { userLock.releaseLock(any()) }
    }

    @Test
    fun `handleAction in asynchronous mode releases the lock through the error answer on failure`() {
        asynchronousMode = true
        coEvery { userLock.tryLock("user") } returns true
        val error = IllegalStateException("boom")
        coEvery { bot.handleAction(any(), any(), any(), any()) } throws error

        runBlocking { controller().handleUserEvent(action, data) }

        verify(exactly = 1) { callback.exceptionThrown(action, error) }
        coVerify(exactly = 1) { userLock.releaseLock("user") }
        verify(exactly = 1) { callback.userLockReleased(action) }
    }

    @Test
    fun `handleAction in asynchronous mode does not leak the exception on lock failure`() {
        asynchronousMode = true
        coEvery { userLock.tryLock(any()) } throws IllegalStateException("db down")

        runBlocking { controller().handleUserEvent(action, data) }

        verify(exactly = 1) { callback.exceptionThrown(action, any()) }
        assertEquals(true, asynchronousMode)
    }

    @Test
    fun `test registerService install healthcheck`() {
        val botVerticle: BotVerticle = mockk()
        val serviceInstaller: BotVerticle.ServiceInstaller = mockk()
        val capturedInstaller = slot<CoroutineRouterSupport.(Router) -> Unit>()
        every { botVerticle.registerServices(any(), capture(capturedInstaller)) } returns serviceInstaller

        val installer: (Router) -> Unit = mockk()
        every { installer.invoke(any()) } returns Unit
        val bot: Bot = mockk()
        every { bot.configuration } returns mockk()
        val controller = TockConnectorController(bot, mockk(), botVerticle, mockk(), mockk())

        controller.registerServices("/path", installer)

        verify { botVerticle.registerServices(any(), any()) }

        val router: Router = mockk()
        val route: Route = mockk()
        every { router.get(any()) } returns route
        every { route.handler(any()) } returns route

        capturedInstaller.invoke(mockk(), router)

        verify { router.get("/path/healthcheck") }
        verify { installer.invoke(router) }
    }
}
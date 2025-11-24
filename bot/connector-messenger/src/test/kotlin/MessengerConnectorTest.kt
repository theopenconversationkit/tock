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
package ai.tock.bot.connector.messenger

import ai.tock.bot.connector.messenger.MessengerConnector.Companion.connectorIdApplicationIdMap
import ai.tock.bot.connector.messenger.MessengerConnector.Companion.connectorIdConnectorControllerMap
import ai.tock.bot.connector.messenger.MessengerConnector.Companion.connectorIdTokenMap
import ai.tock.bot.connector.messenger.MessengerConnector.Companion.pageIdConnectorIdMap
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.ActionMetadata
import ai.tock.bot.engine.user.PlayerId
import ai.tock.shared.Executor
import ai.tock.shared.SimpleExecutor
import ai.tock.shared.tockInternalInjector
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.singleton
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verifyOrder
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class MessengerConnectorTest {
    companion object {
        private fun clearState() {
            pageIdConnectorIdMap.clear()
            connectorIdConnectorControllerMap.clear()
            connectorIdTokenMap.clear()
            connectorIdApplicationIdMap.clear()
        }

        @BeforeAll
        @JvmStatic
        fun injectExecutor() {
            tockInternalInjector =
                KodeinInjector().apply {
                    inject(
                        Kodein {
                            import(
                                Kodein.Module {
                                    bind<Executor>() with singleton { SimpleExecutor(2) }
                                },
                            )
                        },
                    )
                }
        }

        @AfterAll
        @JvmStatic
        fun resetInjection() {
            tockInternalInjector = KodeinInjector()
        }
    }

    init {
        clearState()
    }

    val connectorId1 = "connectorId1"
    val path1 = "pathA"
    val appId1 = "appA"
    val pageId1 = "pageA"
    val appToken1 = "appToken"
    val token1 = "token"
    val verifyToken1 = "verifyToken"

    val messengerClient = mockk<MessengerClient>()

    val messengerConnector1: MessengerConnector =
        MessengerConnector(connectorId1, appId1, path1, pageId1, appToken1, token1, verifyToken1, messengerClient)
    val controller1 =
        mockk<ConnectorController>(relaxed = true).apply {
            every { connector } returns messengerConnector1
        }

    @AfterEach
    fun afterEach() {
        clearState()
    }

    @Test
    fun `GIVEN one messenger connector WHEN registering a new version of this connector and unregistering the old version THEN the new version is correctly registered`() {
        val controller2 =
            mockk<ConnectorController>(relaxed = true).apply {
                every { connector } returns messengerConnector1
            }

        messengerConnector1.register(controller1)
        assertEquals(controller1, connectorIdConnectorControllerMap[connectorId1])
        messengerConnector1.register(controller2)
        assertEquals(controller2, connectorIdConnectorControllerMap[connectorId1])
        messengerConnector1.unregister(controller1)
        assertEquals(controller2, connectorIdConnectorControllerMap[connectorId1])
        assertTrue(pageIdConnectorIdMap[pageId1]!!.contains(connectorId1))
        assertTrue(connectorIdApplicationIdMap[connectorId1]!!.contains(appId1))
    }

    @Test
    fun `GIVEN one messenger connector WHEN unregistering the version THEN version is not registered anymore`() {
        messengerConnector1.register(controller1)
        assertEquals(controller1, connectorIdConnectorControllerMap[connectorId1])
        messengerConnector1.unregister(controller1)
        assertNull(connectorIdConnectorControllerMap[connectorId1])
        assertTrue(pageIdConnectorIdMap[pageId1].isNullOrEmpty())
        assertTrue(connectorIdApplicationIdMap[connectorId1].isNullOrEmpty())
    }

    @Test
    fun `GIVEN two actions of same user WHEN sending the two actions THEN the second action wait the first to be sent`() {
        val connector = spyk(messengerConnector1)
        val userId = PlayerId("userId")
        val action1 =
            mockk<Action> {
                every { recipientId } returns userId
                every { metadata } returns ActionMetadata(lastAnswer = false)
            }
        val action2 =
            mockk<Action> {
                every { recipientId } returns userId
                every { metadata } returns ActionMetadata(lastAnswer = true)
            }
        val callback = MessengerConnectorCallback("appId")

        var time: Long? = null

        every { connector.sendEvent(any()) } answers {
            // check the second call occurs at least 100s after the first (as we wait 500 for the first call)
            if (time != null) {
                assert(System.currentTimeMillis() - time!! >= 100)
            } else {
                time = System.currentTimeMillis()
                Thread.sleep(500)
            }
            null
        }

        connector.send(action1, callback)
        connector.send(action2, callback)

        Thread.sleep(1000)

        verifyOrder {
            connector.send(action1, any(), any())
            connector.send(action2, any(), any())
            connector.sendEvent(action1, any(), any(), any(), any())
            connector.sendEvent(action2, any(), any(), any(), any())
        }
    }
}

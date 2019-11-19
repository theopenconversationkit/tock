/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
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
import ai.tock.bot.connector.messenger.MessengerConnector.Companion.pageIdConnectorIdMap
import ai.tock.bot.engine.ConnectorController
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class MessengerConnectorTest {

    val connectorId1 = "connectorId1"
    val path1 = "pathA"
    val appId1 = "appA"
    val pageId1 = "pageA"
    val appToken1 = "appToken"
    val token1 = "token"
    val verifyToken1 = "verifyToken"

    val messengerClient = mockk<MessengerClient>()

    val messengerConnector1 =
        MessengerConnector(connectorId1, appId1, path1, pageId1, appToken1, token1, verifyToken1, messengerClient)
    val controller1 = mockk<ConnectorController>(relaxed = true).apply {
        every { connector } returns messengerConnector1
    }


    @Test
    fun `GIVEN one messenger connector WHEN registering a new version of this connector and unregistering the old version THEN the new version is correctly registered`() {
        val controller2 = mockk<ConnectorController>(relaxed = true).apply {
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
    fun `GIVEN one messenger connector WHEN unregistering the version THEN not version is not registered anymore`() {
        messengerConnector1.register(controller1)
        assertEquals(controller1, connectorIdConnectorControllerMap[connectorId1])
        messengerConnector1.unregister(controller1)
        assertNull(connectorIdConnectorControllerMap[connectorId1])
        assertTrue(pageIdConnectorIdMap[pageId1].isNullOrEmpty())
        assertTrue(connectorIdApplicationIdMap[connectorId1].isNullOrEmpty())
    }

}
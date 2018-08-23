/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.connector.ga

import com.google.common.io.Resources
import fr.vsct.tock.bot.connector.ConnectorData
import fr.vsct.tock.bot.engine.ConnectorController
import fr.vsct.tock.bot.engine.event.LoginEvent
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType
import fr.vsct.tock.bot.engine.user.UserPreferences
import fr.vsct.tock.shared.resource
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 *
 */
class GAConnectorTest {

    val connector = GAConnector("appId", "/path", emptySet())
    val userPreferences: UserPreferences = UserPreferences()
    val controller: ConnectorController = mockk(relaxed = true)
    val context: RoutingContext = mockk(relaxed = true)
    val response: HttpServerResponse = mockk(relaxed = true)

    @BeforeEach
    fun before() {
        every { context.response() } returns response
    }

    @Test
    fun handleRequest_shouldHandleWell_NamePermissions() {

        every { controller.connector } returns connector
        every { controller.handle(any(), any()) } answers {
            userPreferences.fillWith(
                connector.loadProfile(
                    (secondArg() as ConnectorData).callback,
                    PlayerId("a", PlayerType.user)
                )!!
            )
        }

        connector.handleRequest(
            controller,
            context,
            Resources.toString(resource("/request-with-permission.json"), Charsets.UTF_8)
        )

        assertEquals("Pierre", userPreferences.firstName)
        assertEquals("Totor", userPreferences.lastName)
    }

    @Test
    fun handleRequest_should_send_loginEvent_to_revoke_token() {
        every { controller.connector } returns connector

        connector.handleRequest(
            controller,
            context,
            Resources.toString(resource("/request-with-ACCESS-TOKEN.json"), Charsets.UTF_8)
        )

        val slotLoginEvent = slot<LoginEvent>()
        verify { controller.handle(capture(slotLoginEvent), any()) }
        assertTrue { slotLoginEvent.captured.checkLogin }
    }

}
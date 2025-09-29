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

package ai.tock.bot.connector.ga

import ai.tock.bot.connector.ConnectorData
import ai.tock.bot.connector.ga.GAAccountLinking.Companion.isUserAuthenticated
import ai.tock.bot.connector.ga.GAAccountLinking.Companion.switchTimeLine
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.event.LoginEvent
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType.user
import ai.tock.bot.engine.user.UserPreferences
import ai.tock.shared.resource
import com.google.common.io.Resources
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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

    @AfterTest
    fun after() {
        unmockkObject(GAAccountLinking)
    }

    @Test
    fun handleRequest_shouldHandleWell_NamePermissions() {

        every { controller.connector } returns connector
        every { controller.handle(any(), any()) } answers {
            userPreferences.fillWith(
                connector.loadProfile(
                    (secondArg() as ConnectorData).callback,
                    PlayerId("a", user)
                )!!
            )
        }

        connector.handleRequest(
            controller,
            context,
            Resources.toString(resource("/request_with_permission.json"), Charsets.UTF_8)
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
            Resources.toString(resource("/request_with_signout_error.json"), Charsets.UTF_8)
        )

        val slotLoginEvent = slot<LoginEvent>()
        verify { controller.handle(capture(slotLoginEvent), any()) }
        assertTrue { slotLoginEvent.captured.checkLogin }
    }

    @Test
    fun `google bot timeline is not persisted`() {
        every { controller.connector } returns connector

        connector.handleRequest(
            controller,
            context,
            Resources.toString(resource("/healthcheck.json"), Charsets.UTF_8)
        )

        val connectorData = slot<ConnectorData>()
        verify { controller.handle(any(), capture(connectorData)) }
        assertFalse { connectorData.captured.saveTimeline }
    }

    @Test
    fun `GIVEN not google bot request THEN timeline is persisted`() {
        every { controller.connector } returns connector

        connector.handleRequest(
            controller,
            context,
            Resources.toString(resource("/request_with_signout_error.json"), Charsets.UTF_8)
        )

        val connectorData = slot<ConnectorData>()
        verify { controller.handle(any(), capture(connectorData)) }
        assertTrue { connectorData.captured.saveTimeline }
    }

    @Test
    fun `GIVEN new connected user and login event THEN new time line is created`() {
        every { controller.connector } returns connector
        mockkObject(GAAccountLinking)
        coEvery {
            switchTimeLine(
                "appId",
                PlayerId("jarvisteam@yopmail.com", user),
                PlayerId(
                    "ABwppHHaWSlfkEc3cou4A-K_rzAfjSsLZTkNEq3NLM_d8bVanmj61irxpfM8bPE1DA4NJD6Lw-4ZOQY43LIp9sWNj7w",
                    user
                ),
                controller
            )
        } answers {}

        connector.handleRequest(
            controller,
            context,
            Resources.toString(resource("/request_with_signin.json"), Charsets.UTF_8)
        )

        val connectorData = slot<ConnectorData>()
        verify { controller.handle(any(), capture(connectorData)) }
        assertTrue { connectorData.captured.saveTimeline }
        coVerify(exactly = 1) {
            switchTimeLine(
                "appId",
                PlayerId("jarvisteam@yopmail.com", user),
                PlayerId(
                    "ABwppHHaWSlfkEc3cou4A-K_rzAfjSsLZTkNEq3NLM_d8bVanmj61irxpfM8bPE1DA4NJD6Lw-4ZOQY43LIp9sWNj7w",
                    user
                ),
                controller
            )
        }
    }

    @Test
    fun `GIVEN connected user and not login event THEN login event is sent in order to refresh before callback event`() {
        every { controller.connector } returns connector
        mockkObject(GAAccountLinking)
        every {
            isUserAuthenticated(any())
        } returns true

        connector.handleRequest(
            controller,
            context,
            Resources.toString(resource("/request_with_access_token.json"), Charsets.UTF_8)
        )

        val loginEvent = slot<LoginEvent>()

        verify(exactly = 1) {
            controller.handle(
                capture(loginEvent),
                any()
            )
        }

        assertTrue(loginEvent.captured.checkLogin)
    }
}

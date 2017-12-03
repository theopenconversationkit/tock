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
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import fr.vsct.tock.bot.engine.ConnectorController
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType
import fr.vsct.tock.bot.engine.user.UserPreferences
import fr.vsct.tock.shared.resource
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 *
 */
class GAConnectorTest {

    val connector = GAConnector("", "", emptySet())
    val userPreferences: UserPreferences = UserPreferences()
    val controller: ConnectorController = mock {
        on { connector }.thenReturn(connector)
        on { handle(any(), any()) } doAnswer {
            userPreferences.fillWith(connector.loadProfile("", PlayerId("a", PlayerType.user))!!)
        }
    }
    val context: RoutingContext = mock()
    val response: HttpServerResponse = mock()

    @Before
    fun before() {
        whenever(context.response()).thenReturn(response)
    }

    @Test
    fun handleRequest_shouldHandleWell_NamePermissions() {
        connector.handleRequest(controller, context, Resources.toString(resource("/request-with-permission.json"), Charsets.UTF_8))

        assertEquals("Pierre", userPreferences.firstName)
        assertEquals("Totor", userPreferences.lastName)
    }

    @Test
    fun testHasPunctuation_shouldReturnTrue_whenStringEndWithDot() {
        assertTrue("hkh.".endWithPunctuation())
    }

}
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

package ai.tock.bot.connector.teams.auth

import ai.tock.bot.connector.teams.auth.MockServer.getMicrosoftMockServer
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JWKHandlerTest {

    private var authenticateBotConnectorService: AuthenticateBotConnectorService = AuthenticateBotConnectorService("fakeAppId")
    private lateinit var server: MockWebServer
    private val jwkHandler = JWKHandler()

    @BeforeAll
    fun launchJWKCollector() {
        authenticateBotConnectorService = AuthenticateBotConnectorService("fakeAppId")

        server = getMicrosoftMockServer()

        jwkHandler.setJKSBaseLocation("http://${server.hostName}:${server.port}/")
        jwkHandler.setOpenIdMatadataLocation("http://${server.hostName}:${server.port}/")
        jwkHandler.setOpenIdMatadataLocationBotFwkEmulator("http://${server.hostName}:${server.port}/")

        jwkHandler.launchJWKCollector("connectorId", 3000)

        Thread.sleep(500)
    }

    /**
     * Here we test the cache key system
     * The launch collector is initialized with a duration of 3s.
     * The fifth request, with a timeout of 3s,
     */
    @Test
    fun testJWKCacheSystem() {

        // check that it does not fail
        val firstRecordedRequest = server.takeRequest()
        assertEquals("GET /.well-known/openidconfiguration/ HTTP/1.1", firstRecordedRequest.requestLine)
        val secondRecordedResponse = server.takeRequest()
        assertEquals("GET / HTTP/1.1", secondRecordedResponse.requestLine)
        val thirdRecordedRequest = server.takeRequest(1, TimeUnit.SECONDS)
        assertEquals("GET /.well-known/openid-configuration/ HTTP/1.1", thirdRecordedRequest?.requestLine)
        val fourthRequest = server.takeRequest()
        assertEquals("GET / HTTP/1.1", fourthRequest.requestLine)
        val fifthRequest = server.takeRequest(3, TimeUnit.SECONDS)
        assertEquals("GET /.well-known/openidconfiguration/ HTTP/1.1", fifthRequest?.requestLine)
    }
}

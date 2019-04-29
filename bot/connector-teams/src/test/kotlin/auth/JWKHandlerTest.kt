package fr.vsct.tock.bot.connector.teams.auth

import fr.vsct.tock.bot.connector.teams.auth.MockServer.getMicrosoftMockServer
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JWKHandlerTest {

    private var authenticateBotConnectorService: AuthenticateBotConnectorService = AuthenticateBotConnectorService("fakeAppId")
    private lateinit var server: MockWebServer

    @BeforeAll
    fun launchJWKCollector() {
        authenticateBotConnectorService = AuthenticateBotConnectorService("fakeAppId")

        server = getMicrosoftMockServer()

        JWKHandler.setJKSBaseLocation("http://${server.hostName}:${server.port}/")
        JWKHandler.setOpenIdMatadataLocation("http://${server.hostName}:${server.port}/")
        JWKHandler.setOpenIdMatadataLocationBotFwkEmulator("http://${server.hostName}:${server.port}/")

        JWKHandler.launchJWKCollector(2000)

        Thread.sleep(500)
    }

    /**
     * Here we test the cache key system
     *
     */
    @Test
    fun testJWKCacheSystem() {

        //check that it does not fail
        val firstRecordedRequest = server.takeRequest()
        assertEquals("GET /.well-known/openidconfiguration/ HTTP/1.1", firstRecordedRequest.requestLine)
        val secondRecordedResponse = server.takeRequest()
        assertEquals("GET / HTTP/1.1", secondRecordedResponse.requestLine)
        val thirdRecordedRequest = server.takeRequest(1, TimeUnit.SECONDS)
        assertEquals("GET /.well-known/openid-configuration/ HTTP/1.1", thirdRecordedRequest.requestLine)
        val fourthRequest = server.takeRequest()
        assertEquals("GET / HTTP/1.1", fourthRequest.requestLine)
        val fifthRequest = server.takeRequest(1, TimeUnit.SECONDS)
        assertNull(fifthRequest)

        Thread.sleep(2000)

        val sixthRecordedRequest = server.takeRequest()
        assertEquals("GET /.well-known/openidconfiguration/ HTTP/1.1", sixthRecordedRequest.requestLine)
        val seventhRecordedResponse = server.takeRequest()
        assertEquals("GET / HTTP/1.1", seventhRecordedResponse.requestLine)
    }
}

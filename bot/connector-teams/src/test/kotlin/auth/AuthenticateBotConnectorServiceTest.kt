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
import ai.tock.bot.connector.teams.auth.MockServer.jwk
import com.microsoft.bot.schema.Activity
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import io.vertx.core.MultiMap
import net.jcip.annotations.NotThreadSafe
import net.minidev.json.JSONObject
import net.minidev.json.parser.JSONParser
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import java.lang.Thread.sleep
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.test.assertFailsWith

@NotThreadSafe
@TestInstance(Lifecycle.PER_CLASS)
class AuthenticateBotConnectorServiceTest {
    private val activity =
        Activity.createMessageActivity().apply {
            channelId = "msteams"
            serviceUrl = "https://serviceurl"
        }
    private var authenticateBotConnectorService: AuthenticateBotConnectorService =
        AuthenticateBotConnectorService("fakeAppId")

    private val notBefore = Instant.now().minus(10, ChronoUnit.SECONDS)
    private val expirationDate = Instant.now().plus(60, ChronoUnit.SECONDS)
    private val validJsonPayload: JSONObject =
        JSONParser(JSONParser.MODE_JSON_SIMPLE).parse(
            "{\"iss\":\"https://api.botframework.com\"," +
                "\"iat\":${notBefore.epochSecond}," +
                "\"nbf\":${notBefore.epochSecond}" +
                ",\"exp\":${expirationDate.epochSecond}," +
                "\"aud\":\"fakeAppId\"," +
                "\"sub\":\"test\"," +
                "\"serviceurl\": \"https://serviceurl\"}",
        ) as JSONObject

    private val validJsonPayloadFromBotFwkEmulator: JSONObject =
        JSONParser(JSONParser.MODE_JSON_SIMPLE).parse(
            "{\"iss\":\"https://login.microsoftonline.com/d6d49420-f39b-4df7-a1dc-d59a935871db/v2.0\"," +
                "\"iat\":${notBefore.epochSecond}," +
                "\"nbf\":${notBefore.epochSecond}" +
                ",\"exp\":${expirationDate.epochSecond}," +
                "\"aud\":\"fakeAppId\"," +
                "\"azp\":\"fakeAppId\"," +
                "\"sub\":\"test\"," +
                "\"serviceurl\": \"https://serviceurl\"}",
        ) as JSONObject

    private lateinit var server: MockWebServer

    private val jwkHandler = JWKHandler()

    @BeforeAll
    fun launchJWKCollector() {
        authenticateBotConnectorService = AuthenticateBotConnectorService("fakeAppId")

        server = getMicrosoftMockServer()

        jwkHandler.setJKSBaseLocation("http://${server.hostName}:${server.port}/")
        jwkHandler.setOpenIdMatadataLocation("http://${server.hostName}:${server.port}/")
        jwkHandler.setOpenIdMatadataLocationBotFwkEmulator("http://${server.hostName}:${server.port}/")
        jwkHandler.launchJWKCollector("connectorId", 4000)

        sleep(1000)
    }

    @Test
    fun tokenMustBeValid() {
        val jwsObject =
            JWSObject(
                JWSHeader.Builder(JWSAlgorithm.RS256).keyID(jwk.keyID).build(),
                Payload(validJsonPayload),
            )
        jwsObject.sign(RSASSASigner(jwk))
        val token = jwsObject.serialize()
        val bearerAuthorization = "Bearer $token"

        val headers: MultiMap = MultiMap.caseInsensitiveMultiMap().add("Authorization", bearerAuthorization)

        // check that it does not fail
        authenticateBotConnectorService.checkRequestValidity(jwkHandler, headers, activity)
    }

    @Test
    fun unauthorizedDueToMissingAuthorizationHeader() {
        val headers: MultiMap = MultiMap.caseInsensitiveMultiMap()

        assertFailsWith(ForbiddenException::class) {
            authenticateBotConnectorService.checkRequestValidity(jwkHandler, headers, activity)
        }
    }

    @Test
    fun unauthorizedDueToMissingBearerAuthorizationHeader() {
        val bearerAuthorization = "thisisawrongtoken"
        val headers: MultiMap = MultiMap.caseInsensitiveMultiMap().add("Authorization", bearerAuthorization)

        assertFailsWith(ForbiddenException::class) {
            authenticateBotConnectorService.checkRequestValidity(jwkHandler, headers, activity)
        }
    }

    @Test
    fun unauthorizedDueToMissingIssuerClaim() {
        val jsonPayloadWithoutIssuer: JSONObject =
            JSONParser(JSONParser.MODE_JSON_SIMPLE).parse(
                "{\"iat\":${notBefore.epochSecond}," +
                    "\"nbf\":${notBefore.epochSecond}" +
                    ",\"exp\":${expirationDate.epochSecond}," +
                    "\"aud\":\"fakeAppId\"," +
                    "\"sub\":\"test\"," +
                    "\"serviceurl\": \"https://serviceurl\"}",
            ) as JSONObject
        val jwsObject =
            JWSObject(
                JWSHeader.Builder(JWSAlgorithm.RS256).keyID(jwk.keyID).build(),
                Payload(jsonPayloadWithoutIssuer),
            )
        jwsObject.sign(RSASSASigner(jwk))
        val token = jwsObject.serialize()
        val bearerAuthorization = "Bearer $token"

        val headers: MultiMap = MultiMap.caseInsensitiveMultiMap().add("Authorization", bearerAuthorization)

        assertFailsWith(ForbiddenException::class) {
            authenticateBotConnectorService.checkRequestValidity(jwkHandler, headers, activity)
        }
    }

    @Test
    fun unauthorizedDueToWrongAudienceClaim() {
        val jsonPayloadWithWrongAppId: JSONObject =
            JSONParser(JSONParser.MODE_JSON_SIMPLE).parse(
                "{\"iss\":\"https://api.botframework.com\"," +
                    "\"iat\":${notBefore.epochSecond}," +
                    "\"nbf\":${notBefore.epochSecond}" +
                    ",\"exp\":${expirationDate.epochSecond}," +
                    "\"aud\":\"wrongAppId\"," +
                    "\"sub\":\"test\"," +
                    "\"serviceurl\": \"https://serviceurl\"}",
            ) as JSONObject
        val jwsObject =
            JWSObject(
                JWSHeader.Builder(JWSAlgorithm.RS256).keyID(jwk.keyID).build(),
                Payload(jsonPayloadWithWrongAppId),
            )
        jwsObject.sign(RSASSASigner(jwk))
        val token = jwsObject.serialize()
        val bearerAuthorization = "Bearer $token"

        val headers: MultiMap = MultiMap.caseInsensitiveMultiMap().add("Authorization", bearerAuthorization)

        assertFailsWith(ForbiddenException::class) {
            authenticateBotConnectorService.checkRequestValidity(jwkHandler, headers, activity)
        }
    }

    @Test
    fun unauthorizedDueToExpiredToken() {
        val jsonPayloadWithWrongAppId: JSONObject =
            JSONParser(JSONParser.MODE_JSON_SIMPLE).parse(
                "{\"iss\":\"https://api.botframework.com\"," +
                    "\"iat\":${notBefore.epochSecond}," +
                    "\"nbf\":${notBefore.epochSecond}" +
                    ",\"exp\":${notBefore.epochSecond}," +
                    "\"aud\":\"fakeAppId\"," +
                    "\"sub\":\"test\"," +
                    "\"serviceurl\": \"https://serviceurl\"}",
            ) as JSONObject
        val jwsObject =
            JWSObject(
                JWSHeader.Builder(JWSAlgorithm.RS256).keyID(jwk.keyID).build(),
                Payload(jsonPayloadWithWrongAppId),
            )
        jwsObject.sign(RSASSASigner(jwk))
        val token = jwsObject.serialize()
        val bearerAuthorization = "Bearer $token"

        val headers: MultiMap = MultiMap.caseInsensitiveMultiMap().add("Authorization", bearerAuthorization)

        assertFailsWith(ForbiddenException::class) {
            authenticateBotConnectorService.checkRequestValidity(jwkHandler, headers, activity)
        }
    }

    @Test
    fun unauthorizedDueToNotValidYetToken() {
        val jsonPayloadWithWrongAppId: JSONObject =
            JSONParser(JSONParser.MODE_JSON_SIMPLE).parse(
                "{\"iss\":\"https://api.botframework.com\"," +
                    "\"iat\":${notBefore.epochSecond}," +
                    "\"nbf\":${expirationDate.epochSecond}" +
                    ",\"exp\":${expirationDate.epochSecond}," +
                    "\"aud\":\"fakeAppId\"," +
                    "\"sub\":\"test\"," +
                    "\"serviceurl\": \"https://serviceurl\"}",
            ) as JSONObject
        val jwsObject =
            JWSObject(
                JWSHeader.Builder(JWSAlgorithm.RS256).keyID(jwk.keyID).build(),
                Payload(jsonPayloadWithWrongAppId),
            )
        jwsObject.sign(RSASSASigner(jwk))
        val token = jwsObject.serialize()
        val bearerAuthorization = "Bearer $token"

        val headers: MultiMap = MultiMap.caseInsensitiveMultiMap().add("Authorization", bearerAuthorization)

        assertFailsWith(ForbiddenException::class) {
            authenticateBotConnectorService.checkRequestValidity(jwkHandler, headers, activity)
        }
    }

    @Test
    fun unauthorizedDueToNotValidKey() {
        val anotherJwk =
            RSAKeyGenerator(2048)
                .keyUse(KeyUse.SIGNATURE)
                .keyID(UUID.randomUUID().toString())
                .generate()
        val jwsObject =
            JWSObject(
                JWSHeader.Builder(JWSAlgorithm.RS256).keyID(jwk.keyID).build(),
                Payload(validJsonPayload),
            )
        jwsObject.sign(RSASSASigner(anotherJwk))
        val token = jwsObject.serialize()
        val bearerAuthorization = "Bearer $token"

        val headers: MultiMap = MultiMap.caseInsensitiveMultiMap().add("Authorization", bearerAuthorization)

        assertFailsWith(ForbiddenException::class) {
            authenticateBotConnectorService.checkRequestValidity(jwkHandler, headers, activity)
        }
    }

    @Test
    fun unauthorizedDueToDifferentServiceUrl() {
        val otherActivity =
            Activity.createMessageActivity().apply {
                channelId = "msteams"
                serviceUrl = "https://wrongServiceurl"
            }
        val jwsObject =
            JWSObject(
                JWSHeader.Builder(JWSAlgorithm.RS256).keyID(jwk.keyID).build(),
                Payload(validJsonPayload),
            )
        jwsObject.sign(RSASSASigner(jwk))
        val token = jwsObject.serialize()
        val bearerAuthorization = "Bearer $token"

        val headers: MultiMap = MultiMap.caseInsensitiveMultiMap().add("Authorization", bearerAuthorization)

        assertFailsWith(ForbiddenException::class) {
            authenticateBotConnectorService.checkRequestValidity(jwkHandler, headers, otherActivity)
        }
    }

    @Test
    fun testRequestFromTheBotConnectorService() {
        val jwsObject =
            JWSObject(
                JWSHeader.Builder(JWSAlgorithm.RS256).keyID(jwk.keyID).build(),
                Payload(validJsonPayloadFromBotFwkEmulator),
            )
        jwsObject.sign(RSASSASigner(jwk))
        val token = jwsObject.serialize()
        val bearerAuthorization = "Bearer $token"

        val headers: MultiMap = MultiMap.caseInsensitiveMultiMap().add("Authorization", bearerAuthorization)

        // check that it does not fail
        authenticateBotConnectorService.checkRequestValidity(jwkHandler, headers, activity)
    }

    @AfterAll
    fun shutUp() {
        server.shutdown()
    }
}

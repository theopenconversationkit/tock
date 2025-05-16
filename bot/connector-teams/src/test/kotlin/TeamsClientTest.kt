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

package ai.tock.bot.connector.teams

import ai.tock.bot.connector.teams.messages.TeamsBotTextMessage
import ai.tock.bot.connector.teams.token.TokenHandler
import ai.tock.shared.addJacksonConverter
import ai.tock.shared.create
import ai.tock.shared.longProperty
import ai.tock.shared.retrofitBuilderWithTimeoutAndLogger
import com.microsoft.bot.schema.Activity
import mu.KotlinLogging
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TeamsClientTest {

    private val tokenHandler = TokenHandler("fakeId", "fakePassword")
    private val logger = KotlinLogging.logger {}

    @BeforeEach
    internal fun resetToken() {
        tokenHandler.token = null
    }

    @Test
    fun getTokenWorksFineWithGoodResponse() {
        val server = MockWebServer()

        val apiResponse = MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(
                "{\"token_type\":\"Bearer\",\"expires_in\":3600,\"ext_expires_in\":3600,\"access_token\":\"eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6Im5iQ3dXMTF3M1hrQi14VWFYd0tSU0xqTUhHUSIsImtpZCI6Im5iQ3dXMTF3M1hrQi14VWFYd0tSU0xqTUhHUSJ9.eyJhdWQiOiJodHRwczovL2FwaS5ib3RmcmFtZXdvcmsuY29tIiwiaXNzIjoiaHR0cHM6Ly9zdHMud2luZG93cy5uZXQvZDZkNDk0MjAtZjM5Yi00ZGY3LWExZGMtZDU5YTkzNTg3MWRiLyIsImlhdCI6MTU0NzY0ODM0MywibmJmIjoxNTQ3NjQ4MzQzLCJleHAiOjE1NDc2NTIyNDMsImFpbyI6IjQySmdZS2laKzB2bTg0NmMvTlY1QjNPMExFT1hBZ0E9IiwiYXBwaWQiOiI5MjQyNzQxMC03ZGRjLTQxMTItYjJiOC1lNWE0ZjliN2ZkN2MiLCJhcHBpZGFjciI6IjEiLCJpZHAiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC9kNmQ0OTQyMC1mMzliLTRkZjctYTFkYy1kNTlhOTM1ODcxZGIvIiwidGlkIjoiZDZkNDk0MjAtZjM5Yi00ZGY3LWExZGMtZDU5YTkzNTg3MWRiIiwidXRpIjoiX3BZcDF3MnJzVUcyV1pSdjRRUXBBQSIsInZlciI6IjEuMCJ9.fBXBiXgv_cmsq-0EpZ-zsaGrmtCCkR20kRUNENqWSechZFpyCBaSOSeB9xdvhi2SId8HLXN1tQtwA-u4yPwlLBseXpbv1vCCh1Z6ASB9BmjqYJUk3RNZuGKbf5VDJMYlweeC_FNIV6OiIId9y0gHfuDBRcaYaJwGKVUeGfyYXBMT3ReY3_dYp5POc4eIm6wlLxscCkUKtNiPCxVviQywE3sMYdaH-3Y8rUuSTKB3cqYgIxyvas4Ld42rsWsHv1TFRG8dRO-nPCjBMYwZ_cTIcB1M0F1bipTS39-ij22IV5Cvvs_bzvzLNQPv6mO2MptLL8m-QHUqx_hAyhzeBeJtYw\"}"
            )
            .setResponseCode(200)

        server.enqueue(apiResponse)

        tokenHandler.loginApi = retrofitBuilderWithTimeoutAndLogger(
            longProperty("tock_whatsapp_request_timeout_ms", 30000),
            logger
        ).baseUrl("http://${server.hostName}:${server.port}/").addJacksonConverter(tokenHandler.teamsMapper).build().create()

        tokenHandler.launchTokenCollector("connectorID")
        // Sleep to let the time to fetchToken() async method to execute
        Thread.sleep(1000)
        assertEquals(
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6Im5iQ3dXMTF3M1hrQi14VWFYd0tSU0xqTUhHUSIsImtpZCI6Im5iQ3dXMTF3M1hrQi14VWFYd0tSU0xqTUhHUSJ9.eyJhdWQiOiJodHRwczovL2FwaS5ib3RmcmFtZXdvcmsuY29tIiwiaXNzIjoiaHR0cHM6Ly9zdHMud2luZG93cy5uZXQvZDZkNDk0MjAtZjM5Yi00ZGY3LWExZGMtZDU5YTkzNTg3MWRiLyIsImlhdCI6MTU0NzY0ODM0MywibmJmIjoxNTQ3NjQ4MzQzLCJleHAiOjE1NDc2NTIyNDMsImFpbyI6IjQySmdZS2laKzB2bTg0NmMvTlY1QjNPMExFT1hBZ0E9IiwiYXBwaWQiOiI5MjQyNzQxMC03ZGRjLTQxMTItYjJiOC1lNWE0ZjliN2ZkN2MiLCJhcHBpZGFjciI6IjEiLCJpZHAiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC9kNmQ0OTQyMC1mMzliLTRkZjctYTFkYy1kNTlhOTM1ODcxZGIvIiwidGlkIjoiZDZkNDk0MjAtZjM5Yi00ZGY3LWExZGMtZDU5YTkzNTg3MWRiIiwidXRpIjoiX3BZcDF3MnJzVUcyV1pSdjRRUXBBQSIsInZlciI6IjEuMCJ9.fBXBiXgv_cmsq-0EpZ-zsaGrmtCCkR20kRUNENqWSechZFpyCBaSOSeB9xdvhi2SId8HLXN1tQtwA-u4yPwlLBseXpbv1vCCh1Z6ASB9BmjqYJUk3RNZuGKbf5VDJMYlweeC_FNIV6OiIId9y0gHfuDBRcaYaJwGKVUeGfyYXBMT3ReY3_dYp5POc4eIm6wlLxscCkUKtNiPCxVviQywE3sMYdaH-3Y8rUuSTKB3cqYgIxyvas4Ld42rsWsHv1TFRG8dRO-nPCjBMYwZ_cTIcB1M0F1bipTS39-ij22IV5Cvvs_bzvzLNQPv6mO2MptLL8m-QHUqx_hAyhzeBeJtYw",
            tokenHandler.token
        )
        server.shutdown()
    }

    @Test
    fun getTokenThrowIOExceptionWithBadResponse() {
        val server = MockWebServer()

        val apiResponse = MockResponse()
            .addHeader("Content-Type", "application/json")
            .setResponseCode(403)

        server.enqueue(apiResponse)

        tokenHandler.loginApi = retrofitBuilderWithTimeoutAndLogger(
            longProperty("tock_whatsapp_request_timeout_ms", 30000),
            logger
        )
            .baseUrl("http://${server.hostName}:${server.port}/")
            .addJacksonConverter(tokenHandler.teamsMapper)
            .build()
            .create()

        assertFailsWith(IllegalStateException::class) {
            tokenHandler.checkToken()
        }
        server.shutdown()
    }

    @Test
    fun testTokenValidity() {
        val server = MockWebServer()
        val client = TeamsClient(tokenHandler)

        val apiResponse = MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(
                "{\"token_type\":\"Bearer\",\"expires_in\":11,\"ext_expires_in\":3600,\"access_token\":\"eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6Im5iQ3dXMTF3M1hrQi14VWFYd0tSU0xqTUhHUSIsImtpZCI6Im5iQ3dXMTF3M1hrQi14VWFYd0tSU0xqTUhHUSJ9.eyJhdWQiOiJodHRwczovL2FwaS5ib3RmcmFtZXdvcmsuY29tIiwiaXNzIjoiaHR0cHM6Ly9zdHMud2luZG93cy5uZXQvZDZkNDk0MjAtZjM5Yi00ZGY3LWExZGMtZDU5YTkzNTg3MWRiLyIsImlhdCI6MTU0NzY0ODM0MywibmJmIjoxNTQ3NjQ4MzQzLCJleHAiOjE1NDc2NTIyNDMsImFpbyI6IjQySmdZS2laKzB2bTg0NmMvTlY1QjNPMExFT1hBZ0E9IiwiYXBwaWQiOiI5MjQyNzQxMC03ZGRjLTQxMTItYjJiOC1lNWE0ZjliN2ZkN2MiLCJhcHBpZGFjciI6IjEiLCJpZHAiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC9kNmQ0OTQyMC1mMzliLTRkZjctYTFkYy1kNTlhOTM1ODcxZGIvIiwidGlkIjoiZDZkNDk0MjAtZjM5Yi00ZGY3LWExZGMtZDU5YTkzNTg3MWRiIiwidXRpIjoiX3BZcDF3MnJzVUcyV1pSdjRRUXBBQSIsInZlciI6IjEuMCJ9.fBXBiXgv_cmsq-0EpZ-zsaGrmtCCkR20kRUNENqWSechZFpyCBaSOSeB9xdvhi2SId8HLXN1tQtwA-u4yPwlLBseXpbv1vCCh1Z6ASB9BmjqYJUk3RNZuGKbf5VDJMYlweeC_FNIV6OiIId9y0gHfuDBRcaYaJwGKVUeGfyYXBMT3ReY3_dYp5POc4eIm6wlLxscCkUKtNiPCxVviQywE3sMYdaH-3Y8rUuSTKB3cqYgIxyvas4Ld42rsWsHv1TFRG8dRO-nPCjBMYwZ_cTIcB1M0F1bipTS39-ij22IV5Cvvs_bzvzLNQPv6mO2MptLL8m-QHUqx_hAyhzeBeJtYw\"}"
            )
            .setResponseCode(200)

        val connectorResponse = MockResponse()
            .setBody("{\"id\":\"toto\"}")
            .setResponseCode(200)

        server.enqueue(apiResponse)
        server.enqueue(connectorResponse)

        tokenHandler.loginApi = retrofitBuilderWithTimeoutAndLogger(
            longProperty("tock_whatsapp_request_timeout_ms", 30000),
            logger
        )
            .baseUrl("http://${server.hostName}:${server.port}/")
            .addJacksonConverter(tokenHandler.teamsMapper)
            .build()
            .create()

        val activity = tokenHandler.teamsMapper.readValue(
            "{\"text\":\"plop\",\"textFormat\":\"plain\",\"type\":\"message\",\"timestamp\":\"2019-01-17T09:40:33.755Z\",\"localTimestamp\":\"2019-01-17T10:40:33.755+01:00\",\"id\":\"1547718033691\",\"channelId\":\"msteams\",\"serviceUrl\":\"http://${server.hostName}:${server.port}/\",\"from\":{\"id\":\"29:1y1-JPsfBcQcMo6FnyhuRPyB5mb073MBxGdulNe6GUKE576AkjDw-9Bzgnb4l_kSxEVL1SVf-ShMGMtB8_o6DRg\",\"name\":\"Barre Sébastien\",\"aadObjectId\":\"317e208d-d6b7-451a-ae40-860d2bf09d80\"},\"conversation\":{\"conversationType\":\"personal\",\"id\":\"a:1HWuSX9zTFGpthhsdqTB3qeqxoGjEGnHRMB0O0X6Dop9Nl72GVDcdfTOl7yI5KsnemLBjJFezDgThHsbPHwB14tABOxPV3_m9l_v_JdQMwVpQxkyxibKXa6d9eRHYQ7nD\"},\"recipient\":{\"id\":\"28:92427410-7ddc-4112-b2b8-e5a4f9b7fd7c\",\"name\":\"EVE-DEV\"},\"entities\":[{\"locale\":\"fr-FR\",\"country\":\"FR\",\"platform\":\"Windows\",\"type\":\"clientInfo\"}],\"channelData\":{\"tenant\":{\"id\":\"85eca096-674d-4fd9-9a9e-ae1178e2ee56\"}}}",
            Activity::class.java
        )

        client.sendMessage(activity, TeamsBotTextMessage("plop"))

        assertFalse(tokenHandler.isTokenExpired())
        Thread.sleep(1000)
        assertTrue(tokenHandler.isTokenExpired())
        server.shutdown()
    }
}

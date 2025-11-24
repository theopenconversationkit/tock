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

import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import net.minidev.json.JSONObject
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import java.util.UUID

object MockServer {
    val jwk: RSAKey =
        RSAKeyGenerator(2048)
            .keyUse(KeyUse.SIGNATURE)
            .keyID(UUID.randomUUID().toString())
            .generate()

    private val jwks = "{\"keys\":[${
        JSONObject(
            jwk.toPublicJWK().toJSONObject().apply { put("endorsements", arrayListOf("msteams")) },
        ).toJSONString()
    }]}"

    fun getMicrosoftMockServer(): MockWebServer {
        val server = MockWebServer()

        val dispatcher =
            object : Dispatcher() {
                @Throws(InterruptedException::class)
                override fun dispatch(request: RecordedRequest): MockResponse {
                    return when {
                        request.path == "/.well-known/openidconfiguration/" ->
                            MockResponse()
                                .addHeader("Content-Type", "application/json")
                                .setBody(
                                    "{\"issuer\":\"https://api.botframework.com\"," +
                                        "\"authorization_endpoint\":\"https://invalid.botframework.com\"," +
                                        "\"jwks_uri\":\"http://${server.hostName}:${server.port}/\"," +
                                        "\"id_token_signing_alg_values_supported\":[\"RS256\"]," +
                                        "\"token_endpoint_auth_methods_supported\":[\"private_key_jwt\"]}",
                                )
                                .setResponseCode(200)
                        request.path == "/.well-known/openid-configuration/" ->
                            MockResponse()
                                .addHeader("Content-Type", "application/json")
                                .setBody(
                                    "{\"issuer\":\"https://api.botframework.com\"," +
                                        "\"authorization_endpoint\":\"https://invalid.botframework.com\"," +
                                        "\"jwks_uri\":\"http://${server.hostName}:${server.port}/\"," +
                                        "\"id_token_signing_alg_values_supported\":[\"RS256\"]," +
                                        "\"token_endpoint_auth_methods_supported\":[\"private_key_jwt\"]}",
                                )
                                .setResponseCode(200)
                        request.path == "/" ->
                            MockResponse()
                                .addHeader("Content-Type", "application/json")
                                .setBody(jwks)
                                .setResponseCode(200)
                        else -> MockResponse().setResponseCode(404)
                    }
                }
            }
        server.dispatcher = dispatcher

        return server
    }
}

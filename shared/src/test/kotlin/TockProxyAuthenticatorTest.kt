/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.shared

import io.mockk.mockk
import okhttp3.Credentials
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.commonEmptyResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.net.HttpURLConnection
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TockProxyAuthenticatorTest {
    @AfterEach
    fun cleanup() {
        System.clearProperty("tock_proxy_user")
        System.clearProperty("tock_proxy_password")
    }

    @Test
    fun `WHEN proxy properties are set THEN installForProxy adds proxy authenticator`() {
        System.setProperty("tock_proxy_user", "Bob")
        System.setProperty("tock_proxy_password", "hunter2")

        val builder = OkHttpClient.Builder()
        TockProxyAuthenticator.install(builder)

        assertTrue { builder.build().proxyAuthenticator is TockProxyAuthenticator }
    }

    @ParameterizedTest
    @ValueSource(strings = ["Basic", "OkHttp-Preemptive"])
    fun `WHEN Okhttp performs preemptive or basic auth THEN return request with credentials`(challenge: String) {
        val authenticator = TockProxyAuthenticator("Alice", "hunter2")
        val fakeAuthChallengeResponse = Response.Builder()
            .request(Request.Builder().url("http://localhost".toHttpUrl()).build())
            .protocol(Protocol.HTTP_1_1)
            .code(HttpURLConnection.HTTP_PROXY_AUTH)
            .message("Preemptive Authenticate")
            .body(commonEmptyResponse)
            .sentRequestAtMillis(-1L)
            .receivedResponseAtMillis(-1L)
            .header("Proxy-Authenticate", challenge)
            .build()

        val res = authenticator.authenticate(null, fakeAuthChallengeResponse)
        assertNotNull(res)
        assertEquals(Credentials.basic("Alice", "hunter2"), res.header("Proxy-Authorization"))
    }

    @Test
    fun `WHEN proxy challenge is unrecognized THEN return null`() {
        val authenticator = TockProxyAuthenticator("admin", "admin")
        val fakeAuthChallengeResponse: Response = mockk(relaxed = true)

        val res = authenticator.authenticate(null, fakeAuthChallengeResponse)
        assertNull(res)
    }
}

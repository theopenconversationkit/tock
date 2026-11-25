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

package ai.tock.shared.security

import ai.tock.shared.http.HttpMethod
import ai.tock.shared.http.HttpRequest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HttpRequestFilterTest {
    private fun request(
        remoteAddress: String? = "10.0.0.1",
        headers: Map<String, String> = emptyMap(),
    ) = HttpRequest(HttpMethod.POST, "/", headers = headers.mapValues { listOf(it.value) }, remoteAddress = remoteAddress)

    @Test
    fun `GIVEN no restriction THEN all the requests are accepted`() {
        assertTrue(createHttpRequestFilter(null, null).accept(request()))
        assertTrue(createHttpRequestFilter(setOf(" ", ""), " ").accept(request()))
    }

    @Test
    fun `GIVEN allowed IPs THEN the direct or the last forwarded IP must be allowed`() {
        val filter = createHttpRequestFilter(setOf(" 10.0.0.2 "), null)

        assertFalse(filter.accept(request()))
        assertTrue(filter.accept(request(remoteAddress = "10.0.0.2")))
        assertTrue(filter.accept(request(headers = mapOf("x-forwarded-for" to "1.1.1.1, 10.0.0.2"))))
        assertFalse(filter.accept(request(headers = mapOf("X-Forwarded-For" to "10.0.0.2, 1.1.1.1"))))
    }

    @Test
    fun `GIVEN a token THEN the X-Auth-Token header must match, in addition to the IPs`() {
        val filter = createHttpRequestFilter(setOf("10.0.0.1"), " secret ")

        assertFalse(filter.accept(request()))
        assertTrue(filter.accept(request(headers = mapOf("X-Auth-Token" to "secret"))))
        assertFalse(filter.accept(request(remoteAddress = "10.0.0.3", headers = mapOf("X-Auth-Token" to "secret"))))
    }
}

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

package ai.tock.shared.security.auth

import ai.tock.shared.VertxMock
import ai.tock.shared.vertx.WebVerticle
import io.mockk.mockk
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.User
import io.vertx.ext.auth.authentication.Credentials
import io.vertx.ext.web.handler.AuthenticationHandler
import kotlin.test.Test
import kotlin.test.assertTrue

class SSOTockAuthProviderTest {

    @Test
    fun `excludedPaths match static files`() {
        val sso = object : SSOTockAuthProvider(VertxMock()) {

            fun test(verticle: WebVerticle): Set<Regex> {
                return super.excludedPaths(verticle)
            }

            override fun createAuthHandler(verticle: WebVerticle): AuthenticationHandler {
                return mockk()
            }

            override fun authenticate(credentials: Credentials?): Future<User?> = mockk()
        }
        val excluded = sso.test(mockk(relaxed = true))
        assertTrue { excluded.any { it.matches("5.dc9d94109f8f028b46a1.js") } }
        assertTrue { excluded.any { it.matches("Roboto-Medium.2741a14.woff2") } }
        assertTrue { excluded.any { it.matches("Roboto-Medium.2741a14.woff") } }
        assertTrue { excluded.any { it.matches("Roboto-Medium.2741a14.ttf") } }
    }
}

/*
 * Copyright (C) 2017/2019 VSCT
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

package fr.vsct.tock.shared.security.auth

import fr.vsct.tock.shared.mockedVertx
import fr.vsct.tock.shared.vertx.WebVerticle
import io.mockk.mockk
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.User
import io.vertx.ext.web.handler.AuthHandler
import kotlin.test.Test
import kotlin.test.assertTrue

class SSOTockAuthProviderTest {

    @Test
    fun `excludedPaths match static files`() {
        val sso = object : SSOTockAuthProvider(mockedVertx) {

            fun test(verticle: WebVerticle): Set<Regex> {
                return super.excludedPaths(verticle)
            }

            override fun createAuthHandler(verticle: WebVerticle): AuthHandler {
                return mockk()
            }

            override fun authenticate(p0: JsonObject, p1: Handler<AsyncResult<User>>) {
            }
        }
        val excluded = sso.test(mockk(relaxed = true))
        assertTrue { excluded.any { it.matches("5.dc9d94109f8f028b46a1.js") } }
    }
}
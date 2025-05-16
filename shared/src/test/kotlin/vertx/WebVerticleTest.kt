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

package ai.tock.shared.vertx

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import kotlin.test.Test

class WebVerticleTest {

    // check that WebVerticleImpl does not need to implement healthcheck methods to compile
    class WebVerticleImpl : WebVerticle() {
        override fun configure() {}
    }

    @Test
    fun `GIVEN WebVerticle simple implementation THEN healthcheck returns HTTP 200`() {
        val verticle = WebVerticleImpl()
        val response: HttpServerResponse = mockk {
            every { end() } returns mockk()
        }
        val context: RoutingContext = mockk {
            every { response() } returns response
        }

        verticle.healthcheck().invoke(context)

        verify { response.end() }
    }

    @Test
    fun `GIVEN WebVerticle simple implementation THEN livenesscheck returns HTTP 200`() {
        val verticle = WebVerticleImpl()
        val response: HttpServerResponse = mockk {
            every { end() } returns mockk()
        }
        val context: RoutingContext = mockk {
            every { response() } returns response
        }

        verticle.livenesscheck().invoke(context)

        verify { response.end() }
    }

    @Test
    fun `GIVEN WebVerticle simple implementation THEN readinesscheck returns HTTP 200`() {
        val verticle = WebVerticleImpl()
        val response: HttpServerResponse = mockk {
            every { end() } returns mockk()
        }
        val context: RoutingContext = mockk {
            every { response() } returns response
        }

        verticle.readinesscheck().invoke(context)

        verify { response.end() }
    }
}

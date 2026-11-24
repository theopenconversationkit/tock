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

package ai.tock.bot.connector.web

import ai.tock.shared.security.auth.spi.WebSecurityHandler
import ai.tock.shared.security.auth.spi.WebSecurityMode
import ai.tock.shared.security.web.HttpWebSecurityCookies
import ai.tock.shared.tockInternalInjector
import ai.tock.shared.vertx.WebSecurityCookiesHandler
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import io.vertx.ext.web.RoutingContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WebConnectorSecurityTest {
    private fun inject(handler: WebSecurityHandler) {
        tockInternalInjector =
            KodeinInjector().apply {
                inject(Kodein { bind<WebSecurityHandler>(tag = WebSecurityMode.COOKIES.name) with provider { handler } })
            }
    }

    @AfterEach
    fun reset() {
        tockInternalInjector = KodeinInjector()
    }

    @Test
    fun `GIVEN the Vert_x handler of Tock WHEN the security is chosen THEN the routes are the server independent ones`() {
        inject(WebSecurityCookiesHandler())

        val security = webConnectorSecurity(WebSecurityMode.COOKIES)

        assertTrue((security as WebConnectorSecurity.Http).security is HttpWebSecurityCookies)
    }

    @Test
    fun `GIVEN a Vert_x handler of the project WHEN the security is chosen THEN its Vert_x routes are kept`() {
        val custom =
            object : WebSecurityHandler {
                override fun handle(event: RoutingContext) {
                    event.next()
                }
            }
        inject(custom)

        assertSame(custom, (webConnectorSecurity(WebSecurityMode.COOKIES) as WebConnectorSecurity.Vertx).handler)
    }
}

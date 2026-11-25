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

package ai.tock.bot.engine

import ai.tock.shared.http.HttpResponse
import ai.tock.shared.http.HttpRoutes
import io.mockk.every
import io.mockk.invoke
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod.GET
import io.vertx.core.http.HttpMethod.POST
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.CoroutineRouterSupport
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 *
 */
class VertxBotServerTest {
    @Test
    fun `test registerConnectorServices install healthcheck`() {
        val botVerticle: BotVerticle = mockk()
        val serviceInstaller: BotVerticle.ServiceInstaller = mockk()
        val capturedInstaller = slot<CoroutineRouterSupport.(Router) -> Unit>()
        every { botVerticle.registerServices(any(), capture(capturedInstaller)) } returns serviceInstaller

        val installer: CoroutineRouterSupport.(Router) -> Unit = mockk()
        every { installer.invoke(any(), any()) } returns Unit

        VertxBotServer(botVerticle).registerConnectorServices("/path", installer)

        verify { botVerticle.registerServices("/path", any()) }

        val router: Router = mockk()
        val route: Route = mockk()
        every { router.get(any()) } returns route
        every { route.handler(any()) } returns route

        val routerSupport: CoroutineRouterSupport = mockk()
        capturedInstaller.invoke(routerSupport, router)

        verify { router.get("/path/healthcheck") }
        verify { installer.invoke(routerSupport, router) }
    }

    @Test
    fun `GIVEN server independent routes WHEN they are registered THEN the healthcheck and the routes are mounted on the router`() {
        val botVerticle: BotVerticle = mockk()
        val capturedInstaller = slot<CoroutineRouterSupport.(Router) -> Any?>()
        every { botVerticle.registerServices(any(), capture(capturedInstaller)) } returns mockk()
        val routes =
            HttpRoutes {
                post("/path") { HttpResponse.status() }
                get("/path/:id") { HttpResponse.status() }
            }

        VertxBotServer(botVerticle).registerConnectorRoutes("/path", routes)

        val vertx = Vertx.vertx()
        try {
            val router = Router.router(vertx)
            capturedInstaller.captured.invoke(mockk(), router)

            assertEquals(
                listOf("/path/healthcheck" to setOf(GET), "/path" to setOf(POST), "/path/:id" to setOf(GET)),
                router.routes.map { (it.path ?: it.toString()) to it.methods() },
            )
        } finally {
            vertx.close()
        }
    }
}

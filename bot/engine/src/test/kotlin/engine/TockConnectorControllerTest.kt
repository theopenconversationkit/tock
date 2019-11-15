/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.bot.engine

import io.mockk.every
import io.mockk.invoke
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import org.junit.jupiter.api.Test

/**
 *
 */
class TockConnectorControllerTest {

    @Test
    fun `test registerService install healthcheck`() {
        val botVerticle: BotVerticle = mockk()
        val serviceInstaller: BotVerticle.ServiceInstaller = mockk()
        val capturedInstaller = slot<(Router) -> Unit>()
        every { botVerticle.registerServices(any(), capture(capturedInstaller)) } returns serviceInstaller

        val installer: (Router) -> Unit = mockk()
        every { installer.invoke(any()) } returns Unit
        val controller = TockConnectorController(mockk(), mockk(), botVerticle, mockk(), mockk())

        controller.registerServices("/path", installer)

        verify { botVerticle.registerServices(any(), any()) }

        val router: Router = mockk()
        val route: Route = mockk()
        every { router.get(any()) } returns route
        every { route.handler(any()) } returns route

        capturedInstaller.invoke(router)

        verify { router.get("/path/healthcheck") }
        verify { installer.invoke(router) }
    }
}
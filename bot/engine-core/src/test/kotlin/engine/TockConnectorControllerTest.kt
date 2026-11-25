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

import io.mockk.every
import io.mockk.mockk
import io.vertx.ext.web.Router
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 *
 */
class TockConnectorControllerTest {
    @Test
    fun `GIVEN a connector WHEN it registers then unregisters its services THEN the controller delegates to the bot server`() {
        val server = TestBotServer()
        val bot: Bot = mockk()
        every { bot.configuration } returns mockk()
        val controller = TockConnectorController(bot, mockk(), server, mockk(), mockk())
        val installer: (Router) -> Unit = {}

        controller.registerServices("/path", installer)

        assertEquals(listOf("/path"), server.registrations.map { it.serviceIdentifier })

        controller.unregisterServices()

        assertEquals(emptyList(), server.registrations)
    }
}

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

import ai.tock.bot.connector.alcmeon.AlcmeonAuthorisationHandler
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.vertx.ext.web.RoutingContext
import org.junit.jupiter.api.Test

@Suppress("ktlint:standard:max-line-length")
internal class AlcmeonAuthorisationHandlerTest {
    @Test
    internal fun `authorization success`() {
        val routingContext =
            mockk<RoutingContext>(relaxed = true) {
                every { request().getHeader("X-Alcmeon-Webhook-Signature") } returns "4d737b25de54f0ec47bc9b5fee2b3baf7231733cabe7731830912bb9c0c548a9"
                every { normalizedPath() } returns "/alcmeon/start"
                every {
                    body().asString()
                } returns "{\"backend\": \"whatsapp\", \"parameters\": [], \"user_external_id\": \"33671485945\", \"user_name\": \"Germain\", \"event\": {\"type\": \"text\", \"text\": {\"body\": \"Bonjour\"}}, \"global_variables\": {}}"
            }

        AlcmeonAuthorisationHandler("1309768d3f6f8462").handle(routingContext)

        verify { routingContext.next() }
    }
}

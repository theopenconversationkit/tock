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

package ai.tock.bot.connector.messenger

import ai.tock.shared.bytecode.BytecodeDependencies
import ai.tock.shared.vertx.WebVerticle
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The connector declares server independent routes: it must run without Vert.x.
 */
class MessengerConnectorBytecodeTest {
    @Test
    fun `the connector references neither Vert_x nor the Vert_x classes of tock-shared`() {
        val sharedOnly = BytecodeDependencies.classesOf(BytecodeDependencies.locationOf(WebVerticle::class.java))

        val violations =
            BytecodeDependencies.violations(
                BytecodeDependencies.locationOf(MessengerConnector::class.java),
                forbidden = { to ->
                    // ConnectorProvider.createRequestFilter, inherited by the provider: called by Vert.x connectors only
                    !to.startsWith("ai/tock/shared/security/RequestFilter") &&
                        (to.startsWith("io/vertx/") || to.startsWith("io/netty/") || to in sharedOnly)
                },
            )

        assertEquals("", violations.joinToString("\n"))
    }
}

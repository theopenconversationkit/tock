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

import ai.tock.shared.bytecode.BytecodeDependencies
import ai.tock.shared.vertx.WebVerticle
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * tock-bot-engine-core runs without Vert.x, MongoDB and the classes of tock-shared that depend on them.
 * It still compiles against them, for the public signatures that expose Vert.x types: the classes below may
 * reference them, in methods that only the Vert.x server calls. Any other reference is a regression.
 */
class BotEngineCoreBytecodeTest {
    private val allowed =
        mapOf(
            "ai/tock/bot/connector/ConnectorProvider" to "createRequestFilter returns the Vert.x RequestFilter of tock-shared",
            "ai/tock/bot/engine/TockConnectorController" to "registerServices and coRegisterServices: the Router API of the connectors",
            "ai/tock/bot/engine/VertxRoutesFallback" to "the default registerRoutes of the controllers that only have a Vert.x router",
            "ai/tock/bot/engine/VertxHealthchecks" to "the healthcheck handlers, loaded by the Vert.x server only",
            "ai/tock/bot/engine/WebSocketController" to "setPushHandler, called by the Vert.x server only",
            "ai/tock/bot/engine/config/UploadedFilesService" to "configure and the file download handler: Vert.x routes",
        )

    @Test
    fun `tock-bot-engine-core references Vert_x, MongoDB and tock-shared only from the allowed classes`() {
        val sharedOnly = BytecodeDependencies.classesOf(BytecodeDependencies.locationOf(WebVerticle::class.java))

        val violations =
            BytecodeDependencies.violations(
                BytecodeDependencies.locationOf(BotRepository::class.java),
                forbidden = { it.startsWith("io/vertx/") || it.startsWith("io/netty/") || it.startsWith("com/mongodb/") || it in sharedOnly },
                allowed = { from -> allowed.keys.any { from == it || from.startsWith("$it$") } },
            )

        assertEquals("", violations.joinToString("\n"))
    }
}

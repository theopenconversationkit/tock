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

package ai.tock.bot.test.mock

import ai.tock.bot.definition.ConnectorDef
import ai.tock.bot.definition.HandlerDef
import ai.tock.bot.engine.BotBus
import io.mockk.verify
import org.junit.jupiter.api.Test

class MockTest {

    class MyHandlerDef(bus: BotBus) : HandlerDef<MyConnectorDef>(bus) {

        fun call() {
            c.callConnector()
        }
    }

    class MyConnectorDef(def: MyHandlerDef) : ConnectorDef<MyHandlerDef>(def) {

        fun callConnector() {
        }
    }

    @Test
    fun `test mockConnector function`() {
        val mockk = mockConnector<MyConnectorDef> { bus ->
            MyHandlerDef(bus).call()
        }
        verify {
            mockk.callConnector()
        }
    }
}

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

package ai.tock.bot.definition

import ai.tock.bot.engine.BotBus
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class HandlerStoryDefinitionCreatorTest {

    private val bus = mockk<SubBotBus>(relaxed = true)
    private val data = StoryData("a", null)

    @Test
    fun `GIVEN handlerDef with data WHEN creation with bus and data THEN definition is created with bus and data`() {
        val def: Def = defaultHandlerStoryDefinitionCreator<Def>().create(bus, data)

        assertEquals(bus, def.bus)
        assertEquals(data, def.data)
    }

    @Test
    fun `GIVEN handlerDef with data and default parameter WHEN creation with bus and data THEN definition is created with bus, data and parameter`() {
        val def: DefWithDefaultParameter =
            defaultHandlerStoryDefinitionCreator<DefWithDefaultParameter>().create(bus, data)

        assertEquals(bus, def.bus)
        assertEquals(data, def.data)
        assertEquals("parameter", def.parameter)
    }

    @Test
    fun `GIVEN handlerDef without data WHEN creation with bus and data THEN definition is created with bus`() {
        val def: SimpleDef = defaultHandlerStoryDefinitionCreator<SimpleDef>().create(bus, data)

        assertEquals(bus, def.bus)
    }

    @Test
    fun `GIVEN handlerDef with default parameter WHEN creation with bus and data THEN definition is created with bus and parameter`() {
        val def: SimpleDefWithDefaultParameter =
            defaultHandlerStoryDefinitionCreator<SimpleDefWithDefaultParameter>().create(bus, data)

        assertEquals(bus, def.bus)
        assertEquals("parameter", def.parameter)
    }

    @Test
    fun `GIVEN handlerDef WHEN creation with bus THEN definition is created with bus`() {
        val def: SimpleDef = defaultHandlerStoryDefinitionCreator<SimpleDef>().create(bus)

        assertEquals(bus, def.bus)
    }

    @Test
    fun `GIVEN handlerDef with default parameter WHEN creation with bus THEN definition is created with bus and parameter`() {
        val def: SimpleDefWithDefaultParameter =
            defaultHandlerStoryDefinitionCreator<SimpleDefWithDefaultParameter>().create(bus)

        assertEquals(bus, def.bus)
        assertEquals("parameter", def.parameter)
    }

    abstract class SubBotBus : BotBus

    internal class DefWithDefaultParameter(bus: BotBus, val data: StoryData, val parameter: String = "parameter") :
        HandlerDef<Connector>(bus)

    internal class SimpleDefWithDefaultParameter(bus: BotBus, val parameter: String = "parameter") :
        HandlerDef<Connector>(bus)
}

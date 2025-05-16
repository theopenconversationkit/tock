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

package definition

import ai.tock.bot.connector.ConnectorHandler
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.definition.ConnectorDef
import ai.tock.bot.definition.ConnectorStoryHandler
import ai.tock.bot.definition.HandlerDef
import ai.tock.bot.definition.defaultHandlerStoryDefinitionCreator
import ai.tock.bot.engine.BotBus
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass
import kotlin.test.assertEquals

internal class StoryHandlerDefinitionBaseTest {

    private val bus = mockk<SubBotBus>(relaxed = true)

    @Test
    fun `GIVEN simple connector WHEN creation THEN connector is set`() {
        // GIVEN
        every { bus getProperty "targetConnectorType" } returns ConnectorType(
            "connectorType1"
        )

        // WHEN
        val def: SimpleDefWithConnector = defaultHandlerStoryDefinitionCreator<SimpleDefWithConnector>().create(bus)

        // THEN
        assertTrue(def.connector is FirstConnectorDef)
    }

    @Test
    fun `GIVEN connector with parameter WHEN creation THEN connector is set with default parameter`() {
        // GIVEN
        every { bus getProperty "targetConnectorType" } returns ConnectorType(
            "connectorType2"
        )

        // WHEN
        val def: SimpleDefWithConnector = defaultHandlerStoryDefinitionCreator<SimpleDefWithConnector>().create(bus)

        // THEN
        assertTrue(def.connector is SecondConnectorWithParameterDef)
        assertEquals("parameter", (def.connector as SecondConnectorWithParameterDef).parameter)
    }

    abstract class SubBotBus : BotBus

    @FirstConnectorHandler(FirstConnectorDef::class)
    @SecondConnectorHandler(SecondConnectorWithParameterDef::class)
    class SimpleDefWithConnector(bus: BotBus) : HandlerDef<SimpleConnectorDef>(bus)

    abstract class SimpleConnectorDef(context: SimpleDefWithConnector) : ConnectorDef<SimpleDefWithConnector>(context)

    class FirstConnectorDef(context: SimpleDefWithConnector) : SimpleConnectorDef(context)
    class SecondConnectorWithParameterDef(context: SimpleDefWithConnector, val parameter: String = "parameter") :
        SimpleConnectorDef(context)

    @ConnectorHandler(connectorTypeId = "connectorType1")
    @Target(AnnotationTarget.CLASS)
    @MustBeDocumented
    annotation class FirstConnectorHandler(val value: KClass<out ConnectorStoryHandler<*>>)

    @ConnectorHandler(connectorTypeId = "connectorType2")
    @Target(AnnotationTarget.CLASS)
    @MustBeDocumented
    annotation class SecondConnectorHandler(val value: KClass<out ConnectorStoryHandler<*>>)
}

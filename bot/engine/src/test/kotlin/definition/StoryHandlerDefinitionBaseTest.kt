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

    private val bus = mockk<BotBus>(relaxed = true)

    @Test
    fun `GIVEN simple connector WHEN creation THEN connector is set`() {
        //GIVEN
        every { bus getProperty "targetConnectorType" } returns ConnectorType(
            "connectorType1"
        )

        //WHEN
        val def: SimpleDefWithConnector = defaultHandlerStoryDefinitionCreator<SimpleDefWithConnector>().create(bus)

        //THEN
        assertTrue(def.connector is FirstConnectorDef)
    }

    @Test
    fun `GIVEN connector with parameter WHEN creation THEN connector is set with default parameter`() {
        //GIVEN
        every { bus getProperty "targetConnectorType" } returns ConnectorType(
            "connectorType2"
        )

        //WHEN
        val def: SimpleDefWithConnector = defaultHandlerStoryDefinitionCreator<SimpleDefWithConnector>().create(bus)

        //THEN
        assertTrue(def.connector is SecondConnectorWithParameterDef)
        assertEquals("parameter", (def.connector as SecondConnectorWithParameterDef).parameter)
    }


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

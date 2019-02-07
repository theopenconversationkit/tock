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

package fr.vsct.tock.bot.engine

import fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration
import fr.vsct.tock.bot.connector.Connector
import fr.vsct.tock.bot.connector.ConnectorConfiguration
import fr.vsct.tock.bot.connector.ConnectorProvider
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.definition.BotDefinition
import fr.vsct.tock.bot.definition.BotProvider
import fr.vsct.tock.bot.engine.BotRepository.botProviders
import fr.vsct.tock.bot.engine.BotRepository.connectorProviders
import fr.vsct.tock.shared.defaultLocale
import fr.vsct.tock.shared.mockedVertx
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.invoke
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.vertx.ext.web.Router
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.toId
import kotlin.test.assertEquals

/**
 *
 */
class BotRepositoryTest : BotEngineTest() {

    @BeforeEach
    fun beforeTest() {
        botProviders.clear()
        connectorProviders.clear()
    }

    @Test
    fun `installBots calls nlpClient#createApplication`() {
        BotRepository.registerBotProvider(object : BotProvider {
            override fun botDefinition(): BotDefinition = botDefinition
        })
        BotRepository.registerConnectorProvider(object : ConnectorProvider {
            override val connectorType: ConnectorType = ConnectorType.none
            override fun connector(connectorConfiguration: ConnectorConfiguration): Connector = mockk(relaxed = true)
        })
        BotRepository.installBots(emptyList())
        verify { nlpClient.createApplication(botDefinition.namespace, botDefinition.nlpModelName, defaultLocale) }
    }

    @Test
    fun `installBots refreshes the configuration from the stored bot configuration`() {
        val connectorType = ConnectorType("test")
        val botId = botDefinition.botId
        val botConfs = listOf(
            BotApplicationConfiguration(
                connectorType.id,
                botId,
                botDefinition.namespace,
                botDefinition.nlpModelName,
                connectorType,
                parameters = mapOf("test" to "test")
            )
        )
        val connectorProvider = object : ConnectorProvider {
            override val connectorType: ConnectorType = connectorType
            override fun connector(connectorConfiguration: ConnectorConfiguration): Connector {
                return mockk(relaxed = true)
            }
        }
        BotRepository.registerConnectorProvider(connectorProvider)
        BotRepository.registerBotProvider(object : BotProvider {
            override fun botDefinition(): BotDefinition = botDefinition
        })

        every { botConfDAO.getConfigurations() } returns botConfs
        BotRepository.installBots(emptyList())
        verify { botConfDAO.save(match { it.parameters.containsKey("test") }) }
    }

    @Test
    fun `installBots uses existing stored bot configuration`() {
        val connectorType = ConnectorType("test")
        val botId = botDefinition.botId
        val connector: Connector = mockk(relaxed = true)
        val botConfs = listOf(
            BotApplicationConfiguration(
                connectorType.id,
                botId,
                botDefinition.namespace,
                botDefinition.nlpModelName,
                connectorType,
                parameters = mapOf("test" to "test")
            )
        )
        val connectorProvider = object : ConnectorProvider {
            override val connectorType: ConnectorType = connectorType
            override fun connector(connectorConfiguration: ConnectorConfiguration): Connector = connector
        }
        BotRepository.registerConnectorProvider(connectorProvider)
        BotRepository.registerBotProvider(object : BotProvider {
            override fun botDefinition(): BotDefinition = botDefinition
        })

        every { botConfDAO.getConfigurations() } returns botConfs
        BotRepository.installBots(emptyList())
        verify { connector.register(any()) }
    }

    @Test
    fun `installBots without configuration and connector is ok`() {
        BotRepository.registerBotProvider(object : BotProvider {
            override fun botDefinition(): BotDefinition = botDefinition
        })

        BotRepository.installBots(emptyList())

        verify(exactly = 0) { connector.register(any()) }
    }

    @Test
    fun `install a bot with a configuration with an unknown connector provider does not crash`() {
        val connectorType = ConnectorType("unknownType")
        val botId = botDefinition.botId
        val connector: Connector = mockk(relaxed = true)
        val botConfs = listOf(
            BotApplicationConfiguration(
                connectorType.id,
                botId,
                botDefinition.namespace,
                botDefinition.nlpModelName,
                connectorType,
                parameters = mapOf("test" to "test")
            )
        )
        val connectorProvider = object : ConnectorProvider {
            override val connectorType: ConnectorType = ConnectorType("other")
            override fun connector(connectorConfiguration: ConnectorConfiguration): Connector = mockk(relaxed = true)
        }
        BotRepository.registerConnectorProvider(connectorProvider)
        BotRepository.registerBotProvider(object : BotProvider {
            override fun botDefinition(): BotDefinition = botDefinition
        })
        every { botConfDAO.getConfigurations() } returns botConfs
        BotRepository.installBots(emptyList())
        verify(exactly = 0) { connector.register(any()) }
    }


    @Test
    fun `uninstall a configuration from an update calls controller#unregisterServices`() {
        val connectorType = ConnectorType("test")
        val botId = botDefinition.botId
        val connector: Connector = mockk(relaxed = true)
        val botConfs1 = listOf(
            BotApplicationConfiguration(
                connectorType.id,
                botId,
                botDefinition.namespace,
                botDefinition.nlpModelName,
                connectorType,
                parameters = mapOf("test" to "value1"),
                _id = "id".toId()
            )
        )
        val botConfs2 = listOf(
            BotApplicationConfiguration(
                connectorType.id,
                botId,
                botDefinition.namespace,
                botDefinition.nlpModelName,
                connectorType,
                parameters = mapOf("test" to "value2"),
                _id = "id".toId()
            )
        )
        val connectorProvider = object : ConnectorProvider {
            override val connectorType: ConnectorType = connectorType
            override fun connector(connectorConfiguration: ConnectorConfiguration): Connector = connector
        }
        BotRepository.registerConnectorProvider(connectorProvider)
        BotRepository.registerBotProvider(object : BotProvider {
            override fun botDefinition(): BotDefinition = botDefinition
        })

        val verticleSlot: CapturingSlot<BotVerticle> = slot()
        every { mockedVertx.deployVerticle(capture(verticleSlot)) } returns Unit

        val appSlot: CapturingSlot<BotApplicationConfiguration> = slot()
        every { botConfDAO.save(capture(appSlot)) } answers { appSlot.captured }
        //first time conf1, second time conf2
        var listenChangesCalled = false
        every { botConfDAO.getConfigurations() } answers {
            if (listenChangesCalled) {
                botConfs2
            } else {
                botConfs1
            }
        }
        //listen changes
        every { botConfDAO.listenChanges(captureLambda()) } answers {
            //first configure the verticle
            verticleSlot.captured.configure()
            //then check path is healthcheck + /1
            assertEquals(2, verticleSlot.captured.router.routes.size)
            assertEquals("/1", verticleSlot.captured.router.routes[1].path)
            //then call the update
            listenChangesCalled = true
            lambda<() -> Unit>().invoke()
        }
        //listen connector
        val controllerSlot: CapturingSlot<ConnectorController> = slot()

        val installer1: (Router) -> Unit = mockk()
        every { installer1(any()) } answers {
            verticleSlot.captured.router.route("/1").handler {}
        }
        val installer2: (Router) -> Unit = mockk()
        every { installer2(any()) } answers {
            verticleSlot.captured.router.route("/2").handler {}
        }
        every { connector.register(capture(controllerSlot)) } answers {
            controllerSlot.captured.registerServices(
                "/a",
                if (listenChangesCalled) installer2 else installer1
            )
        }


        BotRepository.installBots(emptyList())

        //check verticle contains installer2 route (and so unregister has been called on installer1)
        assertEquals(2, verticleSlot.captured.router.routes.size)
        assertEquals("/2", verticleSlot.captured.router.routes[1].path)
    }
}
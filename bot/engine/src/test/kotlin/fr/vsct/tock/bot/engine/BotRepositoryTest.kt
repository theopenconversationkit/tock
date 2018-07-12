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
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

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
}
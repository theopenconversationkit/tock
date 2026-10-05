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

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.connector.Connector
import ai.tock.bot.connector.ConnectorCallbackBase
import ai.tock.bot.connector.ConnectorConfiguration
import ai.tock.bot.connector.ConnectorData
import ai.tock.bot.connector.ConnectorProvider
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.NotifyBotStateModifier.KEEP_CURRENT_STATE
import ai.tock.bot.engine.BotEngineTest
import ai.tock.bot.engine.BotRepository
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.event.SkippedEventException
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.shared.coroutines.ExperimentalTockCoroutines
import io.mockk.CapturingSlot
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.slot
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PushNotificationsTest : BotEngineTest() {
    companion object {
        private const val CONNECTOR_ID = "test"
    }

    val intent = Intent("a")
    val recipientId = PlayerId("user")

    @BeforeEach
    fun beforeEach() {
        System.setProperty("tock_bot_locked_attempts_wait_in_ms", "5")
        val connectorType = ConnectorType(CONNECTOR_ID)
        val botId = botDefinition.botId

        val botConfs =
            listOf(
                BotApplicationConfiguration(
                    connectorType.id,
                    botId,
                    botDefinition.namespace,
                    botDefinition.nlpModelName,
                    connectorType,
                    parameters = mapOf(CONNECTOR_ID to CONNECTOR_ID),
                ),
            )

        every { botConfDAO.getConfigurations() } answers { botConfs }

        val connectorProvider =
            object : ConnectorProvider {
                override val connectorType: ConnectorType = connectorType

                override fun connector(connectorConfiguration: ConnectorConfiguration): Connector = connector
            }
        BotRepository.registerConnectorProvider(connectorProvider)
        BotRepository.registerBotProvider(
            object : BotProvider {
                override fun botDefinition(): BotDefinition = botDefinition
            },
        )

        val appSlot: CapturingSlot<BotApplicationConfiguration> = slot()
        every { botConfDAO.save(capture(appSlot)) } answers { appSlot.captured }

        BotRepository.installBots(emptyList())
    }

    @Test
    fun `GIVEN stateModifier THEN notify calls connector notify method`() {
        notify(CONNECTOR_ID, botDefinition.namespace, botDefinition.botId, recipientId, intent, stateModifier = KEEP_CURRENT_STATE)

        coVerify {
            connector.notify(
                any(),
                recipientId,
                intent,
                parameters = any(),
                notificationType = any(),
                errorListener = any(),
            )
        }
    }

    @Test
    fun `GIVEN no notificationType passed THEN notify pass null NotificationType to connector`() {
        notify(CONNECTOR_ID, botDefinition.namespace, botDefinition.botId, recipientId, intent, stateModifier = KEEP_CURRENT_STATE)

        coVerify {
            connector.notify(
                any(),
                recipientId,
                intent,
                parameters = any(),
                notificationType = null,
                errorListener = any(),
            )
        }
    }

    @OptIn(ExperimentalTockCoroutines::class)
    @Test
    suspend fun `GIVEN locked user session WHEN pushNotification is called THEN throw SkippedEventException`() {
        coEvery { userLock.tryLock(recipientId.id) } returns false
        coEvery { connector.notify(any(), any(), any(), notificationType = any(), errorListener = any()) } coAnswers {
            firstArg<ConnectorController>().handleUserEvent(
                SendChoice(
                    PlayerId(botDefinition.botId, PlayerType.bot),
                    CONNECTOR_ID,
                    recipientId,
                    intent.wrappedIntent().name,
                    null,
                    emptyMap(),
                ),
                ConnectorData(
                    ConnectorCallbackBase(CONNECTOR_ID, ConnectorType(CONNECTOR_ID), arg<(Throwable) -> Unit>(7)),
                ),
            )
        }
        assertThrows<SkippedEventException> {
            botDefinition.pushNotification(CONNECTOR_ID, recipientId, intent)
        }
    }
}

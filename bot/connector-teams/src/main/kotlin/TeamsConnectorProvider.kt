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

package ai.tock.bot.connector.teams

import ai.tock.bot.connector.Connector
import ai.tock.bot.connector.ConnectorConfiguration
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorProvider
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.ConnectorTypeConfiguration
import ai.tock.bot.connector.ConnectorTypeConfigurationField
import ai.tock.bot.connector.teams.messages.TeamsBotTextMessage
import ai.tock.bot.connector.teams.messages.TeamsCardAction
import ai.tock.bot.connector.teams.messages.TeamsCarousel
import ai.tock.bot.connector.teams.messages.TeamsHeroCard
import ai.tock.shared.resourceAsString
import kotlin.reflect.KClass

/**
 *
 */
internal object TeamsConnectorProvider : ConnectorProvider {

    private const val APP_ID = "appId"
    private const val PASSWORD = "password"

    override val connectorType: ConnectorType get() = teamsConnectorType

    override fun connector(connectorConfiguration: ConnectorConfiguration): Connector {
        with(connectorConfiguration) {
            return TeamsConnector(
                connectorId = connectorId,
                path = path,
                appId = parameters.getValue(APP_ID),
                appPassword = parameters.getValue(PASSWORD)
            )
        }
    }

    override fun configuration(): ConnectorTypeConfiguration =
        ConnectorTypeConfiguration(
            teamsConnectorType,
            listOf(
                ConnectorTypeConfigurationField(
                    "appId",
                    APP_ID,
                    true
                ),
                ConnectorTypeConfigurationField(
                    "password",
                    PASSWORD,
                    true
                )
            ),
            resourceAsString("/teams.svg")
        )

    override val supportedResponseConnectorMessageTypes: Set<KClass<out ConnectorMessage>> = setOf(
        TeamsBotTextMessage::class,
        TeamsCardAction::class,
        TeamsCarousel::class,
        TeamsHeroCard::class
    )
}

internal class TeamsConnectorProviderService : ConnectorProvider by TeamsConnectorProvider

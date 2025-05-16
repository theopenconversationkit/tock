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

package ai.tock.bot.connector.rocketchat

import ai.tock.bot.connector.Connector
import ai.tock.bot.connector.ConnectorConfiguration
import ai.tock.bot.connector.ConnectorProvider
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.ConnectorTypeConfiguration
import ai.tock.bot.connector.ConnectorTypeConfigurationField
import ai.tock.shared.resourceAsString

/**
 *
 */
internal object RocketChatConnectorProvider : ConnectorProvider {

    private const val ROCKET_CHAT_URL = "_url_"
    private const val LOGIN = "_login_"
    private const val PASSWORD = "_password_"
    private const val AVATAR = "_avatar_"
    private const val DEDICATED_ROOM_ID = "_dedicated_room_id_"

    override val connectorType: ConnectorType get() = rocketChatConnectorType

    override fun connector(connectorConfiguration: ConnectorConfiguration): Connector {
        with(connectorConfiguration) {
            return RocketChatConnector(
                connectorId,
                RocketChatClient(
                    parameters.getValue(ROCKET_CHAT_URL),
                    parameters.getValue(LOGIN),
                    parameters.getValue(PASSWORD),
                    parameters.getValue(AVATAR)
                ),
                parameters[DEDICATED_ROOM_ID]?.run { if (isBlank()) null else trim() }
            )
        }
    }

    override fun configuration(): ConnectorTypeConfiguration =
        ConnectorTypeConfiguration(
            rocketChatConnectorType,
            listOf(
                ConnectorTypeConfigurationField(
                    "Rocket.Chat Server Url",
                    ROCKET_CHAT_URL,
                    true
                ),
                ConnectorTypeConfigurationField(
                    "Bot Login",
                    LOGIN,
                    true
                ),
                ConnectorTypeConfigurationField(
                    "Bot Password",
                    PASSWORD,
                    true
                ),
                ConnectorTypeConfigurationField(
                    "Avatar Url",
                    AVATAR,
                    true
                ),
                ConnectorTypeConfigurationField(
                    "Optional Room Id",
                    DEDICATED_ROOM_ID,
                    false
                )
            ),
            resourceAsString("/rocketchat.svg")
        )
}

internal class RocketChatConnectorProviderService : ConnectorProvider by RocketChatConnectorProvider

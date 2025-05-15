/*
 * Copyright (C) 2017/2024 e-voyageurs technologies
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

package ai.tock.bot.connector.mattermost

import ai.tock.bot.connector.*
import ai.tock.shared.resourceAsString

/**
 *
 */
internal object MattermostConnectorProvider : ConnectorProvider {

    private const val MATTERMOST_URL = "_url_"
    private const val TOKEN = "_token_"
    private const val DEDICATED_CHANNEL_ID = "_dedicated_room_id_"
    private const val OUTGOING_TOKEN = "_outgoing_token_"
    private const val TOCK_USERNAME = "_tock_username_"

    override val connectorType: ConnectorType get() = mattermostConnectorType

    override fun connector(connectorConfiguration: ConnectorConfiguration): Connector {
        with(connectorConfiguration) {
            return MattermostConnector(
                connectorId,
                path,
                parameters.getValue(MATTERMOST_URL),
                parameters.getValue(TOKEN),
                parameters[DEDICATED_CHANNEL_ID]?.run { if (isBlank()) null else trim() },
                parameters.getValue(OUTGOING_TOKEN),
                parameters[TOCK_USERNAME]?.run { if (isBlank()) null else trim() },
            )
        }
    }

    override fun configuration(): ConnectorTypeConfiguration =
        ConnectorTypeConfiguration(
            mattermostConnectorType,
            listOf(
                ConnectorTypeConfigurationField(
                    "Mattermost Server Url",
                    MATTERMOST_URL,
                    true
                ),
                ConnectorTypeConfigurationField(
                    "Incoming webhook token",
                    TOKEN,
                    true
                ),
                ConnectorTypeConfigurationField(
                    "Optional Channel Id",
                    DEDICATED_CHANNEL_ID,
                    false
                ),
                ConnectorTypeConfigurationField(
                    "Outgoing webhook token",
                    OUTGOING_TOKEN,
                    true
                ),
                ConnectorTypeConfigurationField(
                    "Optional Tock Username",
                    TOCK_USERNAME,
                    false
                ),
            ),
            resourceAsString("/mattermost.svg")
        )
}

internal class MattermostConnectorProviderService : ConnectorProvider by MattermostConnectorProvider

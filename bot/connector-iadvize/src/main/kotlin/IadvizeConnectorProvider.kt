/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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
package ai.tock.bot.connector.iadvize

import ai.tock.bot.connector.*
import ai.tock.bot.connector.iadvize.model.response.conversation.IadvizeResponse
import ai.tock.shared.resourceAsString
import kotlin.reflect.KClass

internal object IadvizeConnectorProvider : ConnectorProvider {
    override val connectorType: ConnectorType = iadvizeConnectorType

    private const val EDITOR_URL = "editorUrl"
    private const val FIRST_MESSAGE = "firstMessage"

    override fun connector(connectorConfiguration: ConnectorConfiguration): Connector {
        with(connectorConfiguration) {
            return IadvizeConnector(
                connectorId,
                connectorConfiguration.path,
                parameters.getValue(EDITOR_URL),
                parameters.getValue(FIRST_MESSAGE)
            )
        }
    }

    override fun configuration(): ConnectorTypeConfiguration =
        ConnectorTypeConfiguration(
            connectorType,
            listOf(
                ConnectorTypeConfigurationField(
                    "URL de l'éditeur",
                     EDITOR_URL,
                    true
                ),
                ConnectorTypeConfigurationField(
                    "Premier message affiché par le bot avant le début de conversation",
                     FIRST_MESSAGE,
                    true
                )
            ),
            resourceAsString("/iadvize.svg")
        )

    override val supportedResponseConnectorMessageTypes: Set<KClass<out ConnectorMessage>> = setOf(
        IadvizeResponse::class
    )
}

internal class IadvizeConnectorProviderService : ConnectorProvider by IadvizeConnectorProvider
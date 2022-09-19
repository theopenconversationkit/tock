/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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
import ai.tock.bot.connector.iadvize.model.response.conversation.reply.IadvizeReply
import ai.tock.shared.loadProperties
import ai.tock.shared.resourceAsString
import java.util.*
import kotlin.reflect.KClass

internal object IadvizeConnectorProvider : ConnectorProvider {
    override val connectorType: ConnectorType = iadvizeConnectorType

    private const val EDITOR_URL = "tock_iadvize_editor_url"
    private const val FIRST_MESSAGE = "tock_iadvize_first_message"
    private const val DISTRIBUTION_RULE = "tock_iadvize_distribution_rule"

    override fun connector(connectorConfiguration: ConnectorConfiguration): Connector {
        with(connectorConfiguration) {
            return IadvizeConnector(
                connectorId,
                connectorConfiguration.path,
                parameters.getValue(EDITOR_URL),
                parameters.getValue(FIRST_MESSAGE),
                parameters[DISTRIBUTION_RULE],
            )
        }
    }

    override fun configuration(): ConnectorTypeConfiguration {
        val properties: Properties = loadProperties("/iadvize.properties")
        val editorUrlField = ConnectorTypeConfigurationField(
            properties.getProperty(EDITOR_URL, EDITOR_URL),
            EDITOR_URL,
            true
        )
        val firstMessageField = ConnectorTypeConfigurationField(
            properties.getProperty(FIRST_MESSAGE, FIRST_MESSAGE),
            FIRST_MESSAGE,
            true
        )
        val distributionRuleField = ConnectorTypeConfigurationField(
            properties.getProperty(DISTRIBUTION_RULE, DISTRIBUTION_RULE),
            DISTRIBUTION_RULE,
            false
        )
        return ConnectorTypeConfiguration(
            connectorType,
            listOf(editorUrlField, firstMessageField, distributionRuleField),
            resourceAsString("/iadvize.svg")
        )
    }

    override val supportedResponseConnectorMessageTypes: Set<KClass<out ConnectorMessage>> = setOf(
        IadvizeReply::class
    )
}

internal class IadvizeConnectorProviderService : ConnectorProvider by IadvizeConnectorProvider
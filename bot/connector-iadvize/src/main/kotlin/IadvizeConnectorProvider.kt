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
package ai.tock.bot.connector.iadvize

import ai.tock.bot.connector.Connector
import ai.tock.bot.connector.ConnectorConfiguration
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorProvider
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.ConnectorTypeConfiguration
import ai.tock.bot.connector.ConnectorTypeConfigurationField
import ai.tock.shared.loadProperties
import ai.tock.shared.resourceAsString
import java.util.Properties
import kotlin.reflect.KClass

internal object IadvizeConnectorProvider : ConnectorProvider {
    override val connectorType: ConnectorType = iadvizeConnectorType

    private const val EDITOR_URL = "tock_iadvize_editor_url"
    private const val FIRST_MESSAGE = "tock_iadvize_first_message"
    private const val DISTRIBUTION_RULE = "tock_iadvize_distribution_rule"
    private const val SECRET_TOKEN = "tock_iadvize_secret_token"
    private const val DISTRIBUTION_RULE_UNAVAILABLE_MESSAGE = "tock_iadvize_distribution_rule_unavailable"
    private const val LOCALE_CODE = "tock_iadvize_locale_code"

    override fun connector(connectorConfiguration: ConnectorConfiguration): Connector {
        with(connectorConfiguration) {
            return IadvizeConnector(
                connectorId,
                connectorConfiguration.path,
                parameters.getValue(EDITOR_URL),
                parameters.getValue(FIRST_MESSAGE),
                parameters.getOrDefault(DISTRIBUTION_RULE, null),
                parameters.getOrDefault(SECRET_TOKEN, null),
                parameters.getValue(DISTRIBUTION_RULE_UNAVAILABLE_MESSAGE),
                parameters.getOrDefault(LOCALE_CODE, null),
            )
        }
    }

    override fun configuration(): ConnectorTypeConfiguration {
        val properties: Properties = loadProperties("/iadvize.properties")
        val editorUrlField =
            ConnectorTypeConfigurationField(
                properties.getProperty(EDITOR_URL),
                EDITOR_URL,
                true,
            )
        val firstMessageField =
            ConnectorTypeConfigurationField(
                properties.getProperty(FIRST_MESSAGE),
                FIRST_MESSAGE,
                true,
            )
        val distributionRuleField =
            ConnectorTypeConfigurationField(
                properties.getProperty(DISTRIBUTION_RULE),
                DISTRIBUTION_RULE,
                false,
            )
        val secretToken =
            ConnectorTypeConfigurationField(
                properties.getProperty(SECRET_TOKEN),
                SECRET_TOKEN,
                false,
            )
        val distributionRuleUnvailableMessageField =
            ConnectorTypeConfigurationField(
                properties.getProperty(DISTRIBUTION_RULE_UNAVAILABLE_MESSAGE),
                DISTRIBUTION_RULE_UNAVAILABLE_MESSAGE,
                true,
            )
        val localeCode =
            ConnectorTypeConfigurationField(
                properties.getProperty(LOCALE_CODE),
                LOCALE_CODE,
                false,
            )

        return ConnectorTypeConfiguration(
            connectorType,
            listOf(
                editorUrlField,
                firstMessageField,
                distributionRuleField,
                secretToken,
                distributionRuleUnvailableMessageField,
                localeCode,
            ),
            resourceAsString("/iadvize.svg"),
        )
    }

    override val supportedResponseConnectorMessageTypes: Set<KClass<out ConnectorMessage>> = setOf(IadvizeConnectorMessage::class)
}

internal class IadvizeConnectorProviderService : ConnectorProvider by IadvizeConnectorProvider

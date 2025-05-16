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

package ai.tock.bot.connector

import ai.tock.bot.connector.ConnectorTypeConfiguration.Companion.ALLOWED_IPS_FIELD
import ai.tock.bot.connector.ConnectorTypeConfiguration.Companion.X_AUTH_TOKEN_FIELD
import ai.tock.shared.security.RequestFilter
import kotlin.reflect.KClass

/**
 * To provide a new [Connector] from a [ConnectorConfiguration].
 * The implementation is loaded at runtime to list all available connectors, using the java [java.util.ServiceLoader]
 * - you need to provide a META-INF/services/ai.tock.bot.connector.ConnectorProvider file.
 */
interface ConnectorProvider {

    /**
     * The connector type provided
     */
    val connectorType: ConnectorType

    /**
     * The supported connector messages - used to check the authorized messages.
     */
    val supportedResponseConnectorMessageTypes: Set<KClass<out ConnectorMessage>> get() = emptySet()

    /**
     * Provides a new [Connector] instance from the specified [ConnectorConfiguration].
     */
    fun connector(connectorConfiguration: ConnectorConfiguration): Connector

    /**
     * Checks the connector configuration.
     *
     * @return empty list if there is no error, and a list of errors if this configuration is invalid.
     */
    fun check(connectorConfiguration: ConnectorConfiguration): List<String> {
        val list = mutableListOf<String>()
        with(connectorConfiguration) {
            if (connectorId.isBlank()) {
                list.add("connector id may not be empty")
            }
            if (path.isBlank() || !path.trim().startsWith("/")) {
                list.add("path may not be empty and must start with /")
            }

            configuration().fields.filter { it.mandatory }.forEach {
                if (connectorConfiguration.parameters[it.key].isNullOrBlank()) {
                    list.add("${it.label} is mandatory")
                }
            }
        }
        return list
    }

    /**
     * Describes the configuration parameters of the [connectorType].
     */
    fun configuration(): ConnectorTypeConfiguration = ConnectorTypeConfiguration(connectorType)

    /**
     * Creates a [RequestFilter] from the current configuration.
     */
    fun createRequestFilter(connectorConfiguration: ConnectorConfiguration): RequestFilter =
        ai.tock.shared.security.createRequestFilter(
            connectorConfiguration.parameters[ALLOWED_IPS_FIELD]?.split(",")?.toSet(),
            connectorConfiguration.parameters[X_AUTH_TOKEN_FIELD]
        )
}

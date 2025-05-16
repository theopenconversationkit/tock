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

import java.util.ServiceLoader

/**
 * A connector type configuration definition.
 */
data class ConnectorTypeConfiguration(
    /**
     * The connector type.
     */
    val connectorType: ConnectorType,
    /**
     * The custom parameters for this [ConnectorType].
     */
    val fields: List<ConnectorTypeConfigurationField> = emptyList(),
    /**
     * A svg icon if any.
     */
    val svgIcon: String? = null
) {

    companion object {
        /**
         * The available connector types.
         * Retrieving using [ServiceLoader].
         */
        val connectorConfigurations: Set<ConnectorTypeConfiguration> =
            ServiceLoader.load(ConnectorProvider::class.java).map { it.configuration() }.toSet()

        /**
         * Common allowed ips field.
         */
        internal const val ALLOWED_IPS_FIELD = "AllowedIPs"

        /**
         * Common X_Auth_Token field.
         */
        internal const val X_AUTH_TOKEN_FIELD = "X-Auth-Token"

        /**
         * Returns common security fields.
         */
        fun commonSecurityFields(): List<ConnectorTypeConfigurationField> =
            listOf(
                ConnectorTypeConfigurationField(
                    "Allowed IPs",
                    ALLOWED_IPS_FIELD,
                    false
                ),
                ConnectorTypeConfigurationField(
                    "X-Auth-Token Restriction",
                    X_AUTH_TOKEN_FIELD,
                    false
                )
            )
    }
}

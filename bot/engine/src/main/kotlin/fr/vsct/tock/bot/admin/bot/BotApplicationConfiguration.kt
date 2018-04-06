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

package fr.vsct.tock.bot.admin.bot

import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.shared.property
import org.litote.kmongo.Data
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.net.Inet4Address
import java.net.NetworkInterface

/**
 * Configuration details for a bot and a connector.
 */
@Data
data class BotApplicationConfiguration(
    /**
     * The application identifier.
     */
    val applicationId: String,
    /**
     * The bot identifier.
     */
    val botId: String,
    /**
     * The namespace of the model.
     */
    val namespace: String,
    /**
     * The name of the model.
     */
    val nlpModel: String,
    /**
     * The type of connector for the configuration.
     */
    val connectorType: ConnectorType,
    /**
     * The underlying connector type. For example, you can have connectorType=rest and ownerConnectorType=messenger.
     */
    val ownerConnectorType: ConnectorType? = null,
    /**
     * The name of the configuration.
     */
    val name: String = applicationId,
    /**
     * The base url of the connector.
     */
    val baseUrl: String? = defaultBaseUrl,
    /**
     * Additional parameters for this connector.
     */
    val parameters: Map<String, String> = emptyMap(),
    /**
     * Has this connector been manualy modified?
     */
    val manuallyModified: Boolean = false,
    /**
     * The relative path of the connector. If null, the default path is used.
     */
    val path: String? = null,
    /**
     * The configuration identifier.
     */
    val _id: Id<BotApplicationConfiguration> = newId()
) {

    companion object {
        val defaultBaseUrl: String =
            property(
                "tock_configuration_bot_default_base_url",
                "http://${getLocalhostIP()}:${property("botverticle_port", "8080")}"
            )

        private fun getLocalhostIP(): String {
            return NetworkInterface.getNetworkInterfaces()
                .toList()
                .flatMap { it.inetAddresses.toList().filterIsInstance<Inet4Address>() }
                .find { it.hostName.startsWith("192.168.0") }
                ?.hostName
                    ?: "localhost"
        }
    }

    /**
     * The target connector type is the [ownerConnectorType]. If null [connectorType] is used.
     */
    @Transient
    val targetConnectorType = ownerConnectorType ?: connectorType
}
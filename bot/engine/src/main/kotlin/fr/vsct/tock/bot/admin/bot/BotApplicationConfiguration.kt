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
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface

/**
 *
 */
data class BotApplicationConfiguration(
        val applicationId: String,
        val botId: String,
        val namespace: String,
        val nlpModel: String,
        val connectorType: ConnectorType,
        val ownerConnectorType: ConnectorType? = null,
        val name: String = applicationId,
        val baseUrl: String? = defaultBaseUrl,
        val manuallyModified: Boolean = false,
        val _id: Id<BotApplicationConfiguration> = newId()) {

    companion object {
        val defaultBaseUrl: String =
                property(
                        "tock_configuration_bot_default_base_url",
                        "http://${getLocalhostIP()}:${property("botverticle_port", "8080")}"
                )

        private fun getLocalhostIP(): String {
            return NetworkInterface.getNetworkInterfaces()
                    .toList()
                    .find { it.name.startsWith("eno") }
                    ?.inetAddresses
                    ?.toList()
                    ?.filterIsInstance<Inet4Address>()
                    ?.map { it.hostAddress }
                    ?.firstOrNull()
                    ?: InetAddress.getLocalHost().hostAddress
        }
    }

    @Transient
    val targetConnectorType = ownerConnectorType ?: connectorType
}
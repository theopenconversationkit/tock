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

package fr.vsct.tock.bot.engine

import fr.vsct.tock.bot.connector.ConnectorConfiguration
import fr.vsct.tock.shared.property

/**
 * Used by [Connector] implementation to register new [ConnectorConfiguration].
 */
object ConnectorConfigurationRepository {

    private val configurations: MutableList<ConnectorConfiguration> = mutableListOf()

    internal fun getConfigurations(): List<ConnectorConfiguration> {
        return configurations + loadConnectorsConfigurationFromEnv()
    }

    private fun loadConnectorsConfigurationFromEnv(): List<ConnectorConfiguration> {
        val conf = property("tock_bot_configuration", "")
        return if (conf.isEmpty()) {
            emptyList()
        } else {
            conf.split("|").map {
                val params = it.split(";")
                ConnectorConfiguration(
                    params[0].trim(),
                    params[1].trim(),
                    BotRepository.connectorProviders.find { it.connectorType.id === params[2].trim() }?.connectorType
                            ?: error("connector type not found : ${params[2]} - please register connector first"),
                    null,
                    params.subList(3, params.size).map {
                        val s = it.split("=")
                        s[0].trim() to s[1].trim()
                    }.toMap()
                )
            }
        }
    }

    /**
     * Adds a new configuration.
     */
    fun addConfiguration(conf: ConnectorConfiguration) {
        configurations += conf
    }

    internal fun cleanup() {
        configurations.clear()
    }
}
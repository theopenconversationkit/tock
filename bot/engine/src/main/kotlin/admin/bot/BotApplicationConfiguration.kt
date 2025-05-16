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

package ai.tock.bot.admin.bot

import ai.tock.bot.connector.ConnectorConfiguration
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.engine.BotBus
import ai.tock.shared.property
import ai.tock.shared.tryToFindLocalIp
import org.litote.kmongo.Id
import org.litote.kmongo.newId

/**
 * Configuration details for a bot and a connector.
 */
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
     * The relative path of the connector. If null, the default path is used.
     */
    val path: String? = null,
    /**
     * The configuration identifier.
     */
    val _id: Id<BotApplicationConfiguration> = newId(),
    /**
     * The target configuration identifier (test case).
     */
    val targetConfigurationId: Id<BotApplicationConfiguration>? = null
) {

    companion object {
        val defaultBaseUrl: String =
            property(
                "tock_configuration_bot_default_base_url",
                "http://${tryToFindLocalIp()}:${property("botverticle_port", "8080")}"
            )
    }

    /**
     * The target connector type is the [ownerConnectorType]. If null [connectorType] is used.
     */
    @Transient
    val targetConnectorType = ownerConnectorType ?: connectorType

    /**
     * Returns the linked [ConnectorConfiguration].
     */
    fun toConnectorConfiguration(): ConnectorConfiguration = ConnectorConfiguration(this)

    internal fun equalsWithoutId(conf: BotApplicationConfiguration): Boolean =
        conf.applicationId == applicationId &&
            conf.botId == botId &&
            conf.namespace == namespace &&
            conf.nlpModel == nlpModel &&
            conf.connectorType == connectorType &&
            conf.ownerConnectorType == ownerConnectorType &&
            conf.name == name &&
            conf.baseUrl == baseUrl &&
            conf.parameters == parameters &&
            conf.path == path

    internal fun toKey(): BotApplicationConfigurationKey =
        BotApplicationConfigurationKey(applicationId = applicationId, botId = botId, namespace = namespace)
}

/**
 * Unique key for [BotApplicationConfiguration].
 */
data class BotApplicationConfigurationKey(
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
    val namespace: String
) {
    constructor(applicationId: String, botDefinition: BotDefinition) : this(
        applicationId,
        botDefinition.botId,
        botDefinition.namespace
    )

    constructor(bus: BotBus) : this(bus.connectorId, bus.botDefinition)
}

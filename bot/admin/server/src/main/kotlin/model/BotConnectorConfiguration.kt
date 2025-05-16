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

package ai.tock.bot.admin.model

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.bot.BotApplicationConfiguration.Companion.defaultBaseUrl
import ai.tock.bot.connector.ConnectorType
import ai.tock.shared.defaultLocale
import org.litote.kmongo.Id
import org.litote.kmongo.newId

/**
 *
 */
data class BotConnectorConfiguration(
    val applicationId: String,
    val botId: String,
    val namespace: String,
    val nlpModel: String,
    val connectorType: ConnectorType,
    val ownerConnectorType: ConnectorType? = null,
    val name: String = applicationId,
    val baseUrl: String? = defaultBaseUrl,
    val parameters: Map<String, String> = emptyMap(),
    val path: String? = null,
    val fillMandatoryValues: Boolean = false,
    val _id: Id<BotApplicationConfiguration>? = null,
    val targetConfigurationId: Id<BotApplicationConfiguration>? = null
) {
    fun toBotApplicationConfiguration(): BotApplicationConfiguration =
        BotApplicationConfiguration(
            applicationId,
            botId,
            namespace,
            nlpModel,
            connectorType,
            ownerConnectorType,
            name,
            if (baseUrl.isNullOrBlank() && _id == null) defaultBaseUrl else baseUrl,
            parameters,
            path?.lowercase(defaultLocale),
            _id ?: newId(),
            targetConfigurationId
        )
}

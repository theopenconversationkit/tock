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

package ai.tock.bot.connector.web

import ai.tock.bot.connector.Connector
import ai.tock.bot.connector.ConnectorConfiguration
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorProvider
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.ConnectorTypeConfiguration
import ai.tock.bot.connector.ConnectorTypeConfigurationField
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.resourceAsString
import ai.tock.shared.security.auth.spi.WebSecurityHandler
import ai.tock.shared.security.auth.spi.WebSecurityMode
import kotlin.reflect.KClass

private const val WEB_SECURITY_MODE_PARAM = "web_security_mode"
private const val PUBLIC_PATH_PARAM = "public_path"

// used in file META-INF/services/ai.tock.bot.connector.ConnectorProvider
class WebConnectorProvider : ConnectorProvider {
    override val connectorType: ConnectorType get() = webConnectorType

    override fun connector(connectorConfiguration: ConnectorConfiguration): Connector {
        with(connectorConfiguration) {
            val webSecurityType = WebSecurityMode.find(parameters[WEB_SECURITY_MODE_PARAM])

            val publicPath = parameters[PUBLIC_PATH_PARAM] ?: path

            return WebConnector(
                connectorId,
                path,
                webSecurityHandler = injector.provide<WebSecurityHandler>(tag = webSecurityType.name),
                publicPath = publicPath,
            )
        }
    }

    override fun configuration(): ConnectorTypeConfiguration =
        ConnectorTypeConfiguration(
            webConnectorType,
            svgIcon = resourceAsString("/web.svg"),
            fields =
                listOf(
                    ConnectorTypeConfigurationField(
                        label = "Web Security Mode",
                        key = WEB_SECURITY_MODE_PARAM,
                        mandatory = false,
                    ),
                    ConnectorTypeConfigurationField(
                        label = "Public Path (if different from local REST Path)",
                        key = PUBLIC_PATH_PARAM,
                        mandatory = false,
                    ),
                ),
        )

    override val supportedResponseConnectorMessageTypes: Set<KClass<out ConnectorMessage>> = setOf(WebMessage::class)
}

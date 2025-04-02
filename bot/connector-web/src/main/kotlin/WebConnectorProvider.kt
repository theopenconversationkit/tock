/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

import ai.tock.bot.connector.*
import ai.tock.shared.*
import ai.tock.shared.security.auth.spi.WebSecurityHandler
import ai.tock.shared.security.auth.spi.WebSecurityMode
import kotlin.reflect.KClass

private const val WEB_SECURITY_MODE_PARAM = "web_security_mode"

private val cookieAuth = booleanProperty("tock_web_cookie_auth", false)

internal object WebConnectorProvider : ConnectorProvider {

    override val connectorType: ConnectorType get() = webConnectorType

    override fun connector(connectorConfiguration: ConnectorConfiguration): Connector {
        with(connectorConfiguration) {
            val webSecurityType = parameters[WEB_SECURITY_MODE_PARAM]
                ?.let { WebSecurityMode.findByName(it) }
                // If the setting is valid and not set to "DEFAULT", it keeps it.
                ?.takeIf { it != WebSecurityMode.DEFAULT }
                // But if it's missing or set to "DEFAULT" it chooses between two options :
                // If "cookieAuth" is enabled, it picks COOKIES.
                // If not, it picks PASSTHROUGH (which likely means no special security measures).
                ?: if (cookieAuth) WebSecurityMode.COOKIES else WebSecurityMode.PASSTHROUGH

            return WebConnector(
                connectorId,
                path,
                injector.provide<WebSecurityHandler>(tag = webSecurityType.name)
            )
        }
    }

    override fun configuration(): ConnectorTypeConfiguration =
        ConnectorTypeConfiguration(
            webConnectorType,
            svgIcon = resourceAsString("/web.svg"),
            fields = listOf(
                ConnectorTypeConfigurationField(
                    label = "Web Security Mode",
                    key = WEB_SECURITY_MODE_PARAM,
                    mandatory = false
                )
            )
        )

    override val supportedResponseConnectorMessageTypes: Set<KClass<out ConnectorMessage>> = setOf(WebMessage::class)
}

// used in file META-INF/services/ai.tock.bot.connector.ConnectorProvider
internal class WebConnectorProviderService : ConnectorProvider by WebConnectorProvider

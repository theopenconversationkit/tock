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
import ai.tock.shared.provideOrDefault
import ai.tock.shared.resourceAsString
import ai.tock.shared.security.auth.spi.HttpWebSecurity
import ai.tock.shared.security.auth.spi.WebSecurityMode
import kotlin.reflect.KClass

private const val WEB_SECURITY_MODE_PARAM = "web_security_mode"
private const val PUBLIC_PATH_PARAM = "public_path"

private val vertxAvailable: Boolean =
    try {
        Class.forName("ai.tock.shared.vertx.WebVerticle")
        true
    } catch (_: ClassNotFoundException) {
        false
    }

/**
 * The security of the connector: the Vert.x routes when the project binds its own Vert.x handler for [mode], or
 * when the server-sent events of tock-bot-connector-web-sse are enabled; the server independent routes otherwise.
 */
internal fun webConnectorSecurity(mode: WebSecurityMode): WebConnectorSecurity {
    if (vertxAvailable) {
        WebVertxServices.security(mode, sse = WebConnector.sseEnabled)?.let { return it }
    } else {
        check(!WebConnector.sseEnabled) { "tock_web_sse needs tock-bot-connector-web-sse and Vert.x: it is not supported without them" }
    }
    val security =
        injector.provideOrDefault<HttpWebSecurity>(tag = mode.name) {
            HttpWebSecurity.default(mode) ?: error("no HttpWebSecurity bound for the web security mode $mode")
        }
    return WebConnectorSecurity.Http(security)
}

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
                security = webConnectorSecurity(webSecurityType),
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

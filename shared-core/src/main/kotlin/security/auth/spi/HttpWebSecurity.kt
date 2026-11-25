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

package ai.tock.shared.security.auth.spi

import ai.tock.shared.http.HttpRequest
import ai.tock.shared.security.web.HttpWebSecurityCookies
import ai.tock.shared.security.web.HttpWebSecurityEncryptedCookies
import ai.tock.shared.security.web.HttpWebSecurityPassthrough

/**
 * The authentication of the users of a web connector (web, OpenAI API), independent of the server: the counterpart
 * of [WebSecurityHandler] for the connectors that declare [ai.tock.shared.http.HttpRoutes].
 *
 * A project binds its own implementation with the tag of its mode: `bind<HttpWebSecurity>(tag = WebSecurityMode.JWT.name)`.
 */
interface HttpWebSecurity {
    /**
     * Authenticates [request]: the user id, the attributes for the connector (an optional
     * `ai.tock.bot.definition.DialogContext` under `tock_transient_dialog_context` (WebSecurityHandler.TRANSIENT_DIALOG_CONTEXT_KEY)...), and
     * the cookies to set. An exception fails the request (500).
     */
    fun authenticate(
        request: HttpRequest,
        context: HttpWebSecurityContext,
    ): HttpWebSecurityResult

    companion object {
        /** The implementation of [mode] that Tock provides, null if it has none (JWT). */
        fun default(mode: WebSecurityMode): HttpWebSecurity? =
            when (mode) {
                WebSecurityMode.COOKIES -> HttpWebSecurityCookies()
                WebSecurityMode.COOKIES_ENCRYPTED -> HttpWebSecurityEncryptedCookies()
                WebSecurityMode.PASSTHROUGH -> HttpWebSecurityPassthrough
                WebSecurityMode.DEFAULT -> default(WebSecurityMode.getDefault())
                WebSecurityMode.JWT -> null
            }
    }
}

/**
 * @property connectorId the connector of the request
 * @property publicPath the public path of the connector: the cookies are stored on it
 */
data class HttpWebSecurityContext(
    val connectorId: String,
    val publicPath: String,
)

/**
 * @property userId the user id, which replaces the one of the request; null to keep it
 * @property attributes the attributes for the connector
 * @property setCookies the values of the `Set-Cookie` headers of the response
 */
data class HttpWebSecurityResult(
    val userId: String? = null,
    val attributes: Map<String, Any?> = emptyMap(),
    val setCookies: List<String> = emptyList(),
)

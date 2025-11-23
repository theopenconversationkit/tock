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

package ai.tock.shared

import okhttp3.Authenticator
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TockProxyAuthenticator(proxyUser: String, proxyPassword: String) : Authenticator {
    private val credential = Credentials.basic(proxyUser, proxyPassword)

    companion object {
        fun install(builder: OkHttpClient.Builder) {
            val proxyUser = propertyOrNull("tock_proxy_user")
            val proxyPassword = propertyOrNull("tock_proxy_password")

            if (proxyUser != null && proxyPassword != null) {
                builder.proxyAuthenticator(TockProxyAuthenticator(proxyUser, proxyPassword))
            } else if (proxyUser != null || proxyPassword != null) {
                throw IllegalStateException(
                    "Both tock_proxy_user and tock_proxy_password must be configured simultaneously to authenticate with proxies, only one found",
                )
            }
        }
    }

    override fun authenticate(
        route: Route?,
        response: Response,
    ): Request? {
        if (response.challenges().any {
                // Allow both preemptive and reactive authentication
                it.scheme.equals("Basic", ignoreCase = true) ||
                    it.scheme.equals("OkHttp-Preemptive", ignoreCase = true)
                // give up if we already tried to authenticate
            } && response.request.header("Proxy-Authorization") == null
        ) {
            return response.request.newBuilder()
                .header("Proxy-Authorization", credential)
                .build()
        }

        return null
    }
}

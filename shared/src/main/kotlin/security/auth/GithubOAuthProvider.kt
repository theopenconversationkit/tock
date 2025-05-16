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

package ai.tock.shared.security.auth

import ai.tock.shared.Executor
import ai.tock.shared.injector
import ai.tock.shared.intProperty
import ai.tock.shared.property
import ai.tock.shared.propertyOrNull
import ai.tock.shared.provide
import ai.tock.shared.security.TockUser
import ai.tock.shared.security.TockUserListener
import ai.tock.shared.security.TockUserRole
import ai.tock.shared.vertx.WebVerticle
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.net.ProxyOptions
import io.vertx.ext.auth.oauth2.OAuth2Auth
import io.vertx.ext.auth.oauth2.providers.GithubAuth
import io.vertx.ext.web.handler.AuthenticationHandler
import io.vertx.ext.web.handler.OAuth2AuthHandler
import io.vertx.ext.web.handler.SessionHandler
import mu.KLogger
import mu.KotlinLogging

private val defaultBaseUrl = property("tock_bot_admin_rest_default_base_url", "http://localhost:8080")

/**
 *
 */
internal class GithubOAuthProvider(
        vertx: Vertx,
        private val oauth2: OAuth2Auth = GithubAuth.create(
                vertx,
                property("tock_github_oauth_client_id", "CLIENT_ID"),
                property("tock_github_oauth_secret_key", "SECRET_KEY"),
                HttpClientOptions().apply {
                    val proxyHost = propertyOrNull("tock_oauth2_proxy_host")
                    val proxyPort = intProperty("tock_oauth2_proxy_port", 0)
                    val proxyUsername= propertyOrNull("tock_oauth2_proxy_username")
                    val proxyPassword = propertyOrNull("tock_oauth2_proxy_password")
                    if (proxyHost != null) {
                        logger.info { "set proxy $proxyHost:$proxyPort" }
                        proxyOptions = ProxyOptions().apply {
                            host = proxyHost
                            port = proxyPort
                            if (proxyUsername != null && proxyPassword != null) {
                                username = proxyUsername
                                password = proxyPassword
                            }
                            }
                        }
                }
        )
) : SSOTockAuthProvider(vertx), OAuth2Auth by oauth2 {

    val logger: KLogger = KotlinLogging.logger {}
    private val executor: Executor get() = injector.provide()

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun createAuthHandler(verticle: WebVerticle): AuthenticationHandler =
            OAuth2AuthHandler.create(vertx, oauth2, "$defaultBaseUrl${callbackPath(verticle)}")

    override fun protectPaths(
            verticle: WebVerticle,
            pathsToProtect: Set<String>,
            sessionHandler: SessionHandler
    ): AuthenticationHandler {
        val authHandler =
                super.protectPaths(verticle, pathsToProtect, sessionHandler)
        (authHandler as OAuth2AuthHandler).apply {
            setupCallback(verticle.router.get(callbackPath(verticle)))
        }

        verticle.router.route("/*")
                .handler {
                    val user = it.user()
                    if (user != null && user !is TockUser) {
                        executor.executeBlocking {
                            val login = RetrofitGithubClient.login(user.principal().getString("access_token"))
                            val tockUser = injector.provide<TockUserListener>().registerUser(
                                    TockUser(login, login, TockUserRole.entries.map { r -> r.name }.toSet())
                            )
                            it.setUser(tockUser)
                            it.next()
                        }
                    } else {
                        it.next()
                    }
                }

        return authHandler
    }

    override fun excludedPaths(verticle: WebVerticle): Set<Regex> =
            super.excludedPaths(verticle) + callbackPath(verticle).toRegex()

    private fun callbackPath(verticle: WebVerticle): String = "${verticle.basePath}/callback"
}

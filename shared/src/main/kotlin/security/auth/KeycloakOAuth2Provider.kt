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
import ai.tock.shared.defaultLocale
import ai.tock.shared.defaultNamespace
import ai.tock.shared.injector
import ai.tock.shared.intProperty
import ai.tock.shared.listProperty
import ai.tock.shared.mapProperty
import ai.tock.shared.property
import ai.tock.shared.propertyOrNull
import ai.tock.shared.provide
import ai.tock.shared.security.TockUser
import ai.tock.shared.security.TockUserListener
import ai.tock.shared.security.TockUserRole
import ai.tock.shared.vertx.WebVerticle
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.json.JsonObject
import io.vertx.core.net.ProxyOptions
import io.vertx.ext.auth.oauth2.OAuth2Auth
import io.vertx.ext.auth.oauth2.OAuth2Options
import io.vertx.ext.web.handler.AuthenticationHandler
import io.vertx.ext.web.handler.OAuth2AuthHandler
import io.vertx.ext.web.handler.SessionHandler
import java.util.Base64
import mu.KotlinLogging

internal class KeycloakOAuth2Provider(
    vertx: Vertx,
    private val oauth2: OAuth2Auth = OAuth2Auth.create(
        vertx,
        OAuth2Options()
            .setSupportedGrantTypes(listProperty("tock_keycloak_grant_types", emptyList()).takeUnless { it.isEmpty() })
            .setClientId(property("tock_keycloak_client_id", "tock"))
            .setClientSecret(property("tock_keycloak_secret_key", "thesecretkeyfromkeycloak"))
            .setSite(property("tock_keycloak_site_url", "https://keycloak/realms/myrealm"))
            .setTokenPath(property("tock_keycloak_access_token_path", "/protocol/openid-connect/token"))
            .setAuthorizationPath(property("tock_keycloak_authorize_path", "/protocol/openid-connect/auth"))
            .setUserInfoPath(property("tock_keycloak_userinfo_path", "/protocol/openid-connect/userinfo"))
            .apply {
                val proxyHost = propertyOrNull("tock_keycloak_proxy_host")
                val proxyPort = intProperty("tock_keycloak_proxy_port", 0)
                if (proxyHost != null) {
                    logger.info { "set proxy $proxyHost:$proxyPort" }
                    logger.info { "set proxy $proxyHost:$proxyPort" }
                    httpClientOptions.proxyOptions = ProxyOptions().apply {
                        host = proxyHost
                        port = proxyPort
                    }
                }
            }
    )
) : SSOTockAuthProvider(vertx), OAuth2Auth by oauth2 {

    companion object {
        private val logger = KotlinLogging.logger {}
        private val userNamespaceAttribute = property("tock_custom_namespace_mapping", "tock_namespace")
        private val userRoleAttribute = property("tock_keycloak_user_role_attribute", "tock_roles")
        private val defaultBaseUrl = property("tock_bot_admin_rest_default_base_url", "http://localhost:8080")
    }

    private val executor: Executor get() = injector.provide()

    override fun createAuthHandler(verticle: WebVerticle): AuthenticationHandler =
        OAuth2AuthHandler.create(vertx, oauth2, "$defaultBaseUrl/rest/callback")

    override fun protectPaths(
        verticle: WebVerticle,
        pathsToProtect: Set<String>,
        sessionHandler: SessionHandler
    ): AuthenticationHandler {
        val authHandler = super.protectPaths(verticle, pathsToProtect, sessionHandler)

        (authHandler as OAuth2AuthHandler).apply {
            setupCallback(verticle.router.get(callbackPath(verticle)))
        }

        verticle.router.route("/*").handler { context ->
            val user = context.user()
            if (user?.containsKey("access_token") == true) {
                user.also { u ->
                    val data = if (u.containsKey("email")) u.principal()
                    else JsonObject(String(Base64.getDecoder().decode(user.get<String>("access_token").split(".")[1])))
                    val login: String = data.getString("email").lowercase(defaultLocale)
                    val customRoles: String = data.getString(userRoleAttribute)
                    val roles = parseUserRoles(customRoles)
                    if (roles.isEmpty()) {
                        logger.warn { "empty role for $customRoles" }
                        context.fail(401)
                    } else {
                        var namespace = data.getString(userNamespaceAttribute)
                        if (namespace == null) {
                            logger.warn { "No attribute representing namespace called $userNamespaceAttribute in $data, using default \"app\" namespace" }
                            namespace = defaultNamespace
                        }

                        executor.executeBlocking {
                            val tockUser = injector.provide<TockUserListener>().registerUser(
                                TockUser(login, namespace, roles), true
                            )

                            vertx.runOnContext {
                                sessionHandler
                                    .setUser(context, tockUser)
                                    .onSuccess { context.next() }
                                    .onFailure { err -> context.fail(err) }
                            }
                        }
                    }
                }
            } else {
                context.next()
            }
        }

        return authHandler
    }

    private fun parseCustomRoles(customRoles: String): List<String> = customRoles
        .split(",")
        .map { it.removePrefix("[").removeSuffix("]").trim() }

    private fun parseUserRoles(customRoles: String): Set<String> =
        parseCustomRoles(customRoles)
            .flatMap { TockUserRole.entries.map { r -> TockUserRole.toRole(r.toString()) } }
            .filterNotNull()
            .map { it.name }
            .toSet()

    override fun excludedPaths(verticle: WebVerticle): Set<Regex> =
        super.excludedPaths(verticle) + callbackPath(verticle).toRegex()

    private fun callbackPath(verticle: WebVerticle): String = "${verticle.basePath}/callback"
}

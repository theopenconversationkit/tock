/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.shared.security.auth

import ai.tock.shared.Executor
import ai.tock.shared.injector
import ai.tock.shared.intProperty
import ai.tock.shared.mapProperty
import ai.tock.shared.property
import ai.tock.shared.propertyOrNull
import ai.tock.shared.provide
import ai.tock.shared.security.TockUser
import ai.tock.shared.security.TockUserListener
import ai.tock.shared.security.TockUserRole
import ai.tock.shared.vertx.WebVerticle
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.net.ProxyOptions
import io.vertx.ext.auth.oauth2.AccessToken
import io.vertx.ext.auth.oauth2.OAuth2Auth
import io.vertx.ext.auth.oauth2.OAuth2ClientOptions
import io.vertx.ext.auth.oauth2.OAuth2FlowType
import io.vertx.ext.web.handler.AuthHandler
import io.vertx.ext.web.handler.OAuth2AuthHandler
import io.vertx.ext.web.handler.SessionHandler
import mu.KotlinLogging

/**
 *
 */
internal class OAuth2Provider(
    vertx: Vertx,
    private val oauth2: OAuth2Auth = OAuth2Auth.create(
        vertx, OAuth2ClientOptions()
            .setFlow(OAuth2FlowType.AUTH_CODE)
            .setClientID(
                property("tock_oauth2_client_id", "")
            )
            .setClientSecret(
                property("tock_oauth2_secret_key", "")
            )
            .setSite(
                property("tock_oauth2_site_url", "")
            )
            .setTokenPath(
                property("tock_oauth2_access_token_path", "")
            )
            .setAuthorizationPath(
                property("tock_oauth2_authorize_path", "")
            )
            .setUserInfoPath(
                property("tock_oauth2_userinfo_path", "")
            )
            .apply {
                val proxyHost = propertyOrNull("tock_oauth2_proxy_host")
                val proxyPort = intProperty("tock_oauth2_proxy_port", 0)
                if (proxyHost != null) {
                    logger.info { "set proxy $proxyHost:$proxyPort" }
                    proxyOptions = ProxyOptions().apply {
                        host = proxyHost
                        port = proxyPort
                    }
                }
            }
    ).rbacHandler { _, _, handler ->
        //TODO better
        handler.handle(Future.succeededFuture(true))
    }
) : SSOTockAuthProvider(vertx), OAuth2Auth by oauth2 {

    companion object {
        private val logger = KotlinLogging.logger {}
        private val namespaceMapping = mapProperty("tock_custom_namespace_mapping", emptyMap())
        private val customRolesMapping = mapProperty("tock_custom_roles_mapping", emptyMap())
        private val userRoleAttribute = property("tock_oauth2_user_role_attribute", "custom:roles")
        private val defaultBaseUrl = property("tock_bot_admin_rest_default_base_url", "http://localhost:8080")
    }

    private val executor: Executor get() = injector.provide()

    override fun createAuthHandler(verticle: WebVerticle): AuthHandler =
        OAuth2AuthHandler.create(this, "$defaultBaseUrl/rest/callback")

    override fun protectPaths(
        verticle: WebVerticle,
        pathsToProtect: Set<String>,
        sessionHandler: SessionHandler
    ): AuthHandler {
        val authHandler = super.protectPaths(verticle, pathsToProtect, sessionHandler)

        (authHandler as OAuth2AuthHandler).apply {
            setupCallback(verticle.router.get(callbackPath(verticle)))
        }

        verticle.router.route("/*")
            .handler { context ->
                val user = context.user()
                if (user is AccessToken) {
                    user.userInfo {
                        val login = it.result().getString("email").toLowerCase()
                        val customRoles = it.result().getString(userRoleAttribute)
                        val roles = parseUserRoles(customRoles)
                        if (roles.isEmpty()) {
                            logger.warn { "empty role for $customRoles" }
                            context.fail(401)
                        } else {
                            val namespace = parseNamespace(customRoles)
                            if (namespace == null) {
                                logger.warn { "no namespace for $customRoles" }
                                context.fail(401)
                            } else {
                                executor.executeBlocking {
                                    val tockUser = injector.provide<TockUserListener>().registerUser(
                                        TockUser(login, namespace, roles), true
                                    )
                                    context.setUser(tockUser)
                                    context.next()
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

    private fun parseNamespace(customRoles: String): String? =
        parseCustomRoles(customRoles)
            .flatMap { namespaceMapping[it]?.split(",")?.map { n -> n.trim() } ?: emptyList() }
            .firstOrNull()

    private fun parseUserRoles(customRoles: String): Set<String> =
        parseCustomRoles(customRoles)
            .flatMap { customRolesMapping[it]?.split(",")?.map { r -> TockUserRole.toRole(r) } ?: emptyList() }
            .filterNotNull()
            .map { it.name }
            .toSet()

    override fun excludedPaths(verticle: WebVerticle): Set<Regex> =
        super.excludedPaths(verticle) + callbackPath(verticle).toRegex()

    private fun callbackPath(verticle: WebVerticle): String = "${verticle.basePath}/callback"

}


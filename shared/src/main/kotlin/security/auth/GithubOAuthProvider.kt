/*
 * Copyright (C) 2017 VSCT
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

package fr.vsct.tock.shared.security.auth

import fr.vsct.tock.shared.Executor
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.property
import fr.vsct.tock.shared.provide
import fr.vsct.tock.shared.security.TockUser
import fr.vsct.tock.shared.security.TockUserRole
import fr.vsct.tock.shared.vertx.WebVerticle
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.ext.auth.oauth2.OAuth2Auth
import io.vertx.ext.auth.oauth2.providers.GithubAuth
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.AuthHandler
import io.vertx.ext.web.handler.CookieHandler
import io.vertx.ext.web.handler.OAuth2AuthHandler
import io.vertx.ext.web.handler.SessionHandler
import io.vertx.ext.web.handler.UserSessionHandler
import mu.KLogger
import mu.KotlinLogging


/**
 *
 */
internal class GithubOAuthProvider(
    vertx: Vertx,
    private val oauth2: OAuth2Auth = GithubAuth.create(
        vertx,
        property("tock_github_oauth_client_id", "CLIENT_ID"),
        property("tock_github_oauth_secret_key", "SECRET_KEY")
    ).rbacHandler { user, authority, handler ->
        //TODO better
        handler.handle(Future.succeededFuture(true))
    }
) : SSOTockAuthProvider(vertx), OAuth2Auth by oauth2 {

    val logger: KLogger = KotlinLogging.logger {}
    private val executor: Executor get() = injector.provide()

    override fun createAuthHandler(verticle: WebVerticle): AuthHandler =
        OAuth2AuthHandler.create(this)

    override fun protectPaths(
        verticle: WebVerticle,
        pathsToProtect: Set<String>,
        cookieHandler: CookieHandler,
        sessionHandler: SessionHandler,
        userSessionHandler: UserSessionHandler
    ): AuthHandler {
        val authHandler =
            super.protectPaths(verticle, pathsToProtect, cookieHandler, sessionHandler, userSessionHandler)

        (authHandler as OAuth2AuthHandler).apply {
            setupCallback(verticle.router.get(callbackPath(verticle)))
        }

        verticle.router.route("/*").handler {
            val user = it.user()
            if (user != null && !user.principal().containsKey("login")) {
                executor.executeBlocking {
                    user.principal()
                        .put("login", RetrofitGithubClient.login(user.principal().getString("access_token")))
                    it.next()
                }
            } else {
                it.next()
            }
        }

        return authHandler
    }

    override fun excludedPaths(verticle: WebVerticle): Set<String> =
        super.excludedPaths(verticle) + callbackPath(verticle)

    private fun callbackPath(verticle: WebVerticle): String = "${verticle.basePath}/callback"

    override fun toTockUser(context: RoutingContext): TockUser {
        val user = context.user()
        val login = user.principal().getString("login")
        return TockUser(login, login, TockUserRole.values().map { r -> r.name }.toSet())
    }
}
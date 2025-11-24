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

import ai.tock.shared.jackson.mapper
import ai.tock.shared.vertx.WebVerticle
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.Cookie
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.AuthenticationHandler
import io.vertx.ext.web.handler.SessionHandler

/**
 *
 */
abstract class SSOTockAuthProvider(val vertx: Vertx) : TockAuthProvider {
    object AddSSOCookieHandler : Handler<RoutingContext> {
        override fun handle(c: RoutingContext) {
            val cookie = Cookie.cookie("tock-sso", "1")
            cookie.path = "/"
            // Don't set max age - it's a session cookie
            c.response().addCookie(cookie)
            c.next()
        }
    }

    override val sessionCookieName: String get() = "tock-sso-session"

    abstract fun createAuthHandler(verticle: WebVerticle): AuthenticationHandler

    override fun protectPaths(
        verticle: WebVerticle,
        pathsToProtect: Set<String>,
        sessionHandler: SessionHandler,
    ): AuthenticationHandler {
        val authHandler = createAuthHandler(verticle)
        with(verticle) {
            router.route().handler(WithExcludedPathHandler(defaultExcludedPaths(verticle), sessionHandler))
            router.route().handler(WithExcludedPathHandler(excludedPaths(verticle), authHandler))
            router.route().handler(AddSSOCookieHandler)

            router.get("$basePath/user").handler { it.response().end(mapper.writeValueAsString(toTockUser(it))) }
        }
        return authHandler
    }
}

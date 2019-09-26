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

import fr.vsct.tock.shared.security.TockUser
import fr.vsct.tock.shared.vertx.WebVerticle
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.AuthHandler
import io.vertx.ext.web.handler.SessionHandler

/**
 * Base interface for [AuthProvider] in Tock framework.
 */
interface TockAuthProvider : AuthProvider {

    /**
     * The tock session cookie name.
     */
    val sessionCookieName: String get() = "tock-session"

    /**
     * Paths to exclude from the [AuthProvider].
     */
    fun excludedPaths(verticle: WebVerticle): Set<Regex> =
        listOfNotNull(
            verticle.healthcheckPath?.toRegex(),
            ".*\\.(css|html|js|ico|woff2?|ttf|eot)".toRegex()
        ).toSet()

    /**
     * Protect paths for the specified verticle.
     * @return the [AuthHandler].
     */
    fun protectPaths(
        verticle: WebVerticle,
        pathsToProtect: Set<String>,
        sessionHandler: SessionHandler
    ): AuthHandler

    /**
     * Gets a [TockUser] from current vert.x state.
     */
    fun toTockUser(context: RoutingContext): TockUser = context.user() as TockUser
}
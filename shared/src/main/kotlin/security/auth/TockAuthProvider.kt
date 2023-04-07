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

package ai.tock.shared.security.auth

import ai.tock.shared.exception.ToRestException
import ai.tock.shared.security.TockUser
import ai.tock.shared.vertx.WebVerticle
import io.vertx.ext.auth.authentication.AuthenticationProvider
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.AuthenticationHandler
import io.vertx.ext.web.handler.SessionHandler

/**
 * Base interface for [AuthenticationProvider] in Tock framework.
 */
interface TockAuthProvider<E: ToRestException> : AuthenticationProvider {

    /**
     * The tock session cookie name.
     */
    val sessionCookieName: String get() = "tock-session"

    fun <E: ToRestException>defaultExcludedPaths(verticle: WebVerticle<E>): Set<Regex> = listOfNotNull(
        verticle.healthcheckPath?.toRegex(),
        verticle.livenesscheckPath?.toRegex(),
        verticle.readinesscheckPath?.toRegex(),
        ".*\\.(css|html|js|ico|woff2?|ttf|eot)".toRegex()
    ).toSet()

    /**
     * Paths to exclude from the [AuthProvider].
     */
    fun excludedPaths(verticle: WebVerticle<E>): Set<Regex> = defaultExcludedPaths(verticle)

    /**
     * Protect paths for the specified verticle.
     * @return the [AuthHandler].
     */
    fun protectPaths(
        verticle: WebVerticle<E>,
        pathsToProtect: Set<String>,
        sessionHandler: SessionHandler
    ): AuthenticationHandler

    /**
     * Gets a [TockUser] from current vert.x state.
     */
    fun toTockUser(context: RoutingContext): TockUser? = context.user() as? TockUser
}

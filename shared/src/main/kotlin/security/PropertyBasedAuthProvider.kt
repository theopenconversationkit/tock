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

package fr.vsct.tock.shared.security

import fr.vsct.tock.shared.defaultNamespace
import fr.vsct.tock.shared.listProperty
import fr.vsct.tock.shared.property
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.auth.User

/**
 * Simple [AuthProvider] used in dev mode.
 */
object PropertyBasedAuthProvider : AuthProvider {

    private val allRoles: Set<String> = TockUserRole.values().map { it.name }.toSet()

    private val users: List<String> = listProperty("tock_users", listOf(property("tock_user", "admin@app.com")))
    private val passwords: List<String> = listProperty("tock_passwords", listOf(property("tock_password", "password")))
    private val organizations: List<String> = listProperty("tock_organizations", listOf(defaultNamespace))
    private val roles: List<Set<String>> = listProperty("tock_roles", emptyList()).map { it.split("|").toSet() }

    override fun authenticate(authInfo: JsonObject, resultHandler: Handler<AsyncResult<User>>) {
        val username = authInfo.getString("username")
        val password = authInfo.getString("password")
        resultHandler.handle(
            users
                .indexOfFirst { it == username }
                .takeIf { it != -1 }
                ?.takeIf { passwords[it] == password }
                ?.let {
                    Future.succeededFuture<User>(
                        TockUser(
                            username,
                            organizations[it],
                            roles.getOrNull(it)?.takeIf { r -> r.size > 1 || r.firstOrNull()?.isBlank() == false }
                                    ?: allRoles))
                }
                    ?: Future.failedFuture<User>("invalid credentials")
        )
    }
}
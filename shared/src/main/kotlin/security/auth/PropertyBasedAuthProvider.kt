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

import com.fasterxml.jackson.module.kotlin.readValue
import fr.vsct.tock.shared.defaultNamespace
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.listProperty
import fr.vsct.tock.shared.property
import fr.vsct.tock.shared.security.TockUser
import fr.vsct.tock.shared.security.TockUserRole
import fr.vsct.tock.shared.security.TockUserRole.admin
import fr.vsct.tock.shared.security.TockUserRole.botUser
import fr.vsct.tock.shared.security.TockUserRole.nlpUser
import fr.vsct.tock.shared.security.TockUserRole.technicalAdmin
import fr.vsct.tock.shared.security.TockUserRole.values
import fr.vsct.tock.shared.security.auth.PropertyBasedAuthProvider.authenticate
import fr.vsct.tock.shared.vertx.WebVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.auth.User
import io.vertx.ext.web.handler.AuthHandler
import io.vertx.ext.web.handler.BasicAuthHandler
import io.vertx.ext.web.handler.CookieHandler
import io.vertx.ext.web.handler.SessionHandler
import io.vertx.ext.web.handler.UserSessionHandler

/**
 * Simple [AuthProvider] used in dev mode.
 */
internal object PropertyBasedAuthProvider : TockAuthProvider {

    private data class AuthenticateRequest(val email: String, val password: String)

    private data class AuthenticateResponse(
        val authenticated: Boolean,
        val email: String? = null,
        val organization: String? = null,
        val roles: List<TockUserRole> = emptyList()
    )

    private val allRoles: Set<String> = values().map { it.name }.toSet()

    private val users: List<String> = listProperty("tock_users", listOf(property("tock_user", "admin@app.com")))
    private val passwords: List<String> = listProperty("tock_passwords", listOf(property("tock_password", "password")))
    private val organizations: List<String> = listProperty("tock_organizations", listOf(defaultNamespace))
    private val roles: List<Set<String>> = listProperty("tock_roles", emptyList()).map { it.split("|").toSet() }

    override fun protectPaths(
        verticle: WebVerticle,
        pathsToProtect: Set<String>,
        cookieHandler: CookieHandler,
        sessionHandler: SessionHandler,
        userSessionHandler: UserSessionHandler
    ): AuthHandler {
        val authHandler = BasicAuthHandler.create(this)
        with(verticle) {
            (pathsToProtect + logoutPath + authenticatePath).forEach { protectedPath ->
                router.route(protectedPath).handler(cookieHandler)
                router.route(protectedPath).handler(sessionHandler)
                router.route(protectedPath).handler(userSessionHandler)
            }

            pathsToProtect.forEach { protectedPath ->
                router.route(protectedPath).handler(authHandler)
            }

            router.post(authenticatePath).handler { context ->
                val request = mapper.readValue<AuthenticateRequest>(context.bodyAsString)
                val authInfo = JsonObject().put("username", request.email).put("password", request.password)
                authenticate(authInfo) {
                    if (it.succeeded()) {
                        val user = it.result()
                        context.setUser(user)
                        context.isAuthorized(nlpUser) { nlpUserResult ->
                            context.isAuthorized(botUser) { botUserResult ->
                                context.isAuthorized(admin) { adminResult ->
                                    context.isAuthorized(technicalAdmin) { technicalAdminResult ->
                                        context.endJson(
                                            AuthenticateResponse(
                                                true,
                                                request.email,
                                                (user as TockUser).namespace,
                                                listOfNotNull(
                                                    if (nlpUserResult.result()) nlpUser else null,
                                                    if (botUserResult.result()) botUser else null,
                                                    if (adminResult.result()) admin else null,
                                                    if (technicalAdminResult.result()) technicalAdmin else null
                                                )
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        context.endJson(AuthenticateResponse(false))
                    }
                }
            }

            router.post(logoutPath).handler {
                it.clearUser()
                it.success()
            }
        }

        return authHandler
    }

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
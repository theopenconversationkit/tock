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
import ai.tock.shared.defaultNamespace
import ai.tock.shared.injector
import ai.tock.shared.jackson.mapper
import ai.tock.shared.listProperty
import ai.tock.shared.property
import ai.tock.shared.provide
import ai.tock.shared.security.TockUser
import ai.tock.shared.security.TockUserListener
import ai.tock.shared.security.TockUserRole
import ai.tock.shared.security.TockUserRole.admin
import ai.tock.shared.security.TockUserRole.botUser
import ai.tock.shared.security.TockUserRole.faqBotUser
import ai.tock.shared.security.TockUserRole.faqNlpUser
import ai.tock.shared.security.TockUserRole.nlpUser
import ai.tock.shared.security.TockUserRole.technicalAdmin
import ai.tock.shared.security.TockUserRole.values
import ai.tock.shared.vertx.WebVerticle
import com.fasterxml.jackson.module.kotlin.readValue
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.User
import io.vertx.ext.web.handler.AuthenticationHandler
import io.vertx.ext.web.handler.BasicAuthHandler
import io.vertx.ext.web.handler.SessionHandler
import mu.KotlinLogging

/**
 * Simple [AuthProvider] used in dev mode.
 */
internal object PropertyBasedAuthProvider : TockAuthProvider {

    private val logger = KotlinLogging.logger {}

    private data class AuthenticateRequest(val email: String, val password: String)

    private data class AuthenticateResponse(
        val authenticated: Boolean,
        val email: String? = null,
        val organization: String? = null,
        val roles: Set<TockUserRole> = emptySet()
    )

    private val allRoles: Set<String> = values().map { it.name }.toSet()

    private val users: List<String> = listProperty("tock_users", listOf(property("tock_user", "admin@app.com")))
    private val passwords: List<String> = listProperty("tock_passwords", listOf(property("tock_password", "password")))
    private val organizations: List<String> = listProperty("tock_organizations", listOf(defaultNamespace))
    private val roles: List<Set<String>> = listProperty("tock_roles", emptyList()).map { it.split("|").toSet() }

    private val executor: Executor get() = injector.provide()

    override fun protectPaths(
        verticle: WebVerticle,
        pathsToProtect: Set<String>,
        sessionHandler: SessionHandler
    ): AuthenticationHandler {
        val authHandler = BasicAuthHandler.create(this)
        with(verticle) {
            val excluded = excludedPaths(verticle)
            (pathsToProtect + logoutPath + authenticatePath + "$basePath/user").forEach { protectedPath ->
                router.route(protectedPath).handler(WithExcludedPathHandler(excluded, sessionHandler))
            }
            pathsToProtect.forEach { protectedPath ->
                router.route(protectedPath).handler(WithExcludedPathHandler(excluded, authHandler))
            }

            router.post(authenticatePath).handler { context ->
                val request = mapper.readValue<AuthenticateRequest>(context.body().asString())
                val authInfo = JsonObject().put("username", request.email).put("password", request.password)
                authenticate(authInfo) {
                    if (it.succeeded()) {
                        val user = it.result()
                        context.setUser(user)
                        context.isAuthorized(nlpUser) { nlpUserResult ->
                            context.isAuthorized((faqNlpUser)) { faqNlpUserResult ->
                                context.isAuthorized((faqBotUser)) { faqBotUserResult ->
                                    context.isAuthorized(botUser) { botUserResult ->
                                        context.isAuthorized(admin) { adminResult ->
                                            context.isAuthorized(technicalAdmin) { technicalAdminResult ->
                                                context.endJson(
                                                    // if any of the role is detected for the user
                                                    // add the role to the response
                                                    AuthenticateResponse(
                                                        true,
                                                        request.email,
                                                        (user as TockUser).namespace,
                                                        setOfNotNull(
                                                            if (nlpUserResult.result()) nlpUser else null,
                                                            if (faqNlpUserResult.result()) nlpUser else null,
                                                            if (faqBotUserResult.result()) botUser else null,
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
                            }
                        }
                    } else {
                        context.endJson(AuthenticateResponse(false))
                    }
                }
            }

            router.get("$basePath/user").handler {
                toTockUser(it)?.let { u ->
                    it.response().end(mapper.writeValueAsString(u))
                } ?: it.response().setStatusCode(401).end()
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
        users
            .indexOfFirst { it == username }
            .takeIf { it != -1 }
            ?.takeIf { passwords[it] == password }
            ?.also { index ->
                executor.executeBlocking {
                    val tockUser = injector.provide<TockUserListener>().registerUser(
                        TockUser(
                            username,
                            organizations[index],
                            roles.getOrNull(index)
                                ?.takeIf { role -> role.size > 1 || role.firstOrNull()?.isBlank() == false }
                                ?: allRoles
                        ),
                        true
                    )
                    resultHandler.handle(Future.succeededFuture(tockUser))
                }
            }
            ?: resultHandler.handle(Future.failedFuture<User>("invalid credentials"))
    }
}

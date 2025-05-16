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
import ai.tock.shared.booleanProperty
import ai.tock.shared.injector
import ai.tock.shared.intProperty
import ai.tock.shared.jackson.mapper
import ai.tock.shared.property
import ai.tock.shared.propertyExists
import ai.tock.shared.provide
import ai.tock.shared.security.TockUser
import ai.tock.shared.security.TockUserListener
import ai.tock.shared.vertx.WebVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.User
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.AuthenticationHandler
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.SessionHandler
import io.vertx.ext.web.sstore.LocalSessionStore
import mu.KotlinLogging
import org.pac4j.core.config.Config
import org.pac4j.vertx.auth.Pac4jAuthProvider
import org.pac4j.vertx.auth.Pac4jUser
import org.pac4j.vertx.context.session.VertxSessionStore
import org.pac4j.vertx.handler.impl.CallbackHandler
import org.pac4j.vertx.handler.impl.CallbackHandlerOptions
import org.pac4j.vertx.handler.impl.SecurityHandler
import org.pac4j.vertx.handler.impl.SecurityHandlerOptions

abstract class CASAuthProvider(vertx: Vertx) : SSOTockAuthProvider(vertx) {

    protected val sessionStore: VertxSessionStore
    private val executor: Executor get() = injector.provide()

    companion object {
        private val logger = KotlinLogging.logger {}
        val isJoinNamespace = booleanProperty("tock_cas_join_same_namespace_per_user", true)
    }

    // Either type specialized in HTTP (Successful when Http error code is 2XX)
    protected data class HttpResult<T>(
        val result: T?,
        val cause: Throwable?,
        val code: Int
    ) {
        fun succeeded(): Boolean {
            return code / 100 == 2
        }
    }

    init {
        val vertxSessionStore = LocalSessionStore.create(vertx)
        sessionStore = VertxSessionStore(vertxSessionStore)

        // If you use a CAS Authentication module, chances are that you also are behind corporate proxy (for test env)
        val isBehindProxy = propertyExists("tock_cas_auth_proxy_host") &&
                propertyExists("tock_cas_auth_proxy_port")

        if (isBehindProxy) {
            logger.debug { "HTTP Proxy enabled in CAS Auth module" }

            val proxyHost = property("tock_cas_auth_proxy_host", "127.0.0.1")
            val proxyPort = intProperty("tock_cas_auth_proxy_port", 3128)

            // NOTE: Because module is loaded through a separate JAR, this will not impact main Tock JAR
            System.setProperty("https.proxyHost", proxyHost)
            System.setProperty("https.proxyPort", String.format("%d", proxyPort))
        }
    }

    /**
     * Enable builtin pac4j-cas authorizers
     */
    open val enabledPacAuthorizers: String get() = "isAuthenticated" // TODO: Make csrf authorizer work


    /**
     * Handle failures in 'Pac4J user to Tock User' upgrade process
     */
    open fun handleUpgradeFailure(rc: RoutingContext, code: Int, cause: Throwable?) {
        if (null == cause) {
            logger.error("Caught by default CAS mapping exception handler: $code")
            rc.fail(code)
        } else {
            logger.error("Caught by default CAS mapping exception handler", cause)
            rc.fail(code, cause)
        }
    }

    /**
     * Get customer specific Pac4J Config
     */
    abstract fun getConfig(): Config


    /**
     * Read Toc Login from CAS user info
     */
    abstract fun readCasLogin(user: Pac4jUser): String

    /**
     * Read Toc Namespace from CAS user infos
     */
    abstract fun readRolesByNamespace(user: Pac4jUser): Map<String, Set<String>>

    override fun createAuthHandler(verticle: WebVerticle): AuthenticationHandler {
        val options: SecurityHandlerOptions = SecurityHandlerOptions().setClients("CasClient")
        options.authorizers = enabledPacAuthorizers

        return SecurityHandler(vertx, sessionStore, getConfig(), Pac4jAuthProvider(), options)
    }

    override fun authenticate(authInfo: JsonObject, resultHandler: Handler<AsyncResult<User>>) {
        // Actual authentication is performed by pac4j-cas not CASAuthProvider
        resultHandler.handle(Future.failedFuture("Unauthorized"))
    }

    protected fun registerTockUser(username: String, rolesByNamespace: Map<String, Set<String>>): TockUser {
        var user: TockUser? = null

        // NOTE: Currently registering same user multiple times is the only way to save multiple namespaces
        for ((namespace, roles) in rolesByNamespace) {
            user = injector.provide<TockUserListener>().registerUser(
                TockUser(username, namespace, roles), isJoinNamespace
            )
        }
        return user!! // !! is because user is already guaranteed to be not null
    }

    protected open fun upgradeToTockUser(user: Pac4jUser, resultHandler: Handler<HttpResult<TockUser>>) {
        try {
            val username = readCasLogin(user)
            val rolesByNamespace = readRolesByNamespace(user)
            logger.debug { "authenticate $username/$rolesByNamespace" }

            if (rolesByNamespace.keys.isEmpty()) {
                val error = IllegalStateException("No namespace found in registered user profil")
                logger.trace("Unable to upgrade to tock user", error)
                resultHandler.handle(HttpResult(null, error, 401))
                return
            }

            val tockUser = registerTockUser(username, rolesByNamespace)
            resultHandler.handle(HttpResult(tockUser, null, 200))
        } catch (exc: Exception) {
            logger.trace("Unable to upgrade to tock user", exc)
            resultHandler.handle(HttpResult(null, exc, 500))
        }
    }

    override fun protectPaths(
        verticle: WebVerticle,
        pathsToProtect: Set<String>,
        sessionHandler: SessionHandler
    ): AuthenticationHandler {
        val authHandler = super.protectPaths(verticle, pathsToProtect, sessionHandler)

        val excluded = excludedPaths(verticle)
        verticle.router.route("/*").handler(WithExcludedPathHandler(excluded, sessionHandler))
        verticle.router.route("/*").handler(WithExcludedPathHandler(excluded, authHandler))

        verticle.router.route("/*").handler(WithExcludedPathHandler(excluded) { rc ->
            val user = rc.user()
            if (user != null && user !is TockUser) {
                executor.executeBlocking {
                    upgradeToTockUser(user as Pac4jUser) {
                        if (it.succeeded()) {
                            rc.setUser(it.result)
                            rc.next()
                        } else {
                            rc.clearUser()
                            rc.session().destroy()
                            // note: below method has ability to redirect to custom error pages
                            logger.error("Upgrade to TockUser failed", it.cause)
                            handleUpgradeFailure(rc, it.code, it.cause)
                        }
                    }
                }
            } else {
                rc.next()
            }
        })

        with(verticle) {
            router.get("$basePath/user").handler {
                it.response().end(mapper.writeValueAsString(toTockUser(it)))
            }

            val callbackHandlerOptions = CallbackHandlerOptions().setMultiProfile(false)
            val callbackHandler = CallbackHandler(vertx, sessionStore, getConfig(), callbackHandlerOptions)

            val callbackPath = callbackPath(verticle)
            router.get(callbackPath).handler(sessionHandler)
            router.get(callbackPath).handler(callbackHandler)

            router.post(callbackPath).handler(sessionHandler)
            router.post(callbackPath).handler(BodyHandler.create().setMergeFormAttributes(true))
            router.post(callbackPath).handler(callbackHandler)
        }
        return authHandler
    }

    override fun excludedPaths(verticle: WebVerticle): Set<Regex> =
        super.excludedPaths(verticle) + callbackPath(verticle).toRegex()

    private fun callbackPath(verticle: WebVerticle): String = "${verticle.basePath}/callback"
}

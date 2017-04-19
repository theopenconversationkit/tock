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

package fr.vsct.tock.shared.vertx

import fr.vsct.tock.shared.devEnvironment
import fr.vsct.tock.shared.intProperty
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.jackson.readValue
import fr.vsct.tock.shared.property
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpMethod.DELETE
import io.vertx.core.http.HttpMethod.GET
import io.vertx.core.http.HttpMethod.OPTIONS
import io.vertx.core.http.HttpMethod.POST
import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.AbstractUser
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BasicAuthHandler
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CookieHandler
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.ext.web.handler.SessionHandler
import io.vertx.ext.web.handler.UserSessionHandler
import io.vertx.ext.web.sstore.LocalSessionStore
import mu.KLogger
import java.util.EnumSet

/**
 *
 */
abstract class WebVerticle(protected val logger: KLogger) : AbstractVerticle() {

    private data class AuthenticateRequest(val email: String, val password: String)

    private data class AuthenticateResponse(
            val authenticated: Boolean,
            val email: String? = null,
            val organization: String? = null)

    private data class BooleanResponse(val success: Boolean = true)

    class UserWithOrg(val user: String, val organization: String) : AbstractUser() {
        override fun doIsPermitted(permissionOrRole: String, handler: Handler<AsyncResult<Boolean>>) {
            handler.handle(Future.succeededFuture(true))
        }

        override fun setAuthProvider(authProvider: AuthProvider) {
            //do nothing
        }

        override fun principal(): JsonObject {
            return JsonObject().put("username", user)
        }
    }

    companion object {
        private val username = property("tock_user", "admin@vsct.fr")
        private val password = property("tock_password", "password")
        private val organization = property("tock_organization", "vsc")

        val authProvider: AuthProvider = AuthProvider { authInfo, handler ->
            val username = authInfo.getString("username")
            val password = authInfo.getString("password")
            if (username == Companion.username && password == Companion.password) {
                handler.handle(Future.succeededFuture(UserWithOrg(username, organization)))
            } else {
                handler.handle(Future.failedFuture("invalid credentials"))
            }
        }
    }


    protected val router: Router by lazy {
        Router.router(vertx)
    }

    protected val server: HttpServer by lazy {
        vertx.createHttpServer()
    }

    protected open val rootPath = ""

    protected open val authenticatePath = "/rest/authenticate"

    abstract fun configure()

    abstract fun healthcheck(): (RoutingContext) -> Unit

    override fun start(startFuture: Future<Void>) {
        router.route().handler(bodyHandler())
        addDevCorsHandler()
        authProvider()?.let {
            addAuth(it)
        }

        configure()

        router.get("/healthcheck").handler(healthcheck())

        startServer(startFuture)
    }

    private fun addAuth(authProvider: AuthProvider) {
        router.route().handler(CookieHandler.create())
        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx))
                .setSessionTimeout(6 * 60 * 60 * 1000 /*6h*/)
                .setNagHttps(devEnvironment)
                .setCookieHttpOnlyFlag(!devEnvironment)
                .setCookieSecureFlag(!devEnvironment)
                .setSessionCookieName("tock-session"))
        router.route().handler(UserSessionHandler.create(authProvider))
        val authHandler = BasicAuthHandler.create(authProvider)

        router.route("$rootPath/*").handler(authHandler)

        router.post(authenticatePath).handler { context ->
            val request = mapper.readValue(context.bodyAsString, AuthenticateRequest::class)
            val authInfo = JsonObject().put("username", request.email).put("password", request.password)
            authProvider.authenticate(authInfo, {
                if (it.succeeded()) {
                    val user = it.result()
                    context.setUser(user)
                    context.endJson(AuthenticateResponse(true, request.email, (user as UserWithOrg).organization))
                } else {
                    context.endJson(AuthenticateResponse(false))
                }
            })
        }

        router.post("$rootPath/logout").handler {
            it.clearUser()
            it.success()
        }
    }


    protected open fun authProvider(): AuthProvider? = null

    protected open fun startServer(startFuture: Future<Void>) {
        val verticleName = this::class.simpleName!!
        val port = intProperty("${verticleName.toLowerCase()}_port", 8080)
        server.requestHandler { r -> router.accept(r) }
                .listen(port,
                        { r ->
                            if (r.succeeded()) {
                                logger.info { "$verticleName started on port $port" }
                                startFuture.complete()
                            } else {
                                logger.error { "$verticleName NOT started on port $port" }
                                startFuture.fail(r.cause())
                            }
                        })
    }

    protected fun blocking(method: HttpMethod, path: String, handler: (RoutingContext) -> Unit) {
        router.route(method, "$rootPath$path")
                .blockingHandler({
                    context ->
                    try {
                        handler.invoke(context)
                    } catch (t: Throwable) {
                        if (t is RestException) {
                            context.response().statusMessage = t.message
                            context.fail(t.code)
                        } else {
                            logger.error(t) { t.message }
                            context.fail(t)
                        }
                    }
                }, false)
    }

    protected inline fun <reified I : Any, O> blockingWithBodyJson(method: HttpMethod, path: String, crossinline handler: (RoutingContext, I) -> O) {
        blocking(method, path, {
            context ->
            val input = context.readJson<I>()

            val result = handler.invoke(context, input)

            if (result == null) {
                context.response().statusCode = 204
            }
            context.endJson(result)
        })
    }

    protected fun <O> blockingWithoutBodyJson(method: HttpMethod, path: String, handler: (RoutingContext) -> O) {
        blocking(method, path, {
            context ->
            val result = handler.invoke(context)

            if (result == null) {
                context.response().statusCode = 204
            }
            context.endJson(result)
        })
    }

    protected fun <O> blockingJsonGet(path: String, handler: (RoutingContext) -> O) {
        blocking(GET, path, {
            context ->
            val result = handler.invoke(context)

            context.endJson(result)
        })
    }

    protected fun blockingPost(path: String, handler: (RoutingContext) -> Unit) {
        blocking(POST, path) { context ->
            handler.invoke(context)
            context.success()
        }
    }

    protected inline fun <reified I : Any, O> blockingJsonPost(path: String, crossinline handler: (RoutingContext, I) -> O) {
        blockingWithBodyJson<I, O>(POST, path, handler)
    }

    protected fun blockingDelete(path: String, handler: (RoutingContext) -> Unit) {
        blocking(DELETE, path) { context ->
            handler.invoke(context)
            context.success()
        }
    }

    protected fun blockingJsonDelete(path: String, handler: (RoutingContext) -> Boolean) {
        blockingWithoutBodyJson(DELETE, path, { BooleanResponse(handler.invoke(it)) })
    }

    override fun stop(stopFuture: Future<Void>?) {
        server.close { e -> logger.info { "${this::class.simpleName} stopped result : ${e.succeeded()}" } }
    }

    private fun addDevCorsHandler() {
        if (devEnvironment) {
            router.route().handler(corsHandler())
        }
    }

    protected fun corsHandler(): CorsHandler {
        return CorsHandler.create("*")
                .allowedMethods(EnumSet.of(GET, POST, DELETE, OPTIONS))
                .allowedHeaders(setOf("Access-Control-Allow-Origin", "Authorization", "Content-Type"))
                .allowCredentials(true)
    }

    protected fun bodyHandler(): BodyHandler {
        return BodyHandler.create().setBodyLimit(1000000L).setMergeFormAttributes(false)
    }

    inline fun <reified T : Any> RoutingContext.readJson(): T {
        return mapper.readValue(this.bodyAsString, T::class)
    }

    fun RoutingContext.success() {
        this.endJson(true)
    }

    fun RoutingContext.endJson(success: Boolean) {
        this.endJson(fr.vsct.tock.shared.vertx.WebVerticle.BooleanResponse(success))
    }

    fun RoutingContext.endJson(result: Any?) {
        this.response().endJson(result)
    }

    val RoutingContext.organization: String
        get() = (this.user() as UserWithOrg).organization

    fun HttpServerResponse.endJson(result: Any?) {
        val output = mapper.writeValueAsString(result)
        this.putHeader("content-type", "application/json; charset=utf-8").end(output)
    }

    protected fun unauthorized(): Nothing {
        throw UnauthorizedException()
    }
}
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

import com.fasterxml.jackson.module.kotlin.readValue
import fr.vsct.tock.shared.booleanProperty
import fr.vsct.tock.shared.defaultNamespace
import fr.vsct.tock.shared.devEnvironment
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.intProperty
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.listProperty
import fr.vsct.tock.shared.longProperty
import fr.vsct.tock.shared.property
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpMethod.DELETE
import io.vertx.core.http.HttpMethod.GET
import io.vertx.core.http.HttpMethod.POST
import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.AbstractUser
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.auth.User
import io.vertx.ext.web.FileUpload
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
import org.litote.kmongo.Id
import org.litote.kmongo.toId
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
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

    protected class UserWithOrg(val user: String, val organization: String) : AbstractUser() {
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
        private val users = listProperty("tock_users", listOf(property("tock_user", "admin@app.com")))
        private val passwords = listProperty("tock_passwords", listOf(property("tock_password", "password")))
        private val organizations = listProperty("tock_organizations", listOf(defaultNamespace))

        val authProvider: AuthProvider = AuthProvider { authInfo, handler ->
            val username = authInfo.getString("username")
            val password = authInfo.getString("password")
            handler.handle(
                    users
                            .indexOfFirst { it == username }
                            .takeIf { it != -1 }
                            ?.takeIf { passwords[it] == password }
                            ?.let { Future.succeededFuture<User>(UserWithOrg(username, organizations[it])) }
                            ?: Future.failedFuture<User>("invalid credentials")
            )
        }
    }


    protected val router: Router by lazy {
        Router.router(vertx)
    }

    protected val server: HttpServer by lazy {
        vertx.createHttpServer(
                HttpServerOptions()
                        .setCompressionSupported(verticleBooleanProperty("tock_vertx_compression_supported", true))
                        .setDecompressionSupported(verticleBooleanProperty("tock_vertx_compression_supported", true))
        )
    }

    protected open val rootPath: String = ""

    protected open val authenticatePath: String = "/rest/authenticate"

    private val verticleName: String = this::class.simpleName!!

    protected open fun protectedPath(): String = rootPath

    abstract fun configure()

    abstract fun healthcheck(): (RoutingContext) -> Unit

    override fun start(startFuture: Future<Void>) {
        router.route().handler(bodyHandler())
        addDevCorsHandler()
        authProvider()?.let {
            addAuth(it)
        }

        configure()

        router.get("$rootPath/healthcheck").handler(healthcheck())

        startServer(startFuture)
    }

    private fun addAuth(authProvider: AuthProvider) {
        val protectedPath = "${protectedPath()}/*"
        router.route(protectedPath).handler(CookieHandler.create())
        router.route(protectedPath).handler(SessionHandler.create(LocalSessionStore.create(vertx))
                .setSessionTimeout(6 * 60 * 60 * 1000 /*6h*/)
                .setNagHttps(devEnvironment)
                .setCookieHttpOnlyFlag(!devEnvironment)
                .setCookieSecureFlag(!devEnvironment)
                .setSessionCookieName("tock-session"))
        router.route(protectedPath).handler(UserSessionHandler.create(authProvider))
        val authHandler = BasicAuthHandler.create(authProvider)

        router.route(protectedPath).handler(authHandler)

        router.post(authenticatePath).handler { context ->
            val request = mapper.readValue<AuthenticateRequest>(context.bodyAsString)
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

        router.post("${protectedPath()}/logout").handler {
            it.clearUser()
            it.success()
        }
    }


    protected open fun authProvider(): AuthProvider? = null

    protected open fun startServer(startFuture: Future<Void>) {
        val port = verticleIntProperty("port", 8080)
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

    private fun verticleProperty(propertyName: String) = "${verticleName.toLowerCase()}_$propertyName"

    protected fun verticleIntProperty(propertyName: String, defaultValue: Int): Int = intProperty(verticleProperty(propertyName), defaultValue)

    protected fun verticleLongProperty(propertyName: String, defaultValue: Long): Long = longProperty(verticleProperty(propertyName), defaultValue)

    protected fun verticleBooleanProperty(propertyName: String, defaultValue: Boolean): Boolean = booleanProperty(verticleProperty(propertyName), defaultValue)

    protected fun verticleProperty(propertyName: String, defaultValue: String): String = property(verticleProperty(propertyName), defaultValue)

    protected fun blocking(method: HttpMethod, path: String, handler: (RoutingContext) -> Unit) {
        router.route(method, "$rootPath$path")
                .blockingHandler({ context ->
                    try {
                        handler.invoke(context)
                    } catch (t: Throwable) {
                        if (t is RestException) {
                            context.response().statusMessage = t.message
                            context.fail(t.code)
                        } else {
                            logger.error(t)
                            context.fail(t)
                        }
                    }
                }, false)
    }

    protected inline fun <reified I : Any, O> blockingWithBodyJson(
            method: HttpMethod,
            path: String,
            crossinline handler: (RoutingContext, I) -> O) {
        blocking(method, path, { context ->
            val input = context.readJson<I>()

            val result = handler.invoke(context, input)
            context.endJson(result)
        })
    }

    protected fun <O> blockingWithoutBodyJson(method: HttpMethod, path: String, handler: (RoutingContext) -> O) {
        blocking(method, path, { context ->
            val result = handler.invoke(context)
            context.endJson(result)
        })
    }

    protected fun <O> blockingJsonGet(path: String, handler: (RoutingContext) -> O) {
        blocking(GET, path, { context ->
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

    protected inline fun <reified F : Any, O> blockingUploadJsonPost(path: String, crossinline handler: (RoutingContext, F) -> O) {
        blocking(POST, path) { context ->
            val upload = context.fileUploads().first()
            val f = readJson<F>(upload)
            val result = handler.invoke(context, f)
            context.endJson(result)
        }
    }

    protected inline fun <O> blockingUploadPost(path: String, crossinline handler: (RoutingContext, String) -> O) {
        blocking(POST, path) { context ->
            val upload = context.fileUploads().first()
            val f = readString(upload)
            val result = handler.invoke(context, f)
            context.endJson(result)
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
        server.close { e -> logger.info { "$verticleName stopped result : ${e.succeeded()}" } }
    }

    private fun addDevCorsHandler() {
        if (devEnvironment && booleanProperty("tock_web_use_default_dev_cors_handler", true)) {
            router.route().handler(corsHandler("http://localhost:4200", true))
        }
    }

    protected fun corsHandler(
            origin: String = "*",
            allowCredentials: Boolean = false,
            allowedMethods: Set<HttpMethod> = EnumSet.of(GET, POST, DELETE),
            allowedHeaders: Set<String> = listOfNotNull("X-Requested-With", "Access-Control-Allow-Origin", if (allowCredentials) "Authorization" else null, "Content-Type").toSet()
    ): CorsHandler {
        return CorsHandler.create(origin)
                .allowedMethods(allowedMethods)
                .allowedHeaders(allowedHeaders)
                .allowCredentials(allowCredentials)
    }

    protected fun bodyHandler(): BodyHandler {
        return BodyHandler.create().setBodyLimit(verticleLongProperty("body_limit", 1000000L)).setMergeFormAttributes(false)
    }

    // extension methods ->

    inline fun <reified T : Any> RoutingContext.readJson(): T {
        return mapper.readValue<T>(this.bodyAsString)
    }

    inline fun <reified T : Any> readJson(upload: FileUpload): T {
        return mapper.readValue<T>(File(upload.uploadedFileName()))
    }

    fun readString(upload: FileUpload): String {
        return String(Files.readAllBytes(Paths.get(upload.uploadedFileName())), StandardCharsets.UTF_8)
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

    fun <T> RoutingContext.pathId(name: String): Id<T> = pathParam(name).toId()

    val RoutingContext.organization: String
        get() = (this.user() as UserWithOrg).organization

    fun HttpServerResponse.endJson(result: Any?) {
        if (result == null) {
            statusCode = 204
        }
        this.putHeader("content-type", "application/json; charset=utf-8")
        if (result == null) {
            end()
        } else {
            val output = mapper.writeValueAsString(result)
            end(output)
        }
    }

    fun unauthorized(): Nothing {
        throw UnauthorizedException()
    }

    fun notFound(): Nothing {
        throw NotFoundException()
    }
}
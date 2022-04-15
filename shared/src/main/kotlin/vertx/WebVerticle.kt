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

package ai.tock.shared.vertx

import ai.tock.shared.booleanProperty
import ai.tock.shared.devEnvironment
import ai.tock.shared.error
import ai.tock.shared.intProperty
import ai.tock.shared.jackson.mapper
import ai.tock.shared.longProperty
import ai.tock.shared.property
import ai.tock.shared.security.TockUser
import ai.tock.shared.security.TockUserRole
import ai.tock.shared.security.auth.AWSJWTAuthProvider
import ai.tock.shared.security.auth.CASAuthProvider
import ai.tock.shared.security.auth.GithubOAuthProvider
import ai.tock.shared.security.auth.OAuth2Provider
import ai.tock.shared.security.auth.PropertyBasedAuthProvider
import ai.tock.shared.security.auth.TockAuthProvider
import ai.tock.shared.security.auth.spi.CASAuthProviderFactory
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.fasterxml.jackson.module.kotlin.readValue
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpMethod.DELETE
import io.vertx.core.http.HttpMethod.GET
import io.vertx.core.http.HttpMethod.POST
import io.vertx.core.http.HttpMethod.PUT
import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.FileUpload
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.ext.web.handler.ErrorHandler
import io.vertx.ext.web.handler.SessionHandler
import io.vertx.ext.web.sstore.LocalSessionStore
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.EnumSet
import java.util.Locale
import java.util.ServiceLoader
import kotlin.LazyThreadSafetyMode.PUBLICATION
import mu.KLogger
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.toId

/**
 * Base class for web Tock [io.vertx.core.Verticle]s. Provides utility methods.
 */
abstract class WebVerticle : AbstractVerticle() {

    companion object {
        fun unauthorized(): Nothing = throw UnauthorizedException()

        fun notFound(): Nothing = throw NotFoundException()

        fun badRequest(message: String): Nothing = throw BadRequestException(message)

        /**
         * Default request logger does nothing.
         */
        val defaultRequestLogger: RequestLogger = object : RequestLogger {
            override fun log(context: RoutingContext, data: Any?, error: Boolean) {
                // do nothing
            }
        }

        private val tockErrorHandler: ErrorHandler by lazy(PUBLICATION) {
            ErrorHandler.create()
        }
    }

    open val logger: KLogger = KotlinLogging.logger {}

    private data class BooleanResponse(val success: Boolean = true)

    val router: Router by lazy {
        Router.router(sharedVertx).apply {
            errorHandler(404, defaultErrorHandler(404))
            errorHandler(405, defaultErrorHandler(405))
            errorHandler(406, defaultErrorHandler(406))
            errorHandler(415, defaultErrorHandler(415))
            errorHandler(400, defaultErrorHandler(400))
        }
    }

    protected val server: HttpServer by lazy {
        sharedVertx.createHttpServer(
            HttpServerOptions()
                .setCompressionSupported(verticleBooleanProperty("tock_vertx_compression_supported", true))
                .setDecompressionSupported(verticleBooleanProperty("tock_vertx_compression_supported", true))
        )
    }

    open val basePath: String = "/rest"

    protected open val rootPath: String = ""

    open val authenticatePath: String get() = "$basePath/authenticate"

    open val logoutPath: String get() = "$basePath/logout"

    private val verticleName: String = this::class.simpleName!!

    /**
     * If not null, add a [healthcheck()] for this verticle.
     */
    open val healthcheckPath: String? get() = verticleProperty("tock_vertx_healthcheck_path", "$rootPath/healthcheck")

    /**
     * If not null, add a [readiness()] for this verticle.
     */
    open val readinesscheckPath: String? get() = verticleProperty("tock_vertx_readinesscheck_path", "/health/readiness")

    /**
     * If not null, add a [liveness()] for this verticle.
     */
    open val livenesscheckPath: String? get() = verticleProperty("tock_vertx_livenesscheck_path", "/health/liveness")

    private val cachedAuthProvider: TockAuthProvider? by lazy(PUBLICATION) {
        authProvider()
    }

    protected open fun protectedPaths(): Set<String> = setOf(rootPath)

    abstract fun configure()

    open fun healthcheck(): (RoutingContext) -> Unit =
        if (booleanProperty("tock_detailed_healthcheck_enabled", false))
            detailedHealthcheck()
        else defaultHealthcheck()

    /**
     * Provide basic health information: mainly through HTTP status code
     */
    open fun defaultHealthcheck(): (RoutingContext) -> Unit = { it.response().end() }

    /**
     * Provide basic readiness information: indicates whether the container is ready to respond to requests
     */
    open fun readinesscheck(): (RoutingContext) -> Unit = { it.response().end() }

    /**
     * Provide basic liveness information: indicates whether the verticle is running
     */
    open fun livenesscheck(): (RoutingContext) -> Unit = healthcheck()

    /**
     * Provide enhanced information: HTTP response has JSON body with health status of resources
     */
    open fun detailedHealthcheck(): (RoutingContext) -> Unit = defaultHealthcheck()

    private fun loadCasAuthProvider(vertx: Vertx): CASAuthProvider? {
        var result: CASAuthProvider? = null
        val loader = ServiceLoader.load(CASAuthProviderFactory::class.java)

        val it = loader.iterator()
        if (it.hasNext()) {
            val casAuthProviderFactory = it.next()
            result = casAuthProviderFactory.getCasAuthProvider(vertx)
        } else {
            logger.warn { "No Custom CAS Auth provider found: Defaulting to property based auth" }
        }

        return result
    }

    override fun start(promise: Promise<Void>) {
        vertx.executeBlocking<Unit>(
            {
                try {
                    router.route().handler(bodyHandler())
                    addDevCorsHandler()
                    cachedAuthProvider?.also { p ->
                        addAuth(p)
                    }

                    healthcheckPath?.let { router.get(it).handler(healthcheck()) }
                    livenesscheckPath?.let { router.get(it).handler(livenesscheck()) }
                    readinesscheckPath?.let { router.get(it).handler(readinesscheck()) }
                    configure()

                    it.complete()
                } catch (t: MissingKotlinParameterException) {
                    logger.error(t)
                    it.fail(BadRequestException(t.message ?: ""))
                } catch (t: JsonProcessingException) {
                    logger.error(t)
                    it.fail(BadRequestException(t.message ?: ""))
                } catch (t: Throwable) {
                    logger.error(t)
                    it.fail(t)
                }
            },
            false,
            {
                if (it.succeeded()) {
                    startServer(promise)
                }
            }
        )
    }

    override fun stop(stopFuture: Future<Void>?) {
        server.close { e -> logger.info { "$verticleName stopped result : ${e.succeeded()}" } }
    }

    fun addAuth(
        authProvider: TockAuthProvider = defaultAuthProvider(),
        pathsToProtect: Set<String> = protectedPaths().map { "$it/*" }.toSet()
    ) {
        val https = !devEnvironment && booleanProperty("tock_https_env", true)
        val sessionHandler = SessionHandler.create(LocalSessionStore.create(vertx))
            .setSessionTimeout(6 * 60 * 60 * 1000 /*6h*/)
            .setNagHttps(https)
            .setCookieHttpOnlyFlag(https)
            .setCookieSecureFlag(https)
            .setSessionCookieName(authProvider.sessionCookieName)
            .setAuthProvider(authProvider)

        authProvider.protectPaths(this, pathsToProtect, sessionHandler)
    }

    /**
     * The auth provider provided by default.
     */
    protected open fun defaultAuthProvider(): TockAuthProvider =
        when {
            booleanProperty("tock_aws_jwt_enabled", false) -> AWSJWTAuthProvider(sharedVertx)
            booleanProperty("tock_github_oauth_enabled", false) -> GithubOAuthProvider(sharedVertx)
            booleanProperty("tock_oauth2_enabled", false) -> OAuth2Provider(sharedVertx)
            booleanProperty("tock_cas_auth_enabled", false) ->
                loadCasAuthProvider(sharedVertx) ?: PropertyBasedAuthProvider
            else -> PropertyBasedAuthProvider
        }

    /**
     * By default there is no auth provider - ie nothing is protected.
     */
    protected open fun authProvider(): TockAuthProvider? = null

    /**
     * The default role of a service.
     */
    open fun defaultRole(): TockUserRole? = null
    /**
     * The default roles of a service.
     */
    open fun defaultRoles(): Set<TockUserRole>? = defaultRole()?.let { setOf(defaultRole()!!) }

    /**
     * The default port of the verticle
     */
    protected open val defaultPort: Int = 8080

    protected open fun startServer(promise: Promise<Void>) {
        startServer(promise, verticleIntProperty("port", defaultPort))
    }

    protected open fun startServer(promise: Promise<Void>, port: Int) {
        server.requestHandler { r -> router.handle(r) }
            .listen(
                port
            ) { r ->
                if (r.succeeded()) {
                    logger.info { "$verticleName started on port $port" }
                    promise.complete()
                } else {
                    logger.error { "$verticleName NOT started on port $port" }
                    promise.fail(r.cause())
                }
            }
    }

    private fun verticleProperty(propertyName: String) = "${verticleName.toLowerCase()}_$propertyName"

    protected fun verticleIntProperty(propertyName: String, defaultValue: Int): Int =
        intProperty(verticleProperty(propertyName), defaultValue)

    protected fun verticleLongProperty(propertyName: String, defaultValue: Long): Long =
        longProperty(verticleProperty(propertyName), defaultValue)

    protected fun verticleBooleanProperty(propertyName: String, defaultValue: Boolean): Boolean =
        booleanProperty(verticleProperty(propertyName), defaultValue)

    protected fun verticleProperty(propertyName: String, defaultValue: String): String =
        property(verticleProperty(propertyName), defaultValue)

    protected fun register(
        method: HttpMethod,
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        basePath: String = rootPath,
        handler: (RoutingContext) -> Unit
    ) {

        router.route(method, "$basePath$path")
            .handler { context ->
                val user = context.user()
                if (user == null || roles.isNullOrEmpty()) {
                    handler.invoke(context)
                } else {
                    context.areAuthorized(roles) {
                        if (it.result() == true) {
                            handler.invoke(context)
                        } else {
                            context.fail(401)
                        }
                    }
                }
            }
    }

    fun blocking(
        method: HttpMethod,
        path: String,
        role: TockUserRole,
        basePath: String = rootPath,
        handler: (RoutingContext) -> Unit
    ) {
        blocking(method, path, setOf(role), basePath, handler)
    }

    fun blocking(
        method: HttpMethod,
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        basePath: String = rootPath,
        handler: (RoutingContext) -> Unit
    ) {
        register(method, path, roles, basePath) { it.executeBlocking(handler) }
    }

    fun RoutingContext.isAuthorized(
        role: TockUserRole,
        resultHandler: (AsyncResult<Boolean>) -> Unit
    ) = user()?.isAuthorized(role.name, resultHandler)
        ?: resultHandler.invoke(Future.failedFuture("No user set"))

    /**
     * Check the user has any authorized role
     *
     */
    private fun RoutingContext.areAuthorized(
        roles: Set<TockUserRole?>,
        resultHandler: (AsyncResult<Boolean>) -> Unit
    ) {
        val tockUser = user() as TockUser
        val tockUserRoles = tockUser.roles
        // if any of the role are in the profile then you can invoke the handler
        if (roles.any { tockUserRole -> tockUserRole?.name in tockUser.roles}){
            val aRole = roles.first { it?.name in tockUserRoles }?.name
                user()?.isAuthorized(aRole, resultHandler)
        } else {
            resultHandler.invoke(Future.failedFuture("Not authorized for user"))
        }
    }

    inline fun <reified I : Any, O> blockingWithBodyJson(
        method: HttpMethod,
        path: String,
        roles: Set<TockUserRole>?,
        logger: RequestLogger = defaultRequestLogger,
        crossinline handler: (RoutingContext, I) -> O
    ) {
        blocking(method, path, roles) { context ->
            var input: I? = null
            try {
                input = context.readJson()

                val result = handler.invoke(context, input)
                context.endJson(result)
                logger.log(context, input)
            } catch (t: Throwable) {
                if (t !is UnauthorizedException) {
                    logger.log(context, input, true)
                }
                throw t
            }
        }
    }

    private fun <O> blockingWithoutBodyJson(
        method: HttpMethod,
        path: String,
        roles: Set<TockUserRole>?,
        handler: (RoutingContext) -> O
    ) {
        blocking(method, path, roles) { context ->
            val result = handler.invoke(context)
            context.endJson(result)
        }
    }

    fun <O> blockingJsonGet(
        path: String,
        role: TockUserRole,
        handler: (RoutingContext) -> O
    ) {
        blockingJsonGet(path,setOf(role),handler)
    }

    fun <O> blockingJsonGet(
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        handler: (RoutingContext) -> O
    ) {
        blocking(GET, path, roles) { context ->
            val result = handler.invoke(context)
            context.endJson(result)
        }
    }

    protected fun blockingPost(
        path: String,
        role: TockUserRole,
        logger: RequestLogger = defaultRequestLogger,
        handler: (RoutingContext) -> Unit
    ) {
        blockingPost(path,setOf(role),logger,handler)
    }

    protected fun blockingPost(
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        logger: RequestLogger = defaultRequestLogger,
        handler: (RoutingContext) -> Unit
    ) {
        blocking(POST, path, roles) { context ->
            try {
                handler.invoke(context)
                context.success()
                logger.log(context, null)
            } catch (t: Throwable) {
                if (t !is UnauthorizedException) {
                    logger.log(context, null, true)
                }
                throw t
            }
        }
    }

    protected fun blockingGet(
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        basePath: String = rootPath,
        handler: (RoutingContext) -> String
    ) {
        blocking(GET, path, roles, basePath) { context ->
            context.response().end(handler.invoke(context))
        }
    }

    protected inline fun <reified F : Any, O> blockingUploadJsonPost(
        path: String,
        role: TockUserRole,
        logger: RequestLogger = defaultRequestLogger,
        crossinline handler: (RoutingContext, F) -> O
    ) {
        blockingUploadJsonPost(path, setOf(role),logger,handler)
    }

    protected inline fun <reified F : Any, O> blockingUploadJsonPost(
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        logger: RequestLogger = defaultRequestLogger,
        crossinline handler: (RoutingContext, F) -> O
    ) {
        blocking(POST, path, roles) { context ->
            val upload = context.fileUploads().first()
            var f: F? = null
            try {
                f = readJson(upload)
                val result = handler.invoke(context, f)
                context.endJson(result)
                logger.log(context, f)
            } catch (t: Throwable) {
                if (t !is UnauthorizedException) {
                    logger.log(context, f, true)
                }
                throw t
            }
        }
    }

    protected inline fun <O> blockingUploadPost(
        path: String,
        role: TockUserRole,
        logger: RequestLogger = defaultRequestLogger,
        crossinline handler: (RoutingContext, String) -> O
    ) {
        blockingUploadPost(path,setOf(role),logger,handler)
    }

    protected inline fun <O> blockingUploadPost(
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        logger: RequestLogger = defaultRequestLogger,
        crossinline handler: (RoutingContext, String) -> O
    ) {
        blocking(POST, path, roles) { context ->
            val upload = context.fileUploads().first()
            try {
                val f = readString(upload)
                val result = handler.invoke(context, f)
                context.endJson(result)
                logger.log(context, upload)
            } catch (t: Throwable) {
                if (t !is UnauthorizedException) {
                    logger.log(context, upload, true)
                }
                throw t
            }
        }
    }

    protected inline fun <O> blockingUploadBinaryPost(
        path: String,
        role: TockUserRole,
        crossinline handler: (RoutingContext, Pair<String, ByteArray>) -> O
    ) {
        blockingUploadBinaryPost(path,setOf(role), handler)
    }

    protected inline fun <O> blockingUploadBinaryPost(
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        crossinline handler: (RoutingContext, Pair<String, ByteArray>) -> O
    ) {
        blocking(POST, path, roles) { context ->
            val upload = context.fileUploads().first()
            val result = handler.invoke(context, upload.fileName() to readBytes(upload))
            context.endJson(result)
        }
    }

    inline fun <reified I : Any, O> blockingJsonPost(
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        logger: RequestLogger = defaultRequestLogger,
        crossinline handler: (RoutingContext, I) -> O
    ) {
        blockingWithBodyJson(POST, path, roles, logger, handler)
    }

    inline fun <reified I : Any, O> blockingJsonPost(
        path: String,
        role: TockUserRole,
        logger: RequestLogger = defaultRequestLogger,
        crossinline handler: (RoutingContext, I) -> O
    ) {
        blockingWithBodyJson(POST, path, setOf(role), logger, handler)
    }

    protected inline fun <reified I : Any, O> blockingJsonPut(
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        logger: RequestLogger = defaultRequestLogger,
        crossinline handler: (RoutingContext, I) -> O
    ) {
        blockingWithBodyJson(PUT, path, roles, logger, handler)
    }

    protected inline fun <reified I : Any, O> blockingJsonPut(
        path: String,
        role: TockUserRole? = defaultRole(),
        logger: RequestLogger = defaultRequestLogger,
        crossinline handler: (RoutingContext, I) -> O
    ) {
        blockingWithBodyJson(PUT, path, role?.let { setOf(role) }, logger, handler)
    }

    fun blockingDelete(
        path: String,
        role: TockUserRole,
        logger: RequestLogger = defaultRequestLogger,
        handler: (RoutingContext) -> Unit
    ) {
        blockingDelete(path,role?.let { setOf(role) },logger,handler)
    }

    fun blockingDelete(
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        logger: RequestLogger = defaultRequestLogger,
        handler: (RoutingContext) -> Unit
    ) {
        blocking(DELETE, path, roles) { context ->
            try {
                handler.invoke(context)
                logger.log(context, null)
            } catch (t: Throwable) {
                if (t !is UnauthorizedException) {
                    logger.log(context, null, false)
                }
                throw t
            }
            context.success()
        }
    }

    protected fun blockingJsonDelete(
        path: String,
        role: TockUserRole,
        logger: RequestLogger = defaultRequestLogger,
        handler: (RoutingContext) -> Boolean
    ) {
        blockingJsonDelete(path,setOf(role),logger,handler)
    }

    protected fun blockingJsonDelete(
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        logger: RequestLogger = defaultRequestLogger,
        handler: (RoutingContext) -> Boolean
    ) {
        blockingWithoutBodyJson(DELETE, path, roles) { context ->
            try {
                BooleanResponse(handler.invoke(context))
                logger.log(context, null)
            } catch (t: Throwable) {
                if (t !is UnauthorizedException) {
                    logger.log(context, null, false)
                }
                throw t
            }
        }
    }

    // non blocking methods
    protected inline fun <reified I : Any, O> withBodyJson(
        method: HttpMethod,
        path: String,
        roles: Set<TockUserRole>?,
        crossinline handler: (RoutingContext, I, Handler<O>) -> Unit
    ) {
        register(method, path, roles) { context ->
            try {
                val input = context.readJson<I>()
                handler.invoke(context, input, Handler { event -> context.endJson(event) })
            } catch (e: Throwable) {
                logger.error(e)
                context.fail(e)
            }
        }
    }

    protected inline fun <reified I : Any, O> jsonPost(
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        crossinline handler: (RoutingContext, I, Handler<O>) -> Unit
    ) {
        withBodyJson(POST, path, roles, handler)
    }

    // extension & utility methods

    protected open fun addDevCorsHandler() {
        if (useDefaultCorsHandler) {
            router.route().handler(
                corsHandler(
                    property("tock_web_use_default_cors_handler_url", defaultCorsOrigin),
                    booleanProperty("tock_web_use_default_cors_handler_with_credentials", defaultCorsWithCredentials)
                )
            )
        }
    }

    private val useDefaultCorsHandler: Boolean = booleanProperty("tock_web_use_default_cors_handler", devEnvironment)

    /**
     * Default cors origin (if tock_web_use_default_cors_handler property is set to true).
     */
    protected open val defaultCorsOrigin: String = "http://localhost:4200"

    /**
     * By default, allow credentials for cors origin (if tock_web_use_default_cors_handler property is set to true).
     */
    protected open val defaultCorsWithCredentials: Boolean = true

    fun corsHandler(
        origin: String = "*",
        allowCredentials: Boolean = false,
        allowedMethods: Set<HttpMethod> = EnumSet.of(GET, POST, PUT, DELETE),
        allowedHeaders: Set<String> = listOfNotNull(
            "X-Requested-With",
            "Access-Control-Allow-Origin",
            if (allowCredentials) "Authorization" else null,
            "Content-Type"
        ).toSet()
    ): CorsHandler {
        return CorsHandler.create(origin)
            .allowedMethods(allowedMethods)
            .allowedHeaders(allowedHeaders)
            .allowCredentials(allowCredentials)
    }

    protected fun bodyHandler(): BodyHandler {
        return BodyHandler.create().setBodyLimit(verticleLongProperty("body_limit", 1000000L))
            .setMergeFormAttributes(false)
    }

    inline fun <reified T : Any> RoutingContext.readJson(): T {
        return mapper.readValue(this.bodyAsString)
    }

    inline fun <reified T : Any> readJson(upload: FileUpload): T {
        return mapper.readValue(File(upload.uploadedFileName()))
    }

    fun readBytes(upload: FileUpload): ByteArray = Files.readAllBytes(Paths.get(upload.uploadedFileName()))

    fun readString(upload: FileUpload): String {
        return String(readBytes(upload), StandardCharsets.UTF_8)
    }

    /**
     * Execute blocking code using [Vertx.executeBlocking].
     */
    protected fun RoutingContext.executeBlocking(handler: (RoutingContext) -> Unit) {
        sharedVertx.executeBlocking<Unit>(
            {
                try {
                    handler.invoke(this)
                    it.tryComplete()
                } catch (t: Throwable) {
                    it.tryFail(t)
                }
            },
            false,
            {
                if (it.failed()) {
                    it.cause().apply {
                        when {
                            this is RestException -> {
                                response().statusMessage = message
                                fail(code)
                            }
                            this != null -> {
                                logger.error(this)
                                fail(this)
                            }
                            else -> {
                                logger.error { "unknown error" }
                                fail(500)
                            }
                        }
                    }
                }
            }
        )
    }

    fun RoutingContext.success() {
        this.endJson(true)
    }

    fun RoutingContext.endJson(success: Boolean) {
        this.endJson(BooleanResponse(success))
    }

    fun RoutingContext.endJson(result: Any?) {
        if (result is Boolean) {
            endJson(result)
        } else {
            this.response().endJson(result)
        }
    }

    fun RoutingContext.path(name: String): String = pathParam(name)

    fun RoutingContext.pathToLocale(name: String): Locale = Locale.forLanguageTag(path(name))

    fun <T> RoutingContext.pathId(name: String): Id<T> = path(name).toId()

    fun RoutingContext.firstQueryParam(name: String): String? = request().getParam(name)

    fun <T> RoutingContext.queryId(name: String): Id<T>? = firstQueryParam(name)?.toId()

    val RoutingContext.organization: String
        get() = user?.namespace ?: "none"

    val RoutingContext.user: TockUser?
        get() = cachedAuthProvider?.toTockUser(this)

    val RoutingContext.userLogin: String
        get() = user?.user ?: error("no user in session")

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

    /**
     * The error handler for match failures.
     * See https://vertx.io/docs/vertx-web/java/#_route_match_failures
     */
    open fun defaultErrorHandler(statusCode: Int): Handler<RoutingContext> = Handler<RoutingContext> { event ->
        logger.info { "page not served ($statusCode): ${event.request().path()}" }
        tockErrorHandler.handle(event)
    }
}

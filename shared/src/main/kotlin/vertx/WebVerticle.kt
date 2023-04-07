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

import ai.tock.shared.*
import ai.tock.shared.exception.ToRestException
import ai.tock.shared.exception.rest.*
import ai.tock.shared.jackson.mapper
import ai.tock.shared.security.TockUser
import ai.tock.shared.security.TockUserRole
import ai.tock.shared.security.auth.*
import ai.tock.shared.security.auth.spi.CASAuthProviderFactory
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.fasterxml.jackson.module.kotlin.readValue
import io.vertx.core.*
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpMethod.*
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
import mu.KLogger
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.toId
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.LazyThreadSafetyMode.PUBLICATION

/**
 * Base class for web Tock [io.vertx.core.Verticle]s. Provides utility methods.
 */
abstract class WebVerticle<E : ToRestException> : AbstractVerticle() {

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
            ErrorHandler.create(vertx)
        }
    }

    open val logger: KLogger = KotlinLogging.logger {}

    private data class BooleanResponse(val success: Boolean = true)

    val router: Router by lazy {
        Router.router(sharedVertx).apply {
            errorHandler(400, defaultErrorHandler(400))
            errorHandler(404, defaultErrorHandler(404))
            errorHandler(405, defaultErrorHandler(405))
            errorHandler(406, defaultErrorHandler(406))
            errorHandler(409, defaultErrorHandler(409))
            errorHandler(415, defaultErrorHandler(415))
            errorHandler(500, defaultErrorHandler(500))
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

    open val rootPath: String = ""

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

    private val cachedAuthProvider: TockAuthProvider<E>? by lazy(PUBLICATION) {
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

    private fun loadCasAuthProvider(vertx: Vertx): CASAuthProvider<E>? {
        var result: CASAuthProvider<E>? = null
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
        // Handle server started event by emitting an eventbus message to address 'server.started'
        promise.future()
            .onComplete {
                vertx.eventBus().request<Void>(ServerStatus.SERVER_STARTED, it.succeeded())
            }

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

    override fun stop(stopFuture: Promise<Void>?) {
        server.close { e -> logger.info { "$verticleName stopped result : ${e.succeeded()}" } }
    }

    fun addAuth(
        authProvider: TockAuthProvider<E> = defaultAuthProvider(),
        pathsToProtect: MutableSet<String> = protectedPaths().map { "$it/*" }.toMutableSet()
    ) {
        pathsToProtect.addAll(protectedPaths())
        val https = !devEnvironment && booleanProperty("tock_https_env", true)
        val sessionHandler = SessionHandler.create(LocalSessionStore.create(vertx))
            .setSessionTimeout(6 * 60 * 60 * 1000 /*6h*/)
            .setNagHttps(https)
            .setCookieHttpOnlyFlag(https)
            .setCookieSecureFlag(https)
            .setSessionCookieName(authProvider.sessionCookieName)

        authProvider.protectPaths(this, pathsToProtect, sessionHandler)
    }

    /**
     * The auth provider provided by default.
     */
    protected open fun defaultAuthProvider(): TockAuthProvider<E> =
        when {
            booleanProperty("tock_github_oauth_enabled", false) -> GithubOAuthProvider(sharedVertx)
            booleanProperty("tock_oauth2_enabled", false) -> OAuth2Provider(sharedVertx)
            booleanProperty("tock_cas_auth_enabled", false) ->
                loadCasAuthProvider(sharedVertx) ?: PropertyBasedAuthProvider()

            else -> PropertyBasedAuthProvider()
        }

    /**
     * By default there is no auth provider - ie nothing is protected.
     */
    protected open fun authProvider(): TockAuthProvider<E>? = null

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

    private fun verticleProperty(propertyName: String) = "${verticleName.lowercase()}_$propertyName"

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
        handler: RequestHandler<Unit, E>
    ) {
        blocking(method, path, setOf(role), basePath, handler)
    }

    fun blocking(
        method: HttpMethod,
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        basePath: String = rootPath,
        handler: RequestHandler<Unit, E>
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
        if (roles.any { tockUserRole -> tockUserRole?.name in tockUser.roles }) {
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
        basePath: String = rootPath,
        crossinline handler: BiRequestHandler<I, O, E>
    ) {
        blocking(method, path, roles, basePath) { context ->
            with(context.readJson() as I) {
                handler
                    .invoke(context, this)
                    .map(
                        successMapper = {
                            context.endJson(it).also { logger.log(context, this) }
                        },
                        handleFailure = {
                            if (it !is UnauthorizedException) {
                                logger.log(context, this, true)
                            }
                        })
            }
        }
    }

    private fun <O> blockingWithoutBodyJson(
        method: HttpMethod,
        path: String,
        roles: Set<TockUserRole>?,
        basePath: String = rootPath,
        handler: RequestHandler<O, E>
    ) {
        blocking(method, path, roles, basePath) { context ->
            handler.invoke(context).map { context.endJson(it) }
        }
    }

    fun <O> blockingJsonGet(
        path: String,
        role: TockUserRole,
        basePath: String = rootPath,
        handler: RequestHandler<O, E>
    ) {
        blockingJsonGet(path = path, roles = setOf(role), basePath = basePath, handler = handler)
    }

    fun <O> blockingJsonGet(
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        basePath: String = rootPath,
        handler: RequestHandler<O, E>
    ) {
        blocking(GET, path, roles, basePath) { context ->
            handler.invoke(context).map { context.endJson(it) }
        }
    }

    protected fun blockingPostEmptyResponse(
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        logger: RequestLogger = defaultRequestLogger,
        basePath: String = rootPath,
        handler: RequestHandler<Unit, E>
    ) {
        blockingPost(path, roles, logger, basePath, success = successEmpty, handler)
    }

    protected fun blockingPost(
        path: String,
        role: TockUserRole,
        logger: RequestLogger = defaultRequestLogger,
        basePath: String = rootPath,
        handler: RequestHandler<Unit, E>
    ) {
        blockingPost(path = path, roles = setOf(role), logger = logger, basePath = basePath, handler = handler)
    }

    protected fun blockingPost(
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        logger: RequestLogger = defaultRequestLogger,
        basePath: String = rootPath,
        success: (RoutingContext) -> Unit = successTrue,
        handler: RequestHandler<Unit, E>
    ) {
        blocking(POST, path, roles, basePath) { context ->
            handler
                .invoke(context)
                .map(
                    successMapper = { success.invoke(context).also { logger.log(context, null) } },
                    handleFailure = { handleUnauthorizedException(it, logger, context) }
                )
        }
    }

    protected fun blockingGet(
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        basePath: String = rootPath,
        handler: RequestHandler<String, E>
    ) {
        blocking(GET, path, roles, basePath) { context ->
            handler.invoke(context).map { context.response().end(it) }
        }
    }

    inline fun <reified F : Any, O> blockingUploadJsonPost(
        path: String,
        role: TockUserRole,
        logger: RequestLogger = defaultRequestLogger,
        basePath: String = rootPath,
        crossinline handler: BiRequestHandler<F, O, E>
    ) {
        blockingUploadJsonPost(path, setOf(role), logger, basePath, handler)
    }

    inline fun <reified F : Any, O> blockingUploadJsonPost(
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        logger: RequestLogger = defaultRequestLogger,
        basePath: String = rootPath,
        crossinline handler: BiRequestHandler<F, O, E>
    ) {
        blocking(POST, path, roles, basePath) { context ->
            with(readJson(context.fileUploads().first()) as F) {
                handler
                    .invoke(context, this)
                    .map(
                        successMapper = { context.endJson(it).also { logger.log(context, this) } },
                        handleFailure = { handleUnauthorizedException(it, logger, context) }
                    )
            }
        }
    }

    protected inline fun <O> blockingUploadPost(
        path: String,
        role: TockUserRole,
        logger: RequestLogger = defaultRequestLogger,
        basePath: String = rootPath,
        crossinline handler: BiRequestHandler<String, O, E>
    ) {
        blockingUploadPost(path, setOf(role), logger, basePath, handler)
    }

    protected inline fun <O> blockingUploadPost(
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        logger: RequestLogger = defaultRequestLogger,
        basePath: String = rootPath,
        crossinline handler: BiRequestHandler<String, O, E>
    ) {
        blocking(POST, path, roles, basePath) { context ->
            with(readString(context.fileUploads().first())) {
                handler
                    .invoke(context, this)
                    .map(
                        successMapper = { context.endJson(it).also { logger.log(context, this) } },
                        handleFailure = { handleUnauthorizedException(it, logger, context) }
                    )
            }
        }
    }

    protected inline fun <O> blockingUploadBinaryPost(
        path: String,
        role: TockUserRole,
        crossinline handler: BiRequestHandler<Pair<String, ByteArray>, O, E>
    ) {
        blockingUploadBinaryPost(path, setOf(role), handler)
    }

    protected inline fun <O> blockingUploadBinaryPost(
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        crossinline handler: BiRequestHandler<Pair<String, ByteArray>, O, E>
    ) {
        blocking(POST, path, roles) { context ->
            val upload = context.fileUploads().first()
            handler
                .invoke(context, upload.fileName() to readBytes(upload))
                .map { context.endJson(it) }
        }
    }

    inline fun <reified I : Any, O> blockingJsonPost(
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        logger: RequestLogger = defaultRequestLogger,
        basePath: String = rootPath,
        crossinline handler: BiRequestHandler<I, O, E>
    ) {
        blockingWithBodyJson(POST, path, roles, logger, basePath, handler)
    }

    inline fun <reified I : Any, O> blockingJsonPost(
        path: String,
        role: TockUserRole,
        logger: RequestLogger = defaultRequestLogger,
        basePath: String = rootPath,
        crossinline handler: BiRequestHandler<I, O, E>
    ) {
        blockingWithBodyJson(POST, path, setOf(role), logger, basePath, handler)
    }

    inline fun <reified I : Any, O> blockingJsonPut(
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        logger: RequestLogger = defaultRequestLogger,
        basePath: String = rootPath,
        crossinline handler: BiRequestHandler<I, O, E>
    ) {
        blockingWithBodyJson(PUT, path, roles, logger, basePath, handler)
    }

    protected inline fun <reified I : Any, O> blockingJsonPut(
        path: String,
        role: TockUserRole,
        logger: RequestLogger = defaultRequestLogger,
        basePath: String = rootPath,
        crossinline handler: BiRequestHandler<I, O, E>
    ) {
        blockingWithBodyJson(PUT, path, setOf(role), logger, basePath, handler)
    }

    fun blockingDeleteEmptyResponse(
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        logger: RequestLogger = defaultRequestLogger,
        basePath: String = rootPath,
        handler: RequestHandler<Unit, E>
    ) {
        blockingDelete(path, roles, logger, basePath, successEmpty, handler)
    }

    fun blockingDelete(
        path: String,
        role: TockUserRole,
        logger: RequestLogger = defaultRequestLogger,
        basePath: String = rootPath,
        handler: RequestHandler<Unit, E>
    ) {
        blockingDelete(path = path, roles = setOf(role), logger = logger, basePath = basePath, handler = handler)
    }

    fun blockingDelete(
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        logger: RequestLogger = defaultRequestLogger,
        basePath: String = rootPath,
        success: (RoutingContext) -> Unit = successTrue,
        handler: RequestHandler<Unit, E>
    ) {
        blocking(DELETE, path, roles, basePath) { context ->
            handler
                .invoke(context)
                .map(
                    successMapper = { success.invoke(context).also { logger.log(context, null) } },
                    handleFailure = { handleUnauthorizedException(it, logger, context) }
                )
        }
    }

    fun handleUnauthorizedException(
        error: E,
        logger: RequestLogger,
        context: RoutingContext
    ) {
        if (error !is UnauthorizedException) {
            logger.log(context, null, true)
        }
    }

    protected fun blockingJsonDelete(
        path: String,
        role: TockUserRole,
        logger: RequestLogger = defaultRequestLogger,
        basePath: String = rootPath,
        handler: RequestHandler<Boolean, E>
    ) {
        blockingJsonDelete(path, setOf(role), logger, basePath, handler)
    }

    fun blockingJsonDelete(
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        logger: RequestLogger = defaultRequestLogger,
        basePath: String = rootPath,
        handler: RequestHandler<Boolean, E>
    ) {
        blockingWithoutBodyJson(DELETE, path, roles, basePath) { context ->
            handler
                .invoke(context)
                .map(
                    successMapper = { BooleanResponse(it).also { logger.log(context, null) } },
                    handleFailure = { handleUnauthorizedException(it, logger, context) }
                )
        }
    }

    // non blocking methods
    protected inline fun <reified I : Any, O> withBodyJson(
        method: HttpMethod,
        path: String,
        roles: Set<TockUserRole>?,
        crossinline handler: TriRequestHandler<I, Handler<O>, Unit, E>
    ) {
        register(method, path, roles) { context ->
            val input = context.readJson<I>()
            handler.invoke(context, input, Handler { event -> context.endJson(event) })
                .mapToSuccessUnit(handleFailure = {
                    logger.error(it)
                    context.fail(it)
                })
        }
    }

    protected inline fun <reified I : Any, O> jsonPost(
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        crossinline handler: TriRequestHandler<I, Handler<O>, Unit, E>
    ) {
        withBodyJson(POST, path, roles, handler)
    }

    // extension & utility methods

    protected open fun addDevCorsHandler() {
        if (useDefaultCorsHandler) {
            router.route().handler(
                corsHandler(
                    property(
                        "tock_web_use_default_cors_handler_url",
                        defaultCorsOrigin
                    ).run { if (this == "*") emptyList() else split("|") },
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
        allowedMethods: Set<HttpMethod> = setOf(GET, POST, PUT, DELETE),
        allowedHeaders: Set<String> = listOfNotNull(
            "X-Requested-With",
            "Access-Control-Allow-Origin",
            if (allowCredentials) "Authorization" else null,
            "Content-Type"
        ).toSet()
    ): CorsHandler =
        corsHandler(
            if (origin == "*") emptyList() else listOf(origin),
            allowCredentials,
            allowedMethods,
            allowedHeaders
        )

    fun corsHandler(
        origins: List<String> = emptyList(),
        allowCredentials: Boolean = false,
        allowedMethods: Set<HttpMethod> = setOf(GET, POST, PUT, DELETE),
        allowedHeaders: Set<String> = listOfNotNull(
            "X-Requested-With",
            "Access-Control-Allow-Origin",
            if (allowCredentials) "Authorization" else null,
            "Content-Type"
        ).toSet()
    ): CorsHandler =
        (if (origins.isEmpty()) CorsHandler.create("*") else CorsHandler.create().addOrigins(origins))
            .allowedMethods(allowedMethods)
            .allowedHeaders(allowedHeaders)
            .allowCredentials(allowCredentials)

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
    protected fun <T : ToRestException> RoutingContext.executeBlocking(
        handler: RequestHandler<Unit, T>
    ) {

        sharedVertx.executeBlocking<Unit>(
            {
                handler
                    .invoke(this)
                    .map(
                        successMapper = { _ -> it.tryComplete() },
                        handleFailure = { error -> it.tryFail(error.toRestException()) },
                    )
            },
            false,
            {
                if (it.failed()) {
                    it.cause().apply {
                        when {
                            this is RestException -> {
                                response().statusCode = httpResponseStatus.code()
                                response().statusMessage = message
                                response().endJson(httpResponseBody)
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

    private val successEmpty: RoutingContext.() -> Unit = {
        this.successEmpty()
    }

    fun RoutingContext.successEmpty() {
        this.endJson(null)
    }

    private val successTrue: RoutingContext.() -> Unit = {
        this.success()
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

    private fun HttpServerResponse.endJson(result: Any?) {
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
        logger.error { "Error  $statusCode: ${event.request().path()}" }
        tockErrorHandler.handle(event)
    }
}

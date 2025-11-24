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

package ai.tock.shared.vertx

import ai.tock.shared.booleanProperty
import ai.tock.shared.devEnvironment
import ai.tock.shared.error
import ai.tock.shared.exception.rest.BadRequestException
import ai.tock.shared.exception.rest.NotFoundException
import ai.tock.shared.exception.rest.RestException
import ai.tock.shared.exception.rest.UnauthorizedException
import ai.tock.shared.intProperty
import ai.tock.shared.jackson.mapper
import ai.tock.shared.listProperty
import ai.tock.shared.longProperty
import ai.tock.shared.property
import ai.tock.shared.security.TockUser
import ai.tock.shared.security.TockUserRole
import ai.tock.shared.security.auth.CASAuthProvider
import ai.tock.shared.security.auth.GithubOAuthProvider
import ai.tock.shared.security.auth.KeycloakOAuth2Provider
import ai.tock.shared.security.auth.OAuth2Provider
import ai.tock.shared.security.auth.PropertyBasedAuthProvider
import ai.tock.shared.security.auth.TockAuthProvider
import ai.tock.shared.security.auth.spi.CASAuthProviderFactory
import com.fasterxml.jackson.core.JsonProcessingException
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
import mu.KLogger
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.toId
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Locale
import java.util.ServiceLoader
import kotlin.LazyThreadSafetyMode.PUBLICATION

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
        val defaultRequestLogger: RequestLogger =
            object : RequestLogger {
                override fun log(
                    context: RoutingContext,
                    data: Any?,
                    error: Boolean,
                ) {
                    // do nothing
                }
            }

        private val tockErrorHandler: ErrorHandler by lazy(PUBLICATION) {
            ErrorHandler.create(vertx)
        }

        private val fileUploadDirectory = property("tock_file_upload_directory", "file-uploads")
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
                .setDecompressionSupported(verticleBooleanProperty("tock_vertx_compression_supported", true)),
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

    private val cachedAuthProvider: TockAuthProvider? by lazy(PUBLICATION) {
        authProvider()
    }

    protected open fun protectedPaths(): Set<String> = setOf(rootPath)

    abstract fun configure()

    open fun healthcheck(): (RoutingContext) -> Unit =
        if (booleanProperty("tock_detailed_healthcheck_enabled", false)) {
            detailedHealthcheck()
        } else {
            defaultHealthcheck()
        }

    /**
     * Provide basic health information: mainly through HTTP status code
     */
    open fun defaultHealthcheck(): (RoutingContext) -> Unit = { rc -> rc.response().end() }

    /**
     * Provide basic readiness information: indicates whether the container is ready to respond to requests
     */
    open fun readinesscheck(): (RoutingContext) -> Unit = { rc -> rc.response().end() }

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
        // Handle server started event by emitting an eventbus message to address 'server.started'
        promise.future()
            .onComplete { ar ->
                vertx.eventBus().publish(ServerStatus.SERVER_STARTED, ar.succeeded())
            }

        vertx.blocking<Unit>(
            { p: Promise<Unit> ->
                try {
                    router.route().handler(bodyHandler())
                    addDevCorsHandler()
                    cachedAuthProvider?.also { pvd -> addAuth(pvd) }

                    healthcheckPath?.let { path -> router.get(path).handler(healthcheck()) }
                    livenesscheckPath?.let { path -> router.get(path).handler(livenesscheck()) }
                    readinesscheckPath?.let { path -> router.get(path).handler(readinesscheck()) }

                    configure()
                    p.complete()
                } catch (t: JsonProcessingException) {
                    logger.error(t)
                    p.fail(BadRequestException(t.message ?: ""))
                } catch (t: Throwable) {
                    logger.error(t)
                    p.fail(t)
                } finally {
                    p.tryFail("call not completed")
                }
            },
            { ar ->
                if (ar.succeeded()) {
                    startServer(promise)
                } else {
                    promise.fail(ar.cause())
                }
            },
        )
    }

    override fun stop() {
        server.close()
            .onComplete { ar -> logger.info { "$verticleName stopped result : ${ar.succeeded()}" } }
    }

    fun addAuth(
        authProvider: TockAuthProvider = defaultAuthProvider(),
        pathsToProtect: MutableSet<String> = protectedPaths().map { "$it/*" }.toMutableSet(),
    ) {
        pathsToProtect.addAll(protectedPaths())
        val https = !devEnvironment && booleanProperty("tock_https_env", true)
        val sessionHandler =
            SessionHandler.create(LocalSessionStore.create(vertx))
                .setSessionTimeout(6 * 60 * 60 * 1000) // 6h
                .setNagHttps(https)
                .setCookieHttpOnlyFlag(https)
                .setCookieSecureFlag(https)
                .setSessionCookieName(authProvider.sessionCookieName)

        authProvider.protectPaths(this, pathsToProtect, sessionHandler)
    }

    /**
     * The auth provider provided by default.
     */
    protected open fun defaultAuthProvider(): TockAuthProvider =
        when {
            booleanProperty("tock_github_oauth_enabled", false) -> GithubOAuthProvider(sharedVertx)
            booleanProperty("tock_oauth2_enabled", false) -> OAuth2Provider(sharedVertx)
            booleanProperty("tock_keycloak_enabled", false) -> KeycloakOAuth2Provider(sharedVertx)
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

    protected open fun startServer(
        promise: Promise<Void>,
        port: Int,
    ) {
        server.requestHandler { r -> router.handle(r) }
            .listen(port)
            .onComplete { ar ->
                if (ar.succeeded()) {
                    logger.info { "$verticleName started on port $port" }
                    promise.complete()
                } else {
                    logger.error { "$verticleName NOT started on port $port" }
                    promise.fail(ar.cause())
                }
            }
    }

    private fun verticleProperty(propertyName: String) = "${verticleName.lowercase()}_$propertyName"

    protected fun verticleIntProperty(
        propertyName: String,
        defaultValue: Int,
    ): Int = intProperty(verticleProperty(propertyName), defaultValue)

    protected fun verticleLongProperty(
        propertyName: String,
        defaultValue: Long,
    ): Long = longProperty(verticleProperty(propertyName), defaultValue)

    protected fun verticleBooleanProperty(
        propertyName: String,
        defaultValue: Boolean,
    ): Boolean = booleanProperty(verticleProperty(propertyName), defaultValue)

    protected fun verticleProperty(
        propertyName: String,
        defaultValue: String,
    ): String = property(verticleProperty(propertyName), defaultValue)

    protected fun register(
        method: HttpMethod,
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        basePath: String = rootPath,
        handler: (RoutingContext) -> Unit,
    ) {
        router.route(method, "$basePath$path")
            .handler { context ->
                val u: TockUser? = context.user() as? TockUser ?: context.session()?.get("tockUser")
                if (u == null || roles.isNullOrEmpty()) {
                    handler.invoke(context)
                } else {
                    context.areAuthorized(roles) { ar ->
                        if (ar.succeeded() && ar.result() == true) {
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
        handler: (RoutingContext) -> Unit,
    ) {
        blocking(method, path, setOf(role), basePath, handler)
    }

    fun blocking(
        method: HttpMethod,
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        basePath: String = rootPath,
        handler: (RoutingContext) -> Unit,
    ) {
        register(method, path, roles, basePath) { rc -> rc.executeBlocking(handler) }
    }

    fun RoutingContext.isAuthorized(
        role: TockUserRole,
        resultHandler: (AsyncResult<Boolean>) -> Unit,
    ) {
        val u: TockUser? = user() as? TockUser ?: session()?.get("tockUser")
        if (u == null) {
            resultHandler.invoke(Future.failedFuture("No user set"))
        } else {
            resultHandler.invoke(Future.succeededFuture(u.roles.contains(role.name)))
        }
    }

    /**
     * Check the user has any authorized role
     */
    private fun RoutingContext.areAuthorized(
        roles: Set<TockUserRole?>,
        resultHandler: (AsyncResult<Boolean>) -> Unit,
    ) {
        val tockUser: TockUser? = user() as? TockUser ?: session()?.get("tockUser")
        if (tockUser == null) {
            resultHandler.invoke(Future.failedFuture("No user set"))
            return
        }
        val hasAny = roles.any { r -> r?.name in tockUser.roles }
        if (hasAny) {
            resultHandler.invoke(Future.succeededFuture(true))
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
        crossinline handler: (RoutingContext, I) -> O,
    ) {
        blocking(method, path, roles, basePath) { context ->
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
        basePath: String = rootPath,
        handler: (RoutingContext) -> O,
    ) {
        blocking(method, path, roles, basePath) { context ->
            val result = handler.invoke(context)
            context.endJson(result)
        }
    }

    fun <O> blockingJsonGet(
        path: String,
        role: TockUserRole,
        basePath: String = rootPath,
        handler: (RoutingContext) -> O,
    ) {
        blockingJsonGet(path = path, roles = setOf(role), basePath = basePath, handler = handler)
    }

    fun <O> blockingJsonGet(
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        basePath: String = rootPath,
        handler: (RoutingContext) -> O,
    ) {
        blocking(GET, path, roles, basePath) { context ->
            val result = handler.invoke(context)
            context.endJson(result)
        }
    }

    protected fun blockingPostEmptyResponse(
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        logger: RequestLogger = defaultRequestLogger,
        basePath: String = rootPath,
        handler: (RoutingContext) -> Unit,
    ) {
        blockingPost(path, roles, logger, basePath, successEmpty, handler)
    }

    protected fun blockingPost(
        path: String,
        role: TockUserRole,
        logger: RequestLogger = defaultRequestLogger,
        basePath: String = rootPath,
        handler: (RoutingContext) -> Unit,
    ) {
        blockingPost(path = path, roles = setOf(role), logger = logger, basePath = basePath, handler = handler)
    }

    protected fun blockingPost(
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        logger: RequestLogger = defaultRequestLogger,
        basePath: String = rootPath,
        success: (RoutingContext) -> Unit = successTrue,
        handler: (RoutingContext) -> Unit,
    ) {
        blocking(POST, path, roles, basePath) { context ->
            try {
                handler.invoke(context)
                success.invoke(context)
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
        handler: (RoutingContext) -> String,
    ) {
        blocking(GET, path, roles, basePath) { context ->
            context.response().end(handler.invoke(context))
        }
    }

    protected inline fun <reified F : Any, O> blockingUploadJsonPost(
        path: String,
        role: TockUserRole,
        logger: RequestLogger = defaultRequestLogger,
        basePath: String = rootPath,
        crossinline handler: (RoutingContext, F) -> O,
    ) {
        blockingUploadJsonPost(path, setOf(role), logger, basePath, handler)
    }

    protected inline fun <reified F : Any, O> blockingUploadJsonPost(
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        logger: RequestLogger = defaultRequestLogger,
        basePath: String = rootPath,
        crossinline handler: (RoutingContext, F) -> O,
    ) {
        blocking(POST, path, roles, basePath) { context ->
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
        basePath: String = rootPath,
        crossinline handler: (RoutingContext, String) -> O,
    ) {
        blockingUploadPost(path, setOf(role), logger, basePath, handler)
    }

    protected inline fun <O> blockingUploadPost(
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        logger: RequestLogger = defaultRequestLogger,
        basePath: String = rootPath,
        crossinline handler: (RoutingContext, String) -> O,
    ) {
        blocking(POST, path, roles, basePath) { context ->
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
        crossinline handler: (RoutingContext, Pair<String, ByteArray>) -> O,
    ) {
        blockingUploadBinaryPost(path, setOf(role), handler)
    }

    protected inline fun <O> blockingUploadBinaryPost(
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        crossinline handler: (RoutingContext, Pair<String, ByteArray>) -> O,
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
        basePath: String = rootPath,
        crossinline handler: (RoutingContext, I) -> O,
    ) {
        blockingWithBodyJson(POST, path, roles, logger, basePath, handler)
    }

    inline fun <reified I : Any, O> blockingJsonPost(
        path: String,
        role: TockUserRole,
        logger: RequestLogger = defaultRequestLogger,
        basePath: String = rootPath,
        crossinline handler: (RoutingContext, I) -> O,
    ) {
        blockingWithBodyJson(POST, path, setOf(role), logger, basePath, handler)
    }

    inline fun <reified I : Any, O> blockingJsonPut(
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        logger: RequestLogger = defaultRequestLogger,
        basePath: String = rootPath,
        crossinline handler: (RoutingContext, I) -> O,
    ) {
        blockingWithBodyJson(PUT, path, roles, logger, basePath, handler)
    }

    protected inline fun <reified I : Any, O> blockingJsonPut(
        path: String,
        role: TockUserRole,
        logger: RequestLogger = defaultRequestLogger,
        basePath: String = rootPath,
        crossinline handler: (RoutingContext, I) -> O,
    ) {
        blockingWithBodyJson(PUT, path, setOf(role), logger, basePath, handler)
    }

    fun blockingDeleteEmptyResponse(
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        logger: RequestLogger = defaultRequestLogger,
        basePath: String = rootPath,
        handler: (RoutingContext) -> Unit,
    ) {
        blockingDelete(path, roles, logger, basePath, successEmpty, handler)
    }

    fun blockingDelete(
        path: String,
        role: TockUserRole,
        logger: RequestLogger = defaultRequestLogger,
        basePath: String = rootPath,
        handler: (RoutingContext) -> Unit,
    ) {
        blockingDelete(path = path, roles = setOf(role), logger = logger, basePath = basePath, handler = handler)
    }

    fun blockingDelete(
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        logger: RequestLogger = defaultRequestLogger,
        basePath: String = rootPath,
        success: (RoutingContext) -> Unit = successTrue,
        handler: (RoutingContext) -> Unit,
    ) {
        blocking(DELETE, path, roles, basePath) { context ->
            try {
                handler.invoke(context)
                logger.log(context, null)
            } catch (t: Throwable) {
                if (t !is UnauthorizedException) {
                    logger.log(context, null, false)
                }
                throw t
            }
            success.invoke(context)
        }
    }

    protected fun blockingJsonDelete(
        path: String,
        role: TockUserRole,
        logger: RequestLogger = defaultRequestLogger,
        basePath: String = rootPath,
        handler: (RoutingContext) -> Boolean,
    ) {
        blockingJsonDelete(path, setOf(role), logger, basePath, handler)
    }

    fun blockingJsonDelete(
        path: String,
        roles: Set<TockUserRole>? = defaultRoles(),
        logger: RequestLogger = defaultRequestLogger,
        basePath: String = rootPath,
        handler: (RoutingContext) -> Boolean,
    ) {
        blockingWithoutBodyJson(DELETE, path, roles, basePath) { context ->
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
        crossinline handler: (RoutingContext, I, Handler<O>) -> Unit,
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
        crossinline handler: (RoutingContext, I, Handler<O>) -> Unit,
    ) {
        withBodyJson<I, O>(POST, path, roles, handler)
    }

    // extension & utility methods

    protected open fun addDevCorsHandler() {
        if (useDefaultCorsHandler) {
            router.route().handler(
                corsHandler(
                    property("tock_web_use_default_cors_handler_url", defaultCorsOrigin)
                        .run { if (this == "*") emptyList() else split("|") },
                    booleanProperty("tock_web_use_default_cors_handler_with_credentials", defaultCorsWithCredentials),
                ),
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
        allowedHeaders: Set<String> =
            listOfNotNull(
                "X-Requested-With",
                "Access-Control-Allow-Origin",
                if (allowCredentials) "Authorization" else null,
                "Content-Type",
            ).toSet(),
    ): CorsHandler =
        corsHandler(
            if (origin == "*") emptyList() else listOf(origin),
            allowCredentials,
            allowedMethods,
            allowedHeaders,
        )

    fun corsHandler(
        origins: List<String> = emptyList(),
        allowCredentials: Boolean = false,
        allowedMethods: Set<HttpMethod> = setOf(GET, POST, PUT, DELETE),
        allowedHeaders: Set<String> =
            (
                listOfNotNull(
                    "X-Requested-With",
                    "Access-Control-Allow-Origin",
                    if (allowCredentials) "Authorization" else null,
                    "Content-Type",
                ) +
                    // in order to support extra headers from web connector
                    listProperty("tock_web_connector_extra_headers", emptyList())
            )
                .toSet(),
    ): CorsHandler =
        CorsHandler.create().run {
            (if (origins.isEmpty()) addOrigin("*") else addOrigins(origins))
                .allowedMethods(allowedMethods)
                .allowedHeaders(allowedHeaders)
                .allowCredentials(allowCredentials)
        }

    protected fun bodyHandler(): BodyHandler {
        return BodyHandler
            .create()
            .setUploadsDirectory(fileUploadDirectory)
            .setBodyLimit(verticleLongProperty("body_limit", 1_000_000L))
            .setMergeFormAttributes(false)
    }

    inline fun <reified T : Any> RoutingContext.readJson(): T = mapper.readValue(this.body().asString())

    inline fun <reified T : Any> readJson(upload: FileUpload): T = mapper.readValue(File(upload.uploadedFileName()))

    fun readBytes(upload: FileUpload): ByteArray = Files.readAllBytes(Paths.get(upload.uploadedFileName()))

    fun readString(upload: FileUpload): String = String(readBytes(upload), StandardCharsets.UTF_8)

    /**
     * Execute blocking code using [Vertx.executeBlocking].
     */
    protected fun RoutingContext.executeBlocking(handler: (RoutingContext) -> Unit) {
        sharedVertx.blocking<Unit>(
            { p ->
                try {
                    handler.invoke(this)
                    p.tryComplete()
                } catch (t: Throwable) {
                    p.tryFail(t)
                } finally {
                    p.tryFail("call not completed")
                }
            },
            { ar ->
                if (ar.failed()) {
                    val cause = ar.cause()
                    when (cause) {
                        is RestException -> {
                            response().statusCode = cause.httpResponseStatus.code()
                            response().statusMessage = cause.message
                            response().endJson(cause.httpResponseBody)
                        }

                        null -> {
                            logger.error { "unknown error" }
                            fail(500)
                        }

                        else -> {
                            logger.error(cause)
                            fail(cause)
                        }
                    }
                }
            },
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
        if (ended()) return

        this.putHeader("content-type", "application/json; charset=utf-8")

        if (result == null) {
            statusCode = 204
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
    open fun defaultErrorHandler(statusCode: Int): Handler<RoutingContext> =
        Handler<RoutingContext> { event ->
            logger.info { "Error $statusCode: ${event.request().path()}" }
            tockErrorHandler.handle(event)
        }
}

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

package ai.tock.bot.engine

import ai.tock.bot.engine.WebSocketController.websocketEnabled
import ai.tock.bot.engine.config.UploadedFilesService
import ai.tock.bot.engine.nlp.NlpProxyBotService
import ai.tock.shared.booleanProperty
import ai.tock.shared.error
import ai.tock.shared.listProperty
import ai.tock.shared.property
import ai.tock.shared.security.auth.TockAuthProvider
import ai.tock.shared.security.initEncryptor
import ai.tock.shared.vertx.WebVerticle
import ai.tock.shared.vertx.detailedHealthcheck
import ai.tock.translator.Translator.initTranslator
import io.vertx.core.Promise
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import mu.KLogger
import mu.KotlinLogging
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet

/**
 *
 */
internal class BotVerticle(
    private val nlpProxyOnBot: Boolean = booleanProperty("tock_nlp_proxy_on_bot", false),
    private val serveUploadedFiles: Boolean = booleanProperty("tock_bot_serve_files", true)
) : WebVerticle() {

    inner class ServiceInstaller(
        val serviceId: String,
        private val installer: (Router) -> Any?,
        private val routes: MutableList<Route> = CopyOnWriteArrayList(),
        @Volatile
        var installed: Boolean = false,
        val registrationDate: Instant = Instant.now()
    ) {

        fun install() {
            if (!installed) {
                installed = true
                try {
                    logger.debug("install $serviceId")
                    val registeredRoutes = router.routes
                    installer.invoke(router)
                    routes.addAll(router.routes.subtract(registeredRoutes))
                } catch (e: Exception) {
                    logger.error(e)
                }
            }
        }

        fun uninstall() {
            routes.forEach { it.remove() }
        }
    }

    override val logger: KLogger = KotlinLogging.logger {}

    private val handlers: MutableMap<String, ServiceInstaller> = ConcurrentHashMap()
    private val secondaryInstallers: MutableSet<ServiceInstaller> = CopyOnWriteArraySet()
    private var initialized: Boolean = false

    override val defaultCorsOrigin: String = "*"

    override val defaultCorsWithCredentials: Boolean = false

    override fun authProvider(): TockAuthProvider? = defaultAuthProvider()

    fun registerServices(serviceIdentifier: String, installer: (Router) -> Any?): ServiceInstaller {
        return ServiceInstaller(serviceIdentifier, installer).also {
            if (!handlers.containsKey(serviceIdentifier)) {
                handlers[serviceIdentifier] = it
            } else {
                logger.debug("service $serviceIdentifier already registered - skip it for now")
                secondaryInstallers.add(it)
            }
        }
    }

    fun unregisterServices(installer: ServiceInstaller) {
        if (secondaryInstallers.contains(installer)) {
            secondaryInstallers.remove(installer)
        }
        if (handlers[installer.serviceId] == installer) {
            handlers.remove(installer.serviceId)
                ?.also {
                    val s = secondaryInstallers.find {
                        it.serviceId == installer.serviceId
                    }

                    logger.debug { "remove service ${it.serviceId}" }
                    it.uninstall()
                    if (s != null) {
                        s.install()
                        secondaryInstallers.remove(s)
                        handlers[it.serviceId] = s
                    }
                    return
                }
        }
    }

    override fun protectedPaths(): Set<String> {
        // TODO remove deprecated tock_bot_protected_path property
        val path = property("tock_bot_protected_path", "/admin")
        val paths = listProperty("tock_bot_protected_paths", listOf("/admin"))

        return (paths + path).map { it.trim() }.toSet()
    }

    @Synchronized
    override fun configure() {
        if (!initialized) {
            initialized = true
            initEncryptor()
            initTranslator()
            if (nlpProxyOnBot) {
                registerServices("nlp_proxy_bot", NlpProxyBotService.configure(vertx))
            }
            if (serveUploadedFiles) {
                registerServices("serve_files", UploadedFilesService.configure())
            }
        }

        install()
    }

    private fun install() {
        if (handlers.any { !it.value.installed }) {
            logger.info { "Install Bot Services / ${handlers.size} registered" }
            // sort installers by registration date to keep registration order
            handlers.values.sortedBy { it.registrationDate }.forEach {
                it.install()
            }
        }
    }

    override fun defaultHealthcheck(): (RoutingContext) -> Unit {
        return BotRepository.healthcheckHandler
    }

    override fun detailedHealthcheck(): (RoutingContext) -> Unit = detailedHealthcheck(
        BotRepository.detailedHealthcheckTasks,
        selfCheck = { BotRepository.botsInstalled }
    )

    override fun startServer(promise: Promise<Void>, port: Int) {
        if (websocketEnabled) {
            logger.info { "Install WebSocket handler" }
            server.webSocketHandler { context ->
                try {
                    val key = context.path().let { if (it.startsWith("/")) it.substring(1) else null }

                    if (key !=null && WebSocketController.isAuthorizedKey(key)) {
                        logger.info { "Install WebSocket push handler for ${context.path()}" }
                        WebSocketController.setPushHandler(key) {
                            try {
                                logger.debug { "send: $it" }
                                context.writeTextMessage(it)
                            } catch (e: Exception) {
                                logger.error(e)
                            }
                        }

                        context.textMessageHandler { json ->
                            try {
                                logger.debug { "receive $json" }
                                WebSocketController.getReceiveHandler(key)?.invoke(json)
                            } catch (e: Exception) {
                                logger.error(e)
                            }
                        }.closeHandler {
                            WebSocketController.removePushHandler(key)
                        }
                    } else {
                        logger.warn { "unknown key: $key" }
                        context.reject()
                    }
                } catch (e: Exception) {
                    logger.error(e)
                }
            }
        }
        super.startServer(promise, port)
    }
}

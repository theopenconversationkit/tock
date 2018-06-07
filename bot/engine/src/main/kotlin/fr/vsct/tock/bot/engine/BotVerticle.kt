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

package fr.vsct.tock.bot.engine

import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.property
import fr.vsct.tock.shared.security.initEncryptor
import fr.vsct.tock.shared.vertx.WebVerticle
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import mu.KLogger
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 *
 */
internal class BotVerticle : WebVerticle() {

    private inner class Installer(val router: Router, val installer: (Router) -> Unit) {
        fun install() {
            try {
                logger.debug("install $installer for $router")
                installer.invoke(router)
            } catch (e: Exception) {
                logger.error(e)
            }
        }
    }

    private data class RouterWrapper(val router: Router) : Router by router

    override val logger: KLogger = KotlinLogging.logger {}

    private val handlers: MutableMap<String, Installer> = ConcurrentHashMap()
    private val secondaryInstallers: MutableList<Installer> = CopyOnWriteArrayList()

    override fun authProvider(): AuthProvider? = defaultAuthProvider()

    fun registerServices(rootPath: String, installer: (Router) -> Unit): Router {
        return if (!handlers.containsKey(rootPath)) {
            val router = Router.router(vertx)
            handlers.put(rootPath, Installer(router, installer))
            router
        } else {
            logger.debug("path $rootPath already registered - skip it for now")
            val router = RouterWrapper(handlers[rootPath]!!.router)
            secondaryInstallers.add(Installer(router, installer))
            router
        }
    }

    fun unregisterRouter(router: Router) {
        val secondary = secondaryInstallers.find { it.router == router }
        if (secondary == null) {
            handlers.entries.firstOrNull { it.value.router == router }
                ?.also {
                    val r = it.value.router
                    if (r == router) {
                        val s = secondaryInstallers.find {
                            it.router == router || (it.router as RouterWrapper).router == router
                        }
                        logger.debug { "remove installer $it" }
                        router.clear()
                        if (s != null) {
                            s.install()
                            handlers[it.key] = s
                        } else {
                            router.clear()
                        }
                        return
                    }
                }
        } else {
            secondaryInstallers.remove(secondary)
        }
    }

    override fun protectedPath(): String {
        return property("tock_bot_protected_path", "/admin")
    }

    override fun configure() {
        initEncryptor()

        handlers.forEach {
            it.value.install()
        }
    }

    override fun healthcheck(): (RoutingContext) -> Unit {
        return BotRepository.healthcheckHandler
    }
}
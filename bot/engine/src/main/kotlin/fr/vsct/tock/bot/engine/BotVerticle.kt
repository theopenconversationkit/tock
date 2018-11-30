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
import fr.vsct.tock.translator.Translator.initTranslator
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import mu.KLogger
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet

/**
 *
 */
internal class BotVerticle : WebVerticle() {

    inner class ServiceInstaller(
        val serviceId: String,
        private val installer: (Router) -> Unit,
        var routes: MutableList<Route> = CopyOnWriteArrayList(),
        @Volatile
        var installed: Boolean = false
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
    @Volatile
    private var initialized: Boolean = false

    override fun authProvider(): AuthProvider? = defaultAuthProvider()

    fun registerServices(serviceIdentifier: String, installer: (Router) -> Unit): ServiceInstaller {
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
        } else {
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

    override fun protectedPath(): String {
        return property("tock_bot_protected_path", "/admin")
    }

    override fun configure() {
        if (!initialized) {
            initEncryptor()
            initTranslator()
            initialized = true
        }

        handlers.forEach {
            it.value.install()
        }
    }

    override fun healthcheck(): (RoutingContext) -> Unit {
        return BotRepository.healthcheckHandler
    }
}
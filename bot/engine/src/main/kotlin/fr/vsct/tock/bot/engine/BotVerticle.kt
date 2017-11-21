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

import fr.vsct.tock.shared.property
import fr.vsct.tock.shared.security.initEncryptor
import fr.vsct.tock.shared.vertx.WebVerticle
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import mu.KLogger
import mu.KotlinLogging

/**
 *
 */
class BotVerticle : WebVerticle() {

    override val logger: KLogger = KotlinLogging.logger {}

    private val handlers: MutableMap<String, (Router) -> Unit> = mutableMapOf()

    override fun authProvider(): AuthProvider? = currentAuthProvider()

    fun registerServices(rootPath: String, installer: (Router) -> Unit) {
        if (!(handlers as Map<String, (Router) -> Unit>).containsKey(rootPath)) {
            handlers.put(rootPath, installer)
        } else {
            logger.debug("path $rootPath already registered - skip")
        }
    }

    override fun protectedPath(): String {
        return property("tock_bot_protected_path", "/admin")
    }

    override fun configure() {
        initEncryptor()

        handlers.forEach { it.value.invoke(router) }
    }

    override fun healthcheck(): (RoutingContext) -> Unit {
        return BotRepository.healthcheckHandler
    }
}
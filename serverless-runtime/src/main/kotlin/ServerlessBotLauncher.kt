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

package ai.tock.serverless

import ai.tock.bot.engine.BotRepository
import ai.tock.bot.engine.botModule
import ai.tock.serverless.queue.RequestQueueing
import ai.tock.shared.Executor
import ai.tock.shared.Loader
import ai.tock.shared.changes.ChangeChecks
import ai.tock.shared.injector
import ai.tock.shared.intProperty
import ai.tock.shared.security.initEncryptor
import ai.tock.shared.service.BotAdditionalModulesService
import ai.tock.shared.sharedModule
import ai.tock.stt.noop.noOpSTTModule
import ai.tock.translator.Translator.initTranslator
import ai.tock.translator.noop.noOpTranslatorModule
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.singleton
import mu.KotlinLogging

/**
 * Starts bots in a serverless process: the counterpart of `registerAndInstallBot` without Vert.x nor MongoDB.
 *
 * The [storageModule] binds the runtime DAOs (`XxxRuntimeDAO`), the [ai.tock.bot.engine.user.UserLock] and the
 * DAOs that the connectors bind in their own modules, which may target MongoDB: it overrides them, so it is
 * declared with `Kodein.Module(allowSilentOverride = true)`.
 */
object ServerlessBotLauncher {
    private val logger = KotlinLogging.logger {}

    /** The modules of a serverless process: the executor that lets the requests wait for their work. */
    val serverlessModule =
        Kodein.Module {
            bind<Executor>() with singleton { TrackingExecutor() }
        }

    /**
     * Injects the modules: the shared and bot ones, the modules of the connectors, then [storageModule]
     * and [modules], which override them.
     */
    fun setup(
        storageModule: Kodein.Module,
        modules: List<Kodein.Module> = emptyList(),
    ) {
        val additional = Loader.loadServices<BotAdditionalModulesService>()
        injector.inject(
            Kodein {
                (
                    listOf(sharedModule, botModule, noOpTranslatorModule, noOpSTTModule, serverlessModule) +
                        additional.flatMap { it.defaultModules() } +
                        additional.flatMap { it.customModules() } +
                        storageModule +
                        modules
                ).forEach { import(it, allowOverride = true) }
            },
        )
    }

    /**
     * Installs the registered bots, then starts the HTTP server on [port] (`tock_serverless_port`, or `PORT`
     * as set by Cloud Run and the Lambda Web Adapter, 8080 by default).
     * With [queueing], the webhooks are acknowledged at once and processed when the queue delivers them.
     */
    fun start(
        port: Int = intProperty("tock_serverless_port", System.getenv("PORT")?.toIntOrNull() ?: 8080),
        createApplicationIfNotExists: Boolean = false,
        queueing: RequestQueueing? = null,
    ): ServerlessHttpServer {
        ServerlessRequestHandler.configure(queueing)
        // as BotVerticle: the obfuscators, and the labels of the default namespace in the cache of the translator
        initEncryptor()
        // the state of the storages without change stream, before the bots load their configuration
        ChangeChecks.prepareAll()
        initTranslator()
        BotRepository.installBots(emptyList(), createApplicationIfNotExists)
        return ServerlessHttpServer(port).start().also {
            logger.info { "serverless bots listening on ${it.port}: ${ServerlessBotServer.serviceIdentifiers}" }
        }
    }
}

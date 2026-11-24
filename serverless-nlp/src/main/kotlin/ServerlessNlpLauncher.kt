/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.serverless.nlp

import ai.tock.nlp.api.NlpApiRoutes
import ai.tock.nlp.core.NlpEngineType
import ai.tock.nlp.core.configuration.NlpApplicationConfiguration
import ai.tock.nlp.core.service.coreModule
import ai.tock.nlp.front.service.ParserService
import ai.tock.nlp.front.service.initializeRuntimeConfiguration
import ai.tock.nlp.front.shared.ApplicationRuntimeConfiguration
import ai.tock.nlp.front.shared.Parser
import ai.tock.nlp.model.service.modelModule
import ai.tock.nlp.model.service.storage.NlpApplicationConfigurationDAO
import ai.tock.serverless.ServerlessBotLauncher
import ai.tock.serverless.ServerlessBotServer
import ai.tock.serverless.ServerlessHttpServer
import ai.tock.serverless.ServerlessRequestHandler
import ai.tock.shared.Loader
import ai.tock.shared.changes.ChangeChecks
import ai.tock.shared.injector
import ai.tock.shared.intProperty
import ai.tock.shared.property
import ai.tock.shared.provide
import ai.tock.shared.security.initEncryptor
import ai.tock.shared.service.BotAdditionalModulesService
import ai.tock.shared.sharedModule
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import mu.KotlinLogging
import java.time.Instant

/**
 * Runs the NLP API in a serverless process: the counterpart of the NLP verticle without Vert.x nor MongoDB.
 *
 * The [storageModule] binds the runtime DAOs of the NLP (`nlpDynamoModule`) and the model storage
 * (`modelS3Module`, or `tock_nlp_model_storage=s3` with tock-nlp-model-storage-s3 on the classpath). The models are
 * built by the admin: the process only loads them.
 *
 * The routes are the ones of [NlpApiRoutes], without authentication: the function must be private (IAM, or a
 * token checked by the platform). The applications are created in the admin, and the logs are searched there.
 */
object ServerlessNlpLauncher {
    private val logger = KotlinLogging.logger {}

    /**
     * The configurations of the NLP engines are written by the builds of the models, in the admin. The models
     * stored in S3 carry their configuration: the runtime has none of its own.
     */
    private object RuntimeNlpApplicationConfigurationDAO : NlpApplicationConfigurationDAO {
        override fun saveNewConfiguration(
            applicationName: String,
            engineType: NlpEngineType,
            configuration: NlpApplicationConfiguration,
        ): Unit = throw UnsupportedOperationException("the NLP configurations are written by the admin")

        override fun loadLastConfiguration(
            applicationName: String,
            engineType: NlpEngineType,
            updated: Instant,
        ): NlpApplicationConfiguration? = null
    }

    /** The NLP modules of a serverless process: the parser, on the runtime DAOs. */
    val nlpServerlessModule =
        Kodein.Module {
            bind<Parser>() with provider { ParserService }
            bind<NlpApplicationConfigurationDAO>() with provider { RuntimeNlpApplicationConfigurationDAO }
        }

    /** The modules of the NLP, before the storage ones: for a process that also runs bots (see [install]). */
    val modules: List<Kodein.Module> = listOf(coreModule, modelModule, nlpServerlessModule)

    /**
     * Injects the modules: the shared, serverless and NLP ones, the additional modules, then [storageModule] and
     * [modules], which override them.
     */
    fun setup(
        storageModule: Kodein.Module,
        modules: List<Kodein.Module> = emptyList(),
    ) {
        val additional = Loader.loadServices<BotAdditionalModulesService>()
        injector.inject(
            Kodein {
                (
                    listOf(sharedModule, ServerlessBotLauncher.serverlessModule) +
                        this@ServerlessNlpLauncher.modules +
                        additional.flatMap { it.defaultModules() } +
                        additional.flatMap { it.customModules() } +
                        storageModule +
                        modules
                ).forEach { import(it, allowOverride = true) }
            },
        )
    }

    /**
     * Loads the NLP configuration and registers the routes of the NLP API, under `tock_nlp_root` (`/rest/nlp` by
     * default). A process that also runs bots calls it after [ServerlessBotLauncher.setup] (with [modules] and the
     * NLP storage in its modules), and before [ServerlessBotLauncher.start]; a function without HTTP server calls it
     * from its [ai.tock.serverless.ServerlessApplication].
     */
    fun install(rootPath: String = property("tock_nlp_root", "/rest/nlp")) {
        // as NlpVerticle: the obfuscators of the logs
        initEncryptor()
        // the state of the storages without change stream, before the configuration is loaded
        ChangeChecks.prepareAll()
        // false also when there is no entity type yet (no publication, no built-in entity evaluator): the errors are logged
        if (!initializeRuntimeConfiguration()) {
            logger.warn { "the NLP configuration has no entity type, or cannot be loaded" }
        }
        if ("nlp_api" !in ServerlessBotServer.serviceIdentifiers) {
            ServerlessBotServer.registerRoutes(
                "nlp_api",
                NlpApiRoutes.routes(rootPath, injector.provide<Parser>(), ApplicationRuntimeConfiguration.fromInjector()),
            )
        }
    }

    /**
     * Installs the NLP API, then starts the HTTP server on [port] (`tock_serverless_port`, or `PORT` as set by
     * Cloud Run and the Lambda Web Adapter, 8080 by default).
     */
    fun start(port: Int = intProperty("tock_serverless_port", System.getenv("PORT")?.toIntOrNull() ?: 8080)): ServerlessHttpServer {
        ServerlessRequestHandler.configure(null)
        install()
        return ServerlessHttpServer(port).start().also {
            logger.info { "serverless NLP API listening on ${it.port}" }
        }
    }
}

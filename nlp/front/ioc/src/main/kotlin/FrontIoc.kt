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

package ai.tock.nlp.front.ioc

import ai.tock.duckling.client.ducklingModule
import ai.tock.nlp.core.service.coreModule
import ai.tock.nlp.front.client.FrontClient
import ai.tock.nlp.front.service.frontModule
import ai.tock.nlp.front.storage.mongo.frontMongoModule
import ai.tock.nlp.model.service.modelModule
import ai.tock.nlp.model.service.storage.mongo.modelMongoModule
import ai.tock.shared.Loader
import ai.tock.shared.injector
import ai.tock.shared.service.BotAdditionalModulesService
import ai.tock.shared.sharedModule
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.Kodein.Module
import mu.KotlinLogging
import kotlin.system.exitProcess

/**
 *
 */
object FrontIoc {
    private val logger = KotlinLogging.logger {}

    private val coreModules: List<Module> =
        run {
            val additionalModulesService = Loader.loadServices<BotAdditionalModulesService>()
            listOf(
                sharedModule,
                coreModule,
                modelMongoModule,
                modelModule,
                frontMongoModule,
                frontModule,
                ducklingModule,
            ).plus(additionalModulesService.flatMap { it.defaultModules() }.toList())
                // Add custom modules/services to override default modules/services.
                // The order is very important: we need to inject the default modules/services first, then the custom modules/services.
                .plus(additionalModulesService.flatMap { it.customModules() }.toList())
        }

    fun setup(vararg modules: Module) {
        setup(modules.toList())
    }

    fun setup(modules: List<Module>) {
        logger.debug { "Start nlp injection" }
        injector.inject(
            Kodein {
                coreModules.forEach { import(it, allowOverride = true) }

                // load additional modules
                modules.forEach {
                    if (!coreModules.contains(it)) {
                        import(it, allowOverride = true)
                    }
                }
            },
        )
        if (!FrontClient.initializeConfiguration()) {
            logger.error("configuration not initialized - exit application")
            exitProcess(1)
        }
    }
}

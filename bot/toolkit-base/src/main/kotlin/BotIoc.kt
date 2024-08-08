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

package ai.tock.bot

import ai.tock.shared.Loader
import ai.tock.bot.engine.botModule
import ai.tock.bot.mongo.botMongoModule
import ai.tock.shared.injector
import ai.tock.shared.service.BotAdditionalModulesService
import ai.tock.shared.sharedModule
import ai.tock.stt.noop.noOpSTTModule
import ai.tock.translator.noop.noOpTranslatorModule
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.Kodein.Module
import mu.KotlinLogging

/**
 * Bot module configuration.
 */
object BotIoc {

    private val logger = KotlinLogging.logger {}

    /**
     * The core modules of the bot.
     */
    val coreModules: List<Module> = run {
        val additionalModulesService = Loader.loadServices<BotAdditionalModulesService>()
        listOf(sharedModule, botModule, botMongoModule, noOpTranslatorModule, noOpSTTModule)
            .plus(additionalModulesService.flatMap { it.defaultModules() }.toList())
            // Add custom modules/services to override default modules/services.
            // The order is very important: we need to inject the default modules/services first, then the custom modules/services.
            .plus(additionalModulesService.flatMap { it.customModules() }.toList())
    }

    /**
     * Start the bot with the specified additional [modules].
     */
    fun setup(vararg modules: Module) {
        setup(modules.toList())
    }

    /**
     * Start the bot with the specified additional [modules].
     */
    fun setup(modules: List<Module>) {
        logger.debug { "Start bot injection" }
        injector.inject(
            Kodein {
                coreModules.forEach { import(it, allowOverride = true) }

                // load additional modules
                modules.forEach {
                    import(it, allowOverride = true)
                }
            }
        )
    }
}

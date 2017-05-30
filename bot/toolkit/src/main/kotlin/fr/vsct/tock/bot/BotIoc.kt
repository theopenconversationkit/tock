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

package fr.vsct.tock.bot

import botModule
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.Kodein.Module
import fr.vsct.tock.bot.mongo.botMongoModule
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.sharedModule
import fr.vsct.tock.translator.noop.noOpTranslatorModule
import mu.KotlinLogging

/**
 *
 */
object BotIoc {

    private val logger = KotlinLogging.logger {}

    val coreModules: List<Module> =
            listOf(sharedModule, botModule, botMongoModule, noOpTranslatorModule)

    fun setup(vararg modules: Module) {
        setup(modules.toList())
    }

    fun setup(modules: List<Module>) {
        logger.debug { "Start bot injection" }
        injector.inject(Kodein {
            coreModules.forEach { import(it) }

            //load additional modules
            modules.forEach {
                import(it, allowOverride = true)
            }
        })
    }


}
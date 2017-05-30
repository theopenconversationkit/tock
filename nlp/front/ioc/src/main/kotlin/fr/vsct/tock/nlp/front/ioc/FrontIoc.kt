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

package fr.vsct.tock.nlp.front.ioc

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.Kodein.Module
import fr.vsct.tock.duckling.client.ducklingModule
import fr.vsct.tock.nlp.core.service.coreModule
import fr.vsct.tock.nlp.front.client.FrontClient
import fr.vsct.tock.nlp.front.service.frontModule
import fr.vsct.tock.nlp.front.storage.mongo.frontMongoModule
import fr.vsct.tock.nlp.model.service.modelModule
import fr.vsct.tock.nlp.model.service.storage.mongo.modelMongoModule
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.sharedModule
import mu.KotlinLogging

/**
 *
 */
object FrontIoc {

    private val logger = KotlinLogging.logger {}

    val coreModules: List<Module> =
            listOf(
                    sharedModule,
                    coreModule,
                    modelMongoModule,
                    modelModule,
                    frontMongoModule,
                    frontModule,
                    ducklingModule
            )

    fun setup(vararg modules: Module) {
        setup(modules.toList())
    }

    fun setup(modules: List<Module>) {
        logger.debug { "Start nlp injection" }
        injector.inject(Kodein {
            coreModules.forEach { import(it) }

            //load additional modules
            modules.forEach {
                if (!coreModules.contains(it)) {
                    import(it, allowOverride = true)
                }
            }
        })
        FrontClient.initData()
    }
}
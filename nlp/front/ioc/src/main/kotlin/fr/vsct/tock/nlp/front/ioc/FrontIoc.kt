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
import fr.vsct.tock.nlp.core.service.coreModule
import fr.vsct.tock.nlp.core.service.entity.EntityEvaluatorService
import fr.vsct.tock.nlp.front.service.frontModule
import fr.vsct.tock.nlp.front.storage.mongo.frontMongoModule
import fr.vsct.tock.nlp.model.service.engine.NlpEngineRepository
import fr.vsct.tock.nlp.model.service.modelModule
import fr.vsct.tock.nlp.model.service.storage.mongo.modelMongoModule
import fr.vsct.tock.nlp.opennlp.OpenNlpEngineProvider
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.sharedModule
import fr.vsct.tock.duckling.client.DucklingEntityEvaluatorProvider
import mu.KotlinLogging

/**
 *
 */
object FrontIoc {

    private val logger = KotlinLogging.logger {}

    fun setup() {
        logger.info { "Start nlp injection" }
        injector.inject(Kodein {
            import(sharedModule)
            import(coreModule)
            import(modelModule)
            import(modelMongoModule)
            import(frontModule)
            import(frontMongoModule)

            NlpEngineRepository.registerEngineProvider(OpenNlpEngineProvider)
            EntityEvaluatorService.registerEntityServiceProvider(DucklingEntityEvaluatorProvider)
        })
    }
}
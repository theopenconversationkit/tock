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

package fr.vsct.tock.nlp.integration

import com.fasterxml.jackson.module.kotlin.readValue
import fr.vsct.tock.nlp.core.NlpEngineType
import fr.vsct.tock.nlp.front.client.FrontClient
import fr.vsct.tock.nlp.front.client.FrontClient.import
import fr.vsct.tock.nlp.front.client.FrontClient.updateEntityModelForIntent
import fr.vsct.tock.nlp.front.client.FrontClient.updateIntentsModelForApplication
import fr.vsct.tock.nlp.front.ioc.FrontIoc
import fr.vsct.tock.nlp.front.shared.codec.ApplicationDump
import fr.vsct.tock.shared.defaultNamespace
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.resource
import java.util.Locale

/**
 *
 */
object IntegrationConfiguration {

    fun loadDump(nlpEngineType: NlpEngineType): ApplicationDump {
        val dump: ApplicationDump = mapper.readValue(resource("/dump.json"))
        return dump.copy(application = dump.application.copy(nlpEngineType = nlpEngineType))
    }

    fun init(nlpEngineType: NlpEngineType) {
        FrontIoc.setup()

        println("Start model initialization")
        val report = import(defaultNamespace, loadDump(nlpEngineType))

        if (report.modified) {
            val application = FrontClient.getApplicationByNamespaceAndName(defaultNamespace, "test")!!
            val travelIntentId = FrontClient.getIntentIdByQualifiedName("$defaultNamespace:travel")!!
            val weatherIntentId = FrontClient.getIntentIdByQualifiedName("$defaultNamespace:weather")!!
            updateIntentsModelForApplication(emptyList(), application, Locale.ENGLISH, nlpEngineType)
            updateEntityModelForIntent(emptyList(), application, travelIntentId, Locale.ENGLISH, nlpEngineType)
            updateEntityModelForIntent(emptyList(), application, weatherIntentId, Locale.ENGLISH, nlpEngineType)
        }

        println("End model initialization")
    }
}
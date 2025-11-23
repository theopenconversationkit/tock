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

package ai.tock.nlp.integration

import ai.tock.nlp.core.NlpEngineType
import ai.tock.nlp.front.client.FrontClient
import ai.tock.nlp.front.client.FrontClient.import
import ai.tock.nlp.front.client.FrontClient.updateEntityModelForIntent
import ai.tock.nlp.front.client.FrontClient.updateIntentsModelForApplication
import ai.tock.nlp.front.ioc.FrontIoc
import ai.tock.nlp.front.shared.codec.ApplicationDump
import ai.tock.shared.defaultNamespace
import ai.tock.shared.jackson.mapper
import ai.tock.shared.resource
import com.fasterxml.jackson.module.kotlin.readValue
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

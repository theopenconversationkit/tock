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

import fr.vsct.tock.nlp.core.NlpEngineType
import fr.vsct.tock.nlp.front.client.FrontClient
import fr.vsct.tock.nlp.front.ioc.FrontIoc
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.Classification
import fr.vsct.tock.nlp.front.shared.config.ClassifiedEntity
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentence
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import fr.vsct.tock.nlp.front.shared.config.EntityDefinition
import fr.vsct.tock.nlp.front.shared.config.EntityTypeDefinition
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition
import java.time.Instant
import java.util.Locale

/**
 *
 */
object IntegrationConfiguration {


    fun init() {
        FrontIoc.setup()

        println("Start model initialization")
        val client = FrontClient
        client.save(EntityTypeDefinition("vsc:locality", "custom locality entity type"))
        client.save(EntityTypeDefinition("duckling:datetime", "default datetime entity"))
        var application = ApplicationDefinition("test", "vsc", setOf(), setOf(Locale.ENGLISH))
        client.save(application)
        val travelIntent = IntentDefinition("travel", "vsc", setOf(application._id!!), setOf(EntityDefinition("vsc:locality"), EntityDefinition("duckling:datetime")))
        client.save(travelIntent)
        val weatherIntent = IntentDefinition("weather", "vsc", setOf(application._id!!), setOf(EntityDefinition("vsc:locality")))
        client.save(weatherIntent)

        application = application.copy(intents = setOf(travelIntent._id!!, weatherIntent._id!!))
        client.save(application)

        client.save(createSentence("I want to go to Nice tomorrow", application, travelIntent, "Nice", "tomorrow"))
        client.save(createSentence("I would like to go to New York the first of January", application, travelIntent, "New York", "the first of January"))
        client.save(createSentence("Can I go to Madrid today?", application, travelIntent, "Madrid", "today"))
        client.save(createSentence("go to Paris tomorrow", application, travelIntent, "Paris", "tomorrow"))
        client.save(createSentence("Let's travel to Rio", application, travelIntent, "Rio"))
        client.save(createSentence("Can you give me the weather in Monaco please", application, weatherIntent, "Monaco"))
        client.save(createSentence("Weather for New York?", application, weatherIntent, "New York"))
        client.save(createSentence("Does it rain in Philadelphia?", application, weatherIntent, "Philadelphia"))
        client.save(createSentence("Sunny in Barcelona?", application, weatherIntent, "Barcelona"))
        client.save(createSentence("Weather report for Toronto", application, weatherIntent, "Toronto"))

        client.updateIntentsModelForApplication(emptyList(), application, Locale.ENGLISH, NlpEngineType.opennlp)
        client.updateEntityModelForIntent(emptyList(), application, travelIntent._id!!, Locale.ENGLISH, NlpEngineType.opennlp)
        client.updateEntityModelForIntent(emptyList(), application, weatherIntent._id!!, Locale.ENGLISH, NlpEngineType.opennlp)

        println("End model initialization")
    }

    private fun createSentence(text: String, app: ApplicationDefinition, intent: IntentDefinition, locality: String, date: String? = null): ClassifiedSentence {
        val localityIndex = text.indexOf(locality)
        val dateIndex = if (date == null) -1 else text.indexOf(date)
        return ClassifiedSentence(
                text,
                Locale.ENGLISH,
                app._id!!,
                Instant.now(),
                Instant.now(),
                ClassifiedSentenceStatus.model,
                Classification(
                        intent._id!!,
                        listOfNotNull(
                                ClassifiedEntity("vsc:locality", "locality", localityIndex, localityIndex + locality.length),
                                if (date == null) null else ClassifiedEntity("duckling:datetime", "datetime", dateIndex, dateIndex + date.length))
                )
        )
    }
}
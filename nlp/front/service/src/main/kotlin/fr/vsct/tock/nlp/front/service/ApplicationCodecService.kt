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

package fr.vsct.tock.nlp.front.service

import com.github.salomonbrys.kodein.instance
import fr.vsct.tock.nlp.front.service.FrontRepository.entityTypeExists
import fr.vsct.tock.nlp.front.service.ModelUpdaterService.triggerBuild
import fr.vsct.tock.nlp.front.shared.ApplicationCodec
import fr.vsct.tock.nlp.front.shared.ApplicationConfiguration
import fr.vsct.tock.nlp.front.shared.codec.ApplicationDump
import fr.vsct.tock.nlp.front.shared.codec.ApplicationImportConfiguration
import fr.vsct.tock.nlp.front.shared.codec.ApplicationImportReport
import fr.vsct.tock.nlp.front.shared.codec.DumpType
import fr.vsct.tock.nlp.front.shared.config.SentencesQuery
import fr.vsct.tock.nlp.front.shared.updater.ModelBuildTrigger
import fr.vsct.tock.shared.injector
import mu.KotlinLogging

/**
 *
 */
object ApplicationCodecService : ApplicationCodec {

    private val logger = KotlinLogging.logger {}
    val config: ApplicationConfiguration  by injector.instance()

    override fun export(applicationId: String, dumpType: DumpType): ApplicationDump {
        val app = config.getApplicationById(applicationId)!!
        val entities = config.getEntityTypes()
        val intents = config.getIntentsByApplicationId(applicationId)
        val sentences = config.getSentences(intents.map { it._id!! }.toSet())
        return ApplicationDump(app, entities, intents, sentences)
    }

    override fun prepareImport(dump: ApplicationDump): ApplicationImportConfiguration {
        return ApplicationImportConfiguration(dump.application.name)
    }

    override fun import(namespace: String, dump: ApplicationDump, configuration: ApplicationImportConfiguration): ApplicationImportReport {
        logger.info { "Import dump..." }
        with(configuration) {
            val report = ApplicationImportReport()

            dump.entityTypes.forEach {
                e ->
                if (!entityTypeExists(e.name)) {
                    val newEntity = e.copy(_id = null)
                    config.save(newEntity)
                    report.add(newEntity)
                    logger.debug { "Import entity type $newEntity" }
                }
            }

            val appName = if (configuration.newApplicationName.isNullOrBlank()) dump.application.name else configuration.newApplicationName!!.trim()
            val app = config.getApplicationByNamespaceAndName(namespace, appName)
                    .let { app ->
                        if (app == null) {
                            val appToSave = dump.application.copy(
                                    namespace = namespace,
                                    name = appName,
                                    intents = emptySet(),
                                    intentStatesMap = emptyMap(),
                                    _id = null)
                            report.add(appToSave)
                            logger.debug { "Import application $appToSave" }
                            config.save(appToSave)
                        } else {
                            app
                        }
                    }
            val appId = app._id!!

            val intentsIdsMap = dump.intents.map { i ->
                var intent = config.getIntentByNamespaceAndName(i.namespace, i.name)
                if (intent == null) {
                    intent = i.copy(_id = null, applications = setOf(appId))
                    config.save(intent)
                    report.add(intent)
                    logger.debug { "Import intent $intent" }
                } else {
                    config.save(intent.copy(applications = intent.applications + appId))
                }
                i._id to intent._id
            }.toMap()

            //update application intent list
            config.save(app.copy(
                    intents = app.intents + intentsIdsMap.values.filterNotNull().toSet(),
                    intentStatesMap = app.intentStatesMap + dump.application.intentStatesMap.mapKeys { intentsIdsMap[it.key]!! }
            ))

            dump.sentences.forEach { s ->
                if (config.search(SentencesQuery(appId, s.language, search = s.text, onlyExactMatch = true)).total == 0L) {
                    logger.debug { "Import sentence ${s.text}" }
                    val sentence = s.copy(
                            applicationId = appId,
                            classification = s.classification.copy(intentId = intentsIdsMap[s.classification.intentId]!!))
                    report.add(sentence)
                    config.save(sentence)
                }
            }
            logger.info { "Dump imported! Result : $report" }

            //trigger build
            if (report.modified) {
                triggerBuild(ModelBuildTrigger(appId, true))
            }

            return report
        }
    }
}
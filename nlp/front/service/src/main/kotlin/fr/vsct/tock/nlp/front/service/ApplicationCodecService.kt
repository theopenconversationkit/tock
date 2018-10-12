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

import fr.vsct.tock.nlp.core.Intent
import fr.vsct.tock.nlp.core.Intent.Companion.UNKNOWN_INTENT_NAME
import fr.vsct.tock.nlp.front.service.ConfigurationRepository.entityTypeExists
import fr.vsct.tock.nlp.front.service.ModelUpdaterService.triggerBuild
import fr.vsct.tock.nlp.front.shared.ApplicationCodec
import fr.vsct.tock.nlp.front.shared.ApplicationConfiguration
import fr.vsct.tock.nlp.front.shared.build.ModelBuildTrigger
import fr.vsct.tock.nlp.front.shared.codec.ApplicationDump
import fr.vsct.tock.nlp.front.shared.codec.ApplicationImportConfiguration
import fr.vsct.tock.nlp.front.shared.codec.DumpType
import fr.vsct.tock.nlp.front.shared.codec.ImportReport
import fr.vsct.tock.nlp.front.shared.codec.SentenceDump
import fr.vsct.tock.nlp.front.shared.codec.SentenceEntityDump
import fr.vsct.tock.nlp.front.shared.codec.SentencesDump
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.Classification
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentence
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus.validated
import fr.vsct.tock.nlp.front.shared.config.EntityDefinition
import fr.vsct.tock.nlp.front.shared.config.EntityTypeDefinition
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition
import fr.vsct.tock.nlp.front.shared.config.SentencesQuery
import fr.vsct.tock.shared.defaultLocale
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.name
import fr.vsct.tock.shared.namespace
import fr.vsct.tock.shared.provide
import fr.vsct.tock.shared.security.TockObfuscatorService.obfuscate
import fr.vsct.tock.shared.withoutNamespace
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import org.litote.kmongo.toId
import java.time.Instant

/**
 *
 */
object ApplicationCodecService : ApplicationCodec {

    private val logger = KotlinLogging.logger {}
    val config: ApplicationConfiguration get() = injector.provide()

    override fun export(applicationId: Id<ApplicationDefinition>, dumpType: DumpType): ApplicationDump {
        val app = config.getApplicationById(applicationId)!!
        val entities = config.getEntityTypes()
        val intents = config.getIntentsByApplicationId(applicationId)
        val sentences = config.getSentences(intents.map { it._id }.toSet())
        return ApplicationDump(app, entities, intents, sentences)
    }

    override fun prepareImport(dump: ApplicationDump): ApplicationImportConfiguration {
        return ApplicationImportConfiguration(dump.application.name)
    }

    override fun import(
        namespace: String,
        dump: ApplicationDump,
        configuration: ApplicationImportConfiguration
    ): ImportReport {
        logger.info { "Import dump..." }
        with(configuration) {
            val report = ImportReport()
            try {

                dump.entityTypes.forEach { e ->
                    if (!entityTypeExists(e.name)) {
                        val newEntity = e.copy(_id = newId())
                        config.save(newEntity)
                        report.add(newEntity)
                        logger.debug { "Import entity type $newEntity" }
                    }
                }

                val appName =
                    if (configuration.newApplicationName.isNullOrBlank()) dump.application.name else configuration.newApplicationName!!.trim()
                val app = config.getApplicationByNamespaceAndName(namespace, appName)
                    .let { app ->
                        if (app == null) {
                            val appToSave = dump.application.copy(
                                namespace = namespace,
                                name = appName,
                                intents = emptySet(),
                                intentStatesMap = emptyMap(),
                                _id = newId()
                            )
                            report.add(appToSave)
                            logger.debug { "Import application $appToSave" }
                            config.save(appToSave)
                        } else {
                            //a fresh empty model has been initialized before with the default locale
                            //then remove the default locale
                            if (
                                configuration.defaultModelMayExist
                                && app.supportedLocales.size == 1
                                && app.supportedLocales.contains(defaultLocale)
                                && app.intents.isEmpty()
                            ) {
                                app.copy(supportedLocales = emptySet())
                            } else {
                                app
                            }
                        }
                    }
                val appId = app._id

                val intentsToCreate = mutableListOf<IntentDefinition>()
                var intentsIdsMap = dump.intents.map { i ->
                    var intent = config.getIntentByNamespaceAndName(i.namespace, i.name)
                    if (intent == null) {
                        intent = i.copy(_id = newId(), applications = setOf(appId))
                        intentsToCreate.add(intent)
                    } else {
                        config.save(intent.copy(applications = intent.applications + appId))
                    }
                    i._id to intent._id
                }.toMap()

                //save new intents
                intentsToCreate.forEach { intent ->
                    val newIntent =
                        intent.copy(sharedIntents = intent.sharedIntents.mapNotNull { intentsIdsMap[it] }.toSet())
                    config.save(newIntent)
                    report.add(newIntent)
                    logger.debug { "Import intent $newIntent" }
                }

                //update application intent list & locales
                config.save(
                    app.copy(
                        intents = app.intents + intentsIdsMap.values.toSet(),
                        intentStatesMap = app.intentStatesMap + dump.application.intentStatesMap.mapKeys { intentsIdsMap[it.key]!! },
                        supportedLocales = app.supportedLocales + dump.application.supportedLocales
                    )
                )
                report.localeAdded = !app.supportedLocales.containsAll(dump.application.supportedLocales)

                //add unknown intent to intent map
                intentsIdsMap += (Intent.UNKNOWN_INTENT_NAME.toId<IntentDefinition>() to Intent.UNKNOWN_INTENT_NAME.toId<IntentDefinition>())

                dump.sentences.forEach { s ->
                    if (config.search(
                            SentencesQuery(
                                appId,
                                s.language,
                                search = s.text,
                                onlyExactMatch = true
                            )
                        ).total == 0L
                    ) {
                        logger.debug { "Import sentence ${s.text}" }
                        val sentence = s.copy(
                            applicationId = appId,
                            classification = s.classification.copy(
                                intentId = intentsIdsMap[s.classification.intentId]!!,
                                //ensure that entities are correctly sorted
                                entities = s.classification.entities.sortedBy { it.start }

                            ))
                        report.add(sentence)
                        config.save(sentence)
                    }
                }
                logger.info { "Dump imported! Result : $report" }

                //trigger build
                if (report.modified) {
                    triggerBuild(ModelBuildTrigger(appId, true))
                }

            } catch (t: Throwable) {
                logger.error(t)
                report.success = false
                report.addError(t.message ?: "exception without message")
            }

            return report
        }
    }

    override fun importSentences(namespace: String, dump: SentencesDump): ImportReport {
        logger.info { "Import Sentences dump..." }
        val report = ImportReport()
        try {
            var app =
                config.getApplicationByNamespaceAndName(namespace, dump.applicationName.withoutNamespace(namespace))
                    .let { app ->
                        if (app == null) {
                            val appToSave = ApplicationDefinition(
                                dump.applicationName.withoutNamespace(namespace),
                                namespace
                            )
                            report.add(appToSave)
                            logger.debug { "Import application $appToSave" }
                            config.save(appToSave)
                        } else {
                            app
                        }
                    }

            val appId = app._id
            val intentsByNameMap =
                config.getIntentsByApplicationId(appId)
                    .groupBy { it.qualifiedName }
                    .mapValues { it.value.first() }
                    .toMutableMap()
            val sentencesMap = config
                .getSentences(intentsByNameMap.values.map { it._id }.toSet())
                .groupBy { it.text }
                .toMutableMap()

            dump.sentences.forEach { s ->
                val language = s.language ?: dump.language
                if (language == null) {
                    report.addError("please specify a language for : ${s.text}")
                } else {
                    if (!app.supportedLocales.contains(language)) {
                        app = config.save(app.copy(supportedLocales = app.supportedLocales + language))
                    }

                    val intent: IntentDefinition? = if (s.intent == UNKNOWN_INTENT_NAME) {
                        null
                    } else {
                        val newIntent: IntentDefinition = intentsByNameMap[s.intent]
                            .let { newIntent ->
                                if (newIntent == null) {
                                    val intent =
                                        config.getIntentByNamespaceAndName(s.intent.namespace(), s.intent.name())
                                    if (intent != null) {
                                        val i = intent.copy(applications = intent.applications + appId)
                                        config.save(i)
                                        intentsByNameMap[intent.qualifiedName] = i
                                        config.getSentences(setOf(i._id))
                                            .forEach { sentence ->
                                                sentencesMap.compute(
                                                    sentence.text
                                                ) { _, v -> (v ?: emptyList()) + sentence }
                                            }
                                        i
                                    } else {
                                        IntentDefinition(
                                            s.intent.name(),
                                            s.intent.namespace(),
                                            setOf(appId),
                                            emptySet()
                                        ).run {
                                            config.save(this)
                                            intentsByNameMap[qualifiedName] = this
                                            report.add(this)
                                            this
                                        }
                                    }
                                } else {
                                    newIntent
                                }
                            }

                        s.entities.forEach { e ->
                            if (!entityTypeExists(e.entity)) {
                                val newEntity = EntityTypeDefinition(
                                    e.entity,
                                    ""
                                )
                                config.save(newEntity)
                                report.add(newEntity)
                            }
                            if (newIntent.entities.none { it.entityTypeName == e.entity && it.role == e.role }) {
                                val intentWithEntities = newIntent.copy(
                                    entities = newIntent.entities +
                                            EntityDefinition(
                                                e.entity,
                                                e.role
                                            )
                                )
                                config.save(intentWithEntities)
                                intentsByNameMap[intentWithEntities.qualifiedName] = intentWithEntities
                            }
                        }
                        newIntent
                    }

                    config.save(
                        ClassifiedSentence(
                            s.text,
                            language,
                            appId,
                            Instant.now(),
                            Instant.now(),
                            validated,
                            Classification(
                                intent?._id ?: UNKNOWN_INTENT_NAME.toId(),
                                s.entities.map { it.toClassifiedEntity() }
                            ),
                            1.0,
                            1.0)
                    )
                    report.sentencesImported++
                }
            }

            //update application with intents
            config.save(app.copy(intents = app.intents + intentsByNameMap.values.map { it._id }))

            //trigger build
            if (report.modified) {
                triggerBuild(ModelBuildTrigger(appId, true))
            }

            logger.info { "Sentences Dump imported! Result : $report" }
        } catch (t: Throwable) {
            logger.error(t)
            report.success = false
            report.addError(t.message ?: "exception without message")
        }
        return report
    }

    override fun exportSentences(
        applicationId: Id<ApplicationDefinition>,
        intent: String?,
        query: SentencesQuery?,
        dumpType: DumpType
    ): SentencesDump {
        val app = config.getApplicationById(applicationId)!!

        val filteredIntentId = if (intent == null) null else config.getIntentIdByQualifiedName(intent)

        val intents = config
            .getIntentsByApplicationId(applicationId)
            .filter { filteredIntentId == null || filteredIntentId == it._id }
            .groupBy { it._id }
            .mapValues { it.value.first() }

        val sentences = config
            .search(
                (query ?: SentencesQuery(applicationId, intentId = filteredIntentId))
                    .copy(start = 0, size = Integer.MAX_VALUE, searchMark = null)
            )
            .sentences

        return SentencesDump(
            app.qualifiedName,
            sentences = sentences.mapNotNull { s ->
                val sentenceIntent = intents[s.classification.intentId]
                if (sentenceIntent == null && s.classification.intentId != Intent.UNKNOWN_INTENT_NAME.toId<IntentDefinition>()) {
                    logger.warn { "unknown intent ${s.classification.intentId}" }
                    null
                } else {
                    if (dumpType == DumpType.obfuscated && obfuscate(s.text) != s.text) {
                        null
                    } else {
                        SentenceDump(
                            s.text,
                            sentenceIntent?.qualifiedName ?: Intent.UNKNOWN_INTENT_NAME,
                            s.classification.entities.map { SentenceEntityDump(it) },
                            s.language
                        )
                    }
                }
            }
        )
    }
}
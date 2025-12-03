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

package ai.tock.nlp.front.service

import ai.tock.nlp.core.Intent.Companion.UNKNOWN_INTENT_NAME
import ai.tock.nlp.core.NlpCore
import ai.tock.nlp.front.service.ConfigurationRepository.entityTypeExists
import ai.tock.nlp.front.service.ModelUpdaterService.triggerBuild
import ai.tock.nlp.front.shared.ApplicationCodec
import ai.tock.nlp.front.shared.ApplicationConfiguration
import ai.tock.nlp.front.shared.build.ModelBuildTrigger
import ai.tock.nlp.front.shared.codec.ApplicationDump
import ai.tock.nlp.front.shared.codec.ApplicationImportConfiguration
import ai.tock.nlp.front.shared.codec.DumpType
import ai.tock.nlp.front.shared.codec.ImportReport
import ai.tock.nlp.front.shared.codec.SentenceDump
import ai.tock.nlp.front.shared.codec.SentenceEntityDump
import ai.tock.nlp.front.shared.codec.SentencesDump
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.Classification
import ai.tock.nlp.front.shared.config.ClassifiedEntity
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus.model
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus.validated
import ai.tock.nlp.front.shared.config.EntityDefinition
import ai.tock.nlp.front.shared.config.EntityTypeDefinition
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.nlp.front.shared.config.SentencesQuery
import ai.tock.shared.changeNamespace
import ai.tock.shared.defaultLocale
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.name
import ai.tock.shared.namespace
import ai.tock.shared.provide
import ai.tock.shared.security.TockObfuscatorService.obfuscate
import ai.tock.shared.supportedLanguages
import ai.tock.shared.withoutNamespace
import ai.tock.translator.I18nLabel
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import org.litote.kmongo.toId
import java.time.Instant
import java.util.Locale
import kotlin.LazyThreadSafetyMode.PUBLICATION

/**
 *
 */
internal object ApplicationCodecService : ApplicationCodec {
    private val logger = KotlinLogging.logger {}
    val config: ApplicationConfiguration get() = injector.provide()
    private val core: NlpCore get() = injector.provide()

    private val builtInNamespaces: Set<String> by lazy(PUBLICATION) {
        core.getBuiltInEntityTypes().map { it.namespace() }.toSet()
    }

    override fun export(
        applicationId: Id<ApplicationDefinition>,
        dumpType: DumpType,
    ): ApplicationDump {
        val app = config.getApplicationById(applicationId)!!
        val entities = config.getEntityTypesByNamespaceAndSharedEntityTypes(app.namespace)
        val intents = config.getIntentsByApplicationId(applicationId)
        val sentences = config.getSentences(intents.map { it._id }.toSet()).sortedBy { it.updateDate }
        val faqs = config.getFaqsDefinitionByApplicationId(applicationId)
        return ApplicationDump(app, entities, intents, sentences, faqs)
    }

    override fun prepareImport(dump: ApplicationDump): ApplicationImportConfiguration {
        return ApplicationImportConfiguration(dump.application.name)
    }

    private fun EntityTypeDefinition.newName(namespace: String): String = name.newName(namespace)

    private fun EntityDefinition.newName(namespace: String): String = entityTypeName.newName(namespace)

    private fun String.newName(namespace: String): String {
        val n = this.namespace()
        return if (n == namespace || builtInNamespaces.contains(n)) {
            this
        } else {
            "$namespace:${name()}"
        }
    }

    private fun EntityDefinition.withNewName(namespace: String): EntityDefinition = copy(entityTypeName = newName(namespace))

    private fun changeEntityNames(
        entities: Set<EntityDefinition>,
        namespace: String,
    ): Set<EntityDefinition> = entities.asSequence().map { it.withNewName(namespace) }.toSet()

    private fun ClassifiedEntity.createEntityTypeIfNotExist(
        namespace: String,
        report: ImportReport? = null,
    ) {
        val newName = type.newName(namespace)
        val entityTypeDef =
            if (!entityTypeExists(newName)) {
                val newEntity =
                    EntityTypeDefinition(
                        newName,
                        "",
                    )
                config.save(newEntity)
                report?.add(newEntity)
                newEntity
            } else {
                config.getEntityTypeByName(newName)!!
            }

        if (subEntities.isNotEmpty()) {
            val newEntities =
                (
                    entityTypeDef.subEntities +
                        subEntities.map {
                            it.withNewName(namespace, true, report).let { e -> EntityDefinition(e.type, e.role) }
                        }
                )
                    .distinctBy { it.role }
            if (newEntities.size != entityTypeDef.subEntities.size) {
                config.save(entityTypeDef.copy(subEntities = newEntities))
            }
        }
    }

    private fun ClassifiedEntity.withNewName(
        namespace: String,
        createEntityTypeIfNotExist: Boolean = false,
        report: ImportReport? = null,
    ): ClassifiedEntity {
        if (createEntityTypeIfNotExist) {
            createEntityTypeIfNotExist(namespace, report)
        }
        return copy(
            type = type.newName(namespace),
            subEntities = changeEntityNames(subEntities, namespace),
        )
    }

    private fun changeEntityNames(
        entities: List<ClassifiedEntity>,
        namespace: String,
        createEntityTypeIfNotExist: Boolean = false,
        report: ImportReport? = null,
    ): List<ClassifiedEntity> = entities.map { it.withNewName(namespace, createEntityTypeIfNotExist, report) }

    override fun import(
        namespace: String,
        dump: ApplicationDump,
        configuration: ApplicationImportConfiguration,
    ): ImportReport {
        logger.info { "Import dump..." }
        val report = ImportReport()
        try {
            dump.entityTypes.forEach { e ->
                if (!entityTypeExists(e.newName(namespace))) {
                    val newEntity = e.copy(_id = newId(), name = e.newName(namespace))
                    config.save(newEntity)
                    report.add(newEntity)
                    logger.debug { "Import entity type $newEntity" }
                }
            }
            // register sub entities
            dump.entityTypes.forEach { e ->
                if (e.subEntities.isNotEmpty()) {
                    config.getEntityTypeByName(e.newName(namespace))?.run {
                        config.save(
                            copy(
                                subEntities =
                                    (
                                        subEntities +
                                            e.subEntities.map {
                                                EntityDefinition(
                                                    it.newName(namespace),
                                                    it.role,
                                                )
                                            }
                                    ).distinctBy { it.role },
                            ),
                        )
                    }
                }
            }

            val appName =
                if (configuration.newApplicationName.isNullOrBlank()) dump.application.name else configuration.newApplicationName!!.lowercase().trim()
            val app =
                config.getApplicationByNamespaceAndName(namespace, appName)
                    .let { app ->
                        if (app == null) {
                            val appToSave =
                                dump.application.copy(
                                    namespace = namespace,
                                    name = appName,
                                    intents = emptySet(),
                                    intentStatesMap = emptyMap(),
                                    _id = newId(),
                                )
                            report.add(appToSave)
                            logger.debug { "Import application $appToSave" }
                            config.save(appToSave)
                        } else {
                            // a fresh empty model has been initialized before with the default locale
                            // then remove the default locale
                            if (
                                configuration.defaultModelMayExist &&
                                app.supportedLocales.size == 1 &&
                                app.supportedLocales.contains(defaultLocale) &&
                                app.intents.isEmpty()
                            ) {
                                app.copy(supportedLocales = emptySet())
                            } else {
                                app
                            }
                        }
                    }
            val appId = app._id
            val botId = app.name

            val intentsToCreate = mutableListOf<IntentDefinition>()
            val intentsIdsMap =
                dump.intents.map { i ->
                    var intent = config.getIntentByNamespaceAndName(namespace, i.name)
                    if (intent == null) {
                        intent =
                            i.copy(
                                _id = newId(),
                                namespace = namespace,
                                entities = changeEntityNames(i.entities, namespace),
                                applications = setOf(appId),
                                description = i.description?.replace("<br>", "\n")?.replace("</br>", "\n"),
                            )
                        intentsToCreate.add(intent)
                    } else {
                        config.save(intent.copy(namespace = namespace, applications = intent.applications + appId))
                    }
                    i._id to intent._id
                }.toMap().toMutableMap()

            // save new intents
            intentsToCreate.forEach { intent ->
                val newIntent =
                    intent.copy(
                        sharedIntents =
                            intent.sharedIntents.asSequence().mapNotNull { intentsIdsMap[it] }
                                .toSet(),
                    )
                config.save(newIntent)
                report.add(newIntent)
                logger.debug { "Import intent $newIntent" }
            }

            // update application intent list & locales
            config.save(
                app.copy(
                    intents = app.intents + intentsIdsMap.values.toSet(),
                    intentStatesMap = app.intentStatesMap + dump.application.intentStatesMap.mapKeys { intentsIdsMap[it.key]!! },
                    supportedLocales = app.supportedLocales + dump.application.supportedLocales,
                ),
            )
            report.localeAdded = !app.supportedLocales.containsAll(dump.application.supportedLocales)

            // add unknown intent to intent map
            intentsIdsMap[UNKNOWN_INTENT_NAME.toId()] = UNKNOWN_INTENT_NAME.toId()

            dump.sentences.forEach { s ->
                if (config.search(
                        SentencesQuery(
                            appId,
                            s.language,
                            search = s.text,
                            onlyExactMatch = true,
                            configuration = null,
                        ),
                    ).total == 0L
                ) {
                    logger.debug { "Import sentence ${s.text}" }
                    val sentence =
                        s.copy(
                            applicationId = appId,
                            classification =
                                s.classification.copy(
                                    intentId = intentsIdsMap[s.classification.intentId]!!,
                                    // ensure that entities are correctly sorted
                                    entities = changeEntityNames(s.classification.entities, namespace).sortedBy { it.start },
                                ),
                        )
                    report.add(sentence)
                    config.save(sentence)
                }
            }

            dump.faqs.forEach {
                val intentDump = dump.intents.first { intent -> intent._id == it.intentId }
                val intentDB = config.getIntentByNamespaceAndName(namespace, intentDump.name)!!
                val faq = config.getFaqDefinitionByIntentId(intentDB._id)
                if (faq == null) {
                    // update i18nId namespace for import current namespace
                    val i18nOldNamespace = it.i18nId.namespace()
                    val newI18nId = it.i18nId.toString().replaceFirst(i18nOldNamespace, namespace)
                    val newFaq =
                        it.copy(
                            _id = newId(),
                            botId = botId,
                            namespace = namespace,
                            intentId = intentDB._id,
                            i18nId = newI18nId.toId(),
                        )
                    report.add(newFaq)
                    config.save(newFaq)
                    logger.debug { "Import faq $newFaq" }
                } else {
                    config.save(faq.copy(enabled = it.enabled, tags = it.tags))
                }
            }

            logger.info { "Dump imported! Result : $report" }

            // trigger build
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

    /**
     * Retrieve namespace from i18nLabel
     */
    fun Id<I18nLabel>.namespace(): String = this.toString().substringBefore('_')

    override fun importSentences(
        namespace: String,
        dump: SentencesDump,
    ): ImportReport {
        logger.info { "Import Sentences dump..." }
        val report = ImportReport()
        try {
            val appName = dump.applicationName.withoutNamespace()
            var app =
                config.getApplicationByNamespaceAndName(namespace, appName)
                    .let { app ->
                        if (app == null) {
                            val appToSave = ApplicationDefinition(appName, appName, namespace)
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

            dump.sentences.forEach { s ->
                val language = (s.language ?: dump.language)?.language
                if (language == null) {
                    report.addError("please specify a language for : ${s.text}")
                } else if (!supportedLanguages.containsKey(language)) {
                    report.addError("unknown language : $language")
                } else {
                    val locale = Locale(language)
                    if (!app.supportedLocales.contains(locale)) {
                        app = config.save(app.copy(supportedLocales = app.supportedLocales + locale))
                    }

                    val intent: IntentDefinition? =
                        if (s.intent == UNKNOWN_INTENT_NAME) {
                            null
                        } else {
                            // force intent namespace
                            val sentenceIntent = s.intent.changeNamespace(namespace)
                            val newIntent: IntentDefinition =
                                intentsByNameMap[sentenceIntent]
                                    .let { newIntent ->
                                        if (newIntent == null) {
                                            val intent =
                                                config.getIntentByNamespaceAndName(namespace, sentenceIntent.name())
                                            if (intent != null) {
                                                val i = intent.copy(applications = intent.applications + appId)
                                                config.save(i)
                                                intentsByNameMap[intent.qualifiedName] = i
                                                i
                                            } else {
                                                IntentDefinition(
                                                    sentenceIntent.name(),
                                                    namespace,
                                                    setOf(appId),
                                                    emptySet(),
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
                                val newName = e.entity.newName(namespace)
                                if (newIntent.entities.none { it.entityTypeName == newName && it.role == e.role }) {
                                    val intentWithEntities =
                                        newIntent.copy(
                                            entities =
                                                newIntent.entities +
                                                    EntityDefinition(
                                                        newName,
                                                        e.role,
                                                    ),
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
                            locale,
                            appId,
                            Instant.now(),
                            Instant.now(),
                            // need to switch model status to validated in order to trigger model rebuild
                            s.status.takeUnless { it == model } ?: validated,
                            Classification(
                                intent?._id ?: UNKNOWN_INTENT_NAME.toId(),
                                changeEntityNames(s.entities.map { it.toClassifiedEntity() }.sortedBy { it.start }, namespace, true, report),
                            ),
                            1.0,
                            1.0,
                            forReview = s.forReview,
                            reviewComment = s.reviewComment,
                        ),
                    )
                    report.sentencesImported++
                }
            }

            // update application with intents
            config.save(app.copy(intents = app.intents + intentsByNameMap.values.map { it._id }))

            // trigger build
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
        dumpType: DumpType,
        intent: String?,
        locale: Locale?,
    ): SentencesDump {
        val filteredIntentId = if (intent == null) null else config.getIntentIdByQualifiedName(intent)
        return exportSentences(
            SentencesQuery(applicationId, intentId = filteredIntentId, language = locale),
            dumpType,
        )
    }

    override fun exportSentences(
        queries: List<SentencesQuery>,
        dumpType: DumpType,
    ): SentencesDump {
        // Suppose all queries in the same application/locale/etc.
        val query = queries.first()
        val applicationId = query.applicationId
        val app = config.getApplicationById(applicationId)!!
        val filteredIntentId = query.intentId

        val intents =
            config
                .getIntentsByApplicationId(applicationId)
                .filter { filteredIntentId == null || filteredIntentId == it._id }
                .groupBy { it._id }
                .mapValues { it.value.first() }

        val sentences =
            queries
                .flatMap {
                    config
                        .search(it.copy(start = 0, size = Integer.MAX_VALUE, searchMark = null))
                        .sentences
                }
                .sortedBy { it.updateDate }

        return SentencesDump(
            app.qualifiedName,
            sentences =
                sentences.mapNotNull { s ->
                    val sentenceIntent = intents[s.classification.intentId]
                    if (sentenceIntent == null && s.classification.intentId != UNKNOWN_INTENT_NAME.toId<IntentDefinition>()) {
                        logger.warn { "unknown intent ${s.classification.intentId}" }
                        null
                    } else {
                        if (dumpType == DumpType.obfuscated && obfuscate(s.text) != s.text) {
                            null
                        } else {
                            SentenceDump(
                                s.text,
                                sentenceIntent?.qualifiedName ?: UNKNOWN_INTENT_NAME,
                                s.creationDate,
                                s.classification.entities.map { SentenceEntityDump(it) },
                                s.language,
                                s.status,
                                s.forReview,
                                s.reviewComment,
                            )
                        }
                    }
                },
        )
    }
}

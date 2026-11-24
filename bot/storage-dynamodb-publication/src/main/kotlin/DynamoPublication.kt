/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.storage.dynamodb.publication

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.bot.BotApplicationConfigurationDAO
import ai.tock.bot.admin.bot.BotConfiguration
import ai.tock.bot.admin.indicators.Indicator
import ai.tock.bot.admin.indicators.IndicatorDAO
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.engine.feature.FeatureDAO
import ai.tock.bot.engine.feature.FeatureState
import ai.tock.nlp.core.DictionaryData
import ai.tock.nlp.front.storage.dynamodb.ApplicationDefinitionDynamoDAO
import ai.tock.nlp.front.storage.dynamodb.EntityTypeDefinitionDynamoDAO
import ai.tock.nlp.front.storage.dynamodb.IntentDefinitionDynamoDAO
import ai.tock.nlp.front.storage.dynamodb.NamespaceConfigurationDynamoDAO
import ai.tock.storage.dynamodb.BotApplicationConfigurationDynamoDAO
import ai.tock.storage.dynamodb.DynamoChanges
import ai.tock.storage.dynamodb.FeatureDynamoDAO
import ai.tock.storage.dynamodb.I18nDynamoDAO
import ai.tock.storage.dynamodb.IndicatorDynamoDAO
import ai.tock.storage.dynamodb.StoryDefinitionConfigurationDynamoDAO
import ai.tock.translator.I18nDAO
import ai.tock.translator.I18nLabel
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.litote.kmongo.toId
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Publishes the configuration of the admin, read from its DAOs (MongoDB), to the DynamoDB storage of the
 * serverless runtime: the bot application configurations, the bot configurations, the stories, the features,
 * the labels, the RAG, vector store, observability, document compressor and business rules configurations, the
 * indicators, and, with [nlp], the NLP applications, intents, entity types, dictionaries and validated sentences
 * (see [SentencePublication]).
 *
 * - a publication compares each item with its DynamoDB version and writes only the changed ones;
 * - it deletes only the items it has published (see [PublicationManifest]): the writes of the runtime are kept;
 * - an item of the admin replaces the item of the runtime that has the same unique key (the intent of a story);
 * - the runtimes are notified of the kinds that have changed (see [DynamoChanges]).
 *
 * [start] publishes everything, then publishes again when the admin DAOs report a change (MongoDB change streams,
 * debounced), and every [interval] in any case: the features have no change listener, and a change stream
 * can miss changes (replica set unavailable, restart).
 */
class DynamoPublication(
    private val applicationConfigurations: BotApplicationConfigurationDAO,
    private val stories: StoryDefinitionConfigurationDAO,
    private val features: FeatureDAO,
    private val labels: I18nDAO,
    private val settings: BotSettingsSources = BotSettingsSources(),
    private val indicators: IndicatorDAO? = null,
    private val nlp: NlpSources? = null,
    private val interval: Duration = Duration.ofMinutes(5),
    private val debounce: Duration = Duration.ofSeconds(1),
    sentenceCompleteInterval: Duration = Duration.ofHours(1),
) {
    private val logger = KotlinLogging.logger {}

    /** The kinds of published items, in the order of a publication: the scopes of the next ones come from the first ones. */
    enum class Kind { APPLICATION, BOT, STORY, FEATURE, LABEL, SETTINGS, INDICATOR, NLP_APPLICATION, NLP_INTENT, NLP_ENTITY_TYPE, NLP_DICTIONARY, NLP_NAMESPACE, NLP_SENTENCE }

    private val nlpKinds = setOf(Kind.NLP_APPLICATION, Kind.NLP_INTENT, Kind.NLP_ENTITY_TYPE, Kind.NLP_DICTIONARY, Kind.NLP_NAMESPACE, Kind.NLP_SENTENCE)

    private val sentences = nlp?.let { SentencePublication(it, sentenceCompleteInterval) }

    /** The result of a publication, by kind. */
    data class Report(
        val written: Map<Kind, Int>,
        val deleted: Map<Kind, Int>,
        val failed: Map<Kind, Int>,
    ) {
        val isEmpty: Boolean get() = written.values.sum() + deleted.values.sum() + failed.values.sum() == 0
    }

    private class Counters {
        val written = ConcurrentHashMap<Kind, Int>()
        val deleted = ConcurrentHashMap<Kind, Int>()
        val failed = ConcurrentHashMap<Kind, Int>()

        fun report() = Report(written.toMap(), deleted.toMap(), failed.toMap())
    }

    private var scheduler: ScheduledExecutorService? = null
    private val pending = ConcurrentHashMap.newKeySet<Kind>()
    private val scheduled = AtomicBoolean()

    /** Publishes every kind now, in the calling thread. */
    fun publishAll(): Report = publish(Kind.entries.toSet())

    /** Publishes [kinds] now, in the calling thread. */
    @Synchronized
    fun publish(kinds: Set<Kind>): Report {
        val counters = Counters()
        Kind.entries.filter { it in kinds }.forEach { kind ->
            try {
                when (kind) {
                    Kind.APPLICATION -> publishApplications(counters)
                    Kind.BOT -> publishBots(counters)
                    Kind.STORY -> publishStories(counters)
                    Kind.FEATURE -> publishFeatures(counters)
                    Kind.LABEL -> publishLabels(counters)
                    Kind.SETTINGS -> publishSettings(counters)
                    Kind.INDICATOR -> publishIndicators(counters)
                    Kind.NLP_APPLICATION -> publishNlpApplications(counters)
                    Kind.NLP_INTENT -> publishNlpIntents(counters)
                    Kind.NLP_ENTITY_TYPE -> publishNlpEntityTypes(counters)
                    Kind.NLP_DICTIONARY -> publishNlpDictionaries(counters)
                    Kind.NLP_NAMESPACE -> publishNlpNamespaces(counters)
                    Kind.NLP_SENTENCE -> publishSentences(counters)
                }
            } catch (e: Exception) {
                logger.error(e) { "the publication of the ${kind.name.lowercase()} items to DynamoDB failed" }
                counters.failed.merge(kind, 1, Int::plus)
            }
        }
        return counters.report().also { report ->
            if (!report.isEmpty) logger.info { "published to DynamoDB: $report" }
            notifyRuntimes(report)
        }
    }

    /** Lets the runtimes reload the kinds that have changed (see [DynamoChanges]). */
    private fun notifyRuntimes(report: Report) {
        // the features are cached for a short time, the indicators and the sentences are read at each use
        val channels =
            (report.written.keys + report.deleted.keys).mapNotNull { kind ->
                when (kind) {
                    Kind.APPLICATION -> DynamoChanges.BOT_APPLICATION
                    Kind.BOT -> DynamoChanges.BOT_CONFIGURATION
                    Kind.STORY -> DynamoChanges.STORY
                    Kind.LABEL -> DynamoChanges.LABEL
                    Kind.SETTINGS -> DynamoChanges.SETTINGS
                    Kind.NLP_APPLICATION -> DynamoChanges.NLP_APPLICATION
                    Kind.NLP_INTENT -> DynamoChanges.NLP_INTENT
                    Kind.NLP_ENTITY_TYPE -> DynamoChanges.NLP_ENTITY_TYPE
                    Kind.NLP_DICTIONARY -> DynamoChanges.NLP_DICTIONARY
                    Kind.NLP_NAMESPACE -> DynamoChanges.NLP_NAMESPACE
                    Kind.FEATURE, Kind.INDICATOR, Kind.NLP_SENTENCE -> null
                }
            }
        try {
            DynamoChanges.changed(channels)
        } catch (e: Exception) {
            // the next publication of these kinds notifies the runtimes
            logger.error(e) { "the runtimes cannot be notified of the publication of $channels" }
        }
    }

    /** Publishes everything in background, then the changes, and everything every [interval]. */
    fun start() {
        check(scheduler == null) { "already started" }
        val executor =
            Executors.newSingleThreadScheduledExecutor { Thread(it, "tock-dynamodb-publication").apply { isDaemon = true } }
        scheduler = executor
        executor.scheduleWithFixedDelay({ publishAll() }, 0, interval.toMillis(), TimeUnit.MILLISECONDS)
        // the scopes of the stories, features and labels are the bots of the configurations
        listen("application configurations") { applicationConfigurations.listenChanges { request(Kind.entries.toSet()) } }
        listen("bot configurations") { applicationConfigurations.listenBotChanges { request(Kind.entries.toSet()) } }
        listen("stories") { stories.listenChanges { request(setOf(Kind.STORY)) } }
        listen("labels") { labels.listenI18n { request(setOf(Kind.LABEL)) } }
        if (nlp != null) {
            // a deleted intent or entity changes the sentences without changing their update date
            val structure = {
                sentences?.requestComplete()
                request(nlpKinds)
            }
            listen("NLP applications") { nlp.applications.listenApplicationDefinitionChanges(structure) }
            listen("intents") { nlp.intents.listenIntentDefinitionChanges(structure) }
            listen("entity types") { nlp.entityTypes.listenEntityTypeChanges(structure) }
            listen("dictionaries") { nlp.entityTypes.listenDictionaryDataChanges { request(setOf(Kind.NLP_DICTIONARY)) } }
            nlp.namespaces?.let { namespaces -> listen("namespace configurations") { namespaces.listenNamespaceConfigurationChanges { request(setOf(Kind.NLP_NAMESPACE)) } } }
        }
        logger.info { "publication of the admin configuration to DynamoDB started" }
    }

    /** A change stream needs a replica set: without it, the periodic publications still run. */
    private fun listen(
        name: String,
        subscribe: () -> Unit,
    ) {
        try {
            subscribe()
        } catch (e: Exception) {
            logger.warn(e) { "the changes of the $name are not listened: they are published every $interval" }
        }
    }

    fun stop() {
        scheduler?.shutdownNow()
        scheduler = null
    }

    /** Publishes [kinds] after [debounce]: a change in the admin often writes several documents. */
    private fun request(kinds: Set<Kind>) {
        pending += kinds
        val executor = scheduler ?: return
        if (scheduled.compareAndSet(false, true)) {
            executor.schedule(
                {
                    scheduled.set(false)
                    val requested = Kind.entries.filter { pending.remove(it) }.toSet()
                    if (requested.isNotEmpty()) publish(requested)
                },
                debounce.toMillis(),
                TimeUnit.MILLISECONDS,
            )
        }
    }

    /**
     * Publishes the [source] items of [kind]: writes the ones that differ from [target], deletes the published
     * ones that are not in [source] anymore.
     */
    private fun <T> sync(
        kind: Kind,
        counters: Counters,
        source: Map<String, T>,
        target: Map<String, T>,
        ref: (T) -> Map<String, String>,
        write: (T) -> Unit,
        delete: (key: String, ref: Map<String, String>) -> Unit,
        same: (T, T) -> Boolean = { a, b -> a == b },
    ) {
        val published = PublicationManifest.published(kind.name)
        // the deletions first: they free the unique keys of the items to write
        (published.keys - source.keys).forEach { key ->
            runItem(kind, counters, key) {
                delete(key, published.getValue(key))
                PublicationManifest.remove(kind.name, key)
                counters.deleted.merge(kind, 1, Int::plus)
            }
        }
        source.forEach { (key, item) ->
            runItem(kind, counters, key) {
                val current = target[key]
                if (current == null || !same(current, item)) {
                    write(item)
                    counters.written.merge(kind, 1, Int::plus)
                }
                // a label can change its namespace: the reference is kept up to date
                val itemRef = ref(item)
                if (published[key] != itemRef) {
                    PublicationManifest.add(kind.name, key, itemRef)
                }
            }
        }
    }

    private fun runItem(
        kind: Kind,
        counters: Counters,
        key: String,
        block: () -> Unit,
    ) {
        try {
            block()
        } catch (e: Exception) {
            logger.error(e) { "the publication of the ${kind.name.lowercase()} $key to DynamoDB failed" }
            counters.failed.merge(kind, 1, Int::plus)
        }
    }

    // ---------------------------------------------------------------- scopes

    private data class Bot(
        val namespace: String,
        val botId: String,
    )

    /** The bots of the admin, and the bots of the published items of [kind]: their deleted items must be deleted. */
    private fun bots(kind: Kind): Set<Bot> =
        applicationConfigurations.getConfigurations().map { Bot(it.namespace, it.botId) }.toSet() +
            applicationConfigurations.getBotConfigurations().map { Bot(it.namespace, it.botId) } +
            PublicationManifest.published(kind.name).values.mapNotNull { ref ->
                ref["namespace"]?.let { namespace -> ref["botId"]?.let { Bot(namespace, it) } }
            }

    // ---------------------------------------------------------------- kinds

    private fun publishApplications(counters: Counters) =
        sync(
            Kind.APPLICATION,
            counters,
            source = applicationConfigurations.getConfigurations().associateBy { it._id.toString() },
            target = BotApplicationConfigurationDynamoDAO.getConfigurations().associateBy { it._id.toString() },
            ref = { mapOf("namespace" to it.namespace, "botId" to it.botId) },
            write = { conf ->
                // an application of the runtime with the same unique keys (namespace, application, bot / path) is replaced
                BotApplicationConfigurationDynamoDAO
                    .getConfigurations()
                    .filter { it._id != conf._id && (it.sameApplication(conf) || (conf.path != null && it.path == conf.path)) }
                    .forEach { BotApplicationConfigurationDynamoDAO.delete(it) }
                BotApplicationConfigurationDynamoDAO.save(conf)
            },
            delete = { key, _ -> BotApplicationConfigurationDynamoDAO.getConfigurationById(key.toId())?.let { BotApplicationConfigurationDynamoDAO.delete(it) } },
        )

    private fun BotApplicationConfiguration.sameApplication(other: BotApplicationConfiguration) = namespace == other.namespace && applicationId == other.applicationId && botId == other.botId

    private fun BotConfiguration.key() = "$namespace#$botId#$name"

    private fun publishBots(counters: Counters) =
        sync(
            Kind.BOT,
            counters,
            source = applicationConfigurations.getBotConfigurations().associateBy { it.key() },
            target = BotApplicationConfigurationDynamoDAO.getBotConfigurations().associateBy { it.key() },
            ref = { mapOf("namespace" to it.namespace, "botId" to it.botId, "name" to it.name) },
            write = { BotApplicationConfigurationDynamoDAO.save(it) },
            delete = { _, ref ->
                BotApplicationConfigurationDynamoDAO
                    .getBotConfigurationsByNamespaceAndNameAndBotId(ref.getValue("namespace"), ref.getValue("name"), ref.getValue("botId"))
                    ?.let { BotApplicationConfigurationDynamoDAO.delete(it) }
            },
        )

    private fun StoryDefinitionConfiguration.key() = "$namespace#$botId#$_id"

    private fun publishStories(counters: Counters) {
        val bots = bots(Kind.STORY)
        val target = bots.flatMap { StoryDefinitionConfigurationDynamoDAO.getStoryDefinitionsByNamespaceAndBotId(it.namespace, it.botId) }
        sync(
            Kind.STORY,
            counters,
            source = bots.flatMap { stories.getStoryDefinitionsByNamespaceAndBotId(it.namespace, it.botId) }.associateBy { it.key() },
            target = target.associateBy { it.key() },
            ref = { mapOf("namespace" to it.namespace, "botId" to it.botId, "id" to it._id.toString()) },
            write = { story ->
                try {
                    StoryDefinitionConfigurationDynamoDAO.save(story)
                } catch (_: IllegalArgumentException) {
                    // another story has the intent: a built-in story created by the runtime, or a published story whose
                    // intent has changed (it is written again with its new intent if it is still in the admin)
                    StoryDefinitionConfigurationDynamoDAO
                        .getStoryDefinitionsByNamespaceAndBotId(story.namespace, story.botId)
                        .filter { it._id != story._id && it.intent.name == story.intent.name }
                        .forEach { StoryDefinitionConfigurationDynamoDAO.delete(it) }
                    StoryDefinitionConfigurationDynamoDAO.save(story)
                }
            },
            delete = { _, ref ->
                StoryDefinitionConfigurationDynamoDAO
                    .getStoryDefinitionsByNamespaceAndBotId(ref.getValue("namespace"), ref.getValue("botId"))
                    .firstOrNull { it._id.toString() == ref.getValue("id") }
                    ?.let { StoryDefinitionConfigurationDynamoDAO.delete(it) }
            },
        )
    }

    private data class PublishedFeature(
        val bot: Bot,
        val state: FeatureState,
    ) {
        val key: String get() = "${bot.botId},${bot.namespace},${state.category},${state.name}${state.applicationId?.let { "+$it" } ?: ""}"

        /** The dates of DynamoDB are in UTC: the instants are compared. */
        fun sameAs(other: PublishedFeature): Boolean =
            state.copy(startDate = null, endDate = null) == other.state.copy(startDate = null, endDate = null) &&
                state.startDate?.toInstant() == other.state.startDate?.toInstant() &&
                state.endDate?.toInstant() == other.state.endDate?.toInstant()
    }

    private fun publishFeatures(counters: Counters) {
        val bots = bots(Kind.FEATURE)

        fun read(dao: FeatureDAO) =
            bots
                .flatMap { bot -> runBlocking { dao.getFeatures(bot.botId, bot.namespace) }.map { PublishedFeature(bot, it) } }
                .associateBy { it.key }
        sync(
            Kind.FEATURE,
            counters,
            source = read(features),
            target = read(FeatureDynamoDAO),
            ref = {
                mapOf("namespace" to it.bot.namespace, "botId" to it.bot.botId, "category" to it.state.category, "name" to it.state.name) +
                    listOfNotNull(it.state.applicationId?.let { id -> "applicationId" to id })
            },
            write = {
                runBlocking {
                    FeatureDynamoDAO.addFeature(
                        it.bot.botId,
                        it.bot.namespace,
                        it.state.enabled,
                        it.state.category,
                        it.state.name,
                        it.state.startDate,
                        it.state.endDate,
                        it.state.applicationId,
                        it.state.graduation,
                    )
                }
            },
            delete = { _, ref ->
                runBlocking {
                    FeatureDynamoDAO.deleteFeature(ref.getValue("botId"), ref.getValue("namespace"), ref.getValue("category"), ref.getValue("name"), ref["applicationId"])
                }
            },
            same = { a, b -> a.sameAs(b) },
        )
    }

    private fun publishLabels(counters: Counters) {
        val namespaces = bots(Kind.LABEL).map { it.namespace }.toSet()
        sync(
            Kind.LABEL,
            counters,
            source = namespaces.flatMap { labels.getLabels(it) }.associateBy { it._id.toString() },
            target = namespaces.flatMap { I18nDynamoDAO.getLabels(it) }.associateBy { it._id.toString() },
            ref = { mapOf("namespace" to it.namespace) },
            write = { label: I18nLabel -> I18nDynamoDAO.publish(label) },
            delete = { key, ref -> I18nDynamoDAO.deleteByNamespaceAndId(ref.getValue("namespace"), key.toId()) },
        )
    }

    private data class PublishedSettings(
        val source: BotSettingsSource<*>,
        val bot: Bot,
        val settings: Any,
    ) {
        val key: String get() = "${source.kind}#${bot.namespace}#${bot.botId}"
    }

    private fun publishSettings(counters: Counters) {
        val bots = bots(Kind.SETTINGS)

        fun read(fromAdmin: Boolean) =
            settings.all
                .flatMap { source ->
                    bots.mapNotNull { bot ->
                        (if (fromAdmin) source.admin(bot.namespace, bot.botId) else source.runtime(bot.namespace, bot.botId))
                            ?.let { PublishedSettings(source, bot, it) }
                    }
                }.associateBy { it.key }
        sync(
            Kind.SETTINGS,
            counters,
            source = read(fromAdmin = true),
            target = read(fromAdmin = false),
            ref = { mapOf("kind" to it.source.kind, "namespace" to it.bot.namespace, "botId" to it.bot.botId) },
            write = { it.source.publish(it.settings) },
            delete = { _, ref -> settings.all.firstOrNull { it.kind == ref["kind"] }?.delete(ref.getValue("namespace"), ref.getValue("botId")) },
            same = { a, b -> a.settings == b.settings },
        )
    }

    private fun Indicator.key() = "$namespace#$botId#$name"

    private fun publishIndicators(counters: Counters) {
        val dao = indicators ?: return
        val bots = bots(Kind.INDICATOR)
        sync(
            Kind.INDICATOR,
            counters,
            source = bots.flatMap { dao.findAllByBotId(it.namespace, it.botId) }.associateBy { it.key() },
            target = bots.flatMap { IndicatorDynamoDAO.findAllByBotId(it.namespace, it.botId) }.associateBy { it.key() },
            ref = { mapOf("namespace" to it.namespace, "botId" to it.botId, "name" to it.name) },
            write = { IndicatorDynamoDAO.save(it) },
            delete = { _, ref ->
                IndicatorDynamoDAO.findByNameAndBotId(ref.getValue("name"), ref.getValue("namespace"), ref.getValue("botId"))?.let { IndicatorDynamoDAO.delete(it) }
            },
        )
    }

    // ---------------------------------------------------------------- NLP

    private fun publishNlpApplications(counters: Counters) {
        val sources = nlp ?: return
        sync(
            Kind.NLP_APPLICATION,
            counters,
            source = sources.applications.getApplications().associateBy { it._id.toString() },
            target = ApplicationDefinitionDynamoDAO.getApplications().associateBy { it._id.toString() },
            ref = { mapOf("namespace" to it.namespace, "name" to it.name) },
            write = { application ->
                // an application of the runtime with the same name is replaced
                ApplicationDefinitionDynamoDAO
                    .getApplications()
                    .filter { it._id != application._id && it.namespace == application.namespace && it.name == application.name }
                    .forEach { ApplicationDefinitionDynamoDAO.documents.delete(it._id.toString()) }
                ApplicationDefinitionDynamoDAO.documents.save(application)
            },
            delete = { key, _ -> ApplicationDefinitionDynamoDAO.documents.delete(key) },
        )
    }

    private fun publishNlpIntents(counters: Counters) {
        val sources = nlp ?: return
        sync(
            Kind.NLP_INTENT,
            counters,
            // an intent can be shared by several applications
            source =
                sources.applications
                    .getApplications()
                    .flatMap { sources.intents.getIntentsByApplicationId(it._id) }
                    .associateBy { it._id.toString() },
            target = IntentDefinitionDynamoDAO.documents.all().associateBy { it._id.toString() },
            ref = { mapOf("namespace" to it.namespace, "name" to it.name) },
            write = { IntentDefinitionDynamoDAO.documents.save(it) },
            delete = { key, _ -> IntentDefinitionDynamoDAO.documents.delete(key) },
        )
    }

    private fun publishNlpEntityTypes(counters: Counters) {
        val sources = nlp ?: return
        sync(
            Kind.NLP_ENTITY_TYPE,
            counters,
            source = sources.entityTypes.getEntityTypes().associateBy { it.name },
            target = EntityTypeDefinitionDynamoDAO.getEntityTypes().associateBy { it.name },
            ref = { emptyMap() },
            write = { EntityTypeDefinitionDynamoDAO.save(it) },
            delete = { key, _ -> EntityTypeDefinitionDynamoDAO.entityTypes.delete(key) },
        )
    }

    private fun DictionaryData.key() = "$namespace:$entityName"

    private fun publishNlpDictionaries(counters: Counters) {
        val sources = nlp ?: return
        sync(
            Kind.NLP_DICTIONARY,
            counters,
            source = sources.entityTypes.getAllDictionaryData().associateBy { it.key() },
            target = EntityTypeDefinitionDynamoDAO.getAllDictionaryData().associateBy { it.key() },
            ref = { emptyMap() },
            write = { EntityTypeDefinitionDynamoDAO.save(it) },
            delete = { key, _ -> EntityTypeDefinitionDynamoDAO.dictionaries.delete(key) },
        )
    }

    /** The configurations of the namespaces of the applications: the imports of their intents. */
    private fun publishNlpNamespaces(counters: Counters) {
        val sources = nlp ?: return
        val dao = sources.namespaces ?: return
        val namespaces =
            sources.applications
                .getApplications()
                .map { it.namespace }
                .toSet()
        sync(
            Kind.NLP_NAMESPACE,
            counters,
            source = namespaces.mapNotNull { dao.getNamespaceConfiguration(it) }.associateBy { it.namespace },
            target = NamespaceConfigurationDynamoDAO.documents.all().associateBy { it.namespace },
            ref = { emptyMap() },
            write = { NamespaceConfigurationDynamoDAO.documents.save(it) },
            delete = { key, _ -> NamespaceConfigurationDynamoDAO.documents.delete(key) },
        )
    }

    private fun publishSentences(counters: Counters) {
        val publication = sentences ?: return
        val result = publication.publish()
        if (result.written > 0) counters.written.merge(Kind.NLP_SENTENCE, result.written, Int::plus)
        if (result.deleted > 0) counters.deleted.merge(Kind.NLP_SENTENCE, result.deleted, Int::plus)
    }
}

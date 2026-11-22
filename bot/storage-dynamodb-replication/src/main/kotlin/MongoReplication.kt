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

package ai.tock.storage.dynamodb.replication

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.bot.BotApplicationConfigurationDAO
import ai.tock.bot.admin.bot.BotConfiguration
import ai.tock.bot.admin.indicators.Indicator
import ai.tock.bot.admin.indicators.IndicatorDAO
import ai.tock.bot.admin.indicators.metric.Metric
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.Snapshot
import ai.tock.bot.engine.feature.FeatureDAO
import ai.tock.bot.engine.user.PlayerType
import ai.tock.bot.mongo.ArchivedEntityValuesCol
import ai.tock.bot.mongo.ConnectorMessageCol
import ai.tock.bot.mongo.ConnectorMessageColId
import ai.tock.bot.mongo.DialogCol
import ai.tock.bot.mongo.NlpStatsCol
import ai.tock.bot.mongo.NlpStatsColId
import ai.tock.bot.mongo.SnapshotCol
import ai.tock.bot.mongo.UserTimelineCol
import ai.tock.nlp.front.service.storage.ClassifiedSentenceDAO
import ai.tock.nlp.front.service.storage.ParseRequestLogDAO
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus.inbox
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus.model
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus.validated
import ai.tock.nlp.front.shared.config.SentencesQuery
import ai.tock.nlp.front.shared.monitoring.ParseRequestLog
import ai.tock.nlp.front.storage.dynamodb.ClassifiedSentenceDynamoDAO
import ai.tock.storage.dynamodb.DOC
import ai.tock.storage.dynamodb.DocumentCodec
import ai.tock.storage.dynamodb.FeatureDynamoDAO
import ai.tock.storage.dynamodb.PK
import ai.tock.storage.dynamodb.SK
import ai.tock.storage.dynamodb.UserTimelineDynamoDAO
import ai.tock.translator.I18nDAO
import ai.tock.translator.I18nLabel
import ai.tock.translator.I18nLabelStat
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.kotlin.client.MongoDatabase
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.and
import org.litote.kmongo.combine
import org.litote.kmongo.deleteMany
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollection
import org.litote.kmongo.push
import org.litote.kmongo.save
import org.litote.kmongo.setValue
import org.litote.kmongo.toId
import org.litote.kmongo.updateOneById
import org.litote.kmongo.upsert
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.OperationType
import software.amazon.awssdk.services.dynamodb.model.Record
import java.time.Instant
import java.time.temporal.ChronoUnit

/** The document of the Studio text search: the same fields as the `DialogTextCol` of the MongoDB storage. */
internal data class DialogTextDocument(
    val text: String,
    val dialogId: Id<Dialog>,
    val date: Instant = Instant.now(),
)

/**
 * Applies to the MongoDB of the admin the writes of the serverless runtime, read from the DynamoDB streams.
 *
 * - the documents of the dialogs (timelines, dialogs, snapshots, connector messages, NLP statistics, archived
 *   values) are the same classes in both storages: they are stored as they are, in the collections of the
 *   MongoDB storage;
 * - the configuration created by the runtime (built-in stories, registered features, created labels, bot
 *   configurations created at the installation) is added through the admin DAOs, only if the admin does not have
 *   it: what the admin publishes comes back through the stream and is ignored;
 * - the metrics are stored as they are, for the analytics of the Studio;
 * - the label statistics are increments: the difference between the new and the old counter is added.
 *
 * With [nlp], the writes of the NLP:
 * - the parse logs, saved through the admin DAO (it also counts the usages of the sentences);
 * - the sentences of the inbox, unless the admin has a validated sentence with the same text (validated in the
 *   Studio while the runtime was parsing it);
 * - the unknown counters of the sentences, as increments.
 *
 * The expirations of the DynamoDB TTL are not replicated (MongoDB has its own TTL indexes), nor the deletions of
 * the configuration (the admin owns it).
 */
internal class MongoReplication(
    database: MongoDatabase,
    private val applications: BotApplicationConfigurationDAO,
    private val stories: StoryDefinitionConfigurationDAO,
    private val features: FeatureDAO,
    private val labels: I18nDAO,
    private val indicators: IndicatorDAO? = null,
    private val nlp: NlpTargets? = null,
) {
    private val logger = KotlinLogging.logger {}

    // the collections of the MongoDB storage (see UserTimelineMongoDAO)
    private val timelineCol = database.getCollection<UserTimelineCol>("user_timeline")
    private val dialogCol = database.getCollection<DialogCol>("dialog")
    private val dialogTextCol = database.getCollection<DialogTextDocument>("dialog_text")
    private val connectorMessageCol = database.getCollection<ConnectorMessageCol>("connector_message")
    private val nlpStatsCol = database.getCollection<NlpStatsCol>("action_nlp_stats")
    private val snapshotCol = database.getCollection<SnapshotCol>("dialog_snapshot")
    private val archivedEntityValuesCol = database.getCollection<ArchivedEntityValuesCol>("archived_entity_values")
    private val metricCol = database.getCollection<Metric>("metric")

    private fun Map<String, AttributeValue>.str(name: String): String? = get(name)?.s()

    private inline fun <reified T : Any> Map<String, AttributeValue>.document(): T = DocumentCodec.decode(getValue(DOC).b().asByteArray())

    private val Record.isExpiration: Boolean
        get() = userIdentity()?.let { it.type() == "Service" && it.principalId() == "dynamodb.amazonaws.com" } == true

    fun applyRuntime(record: Record) {
        val keys = record.dynamodb().keys()
        val pk = keys.str(PK) ?: return
        val sk = keys.str(SK) ?: return
        val removed = record.eventName() == OperationType.REMOVE
        if (removed && record.isExpiration) return
        val image = (if (removed) record.dynamodb().oldImage() else record.dynamodb().newImage()) ?: return
        when {
            pk.startsWith("TL#") && sk == "TL" -> {
                val timeline = image.document<UserTimelineCol>()
                if (removed) timelineCol.deleteOneById(timeline._id) else timelineCol.save(timeline)
            }

            pk.startsWith("DLG#") -> {
                replicateDialogItem(pk.removePrefix("DLG#"), sk, image, removed)
            }

            pk.startsWith("AEV#") && !removed -> {
                archivedEntityValuesCol.save(image.document<ArchivedEntityValuesCol>())
            }

            // the analytics of the Studio read the metrics in MongoDB
            pk.startsWith("METRIC#") && !removed -> {
                metricCol.save(image.document<Metric>())
            }

            pk.startsWith("PARSELOG#") && record.eventName() == OperationType.INSERT && nlp != null -> {
                nlp.logs.save(image.document<ParseRequestLog>())
            }

            pk.startsWith("I18NSTAT#") && !removed -> {
                val delta =
                    (image["count"]?.n()?.toLong() ?: 0) - (
                        record
                            .dynamodb()
                            .oldImage()
                            ?.get("count")
                            ?.n()
                            ?.toLong() ?: 0
                    )
                if (delta > 0) {
                    labels.incrementLabelStat(image.document<I18nLabelStat>().copy(count = delta.toInt(), lastUpdate = Instant.now()))
                }
            }
        }
    }

    private fun replicateDialogItem(
        dialogId: String,
        sk: String,
        image: Map<String, AttributeValue>,
        removed: Boolean,
    ) {
        when {
            sk == "H" -> {
                if (removed) {
                    removeDialog(dialogId)
                } else {
                    // the current document: it may be split in chunks, written before the header
                    val (dialog, _) = UserTimelineDynamoDAO.loadDialogDocument(dialogId) ?: return
                    dialogCol.save(dialog)
                    saveDialogText(dialog)
                }
            }

            sk.startsWith("SN#") && !removed -> {
                // MongoDB stores the dates in milliseconds: the comparison is on the stored value
                val snapshot = image.document<Snapshot>().let { it.copy(date = it.date.truncatedTo(ChronoUnit.MILLIS)) }
                val id = dialogId.toId<Dialog>()
                // a record may be given twice: a snapshot is added once
                if (snapshotCol.findOneById(id)?.snapshots?.contains(snapshot) != true) {
                    snapshotCol.updateOneById(
                        id,
                        combine(push(SnapshotCol::snapshots, snapshot), setValue(SnapshotCol::lastUpdateDate, Instant.now())),
                        upsert(),
                    )
                }
            }

            sk.startsWith("CM#") -> {
                val message = image.document<ConnectorMessageCol>()
                if (removed) connectorMessageCol.deleteOneById(message._id) else connectorMessageCol.save(message)
            }

            sk.startsWith("NS#") -> {
                val stats = image.document<NlpStatsCol>()
                if (removed) nlpStatsCol.deleteOneById(stats._id) else nlpStatsCol.save(stats)
            }
        }
    }

    /** The last sentence of the user, as the MongoDB storage indexes it for the search of the Studio. */
    private fun saveDialogText(dialog: DialogCol) {
        val text =
            dialog.stories
                .flatMap { it.actions }
                .lastOrNull { it.playerId.type == PlayerType.user }
                ?.let { it as? DialogCol.SendSentenceMongoWrapper }
                ?.text
                ?.let { if (it.length > 512) it.take(512) else it }
                ?: return
        dialogTextCol.replaceOne(
            and(DialogTextDocument::text eq text, DialogTextDocument::dialogId eq dialog._id),
            DialogTextDocument(text, dialog._id),
            ReplaceOptions().upsert(true),
        )
    }

    private fun removeDialog(dialogId: String) {
        val id = dialogId.toId<Dialog>()
        dialogCol.deleteOneById(id)
        snapshotCol.deleteOneById(id)
        dialogTextCol.deleteMany(DialogTextDocument::dialogId eq id)
        connectorMessageCol.deleteMany(ConnectorMessageCol::_id / ConnectorMessageColId::dialogId eq id)
        nlpStatsCol.deleteMany(NlpStatsCol::_id / NlpStatsColId::dialogId eq id)
    }

    fun applyConfiguration(record: Record) {
        if (record.eventName() == OperationType.REMOVE) return
        val image = record.dynamodb().newImage() ?: return
        val pk = image.str(PK) ?: return
        val sk = image.str(SK) ?: return
        if (!image.containsKey(DOC)) return
        when {
            pk.startsWith("I18N#") && sk == "L" -> {
                val label = image.document<I18nLabel>()
                if (labels.getLabelById(label._id) == null) {
                    labels.save(label)
                }
            }

            pk.startsWith("STORY#") && sk.startsWith("S#") -> {
                val story = image.document<StoryDefinitionConfiguration>()
                if (stories.getStoryDefinitionById(story._id) == null) {
                    // inserted only if the admin has no story with this id or this intent
                    stories.createBuiltInStoriesIfNotExist(listOf(story))
                }
            }

            FeatureDynamoDAO.isFeature(image) -> {
                val (botId, namespace, state) = FeatureDynamoDAO.featureOf(image)
                runBlocking {
                    val existing = features.getFeatures(botId, namespace)
                    if (existing.none { it.category == state.category && it.name == state.name && it.applicationId == state.applicationId }) {
                        features.addFeature(botId, namespace, state.enabled, state.category, state.name, state.startDate, state.endDate, state.applicationId, state.graduation)
                    }
                }
            }

            pk.startsWith("IND#") && indicators != null -> {
                val indicator = image.document<Indicator>()
                if (indicators.findByNameAndBotId(indicator.name, indicator.namespace, indicator.botId) == null) {
                    indicators.save(indicator)
                }
            }

            pk == "BAC" -> {
                val conf = image.document<BotApplicationConfiguration>()
                if (applications.getConfigurationById(conf._id) == null &&
                    applications.getConfigurationByApplicationIdAndBotId(conf.namespace, conf.applicationId, conf.botId) == null
                ) {
                    applications.save(conf)
                }
            }

            pk == "BOT" -> {
                val conf = image.document<BotConfiguration>()
                if (applications.getBotConfigurationsByNamespaceAndNameAndBotId(conf.namespace, conf.name, conf.botId) == null) {
                    applications.save(conf)
                }
            }
        }
    }

    fun applySentence(record: Record) {
        val targets = nlp ?: return
        if (record.eventName() == OperationType.REMOVE) return
        val image = record.dynamodb().newImage() ?: return
        if (!image.containsKey(DOC)) return
        val sentence = image.document<ClassifiedSentence>()
        if (image.str(ClassifiedSentenceDynamoDAO.STATUS) == inbox.name && !image.containsKey(ClassifiedSentenceDynamoDAO.FINGERPRINT) &&
            record.dynamodb().oldImage()?.get(DOC) != image[DOC]
        ) {
            val existing =
                targets.sentences
                    .search(
                        SentencesQuery(
                            sentence.applicationId,
                            sentence.language,
                            search = sentence.text,
                            onlyExactMatch = true,
                            notStatus = null,
                        ),
                    ).sentences
                    .firstOrNull()
            if (existing?.status != validated && existing?.status != model) {
                targets.sentences.save(sentence)
            }
        }
        val count = image[ClassifiedSentenceDynamoDAO.UNKNOWN_COUNT]?.n()?.toLong() ?: 0
        val previous =
            record
                .dynamodb()
                .oldImage()
                ?.get(ClassifiedSentenceDynamoDAO.UNKNOWN_COUNT)
                ?.n()
                ?.toLong() ?: 0
        // a publication replaces the item without its counter: the decrease is not replicated
        repeat((count - previous).coerceIn(0, MAX_UNKNOWN_INCREMENTS).toInt()) {
            targets.sentences.incrementUnknownStat(sentence.applicationId, sentence.language, sentence.text)
        }
    }

    private companion object {
        /** A record is one increment: more is a stream that restarted from an old image. */
        const val MAX_UNKNOWN_INCREMENTS = 100L
    }
}

/** The NLP DAOs of the admin (MongoDB), written by the replication. */
class NlpTargets(
    val sentences: ClassifiedSentenceDAO,
    val logs: ParseRequestLogDAO,
)

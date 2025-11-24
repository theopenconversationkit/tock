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

package ai.tock.bot.mongo

import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.dialog.ApplicationDialogFlowData
import ai.tock.bot.admin.dialog.DialogFlowAggregateData
import ai.tock.bot.admin.dialog.DialogFlowStateData
import ai.tock.bot.admin.dialog.DialogFlowStateTransitionData
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.definition.DialogFlowDefinition
import ai.tock.bot.definition.DialogFlowStateTransitionType
import ai.tock.bot.definition.DialogFlowStateTransitionType.attachment
import ai.tock.bot.definition.DialogFlowStateTransitionType.choice
import ai.tock.bot.definition.DialogFlowStateTransitionType.location
import ai.tock.bot.definition.DialogFlowStateTransitionType.nlp
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendAttachment
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.action.SendLocation
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.DialogFlowDAO
import ai.tock.bot.engine.dialog.Snapshot
import ai.tock.bot.engine.user.UserTimeline
import ai.tock.bot.mongo.BotApplicationConfigurationMongoDAO.getConfigurationByApplicationIdAndBotId
import ai.tock.bot.mongo.DialogFlowAggregateResult_.Companion.Count
import ai.tock.bot.mongo.DialogFlowAggregateResult_.Companion.SeriesKey
import ai.tock.bot.mongo.DialogFlowStateCol_.Companion.BotId
import ai.tock.bot.mongo.DialogFlowStateCol_.Companion.Entities
import ai.tock.bot.mongo.DialogFlowStateCol_.Companion.Intent
import ai.tock.bot.mongo.DialogFlowStateCol_.Companion.Namespace
import ai.tock.bot.mongo.DialogFlowStateCol_.Companion.Step
import ai.tock.bot.mongo.DialogFlowStateCol_.Companion.StoryDefinitionId
import ai.tock.bot.mongo.DialogFlowStateCol_.Companion.StoryName
import ai.tock.bot.mongo.DialogFlowStateCol_.Companion.StoryType
import ai.tock.bot.mongo.DialogFlowStateCol_.Companion._id
import ai.tock.bot.mongo.DialogFlowStateTransitionCol_.Companion.NewEntities
import ai.tock.bot.mongo.DialogFlowStateTransitionCol_.Companion.NextStateId
import ai.tock.bot.mongo.DialogFlowStateTransitionCol_.Companion.PreviousStateId
import ai.tock.bot.mongo.DialogFlowStateTransitionCol_.Companion.Type
import ai.tock.bot.mongo.DialogFlowStateTransitionStatCol_.Companion.ApplicationId
import ai.tock.bot.mongo.DialogFlowStateTransitionStatCol_.Companion.Date
import ai.tock.bot.mongo.DialogFlowStateTransitionStatCol_.Companion.DialogId
import ai.tock.bot.mongo.DialogFlowStateTransitionStatCol_.Companion.ProcessedLevel
import ai.tock.bot.mongo.DialogFlowStateTransitionStatCol_.Companion.TransitionId
import ai.tock.bot.mongo.DialogFlowStateTransitionStatDateAggregationCol_.Companion.ActionType
import ai.tock.bot.mongo.DialogFlowStateTransitionStatDateAggregationCol_.Companion.ConfigurationName
import ai.tock.bot.mongo.DialogFlowStateTransitionStatDateAggregationCol_.Companion.HourOfDay
import ai.tock.bot.mongo.DialogFlowStateTransitionStatDateAggregationCol_.Companion.Locale
import ai.tock.bot.mongo.DialogFlowStateTransitionStatDateAggregationCol_.Companion.StoryCategory
import ai.tock.bot.mongo.GroupById_.Companion.ConnectorType
import ai.tock.shared.booleanProperty
import ai.tock.shared.defaultLocale
import ai.tock.shared.defaultZoneId
import ai.tock.shared.ensureIndex
import ai.tock.shared.error
import ai.tock.shared.longProperty
import ai.tock.shared.security.TockObfuscatorService.obfuscate
import ai.tock.shared.sumByLong
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.UpdateOneModel
import com.mongodb.client.model.UpdateOptions
import mu.KotlinLogging
import org.bson.conversions.Bson
import org.litote.jackson.data.JacksonData
import org.litote.kmongo.Data
import org.litote.kmongo.Id
import org.litote.kmongo.aggregate
import org.litote.kmongo.all
import org.litote.kmongo.and
import org.litote.kmongo.dateToString
import org.litote.kmongo.deleteMany
import org.litote.kmongo.document
import org.litote.kmongo.eq
import org.litote.kmongo.find
import org.litote.kmongo.findOne
import org.litote.kmongo.findOneById
import org.litote.kmongo.from
import org.litote.kmongo.getCollection
import org.litote.kmongo.group
import org.litote.kmongo.gt
import org.litote.kmongo.gte
import org.litote.kmongo.ifNull
import org.litote.kmongo.`in`
import org.litote.kmongo.inc
import org.litote.kmongo.lt
import org.litote.kmongo.lte
import org.litote.kmongo.match
import org.litote.kmongo.ne
import org.litote.kmongo.project
import org.litote.kmongo.projection
import org.litote.kmongo.save
import org.litote.kmongo.set
import org.litote.kmongo.setTo
import org.litote.kmongo.size
import org.litote.kmongo.sum
import org.litote.kmongo.toId
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit.DAYS
import java.time.temporal.TemporalAccessor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.reflect.KProperty

/**
 *
 */
internal object DialogFlowMongoDAO : DialogFlowDAO {
    private val logger = KotlinLogging.logger {}

    internal val flowStateCol =
        MongoBotConfiguration.database.getCollection<DialogFlowStateCol>("flow_state")
            .apply {
                ensureIndex(Namespace, BotId, StoryDefinitionId, Intent, Step, Entities, StoryType, StoryName)
            }

    internal val flowTransitionCol =
        MongoBotConfiguration.database.getCollection<DialogFlowStateTransitionCol>("flow_transition")
            .apply {
                ensureIndex(Namespace, BotId, PreviousStateId, NextStateId, Intent, Step, NewEntities, Type)
            }

    internal val flowTransitionStatsCol =
        MongoBotConfiguration.database.getCollection<DialogFlowStateTransitionStatCol>("flow_transition_stats")
            .apply {
                try {
                    ensureIndex(TransitionId, Date)
                    ensureIndex(ApplicationId, Date)
                    ensureIndex(DialogId)
                    ensureIndex(ProcessedLevel)
                    ensureIndex(
                        Date,
                        indexOptions =
                            IndexOptions()
                                .expireAfter(longProperty("tock_bot_flow_stats_index_ttl_days", 365), TimeUnit.DAYS),
                    )
                } catch (e: Exception) {
                    logger.error(e)
                }
            }

    private val flowTransitionStatsDateAggregationCol =
        MongoBotConfiguration.database.getCollection<DialogFlowStateTransitionStatDateAggregationCol>("flow_transition_stats_date")
            .apply {
                try {
                    ensureIndex(
                        ApplicationId,
                        Date,
                        Intent,
                        StoryDefinitionId,
                        StoryCategory,
                        StoryType,
                        Locale,
                        ConfigurationName,
                        ConnectorType,
                        ActionType,
                        HourOfDay,
                        indexOptions = IndexOptions().name("flow_stats_date_index"),
                    )
                } catch (e: Exception) {
                    logger.error(e)
                }
            }

    private val flowTransitionStatsDialogAggregationColTTL = longProperty("tock_bot_flow_stats_index_ttl_days", 365)

    private val flowTransitionStatsDialogAggregationCol =
        MongoBotConfiguration.database.getCollection<DialogFlowStateTransitionStatDialogAggregationCol>("flow_transition_stats_dialog")
            .apply {
                try {
                    ensureIndex(
                        ApplicationId,
                        Date,
                        DialogId,
                    )
                    ensureIndex(
                        Date,
                        ApplicationId,
                    )
                    ensureIndex(
                        Date,
                        indexOptions =
                            IndexOptions()
                                .expireAfter(flowTransitionStatsDialogAggregationColTTL, TimeUnit.DAYS),
                    )
                } catch (e: Exception) {
                    logger.error(e)
                }
            }

    private val flowTransitionStatsUserAggregationCol =
        MongoBotConfiguration.database.getCollection<DialogFlowStateTransitionStatUserAggregationCol>("flow_transition_stats_user")
            .apply {
                try {
                    ensureIndex(
                        ApplicationId,
                        Date,
                    )
                } catch (e: Exception) {
                    logger.error(e)
                }
            }

    private const val DEFAULT_CONF_KEY = "default"

    private val currentProcessedLevelCol =
        MongoBotConfiguration.database.getCollection<DialogFlowConfiguration>("flow_configuration")

    internal const val CURRENT_PROCESS_LEVEL = 3

    private val crawlStats: Boolean = booleanProperty("tock_dialog_flow_crawl_stats", true)

    private fun schedule(
        delay: Long,
        unit: TimeUnit,
        runnable: Runnable,
    ) = Executors.newSingleThreadScheduledExecutor().schedule(runnable, delay, unit)

    private fun scheduleWithFixedDelay(
        initialDelay: Long,
        delay: Long,
        unit: TimeUnit,
        runnable: Runnable,
    ) = Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(runnable, initialDelay, delay, unit)

    override fun initFlowStatCrawl() {
        if (crawlStats) {
            schedule(10, TimeUnit.SECONDS) {
                try {
                    val conf = currentProcessedLevelCol.findOneById(DEFAULT_CONF_KEY)
                    if (conf?.currentProcessedLevel != CURRENT_PROCESS_LEVEL) {
                        logger.info { "update flow stats to level $CURRENT_PROCESS_LEVEL" }
                        logger.info("cleanup stats dates")
                        flowTransitionStatsDateAggregationCol.deleteMany()
                        logger.info("cleanup stats dialogs")
                        flowTransitionStatsDialogAggregationCol.deleteMany()
                        logger.info("cleanup stats users")
                        flowTransitionStatsUserAggregationCol.deleteMany()
                        logger.info("reset processed level")
                        flowTransitionStatsCol.updateMany(
                            ProcessedLevel eq CURRENT_PROCESS_LEVEL,
                            set(ProcessedLevel setTo 0),
                        )
                        logger.info("persist flow configuration")
                        currentProcessedLevelCol.save(DialogFlowConfiguration(DEFAULT_CONF_KEY, CURRENT_PROCESS_LEVEL))
                        logger.info { "end update flow stats to level $CURRENT_PROCESS_LEVEL" }
                    }
                } catch (e: Throwable) {
                    logger.error(e)
                    logger.error("Waiting for flow state set")
                    Thread.sleep(1000 * 60 * 60)
                    initFlowStatCrawl()
                    return@schedule
                }
                scheduleWithFixedDelay(1, 1, TimeUnit.MINUTES) {
                    try {
                        var found = true
                        var minDate: LocalDateTime? = null
                        val applicationsMap =
                            mutableMapOf<Id<BotApplicationConfiguration>, BotApplicationConfiguration?>()
                        val nextStateMap = mutableMapOf<Id<DialogFlowStateCol>, DialogFlowStateCol?>()
                        val storyMap = mutableMapOf<Triple<String, String, String>, StoryDefinitionConfiguration?>()
                        while (found) {
                            val transitionsMap =
                                mutableMapOf<Id<DialogFlowStateTransitionCol>, DialogFlowStateTransitionCol?>()
                            found = false
                            flowTransitionStatsCol
                                .find(ProcessedLevel ne CURRENT_PROCESS_LEVEL)
                                .limit(1000)
                                .toList()
                                .apply {
                                    if (isNotEmpty()) {
                                        logger.debug { "update $size stats" }
                                        val dateStats: List<UpdateOneModel<DialogFlowStateTransitionStatDateAggregationCol>> =
                                            mapNotNull {
                                                try {
                                                    val date = it.date.atZone(defaultZoneId).toLocalDateTime()
                                                    val truncatedDate = date.truncatedTo(DAYS)
                                                    minDate =
                                                        minDate?.let { d -> minOf(d, truncatedDate) }
                                                            ?: truncatedDate

                                                    val transition =
                                                        transitionsMap.getOrPut(it.transitionId) {
                                                            flowTransitionCol.findOneById(it.transitionId)
                                                        } ?: error("no transition for id ${it.transitionId}")

                                                    val configuration =
                                                        applicationsMap.getOrPut(it.applicationId) {
                                                            BotApplicationConfigurationMongoDAO.getConfigurationById(it.applicationId)
                                                        } ?: error("no application for id ${it.applicationId}")

                                                    val nextState =
                                                        nextStateMap.getOrPut(transition.nextStateId) {
                                                            flowStateCol.findOneById(transition.nextStateId)
                                                        } ?: error("no state for id ${transition.nextStateId}")

                                                    val story =
                                                        storyMap.getOrPut(
                                                            Triple(
                                                                configuration.namespace,
                                                                configuration.botId,
                                                                nextState.storyDefinitionId,
                                                            ),
                                                        ) {
                                                            StoryDefinitionConfigurationMongoDAO.getStoryDefinitionByNamespaceAndBotIdAndStoryId(
                                                                configuration.namespace,
                                                                configuration.botId,
                                                                nextState.storyDefinitionId,
                                                            )
                                                                ?: StoryDefinitionConfigurationMongoDAO.getStoryDefinitionById(
                                                                    nextState.storyDefinitionId.toId(),
                                                                )
                                                        }

                                                    UpdateOneModel(
                                                        and(
                                                            ApplicationId eq it.applicationId,
                                                            DialogFlowStateTransitionStatDateAggregationCol_.Date eq truncatedDate,
                                                            Intent eq transition.intent,
                                                            StoryDefinitionId eq nextState.storyName,
                                                            StoryCategory eq (story?.category ?: "default"),
                                                            StoryType eq (
                                                                nextState.storyType
                                                                    ?: AnswerConfigurationType.builtin
                                                            ),
                                                            Locale eq (it.locale ?: defaultLocale),
                                                            ConfigurationName eq configuration.name,
                                                            ConnectorType eq configuration.connectorType,
                                                            ActionType eq transition.type,
                                                            HourOfDay eq date.hour,
                                                        ),
                                                        inc(Count, 1),
                                                        UpdateOptions().upsert(true),
                                                    )
                                                } catch (t: Throwable) {
                                                    logger.error(t)
                                                    null
                                                }
                                            }

                                        logger.debug { "statsDate calculated" }

                                        val dialogStats: List<UpdateOneModel<DialogFlowStateTransitionStatDialogAggregationCol>> =
                                            mapNotNull {
                                                try {
                                                    val date = it.date.atZone(defaultZoneId).toLocalDateTime()

                                                    UpdateOneModel(
                                                        and(
                                                            ApplicationId eq it.applicationId,
                                                            DialogFlowStateTransitionStatDialogAggregationCol_.Date eq
                                                                date.truncatedTo(
                                                                    DAYS,
                                                                ),
                                                            DialogId eq it.dialogId,
                                                        ),
                                                        inc(Count, 1),
                                                        UpdateOptions().upsert(true),
                                                    )
                                                } catch (t: Throwable) {
                                                    logger.error(t)
                                                    null
                                                }
                                            }

                                        val stats: List<UpdateOneModel<DialogFlowStateTransitionStatCol>> =
                                            map {
                                                UpdateOneModel(
                                                    and(
                                                        ApplicationId eq it.applicationId,
                                                        TransitionId eq it.transitionId,
                                                        DialogId eq it.dialogId,
                                                        Date eq it.date,
                                                        ProcessedLevel ne CURRENT_PROCESS_LEVEL,
                                                    ),
                                                    set(ProcessedLevel setTo CURRENT_PROCESS_LEVEL),
                                                )
                                            }
                                        logger.debug { "stats calculated" }

                                        if (dateStats.isNotEmpty()) {
                                            flowTransitionStatsDateAggregationCol.bulkWrite(dateStats)
                                        }

                                        if (stats.isNotEmpty()) {
                                            found = true
                                            flowTransitionStatsCol.bulkWrite(stats)
                                        }

                                        logger.debug { "stats persisted" }

                                        if (dialogStats.isNotEmpty()) {
                                            flowTransitionStatsDialogAggregationCol.bulkWrite(dialogStats)
                                        }

                                        logger.debug { "stats date persisted" }
                                    }
                                }
                        }
                        if (minDate != null) {
                            logger.debug { "min date $minDate" }
                            val match = match(DialogFlowStateTransitionStatDialogAggregationCol_.Date gte minDate)
                            updateUserStats(match)
                        }
                    } catch (e: Throwable) {
                        logger.error(e)
                    }
                }

                // check data
                val now = Instant.now().atZone(defaultZoneId)
                schedule(
                    Duration.between(now, now.plusDays(1).withHour(1)).seconds,
                    TimeUnit.SECONDS,
                ) {
                    try {
                        val distinct =
                            group(
                                document(
                                    ApplicationId from ApplicationId,
                                    Date from Date,
                                ),
                            )
                        val proj =
                            project(
                                Date from GroupByIdContainer_._id.date.projection,
                                ApplicationId from GroupByIdContainer_._id.applicationId.projection,
                            )
                        flowTransitionStatsDateAggregationCol
                            .aggregate<DialogFlowAggregateApplicationIdResult>(distinct, proj)
                            .forEach {
                                if (Duration.between(it.date.atZone(defaultZoneId), now)
                                        .toDays() < flowTransitionStatsDialogAggregationColTTL - 2
                                ) {
                                    val match =
                                        match(DialogFlowStateTransitionStatDialogAggregationCol_.Date eq it.date)
                                    updateUserStats(match)
                                }
                            }
                    } catch (e: Throwable) {
                        logger.error(e)
                    }
                }
            }
        }
    }

    private fun updateUserStats(match: Bson) {
        val distinct =
            group(
                document(
                    ApplicationId from ApplicationId,
                    Date from Date,
                ),
                Count sum 1,
            )
        val proj =
            project(
                Date from GroupByIdContainer_._id.date.projection,
                ApplicationId from GroupByIdContainer_._id.applicationId.projection,
                Count from Count.projection,
            )
        val userStats: List<UpdateOneModel<DialogFlowStateTransitionStatUserAggregationCol>> =
            flowTransitionStatsDialogAggregationCol
                .aggregate<DialogFlowAggregateApplicationIdResult>(match, distinct, proj)
                .toList()
                .map {
                    logger.debug { it }
                    UpdateOneModel(
                        and(
                            ApplicationId eq it.applicationId,
                            DialogFlowStateTransitionStatUserAggregationCol_.Date eq it.date,
                        ),
                        set(Count setTo it.count),
                        UpdateOptions().upsert(true),
                    )
                }
        if (userStats.isNotEmpty()) {
            flowTransitionStatsUserAggregationCol.bulkWrite(userStats)
        }
    }

    override fun saveFlow(
        bot: BotDefinition,
        flow: DialogFlowDefinition,
    ) {
        TODO("not implemented")
    }

    override fun loadApplicationData(
        namespace: String,
        botId: String,
        applicationIds: Set<Id<BotApplicationConfiguration>>,
        from: LocalDateTime?,
        to: LocalDateTime?,
        intent: String?,
    ): ApplicationDialogFlowData {
        logger.debug { "Fetching application flow for ns $namespace, bot $botId, apps $applicationIds from $from to $to..." }
        val states = findStates(namespace, botId)
        val transitions = findTransitions(namespace, botId)
        val supportedTransitions = transitions.map { it._id }.toSet()
        val stats =
            findStats(applicationIds, from, to)
                .asSequence()
                .filter { supportedTransitions.contains(it.first) }
                .associateBy { it.first }
                .mapValues { it.value.second }

        @Suppress("UNCHECKED_CAST")
        val transitionsWithStats =
            transitions.map {
                DialogFlowStateTransitionData(
                    it.previousStateId as? Id<DialogFlowStateData>?,
                    it.nextStateId as Id<DialogFlowStateData>,
                    it.intent,
                    it.step,
                    it.newEntities,
                    it.type,
                    stats[it._id] ?: 0,
                )
            }.filter { it.count != 0L }

        val transitionCountByNext =
            transitionsWithStats.groupBy { it.nextStateId }.mapValues { e -> e.value.sumByLong { it.count } }

        @Suppress("UNCHECKED_CAST")
        val statesWithStats =
            states.map { s ->
                DialogFlowStateData(
                    s.storyDefinitionId,
                    s.intent,
                    s.step,
                    s.entities,
                    s.storyType,
                    s.storyName,
                    transitionCountByNext[s._id as Id<DialogFlowStateData>] ?: 0L,
                    s._id as Id<DialogFlowStateData>,
                )
            }.filter {
                it.count != 0L
            }

        return ApplicationDialogFlowData(statesWithStats, transitionsWithStats, emptyList())
    }

    private fun findStates(
        namespace: String,
        botId: String,
    ): List<DialogFlowStateCol> = flowStateCol.find(Namespace eq namespace, BotId eq botId).toList()

    private fun findTransitions(
        namespace: String,
        botId: String,
    ): List<DialogFlowStateTransitionCol> = flowTransitionCol.find(Namespace eq namespace, BotId eq botId).toList()

    override fun countMessagesByDate(
        namespace: String,
        botId: String,
        applicationIds: Set<Id<BotApplicationConfiguration>>,
        from: LocalDateTime?,
        to: LocalDateTime?,
    ): Map<String, List<DialogFlowAggregateData>> {
        val match = buildAnalyticsFilter(applicationIds, from, to)
        // group messages that were sent the same day together
        val group = group(Date.kDateToString(), Count.sum(Count.projection))

        val proj =
            project(
                Date from _id.projection,
                Count from Count.projection,
                SeriesKey from "Messages",
            )
        return flowTransitionStatsDateAggregationCol
            .aggregate<DialogFlowAggregateResult>(match, group, proj)
            .groupBy({ it.date }) { DialogFlowAggregateData(it.seriesKey, it.count) }
            .mapValues { entry -> entry.value.sortedBy { it.seriesKey } }
    }

    override fun countUsersByDate(
        namespace: String,
        botId: String,
        applicationIds: Set<Id<BotApplicationConfiguration>>,
        from: LocalDateTime?,
        to: LocalDateTime?,
    ): Map<String, List<DialogFlowAggregateData>> {
        val match = buildAnalyticsFilter(applicationIds, from, to)
        val distinct =
            group(
                Date.kDateToString(),
                Count sum Count.projection,
            )
        val proj =
            project(
                Date from _id.projection,
                Count from Count.projection,
                SeriesKey from "Users",
            )
        return flowTransitionStatsUserAggregationCol
            .aggregate<DialogFlowAggregateResult>(match, distinct, proj)
            .groupBy({ it.date }) { DialogFlowAggregateData(it.seriesKey, it.count) }
            .mapValues { entry -> entry.value.sortedBy { it.seriesKey } }
    }

    override fun countMessagesByDateAndConnectorType(
        namespace: String,
        botId: String,
        applicationIds: Set<Id<BotApplicationConfiguration>>,
        from: LocalDateTime?,
        to: LocalDateTime?,
    ): Map<String, List<DialogFlowAggregateData>> {
        val match = buildAnalyticsFilter(applicationIds, from, to)
        val group =
            group(
                document(
                    Date from Date.kDateToString(),
                    ConnectorType from ConnectorType.id,
                ),
                Count sum Count.projection,
            )
        val proj =
            project(
                Date from GroupByIdContainer_._id.date.projection,
                Count from Count.projection,
                SeriesKey from GroupByIdContainer_._id.connectorType.projection,
            )
        return aggregateFlowTransitionStats(match, group, proj)
    }

    override fun countMessagesByDateAndConfiguration(
        namespace: String,
        botId: String,
        applicationIds: Set<Id<BotApplicationConfiguration>>,
        from: LocalDateTime?,
        to: LocalDateTime?,
    ): Map<String, List<DialogFlowAggregateData>> {
        val match = buildAnalyticsFilter(applicationIds, from, to)
        val group =
            group(
                document(
                    Date from Date.kDateToString(),
                    GroupById_.Configuration from ConfigurationName,
                ),
                Count sum Count.projection,
            )
        val proj =
            project(
                Date from GroupByIdContainer_._id.date.projection,
                Count from Count.projection,
                SeriesKey from GroupByIdContainer_._id.configuration.projection,
            )
        return aggregateFlowTransitionStats(match, group, proj)
    }

    override fun countMessagesByDayOfWeek(
        namespace: String,
        botId: String,
        applicationIds: Set<Id<BotApplicationConfiguration>>,
        from: LocalDateTime?,
        to: LocalDateTime?,
    ): Map<DayOfWeek, Int> {
        val match = buildAnalyticsFilter(applicationIds, from, to)
        val group = group(Date.kDateToString(format = "%u"), Count sum Count.projection)
        val proj =
            project(
                DialogFlowAggregateResult::date from _id.projection,
                DialogFlowAggregateResult::count from Count.projection,
            )
        logger.debug { "Flow Message pipeline: [$match, $group, $proj]" }
        return flowTransitionStatsDateAggregationCol.aggregate<DialogFlowAggregateResult>(match, group, proj)
            .associateBy({ DayOfWeek.of(it.date.toInt()) }, { it.count })
    }

    override fun countMessagesByHour(
        namespace: String,
        botId: String,
        applicationIds: Set<Id<BotApplicationConfiguration>>,
        from: LocalDateTime?,
        to: LocalDateTime?,
    ): Map<Int, Int> {
        val match = buildAnalyticsFilter(applicationIds, from, to)
        val group = group(HourOfDay, Count sum Count.projection)
        val proj =
            project(
                DialogFlowAggregateResult::date from _id.projection,
                DialogFlowAggregateResult::count from Count.projection,
            )
        logger.debug { "Flow Message pipeline: [$match, $group, $proj]" }
        return flowTransitionStatsDateAggregationCol
            .aggregate<DialogFlowAggregateResult>(match, group, proj)
            .associateBy({ it.date.toInt() }, { it.count })
    }

    override fun countMessagesByDateAndIntent(
        namespace: String,
        botId: String,
        applicationIds: Set<Id<BotApplicationConfiguration>>,
        from: LocalDateTime?,
        to: LocalDateTime?,
    ): Map<String, List<DialogFlowAggregateData>> {
        val match = buildAnalyticsFilter(applicationIds, from, to)
        val group =
            group(
                document(
                    Date from Date.kDateToString(),
                    Intent from ifNull(Intent, "unknown"),
                ),
                Count sum Count.projection,
            )
        val proj =
            project(
                Date from GroupByIdContainer_._id.date.projection,
                Count from Count.projection,
                SeriesKey from GroupByIdContainer_._id.intent.projection,
            )
        return aggregateFlowTransitionStats(match, group, proj)
    }

    override fun countMessagesByIntent(
        namespace: String,
        botId: String,
        applicationIds: Set<Id<BotApplicationConfiguration>>,
        from: LocalDateTime?,
        to: LocalDateTime?,
    ): Map<String, Int> {
        val match = buildAnalyticsFilter(applicationIds, from, to)
        val group =
            group(
                ifNull(Intent, "unknown"),
                Count sum Count.projection,
            )
        return aggregateStatDate(match, group)
    }

    override fun countMessagesByStory(
        namespace: String,
        botId: String,
        applicationIds: Set<Id<BotApplicationConfiguration>>,
        from: LocalDateTime?,
        to: LocalDateTime?,
    ): Map<String, Int> {
        val match = buildAnalyticsFilter(applicationIds, from, to)
        val group =
            group(
                StoryDefinitionId,
                Count sum Count.projection,
            )

        return aggregateStatDate(match, group)
    }

    override fun countMessagesByDateAndStory(
        namespace: String,
        botId: String,
        applicationIds: Set<Id<BotApplicationConfiguration>>,
        from: LocalDateTime?,
        to: LocalDateTime?,
    ): Map<String, List<DialogFlowAggregateData>> {
        val match = buildAnalyticsFilter(applicationIds, from, to)
        val group =
            group(
                document(
                    Date from Date.kDateToString(),
                    StoryDefinitionId from StoryDefinitionId,
                ),
                Count sum Count.projection,
            )
        val proj =
            project(
                Date from GroupByIdContainer_._id.date.projection,
                Count from Count.projection,
                SeriesKey from GroupByIdContainer_._id.storyDefinitionId.projection,
            )
        return aggregateFlowTransitionStats(match, group, proj)
    }

    override fun countMessagesByStoryCategory(
        namespace: String,
        botId: String,
        applicationIds: Set<Id<BotApplicationConfiguration>>,
        from: LocalDateTime?,
        to: LocalDateTime?,
    ): Map<String, Int> {
        val match = buildAnalyticsFilter(applicationIds, from, to)
        val group =
            group(
                StoryCategory,
                Count sum Count.projection,
            )

        return aggregateStatDate(match, group)
    }

    override fun countMessagesByStoryType(
        namespace: String,
        botId: String,
        applicationIds: Set<Id<BotApplicationConfiguration>>,
        from: LocalDateTime?,
        to: LocalDateTime?,
    ): Map<String, Int> {
        val match = buildAnalyticsFilter(applicationIds, from, to)
        val group =
            group(
                StoryType,
                Count sum Count.projection,
            )

        return aggregateStatDate(match, group)
    }

    override fun countMessagesByStoryLocale(
        namespace: String,
        botId: String,
        applicationIds: Set<Id<BotApplicationConfiguration>>,
        from: LocalDateTime?,
        to: LocalDateTime?,
    ): Map<String, Int> {
        val match = buildAnalyticsFilter(applicationIds, from, to)
        val group =
            group(
                Locale,
                Count sum Count.projection,
            )

        return aggregateStatDate(match, group)
    }

    override fun countMessagesByActionType(
        namespace: String,
        botId: String,
        applicationIds: Set<Id<BotApplicationConfiguration>>,
        from: LocalDateTime?,
        to: LocalDateTime?,
    ): Map<String, Int> {
        val match = buildAnalyticsFilter(applicationIds, from, to)
        val group =
            group(
                ifNull(ActionType, nlp),
                Count sum Count.projection,
            )
        return aggregateStatDate(match, group)
    }

    private fun aggregateStatDate(
        match: Bson,
        group: Bson,
    ): Map<String, Int> {
        val proj = projectToResult()
        logger.debug { "Flow Message pipeline: [$match, $group, $proj]" }
        return flowTransitionStatsDateAggregationCol.aggregate<DialogFlowAggregateResult>(match, group, proj)
            .associateBy({ it.seriesKey }, { it.count })
    }

    private fun projectToResult() =
        project(
            SeriesKey from _id.projection,
            Count from Count.projection,
        )

    private fun buildAnalyticsFilter(
        applicationIds: Set<Id<BotApplicationConfiguration>>,
        from: LocalDateTime?,
        to: LocalDateTime?,
    ) = match(
        and(
            listOfNotNull(
                if (applicationIds.isEmpty()) null else ApplicationId `in` applicationIds,
                if (from == null) null else Date gte from.atZone(defaultZoneId).toInstant(),
                if (to == null) null else Date lte to.atZone(defaultZoneId)?.toInstant(),
            ),
        ),
    )

    private fun aggregateFlowTransitionStats(vararg pipeline: Bson): Map<String, List<DialogFlowAggregateData>> {
        logger.debug { "Flow Message pipeline: ${pipeline.contentToString()}" }
        return flowTransitionStatsDateAggregationCol
            .aggregate<DialogFlowAggregateResult>(*pipeline)
            .groupBy({ it.date }) { DialogFlowAggregateData(it.seriesKey, it.count) }
            .mapValues { entry -> entry.value.sortedBy { it.seriesKey } }
    }

    private fun findStats(
        botAppConfIds: Set<Id<BotApplicationConfiguration>>,
        from: LocalDateTime?,
        to: LocalDateTime?,
    ): List<Pair<Id<DialogFlowStateTransitionCol>, Long>> =
        flowTransitionStatsCol
            .aggregate<Pair<String, Long>>(
                match(
                    and(
                        listOfNotNull(
                            if (botAppConfIds.isEmpty()) null else ApplicationId `in` botAppConfIds,
                            if (from == null) null else Date gt from.atZone(defaultZoneId)?.toInstant(),
                            if (to == null) null else Date lt to.atZone(defaultZoneId)?.toInstant(),
                        ),
                    ),
                ),
                group(
                    TransitionId,
                    Pair<*, Long>::second sum 1,
                ),
                project(
                    Pair<Id<DialogFlowStateTransitionCol>, Long>::first from _id,
                    Pair<*, Long>::second from Pair<*, Long>::second,
                ),
            )
            .map { it.first.toId<DialogFlowStateTransitionCol>() to it.second }
            .toList()

    private fun findState(
        botDefinition: BotDefinition,
        snapshot: Snapshot?,
    ): DialogFlowStateCol? {
        val storyDefinitionId = snapshot?.storyDefinitionId
        val intentName = snapshot?.intentName
        return if (storyDefinitionId != null && intentName != null) {
            DialogFlowStateCol(
                botDefinition.namespace,
                botDefinition.botId,
                storyDefinitionId,
                intentName,
                snapshot.step,
                snapshot.entityValues.map { it.entity.role }.toSortedSet(),
                storyType = snapshot.storyType,
                storyName = snapshot.storyName ?: storyDefinitionId,
            ).run {
                flowStateCol.findOne(
                    Namespace eq namespace,
                    BotId eq botId,
                    StoryDefinitionId eq storyDefinitionId,
                    Intent eq intentName,
                    Step eq step,
                    if (entities.size < 2) {
                        Entities eq entities
                    } else {
                        and(
                            Entities size entities.size,
                            Entities all entities,
                        )
                    },
                    StoryType eq storyType,
                    StoryName eq storyName,
                ) ?: (this.apply { flowStateCol.insertOne(this) })
            }
        } else {
            null
        }
    }

    private fun findTransition(
        botDefinition: BotDefinition,
        previousState: DialogFlowStateCol?,
        state: DialogFlowStateCol,
        lastUserAction: Action?,
    ): DialogFlowStateTransitionCol =
        findTransition(
            botDefinition,
            previousState?._id,
            state._id,
            lastUserAction?.state?.intent,
            lastUserAction?.state?.step,
            lastUserAction?.state?.entityValues?.map { it.entity.role }?.toSortedSet() ?: emptySet(),
            when (lastUserAction) {
                is SendChoice -> choice
                is SendLocation -> location
                is SendAttachment -> attachment
                else -> nlp
            },
        )

    private fun findTransition(
        botDefinition: BotDefinition,
        previousStateId: Id<DialogFlowStateCol>?,
        nextStateId: Id<DialogFlowStateCol>,
        intent: String?,
        step: String?,
        newEntities: Set<String>,
        type: DialogFlowStateTransitionType,
    ): DialogFlowStateTransitionCol =
        flowTransitionCol.findOne(
            Namespace eq botDefinition.namespace,
            BotId eq botDefinition.botId,
            PreviousStateId eq previousStateId,
            NextStateId eq nextStateId,
            Intent eq intent,
            Step eq step,
            if (newEntities.size < 2) {
                NewEntities eq newEntities
            } else {
                and(
                    NewEntities size newEntities.size,
                    NewEntities all newEntities,
                )
            },
            Type eq type,
        ) ?: (
            DialogFlowStateTransitionCol(
                botDefinition.namespace,
                botDefinition.botId,
                previousStateId,
                nextStateId,
                intent,
                step,
                newEntities,
                type,
            )
                .also { flowTransitionCol.insertOne(it) }
        )

    fun addFlowStat(
        userTimeline: UserTimeline,
        botDefinition: BotDefinition,
        lastUserAction: Action,
        dialog: Dialog,
        snapshot: SnapshotCol,
    ) {
        val previousState = findState(botDefinition, snapshot.snapshots.getOrNull(snapshot.snapshots.size - 2))
        val state = findState(botDefinition, snapshot.snapshots.lastOrNull())
        if (state != null) {
            val transition = findTransition(botDefinition, previousState, state, lastUserAction)
            val botAppConf =
                getConfigurationByApplicationIdAndBotId(
                    botDefinition.namespace,
                    lastUserAction.applicationId,
                    botDefinition.botId,
                )
            if (botAppConf != null) {
                flowTransitionStatsCol.insertOne(
                    DialogFlowStateTransitionStatCol(
                        botAppConf._id,
                        transition._id,
                        dialog.id,
                        obfuscate((lastUserAction as? SendSentence)?.stringText),
                        (lastUserAction as? SendSentence)?.nlpStats?.locale ?: userTimeline.userPreferences.locale,
                    ),
                )
            } else {
                logger.warn { "unknown applicationId : ${lastUserAction.applicationId} for $botDefinition" }
            }
        } else {
            logger.warn { "unknown state : $dialog" }
        }
    }
}

@Data(internal = true)
@JacksonData(internal = true)
internal data class GroupByIdContainer(val _id: GroupById)

@Data(internal = true)
@JacksonData(internal = true)
internal data class GroupById(
    val date: String,
    val dialogId: String,
    val connectorType: ConnectorType,
    val configuration: String,
    val intent: String,
    val storyDefinitionId: String,
    val applicationId: Id<BotApplicationConfiguration>,
)

@Data(internal = true)
@JacksonData(internal = true)
internal data class ConfigurationLookup(val configuration: BotApplicationConfiguration)

@Data(internal = true)
@JacksonData(internal = true)
internal data class TransitionLookup(val transition: DialogFlowStateTransitionCol)

@Data(internal = true)
@JacksonData(internal = true)
internal data class NextStateLookup(val nextState: DialogFlowStateCol)

@Data(internal = true)
@JacksonData(internal = true)
internal data class StoryLookup(val story: StoryDefinitionConfiguration)

private fun KProperty<TemporalAccessor?>.kDateToString(
    format: String? = "%Y-%m-%d",
    zoneId: ZoneId = defaultZoneId,
    onNull: String? = null,
): Bson = dateToString(format, zoneId, onNull)

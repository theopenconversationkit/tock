/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
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

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.dialog.ApplicationDialogFlowData
import ai.tock.bot.admin.dialog.DialogFlowStateData
import ai.tock.bot.admin.dialog.DialogFlowStateTransitionData
import ai.tock.bot.admin.dialog.DialogFlowTransitionStatsData
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
import ai.tock.bot.mongo.BotApplicationConfigurationMongoDAO.getHackedConfigurationByApplicationIdAndBot
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
import ai.tock.bot.mongo.DialogFlowStateTransitionStatCol_.Companion.TransitionId
import ai.tock.shared.ensureIndex
import ai.tock.shared.error
import ai.tock.shared.longProperty
import ai.tock.shared.security.TockObfuscatorService.obfuscate
import ai.tock.shared.sumByLong
import com.mongodb.client.model.IndexOptions
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.`in`
import org.litote.kmongo.aggregate
import org.litote.kmongo.all
import org.litote.kmongo.and
import org.litote.kmongo.ascendingSort
import org.litote.kmongo.eq
import org.litote.kmongo.find
import org.litote.kmongo.findOne
import org.litote.kmongo.from
import org.litote.kmongo.getCollection
import org.litote.kmongo.group
import org.litote.kmongo.gt
import org.litote.kmongo.lt
import org.litote.kmongo.match
import org.litote.kmongo.project
import org.litote.kmongo.size
import org.litote.kmongo.sum
import org.litote.kmongo.toId
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

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
                            ensureIndex(TransitionId)
                            ensureIndex(TransitionId, Date)
                            ensureIndex(DialogId)
                            ensureIndex(
                                    Date,
                                    indexOptions = IndexOptions()
                                            .expireAfter(longProperty("tock_bot_flow_stats_index_ttl_days", 365), TimeUnit.DAYS)
                            )
                        } catch (e: Exception) {
                            logger.error(e)
                        }
                    }

    override fun saveFlow(bot: BotDefinition, flow: DialogFlowDefinition) {
        TODO("not implemented")
    }

    override fun loadApplicationData(
            namespace: String,
            botId: String,
            applicationIds: Set<Id<BotApplicationConfiguration>>,
            from: ZonedDateTime?,
            to: ZonedDateTime?,
            intent: String?
    ): ApplicationDialogFlowData {
        logger.debug { "Fetching application flow for ns $namespace, bot $botId, apps $applicationIds from $from to $to..." }
        val states = findStates(namespace, botId)
        val transitions = findTransitions(namespace, botId)
        val stats =
                findStats(transitions.map { it._id }, applicationIds, from, to).associateBy { it.first }.mapValues { it.value.second }

        @Suppress("UNCHECKED_CAST")
        val transitionsWithStats = transitions.map {
            DialogFlowStateTransitionData(
                    it.previousStateId as? Id<DialogFlowStateData>?,
                    it.nextStateId as Id<DialogFlowStateData>,
                    it.intent,
                    it.step,
                    it.newEntities,
                    it.type,
                    stats[it._id] ?: 0
            )
        }.filter { it.count != 0L }

        val transitionCountByNext = transitionsWithStats.groupBy { it.nextStateId }.mapValues { e -> e.value.sumByLong { it.count } }

        @Suppress("UNCHECKED_CAST")
        val statesWithStats = states.map { s ->
            DialogFlowStateData(
                    s.storyDefinitionId,
                    s.intent,
                    s.step,
                    s.entities,
                    s.storyType,
                    s.storyName,
                    transitionCountByNext[s._id as Id<DialogFlowStateData>] ?: 0L,
                    s._id as Id<DialogFlowStateData>
            )
        }.filter {
            it.count != 0L
        }

        return ApplicationDialogFlowData(statesWithStats, transitionsWithStats, emptyList()/*TODO*/)
    }

    override fun search(namespace: String,
            botId: String,
            applicationIds: Set<Id<BotApplicationConfiguration>>,
            from: ZonedDateTime?,
            to: ZonedDateTime?,
            intent: String?
    ): List<DialogFlowTransitionStatsData> {
        val filter =
                and(
                    listOfNotNull(
                        if (applicationIds.isEmpty()) null else ApplicationId `in` applicationIds,
                        if (from == null) null else Date gt from.toInstant(),
                        if (to == null) null else Date lt to.toInstant()
                    )
                )
        logger.debug { "Flow Message filter: $filter" }
        val zoneId = ZoneId.of("Europe/Paris")
        return flowTransitionStatsCol.find(filter).ascendingSort(Date).toList()
                .map {
                    DialogFlowTransitionStatsData(
                            applicationId = it.applicationId.toString(),
                            transitionId = it.transitionId.toString(),
                            dialogId = it.dialogId.toString(),
                            text = it.text,
                            date = LocalDateTime.ofInstant(it.date, zoneId)
                    )
                }
    }

    override fun searchByDateWithIntent(namespace: String,
                                 botId: String,
                                 applicationIds: Set<Id<BotApplicationConfiguration>>,
                                 from: ZonedDateTime?,
                                 to: ZonedDateTime?,
                                 intent: String?
    ): Pair<List<DialogFlowTransitionStatsData>, List<String>> {
        val transitions = findTransitions(namespace, botId).groupBy { it._id.toString() }.mapValues { it.value.firstOrNull() }
        val messages = search(namespace, botId, applicationIds, from, to, intent)
        val transitionToIntent = messages.groupBy { it.transitionId }.keys.associateBy({it}, {transitions[it]?.intent})
        return Pair(messages.map {
            DialogFlowTransitionStatsData(
                    applicationId = it.applicationId,
                    transitionId = it.transitionId,
                    dialogId = it.dialogId,
                    text = transitionToIntent[it.transitionId],
                    date = it.date
            )
        }, emptyList())
    }

    override fun searchByDateWithActionType(namespace: String,
                                        botId: String,
                                        applicationIds: Set<Id<BotApplicationConfiguration>>,
                                        from: ZonedDateTime?,
                                        to: ZonedDateTime?,
                                        intent: String?
    ): Pair<List<DialogFlowTransitionStatsData>, List<String>> {
        val transitions = findTransitions(namespace, botId)
        val filteredTransitions = transitions
            .groupBy { it._id.toString() }
            .mapValues { it.value.firstOrNull() }
        val filteredTransitionsByIntent = filteredTransitions.filter { it.value?.intent == intent || intent.isNullOrEmpty() }

        val messages = search(namespace, botId, applicationIds, from, to, null)
        val transitionToType = messages.groupBy { it.transitionId }.keys.associateBy({it}, {filteredTransitionsByIntent[it]})
        val intents = messages.groupBy { it.transitionId }.keys.associateBy({it}, {filteredTransitions[it]}).values.mapNotNull { it?.intent }.distinct().sorted()
        return Pair(messages.filter { transitionToType[it.transitionId]?.type != null }.map {
            DialogFlowTransitionStatsData(
                    applicationId = it.applicationId,
                    transitionId = it.transitionId,
                    dialogId = it.dialogId,
                    text = transitionToType[it.transitionId]?.type.toString(),
                    date = it.date
            )
        }, intents)
    }

    override fun searchByDateWithStory(namespace: String,
                                       botId: String,
                                       applicationIds: Set<Id<BotApplicationConfiguration>>,
                                       from: ZonedDateTime?,
                                       to: ZonedDateTime?,
                                       intent: String?
    ): Pair<List<DialogFlowTransitionStatsData>, List<String>> {
        val states = findStates(namespace, botId).groupBy { it._id.toString() }.mapValues { it.value.firstOrNull() }
        val transitions = findTransitions(namespace, botId).groupBy { it._id.toString() }.mapValues { it.value.firstOrNull() }
        val messages = search(namespace, botId, applicationIds, from, to, intent)
        val transitionToState = messages.groupBy { it.transitionId }.keys.associateBy({it}, {transitions[it]?.nextStateId.toString()})
        val transitionToStory = transitionToState.mapValues { states[it.value]?.storyDefinitionId }
        return Pair(messages.map {
            DialogFlowTransitionStatsData(
                    applicationId = it.applicationId,
                    transitionId = it.transitionId,
                    dialogId = it.dialogId,
                    text = transitionToStory[it.transitionId],
                    date = it.date
            )
        }, emptyList())
    }

    private fun findStates(namespace: String, botId: String): List<DialogFlowStateCol> =
            flowStateCol.find(Namespace eq namespace, BotId eq botId).toList()

    private fun findTransitions(namespace: String, botId: String): List<DialogFlowStateTransitionCol> =
            flowTransitionCol.find(Namespace eq namespace, BotId eq botId).toList()

    private fun findStats(
            transitionIds: List<Id<DialogFlowStateTransitionCol>>,
            botAppConfIds: Set<Id<BotApplicationConfiguration>>,
            from: ZonedDateTime?,
            to: ZonedDateTime?
    ): List<Pair<Id<DialogFlowStateTransitionCol>, Long>> =
            flowTransitionStatsCol.aggregate<Pair<String, Long>>(
                    match(
                            and(
                                    listOfNotNull(
                                            TransitionId `in` transitionIds,
                                            if (botAppConfIds.isEmpty()) null else ApplicationId `in` botAppConfIds,
                                            if (from == null) null else Date gt from.toInstant(),
                                            if (to == null) null else Date lt to.toInstant()
                                    )
                            )
                    ),
                    group(
                            TransitionId,
                            Pair<*, Long>::second sum 1
                    ),
                    project(
                            Pair<Id<DialogFlowStateTransitionCol>, Long>::first from _id,
                            Pair<*, Long>::second from Pair<*, Long>::second
                    )

            ).map { it.first.toId<DialogFlowStateTransitionCol>() to it.second }.toList()

    private fun findState(botDefinition: BotDefinition, snapshot: Snapshot?): DialogFlowStateCol? {
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
                    storyName = snapshot.storyName ?: storyDefinitionId
            ).run {
                flowStateCol.findOne(
                        Namespace eq namespace,
                        BotId eq botId,
                        StoryDefinitionId eq storyDefinitionId,
                        Intent eq intentName,
                        Step eq step,
                        if (entities.size < 2) Entities eq entities else and(
                                Entities size entities.size,
                                Entities all entities
                        ),
                        StoryType eq storyType,
                        StoryName eq storyName
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
            lastUserAction: Action?
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
                    }
            )

    private fun findTransition(
            botDefinition: BotDefinition,
            previousStateId: Id<DialogFlowStateCol>?,
            nextStateId: Id<DialogFlowStateCol>,
            intent: String?,
            step: String?,
            newEntities: Set<String>,
            type: DialogFlowStateTransitionType
    ): DialogFlowStateTransitionCol =
            flowTransitionCol.findOne(
                    Namespace eq botDefinition.namespace,
                    BotId eq botDefinition.botId,
                    PreviousStateId eq previousStateId,
                    NextStateId eq nextStateId,
                    Intent eq intent,
                    Step eq step,
                    if (newEntities.size < 2) NewEntities eq newEntities else and(
                            NewEntities size newEntities.size,
                            NewEntities all newEntities
                    ),
                    Type eq type
            ) ?: (
                    DialogFlowStateTransitionCol(
                            botDefinition.namespace,
                            botDefinition.botId,
                            previousStateId,
                            nextStateId,
                            intent,
                            step,
                            newEntities,
                            type
                    )
                            .also { flowTransitionCol.insertOne(it) }
                    )


    fun addFlowStat(botDefinition: BotDefinition, lastUserAction: Action, dialog: Dialog, snapshot: SnapshotCol) {

        val previousState = findState(botDefinition, snapshot.snapshots.getOrNull(snapshot.snapshots.size - 2))
        val state = findState(botDefinition, snapshot.snapshots.lastOrNull())
        if (state != null) {
            val transition = findTransition(botDefinition, previousState, state, lastUserAction)
            val botAppConf = getHackedConfigurationByApplicationIdAndBot(
                    botDefinition.namespace, lastUserAction.applicationId, botDefinition.botId
            )
            if (botAppConf != null) {
                flowTransitionStatsCol.insertOne(
                        DialogFlowStateTransitionStatCol(
                                botAppConf._id,
                                transition._id,
                                dialog.id,
                                obfuscate((lastUserAction as? SendSentence)?.stringText)
                        )
                )
            } else {
                logger.warn { "unknown applicationId : ${lastUserAction.applicationId} for $botDefinition" }
            }
        } else {
            logger.warn { "unknown state : $dialog" }
        }
    }
}
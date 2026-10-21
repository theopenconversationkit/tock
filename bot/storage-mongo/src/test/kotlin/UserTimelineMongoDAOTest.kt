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

import ai.tock.bot.admin.annotation.BotAnnotation
import ai.tock.bot.admin.annotation.BotAnnotationEventComment
import ai.tock.bot.admin.annotation.BotAnnotationReasonType
import ai.tock.bot.admin.annotation.BotAnnotationState
import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.dialog.AnnotationReportQuery
import ai.tock.bot.admin.dialog.DialogReportQuery
import ai.tock.bot.admin.user.UserReportQuery
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.SimpleStoryHandlerBase
import ai.tock.bot.definition.StoryDefinitionBase
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.action.ActionMetadata
import ai.tock.bot.engine.action.SendAttachment
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.action.SendDebug
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.EventState
import ai.tock.bot.engine.dialog.Story
import ai.tock.bot.engine.message.DebugMessage
import ai.tock.bot.engine.nlp.NlpCallStats
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.bot.engine.user.UserTimeline
import ai.tock.nlp.api.client.model.NlpQuery
import ai.tock.nlp.api.client.model.NlpQueryContext
import ai.tock.nlp.api.client.model.NlpResult
import ai.tock.shared.defaultNamespace
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.litote.kmongo.eq
import org.litote.kmongo.newId
import java.time.Instant
import java.time.ZonedDateTime
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 *
 */
internal class UserTimelineMongoDAOTest : AbstractTest() {
    @Test
    fun `getClientDialogs retrieves user timeline WHEN clientId is not null`() =
        runBlocking {
            val id = PlayerId("id", PlayerType.user, "clientId")
            val u = UserTimeline(id, dialogs = mutableListOf(Dialog(setOf(id))))
            UserTimelineMongoDAO.save(u, "namespace")
            assertNotEquals(
                u.dialogs,
                UserTimelineMongoDAO.getClientDialogs("namespace", id.clientId!!) { error("no story provided") },
            )
            delay(1000)
            assertEquals(
                u.dialogs,
                UserTimelineMongoDAO.getClientDialogs("namespace", id.clientId!!) { error("no story provided") },
            )
        }

    @Test
    fun `search with flags does not fail`() {
        UserTimelineMongoDAO.search(
            UserReportQuery(
                defaultNamespace,
                "bot_open_data",
                Locale.FRENCH,
                flags = mapOf("tock_profile_loaded" to "true"),
            ),
        )
    }

    @Test
    fun `get userTimeLine with temporaryIds `() =
        runBlocking {
            val id = PlayerId("id", PlayerType.user, "clientId")
            val u = UserTimeline(id, dialogs = mutableListOf(Dialog(setOf(id))), temporaryIds = mutableSetOf("123456879", "1477854545"))
            UserTimelineMongoDAO.save(u, "namespace")
            assertEquals(
                u.toString(),
                UserTimelineMongoDAO.loadByTemporaryIdsWithoutDialogs("namespace", listOf("123456879", "99999999")).firstOrNull()?.toString(),
            )
        }

    @Test
    fun `updatePlayerId update timeline and dialog player id`() =
        runBlocking {
            val id = PlayerId("id", PlayerType.user)
            val u = UserTimeline(id, dialogs = mutableListOf(Dialog(setOf(id))))
            u.userPreferences.firstName = "Ada"
            UserTimelineMongoDAO.save(u, "namespace")
            UserTimelineMongoDAO.dialogTextCol.save(DialogTextCol("migration text", u.dialogs.single().id))
            println(UserTimelineMongoDAO.loadWithLastValidDialog("namespace", id, null) { error("no story provided") })

            val newId = PlayerId("new-id", PlayerType.user, "a")
            UserTimelineMongoDAO.updatePlayerId("namespace", id, newId)
            println(UserTimelineMongoDAO.loadWithLastValidDialog("namespace", newId, null) { error("no story provided") })
            assertEquals(
                u.dialogs.map { it.copy(playerIds = setOf(newId)) },
                UserTimelineMongoDAO.getClientDialogs("namespace", newId.clientId!!) { error("no story provided") },
            )
            assertEquals(
                newId,
                UserTimelineMongoDAO.loadWithoutDialogs("namespace", newId).playerId,
            )
            assertEquals(
                u.userPreferences,
                UserTimelineMongoDAO.loadWithoutDialogs("namespace", newId).userPreferences,
            )
            assertEquals(
                u.dialogs.single().id,
                UserTimelineMongoDAO.dialogTextCol.findOne(DialogTextCol::text eq "migration text")?.dialogId,
            )
        }

    @Test
    suspend fun `remove deletes dialog-owned side collections`() {
        val namespace = "remove-dialog-dependencies"
        val user = PlayerId("remove-dialog-dependencies-user", PlayerType.user)
        val bot = PlayerId("remove-dialog-dependencies-bot", PlayerType.bot)
        val actionId = newId<ai.tock.bot.engine.action.Action>()
        val storyDefinition =
            StoryDefinitionBase(
                "remove-dialog-dependencies-story",
                object : SimpleStoryHandlerBase() {
                    override fun action(bus: BotBus) {}
                },
            )
        val dialog =
            Dialog(
                playerIds = setOf(user, bot),
                stories =
                    mutableListOf(
                        Story(
                            storyDefinition,
                            Intent.unknown,
                            actions =
                                mutableListOf(
                                    SendSentence(
                                        user,
                                        "remove-dialog-dependencies-app",
                                        bot,
                                        "text",
                                        mutableListOf(),
                                        actionId,
                                        state = EventState(),
                                    ),
                                ),
                        ),
                    ),
            )
        UserTimelineMongoDAO.save(UserTimeline(user, dialogs = mutableListOf(dialog)), namespace)
        delay(100)

        UserTimelineMongoDAO.dialogTextCol.deleteMany(DialogTextCol::dialogId eq dialog.id)
        UserTimelineMongoDAO.dialogTextCol.save(DialogTextCol("text", dialog.id))
        UserTimelineMongoDAO.connectorMessageCol.save(ConnectorMessageCol(ConnectorMessageColId(actionId, dialog.id), emptyList()))
        UserTimelineMongoDAO.nlpStatsCol.save(
            NlpStatsCol(
                NlpStatsColId(actionId, dialog.id),
                NlpCallStats(
                    Locale.ENGLISH,
                    nlpQuery = NlpQuery(listOf("text"), namespace, "app", NlpQueryContext(Locale.ENGLISH)),
                    nlpResult = NlpResult("unknown", namespace, Locale.ENGLISH, retainedQuery = "text"),
                ),
                namespace,
            ),
        )
        UserTimelineMongoDAO.snapshotCol.save(SnapshotCol(dialog.id, emptyList()))
        val archivedId = newId<ArchivedEntityValuesCol>()
        UserTimelineMongoDAO.archivedEntityValuesCol.save(
            ArchivedEntityValuesCol(
                archivedId,
                listOf(ArchivedEntityValuesCol.ArchivedEntityValueWrapper(null, actionId)),
            ),
        )

        assertTrue(UserTimelineMongoDAO.remove(namespace, user) > 0)

        assertNull(UserTimelineMongoDAO.dialogTextCol.findOne(DialogTextCol::dialogId eq dialog.id))
        assertNull(UserTimelineMongoDAO.connectorMessageCol.findOneById(ConnectorMessageColId(actionId, dialog.id)))
        assertNull(UserTimelineMongoDAO.nlpStatsCol.findOneById(NlpStatsColId(actionId, dialog.id)))
        assertNull(UserTimelineMongoDAO.snapshotCol.findOneById(dialog.id))
        assertNull(UserTimelineMongoDAO.archivedEntityValuesCol.findOneById(archivedId))
    }

    @Test
    fun `save appends dialog snapshots without overwriting previous snapshots`() =
        runBlocking {
            val namespace = "test_dialog_snapshot_append"
            val applicationId = "test_app_dialog_snapshot_append"
            val userId = PlayerId("user_dialog_snapshot_append", PlayerType.user)
            val botPlayerId = PlayerId("bot_dialog_snapshot_append", PlayerType.bot)
            val storyDef =
                StoryDefinitionBase(
                    "test_story_dialog_snapshot_append",
                    object : SimpleStoryHandlerBase() {
                        override fun action(bus: BotBus) {}
                    },
                )

            val firstAction =
                SendSentence(
                    botPlayerId,
                    applicationId,
                    userId,
                    "first",
                    mutableListOf(),
                    newId(),
                    ZonedDateTime.parse("2025-12-01T10:00:00Z").toInstant(),
                    EventState(),
                    ActionMetadata(),
                )
            val secondAction =
                SendSentence(
                    botPlayerId,
                    applicationId,
                    userId,
                    "second",
                    mutableListOf(),
                    newId(),
                    ZonedDateTime.parse("2025-12-01T10:00:01Z").toInstant(),
                    EventState(),
                    ActionMetadata(),
                )

            val dialogId = newId<Dialog>()
            val fullDialog =
                Dialog(
                    playerIds = setOf(userId, botPlayerId),
                    id = dialogId,
                    stories =
                        mutableListOf(
                            Story(
                                storyDef,
                                Intent("test"),
                                actions = mutableListOf(firstAction, secondAction),
                            ),
                        ),
                )
            val staleDialog =
                Dialog(
                    playerIds = setOf(userId, botPlayerId),
                    id = dialogId,
                    stories =
                        mutableListOf(
                            Story(
                                storyDef,
                                Intent("test"),
                                actions = mutableListOf(firstAction),
                            ),
                        ),
                )

            UserTimelineMongoDAO.save(UserTimeline(userId, dialogs = mutableListOf(fullDialog)), namespace)
            delay(200)
            UserTimelineMongoDAO.save(UserTimeline(userId, dialogs = mutableListOf(staleDialog)), namespace)
            delay(200)

            val snapshots = UserTimelineMongoDAO.getSnapshots(dialogId)

            assertEquals(2, snapshots.size)
            assertTrue(snapshots.all { it.storyDefinitionId == storyDef.id })
        }

    @Test
    fun `dialogActivity filters should handle all null and non-null date combinations`() =
        runBlocking {
            val namespace = "test_activity_filter_all_cases"
            val applicationId = "test_app_activity_all_cases"
            val nlpModel = "test_model_activity_all_cases"
            val userId = PlayerId("user_activity_all_cases_test", PlayerType.user)
            val botPlayerId = PlayerId("bot_activity_all_cases_test", PlayerType.bot)

            val botConfig =
                BotApplicationConfiguration(
                    applicationId = applicationId,
                    botId = botPlayerId.id,
                    namespace = namespace,
                    nlpModel = nlpModel,
                    connectorType = ConnectorType("test"),
                )
            BotApplicationConfigurationMongoDAO.save(botConfig)

            try {
                // Dialog A: Dec 10-12 (overlaps filter Dec 11-13)
                val dialogAStart = ZonedDateTime.parse("2025-12-10T10:00:00Z")
                val dialogAEnd = ZonedDateTime.parse("2025-12-12T10:00:00Z")

                // Dialog B: Dec 12-14 (overlaps filter Dec 11-13)
                val dialogBStart = ZonedDateTime.parse("2025-12-12T10:00:00Z")
                val dialogBEnd = ZonedDateTime.parse("2025-12-14T10:00:00Z")

                // Dialog C: Dec 8-9 (does NOT overlap filter Dec 11-13)
                val dialogCStart = ZonedDateTime.parse("2025-12-08T10:00:00Z")
                val dialogCEnd = ZonedDateTime.parse("2025-12-09T10:00:00Z")

                // Dialog D: Dec 15-16 (does NOT overlap filter Dec 11-13)
                val dialogDStart = ZonedDateTime.parse("2025-12-15T10:00:00Z")
                val dialogDEnd = ZonedDateTime.parse("2025-12-16T10:00:00Z")

                // Filter period: Dec 11-13 (inclusive both ends)
                val filterStartDate = ZonedDateTime.parse("2025-12-11T10:00:00Z")
                val filterEndDate = ZonedDateTime.parse("2025-12-13T10:00:00Z")

                val storyHandler =
                    object : SimpleStoryHandlerBase() {
                        override fun action(bus: BotBus) {
                            // Empty handler for test
                        }
                    }
                val storyDef = StoryDefinitionBase("test_story", storyHandler)

                fun createDialogWithTwoActions(
                    startDate: ZonedDateTime,
                    endDate: ZonedDateTime,
                    action1Text: String,
                    action2Text: String,
                ): Dialog =
                    Dialog(
                        playerIds = setOf(userId, botPlayerId),
                        stories =
                            mutableListOf(
                                Story(
                                    storyDef,
                                    Intent("test"),
                                    actions =
                                        mutableListOf(
                                            SendSentence(userId, applicationId, botPlayerId, action1Text, mutableListOf(), newId(), startDate.toInstant(), EventState(), ActionMetadata()),
                                            SendSentence(userId, applicationId, botPlayerId, action2Text, mutableListOf(), newId(), endDate.toInstant(), EventState(), ActionMetadata()),
                                        ),
                                ),
                            ),
                    )

                val dialogA = createDialogWithTwoActions(dialogAStart, dialogAEnd, "action1", "action2")
                val dialogB = createDialogWithTwoActions(dialogBStart, dialogBEnd, "action3", "action4")
                val dialogC = createDialogWithTwoActions(dialogCStart, dialogCEnd, "action5", "action6")
                val dialogD = createDialogWithTwoActions(dialogDStart, dialogDEnd, "action7", "action8")

                val userTimeline =
                    UserTimeline(
                        playerId = userId,
                        dialogs = mutableListOf(dialogA, dialogB, dialogC, dialogD),
                    )

                UserTimelineMongoDAO.save(userTimeline, namespace)
                delay(100)

                // Test 1: Both dates null -> should return all dialogs
                val queryBothNull =
                    DialogReportQuery(
                        namespace = namespace,
                        nlpModel = nlpModel,
                        applicationId = applicationId,
                        dialogActivityFrom = null,
                        dialogActivityTo = null,
                        displayTests = true,
                    )
                val resultBothNull = UserTimelineMongoDAO.search(queryBothNull)
                assertTrue(
                    resultBothNull.total >= 4L,
                    "All dialogs should be included when both dialogActivityFrom and dialogActivityTo are null. Found: ${resultBothNull.total}",
                )

                // Test 2: Only dialogActivityFrom set -> condition: at least one action >= fromDate
                // Dialog A: actions at Dec 10, Dec 12 -> Dec 12 >= Dec 11 -> TRUE
                // Dialog B: actions at Dec 12, Dec 14 -> Dec 14 >= Dec 11 -> TRUE
                // Dialog C: actions at Dec 8, Dec 9 -> Dec 9 < Dec 11 -> FALSE
                // Dialog D: actions at Dec 15, Dec 16 -> Dec 16 >= Dec 11 -> TRUE
                val queryFromOnly =
                    DialogReportQuery(
                        namespace = namespace,
                        nlpModel = nlpModel,
                        applicationId = applicationId,
                        dialogActivityFrom = filterStartDate,
                        dialogActivityTo = null,
                        displayTests = true,
                    )
                val resultFromOnly = UserTimelineMongoDAO.search(queryFromOnly)
                assertEquals(
                    3L,
                    resultFromOnly.total,
                    "DialogA, DialogB, and DialogD should be included when only dialogActivityFrom is set (at least one action >= Dec 11). Found: ${resultFromOnly.total}",
                )

                // Test 3: Only dialogActivityTo set -> condition: at least one action <= toDate
                // Dialog A: actions at Dec 10, Dec 12 -> Dec 12 <= Dec 13 -> TRUE
                // Dialog B: actions at Dec 12, Dec 14 -> Dec 12 <= Dec 13 -> TRUE
                // Dialog C: actions at Dec 8, Dec 9 -> Dec 9 <= Dec 13 -> TRUE
                // Dialog D: actions at Dec 15, Dec 16 -> Dec 15 > Dec 13, Dec 16 > Dec 13 -> FALSE
                val queryToOnly =
                    DialogReportQuery(
                        namespace = namespace,
                        nlpModel = nlpModel,
                        applicationId = applicationId,
                        dialogActivityFrom = null,
                        dialogActivityTo = filterEndDate,
                        displayTests = true,
                    )
                val resultToOnly = UserTimelineMongoDAO.search(queryToOnly)
                assertEquals(
                    3L,
                    resultToOnly.total,
                    "DialogA, DialogB, and DialogC should be included when only dialogActivityTo is set (at least one action <= Dec 13). Found: ${resultToOnly.total}",
                )

                // Test 4: Both dates set -> condition: at least one action >= fromDate AND at least one action <= toDate
                // Dialog A: actions at Dec 10, Dec 12 -> Dec 12 >= Dec 11 AND Dec 10 <= Dec 13 -> TRUE
                // Dialog B: actions at Dec 12, Dec 14 -> Dec 14 >= Dec 11 AND Dec 12 <= Dec 13 -> TRUE
                // Dialog C: actions at Dec 8, Dec 9 -> Dec 9 < Dec 11 -> FALSE
                // Dialog D: actions at Dec 15, Dec 16 -> Dec 15 > Dec 13, Dec 16 > Dec 13 -> FALSE
                val queryBothSet =
                    DialogReportQuery(
                        namespace = namespace,
                        nlpModel = nlpModel,
                        applicationId = applicationId,
                        dialogActivityFrom = filterStartDate,
                        dialogActivityTo = filterEndDate,
                        displayTests = true,
                    )
                val resultBothSet = UserTimelineMongoDAO.search(queryBothSet)
                assertEquals(
                    2L,
                    resultBothSet.total,
                    "Only dialogA and dialogB should be included when both dates are set (activity period Dec 11-13 inclusive). " +
                        "DialogA: Dec 10-12, DialogB: Dec 12-14, DialogC: Dec 8-9 (excluded), DialogD: Dec 15-16 (excluded). " +
                        "Found: ${resultBothSet.total}",
                )
            } finally {
                BotApplicationConfigurationMongoDAO.delete(botConfig)
            }
        }

    @Test
    fun `date filters should be inclusive on both bounds`() =
        runBlocking {
            val namespace = "test_inclusive_bounds"
            val applicationId = "test_app_inclusive"
            val nlpModel = "test_model_inclusive"
            val userId = PlayerId("user_inclusive_test", PlayerType.user)
            val botPlayerId = PlayerId("bot_inclusive_test", PlayerType.bot)

            val botConfig =
                BotApplicationConfiguration(
                    applicationId = applicationId,
                    botId = botPlayerId.id,
                    namespace = namespace,
                    nlpModel = nlpModel,
                    connectorType = ConnectorType("test"),
                )
            BotApplicationConfigurationMongoDAO.save(botConfig)

            try {
                // Exact filter boundaries
                val filterDate = ZonedDateTime.parse("2025-12-15T10:00:00Z")

                // Dialog with action exactly at the filter date
                val dialogAtExactDate = ZonedDateTime.parse("2025-12-15T10:00:00Z")

                // Dialog with action before the filter date
                val dialogBeforeDate = ZonedDateTime.parse("2025-12-14T10:00:00Z")

                // Dialog with action after the filter date
                val dialogAfterDate = ZonedDateTime.parse("2025-12-16T10:00:00Z")

                val storyHandler =
                    object : SimpleStoryHandlerBase() {
                        override fun action(bus: BotBus) {}
                    }
                val storyDef = StoryDefinitionBase("test_story", storyHandler)

                fun createSingleActionDialog(actionDate: ZonedDateTime): Dialog =
                    Dialog(
                        playerIds = setOf(userId, botPlayerId),
                        stories =
                            mutableListOf(
                                Story(
                                    storyDef,
                                    Intent("test"),
                                    actions =
                                        mutableListOf(
                                            SendSentence(
                                                userId,
                                                applicationId,
                                                botPlayerId,
                                                "test message",
                                                mutableListOf(),
                                                newId(),
                                                actionDate.toInstant(),
                                                EventState(),
                                                ActionMetadata(),
                                            ),
                                        ),
                                ),
                            ),
                    )

                val dialogExact = createSingleActionDialog(dialogAtExactDate)
                val dialogBefore = createSingleActionDialog(dialogBeforeDate)
                val dialogAfter = createSingleActionDialog(dialogAfterDate)

                val userTimeline =
                    UserTimeline(
                        playerId = userId,
                        dialogs = mutableListOf(dialogExact, dialogBefore, dialogAfter),
                    )

                UserTimelineMongoDAO.save(userTimeline, namespace)
                delay(100)

                // Test 1: dialogActivityFrom inclusive - dialog at exact date should be included
                val queryActivityFrom =
                    DialogReportQuery(
                        namespace = namespace,
                        nlpModel = nlpModel,
                        applicationId = applicationId,
                        dialogActivityFrom = filterDate,
                        dialogActivityTo = null,
                        displayTests = true,
                    )
                val resultActivityFrom = UserTimelineMongoDAO.search(queryActivityFrom)
                assertTrue(
                    resultActivityFrom.total >= 2L,
                    "dialogActivityFrom should be INCLUSIVE: dialogs with activity at exact date ($dialogAtExactDate) and after ($dialogAfterDate) " +
                        "should be included. Found: ${resultActivityFrom.total}",
                )

                // Test 2: dialogActivityTo inclusive - dialog at exact date should be included
                val queryActivityTo =
                    DialogReportQuery(
                        namespace = namespace,
                        nlpModel = nlpModel,
                        applicationId = applicationId,
                        dialogActivityFrom = null,
                        dialogActivityTo = filterDate,
                        displayTests = true,
                    )
                val resultActivityTo = UserTimelineMongoDAO.search(queryActivityTo)
                assertTrue(
                    resultActivityTo.total >= 2L,
                    "dialogActivityTo should be INCLUSIVE: dialogs with activity at exact date ($dialogAtExactDate) and before ($dialogBeforeDate) " +
                        "should be included. Found: ${resultActivityTo.total}",
                )
            } finally {
                BotApplicationConfigurationMongoDAO.delete(botConfig)
            }
        }

    @Test
    fun `evaluableActionsOnly filter excludes dialogs with only debug or empty text actions`() =
        runBlocking {
            val namespace = "test_evaluable_actions_filter"
            val applicationId = "test_app_evaluable"
            val nlpModel = "test_model_evaluable"
            val userId = PlayerId("user_evaluable_test", PlayerType.user)
            val botPlayerId = PlayerId("bot_evaluable_test", PlayerType.bot)

            val botConfig =
                BotApplicationConfiguration(
                    applicationId = applicationId,
                    botId = botPlayerId.id,
                    namespace = namespace,
                    nlpModel = nlpModel,
                    connectorType = ConnectorType("test"),
                )
            BotApplicationConfigurationMongoDAO.save(botConfig)

            try {
                val storyHandler =
                    object : SimpleStoryHandlerBase() {
                        override fun action(bus: BotBus) {}
                    }
                val storyDef = StoryDefinitionBase("test_story_evaluable", storyHandler)
                val actionDate = ZonedDateTime.parse("2025-12-15T10:00:00Z").toInstant()

                // Dialog A: bot Sentence with non-empty text (evaluable)
                val dialogA =
                    Dialog(
                        playerIds = setOf(userId, botPlayerId),
                        stories =
                            mutableListOf(
                                Story(
                                    storyDef,
                                    Intent("test"),
                                    actions =
                                        mutableListOf(
                                            SendSentence(
                                                botPlayerId,
                                                applicationId,
                                                userId,
                                                "Hello",
                                                mutableListOf(),
                                                newId(),
                                                actionDate,
                                                EventState(),
                                                ActionMetadata(),
                                            ),
                                        ),
                                ),
                            ),
                    )

                // Dialog B: only SendDebug (not evaluable)
                val dialogB =
                    Dialog(
                        playerIds = setOf(userId, botPlayerId),
                        stories =
                            mutableListOf(
                                Story(
                                    storyDef,
                                    Intent("test"),
                                    actions =
                                        mutableListOf(
                                            SendDebug(
                                                botPlayerId,
                                                applicationId,
                                                userId,
                                                "debug text",
                                                null,
                                                newId(),
                                                actionDate,
                                                EventState(),
                                                ActionMetadata(),
                                            ),
                                        ),
                                ),
                            ),
                    )

                // Dialog C: bot Sentence with empty text (not evaluable)
                val dialogC =
                    Dialog(
                        playerIds = setOf(userId, botPlayerId),
                        stories =
                            mutableListOf(
                                Story(
                                    storyDef,
                                    Intent("test"),
                                    actions =
                                        mutableListOf(
                                            SendSentence(
                                                botPlayerId,
                                                applicationId,
                                                userId,
                                                "",
                                                mutableListOf(),
                                                newId(),
                                                actionDate,
                                                EventState(),
                                                ActionMetadata(),
                                            ),
                                        ),
                                ),
                            ),
                    )

                // Dialog D: only SendChoice (evaluable - new behavior)
                val dialogD =
                    Dialog(
                        playerIds = setOf(userId, botPlayerId),
                        stories =
                            mutableListOf(
                                Story(
                                    storyDef,
                                    Intent("test"),
                                    actions =
                                        mutableListOf(
                                            SendChoice(
                                                botPlayerId,
                                                applicationId,
                                                userId,
                                                "test_intent",
                                                emptyMap(),
                                                newId(),
                                                actionDate,
                                                EventState(),
                                                ActionMetadata(),
                                            ),
                                        ),
                                ),
                            ),
                    )

                val userTimeline =
                    UserTimeline(
                        playerId = userId,
                        dialogs = mutableListOf(dialogA, dialogB, dialogC, dialogD),
                    )

                UserTimelineMongoDAO.save(userTimeline, namespace)
                delay(100)

                val queryWithFilter =
                    DialogReportQuery(
                        namespace = namespace,
                        nlpModel = nlpModel,
                        applicationId = applicationId,
                        displayTests = true,
                        evaluableActionsOnly = true,
                    )
                val resultWithFilter = UserTimelineMongoDAO.search(queryWithFilter)

                assertEquals(
                    2L,
                    resultWithFilter.total,
                    "DialogA (Sentence with text) and DialogD (Choice) should be included. " +
                        "DialogB (Debug) and DialogC (empty text) should be excluded. Found: ${resultWithFilter.total}",
                )

                val queryWithoutFilter =
                    DialogReportQuery(
                        namespace = namespace,
                        nlpModel = nlpModel,
                        applicationId = applicationId,
                        displayTests = true,
                        evaluableActionsOnly = false,
                    )
                val resultWithoutFilter = UserTimelineMongoDAO.search(queryWithoutFilter)

                assertTrue(
                    resultWithoutFilter.total >= 4L,
                    "Without evaluableActionsOnly filter, all 4 dialogs should be included. Found: ${resultWithoutFilter.total}",
                )
            } finally {
                BotApplicationConfigurationMongoDAO.delete(botConfig)
            }
        }

    @Test
    fun `evaluableActionsOnly includes dialogs with only Attachment actions`() =
        runBlocking {
            val namespace = "test_evaluable_attachment"
            val applicationId = "test_app_attachment"
            val nlpModel = "test_model_attachment"
            val userId = PlayerId("user_attachment_test", PlayerType.user)
            val botPlayerId = PlayerId("bot_attachment_test", PlayerType.bot)

            val botConfig =
                BotApplicationConfiguration(
                    applicationId = applicationId,
                    botId = botPlayerId.id,
                    namespace = namespace,
                    nlpModel = nlpModel,
                    connectorType = ConnectorType("test"),
                )
            BotApplicationConfigurationMongoDAO.save(botConfig)

            try {
                val storyHandler =
                    object : SimpleStoryHandlerBase() {
                        override fun action(bus: BotBus) {}
                    }
                val storyDef = StoryDefinitionBase("test_story_attachment", storyHandler)
                val actionDate = ZonedDateTime.parse("2025-12-15T10:00:00Z").toInstant()

                val dialogWithAttachment =
                    Dialog(
                        playerIds = setOf(userId, botPlayerId),
                        stories =
                            mutableListOf(
                                Story(
                                    storyDef,
                                    Intent("test"),
                                    actions =
                                        mutableListOf(
                                            SendAttachment(
                                                botPlayerId,
                                                applicationId,
                                                userId,
                                                "https://example.com/file.pdf",
                                                SendAttachment.AttachmentType.file,
                                                newId(),
                                                actionDate,
                                                EventState(),
                                                ActionMetadata(),
                                            ),
                                        ),
                                ),
                            ),
                    )

                val userTimeline =
                    UserTimeline(
                        playerId = userId,
                        dialogs = mutableListOf(dialogWithAttachment),
                    )

                UserTimelineMongoDAO.save(userTimeline, namespace)
                delay(100)

                val query =
                    DialogReportQuery(
                        namespace = namespace,
                        nlpModel = nlpModel,
                        applicationId = applicationId,
                        displayTests = true,
                        evaluableActionsOnly = true,
                    )
                val result = UserTimelineMongoDAO.search(query)

                assertEquals(
                    1L,
                    result.total,
                    "Dialog with only SendAttachment should be included. Found: ${result.total}",
                )
            } finally {
                BotApplicationConfigurationMongoDAO.delete(botConfig)
            }
        }

    @Test
    fun `evaluableActionsOnly filters actions when returning evaluable dialogs`() =
        runBlocking {
            val namespace = "test_evaluable_filter_actions"
            val applicationId = "test_app_filter_actions"
            val nlpModel = "test_model_filter_actions"
            val userId = PlayerId("user_filter_test", PlayerType.user)
            val botPlayerId = PlayerId("bot_filter_test", PlayerType.bot)

            val botConfig =
                BotApplicationConfiguration(
                    applicationId = applicationId,
                    botId = botPlayerId.id,
                    namespace = namespace,
                    nlpModel = nlpModel,
                    connectorType = ConnectorType("test"),
                )
            BotApplicationConfigurationMongoDAO.save(botConfig)

            try {
                val storyHandler =
                    object : SimpleStoryHandlerBase() {
                        override fun action(bus: BotBus) {}
                    }
                val storyDef = StoryDefinitionBase("test_story_filter", storyHandler)
                val actionDate = ZonedDateTime.parse("2025-12-15T10:00:00Z").toInstant()

                val dialogWithMixedActions =
                    Dialog(
                        playerIds = setOf(userId, botPlayerId),
                        stories =
                            mutableListOf(
                                Story(
                                    storyDef,
                                    Intent("test"),
                                    actions =
                                        mutableListOf(
                                            SendSentence(
                                                botPlayerId,
                                                applicationId,
                                                userId,
                                                "Hello",
                                                mutableListOf(),
                                                newId(),
                                                actionDate,
                                                EventState(),
                                                ActionMetadata(),
                                            ),
                                            SendDebug(
                                                botPlayerId,
                                                applicationId,
                                                userId,
                                                "debug text",
                                                null,
                                                newId(),
                                                actionDate,
                                                EventState(),
                                                ActionMetadata(),
                                            ),
                                            SendChoice(
                                                botPlayerId,
                                                applicationId,
                                                userId,
                                                "test_intent",
                                                emptyMap(),
                                                newId(),
                                                actionDate,
                                                EventState(),
                                                ActionMetadata(),
                                            ),
                                        ),
                                ),
                            ),
                    )

                val userTimeline =
                    UserTimeline(
                        playerId = userId,
                        dialogs = mutableListOf(dialogWithMixedActions),
                    )

                UserTimelineMongoDAO.save(userTimeline, namespace)
                delay(100)

                val query =
                    DialogReportQuery(
                        namespace = namespace,
                        nlpModel = nlpModel,
                        applicationId = applicationId,
                        displayTests = true,
                        evaluableActionsOnly = true,
                    )
                val result = UserTimelineMongoDAO.search(query)

                assertEquals(1, result.dialogs.size)
                val dialogReport = result.dialogs.first()
                assertEquals(
                    2,
                    dialogReport.actions.size,
                    "DialogReport should contain only Sentence and Choice (Debug excluded). Found: ${dialogReport.actions.size}",
                )
                val hasDebugMessage =
                    dialogReport.actions.any { it.message is DebugMessage }
                assertTrue(
                    !hasDebugMessage,
                    "DialogReport actions should not contain DebugMessage",
                )
            } finally {
                BotApplicationConfigurationMongoDAO.delete(botConfig)
            }
        }

    @Test
    fun `searchAnnotations returns flat cross-dialog list of ANOMALY and REVIEW_NEEDED annotations sorted by lastUpdateDate desc`() =
        runBlocking {
            val namespace = "ns_annotations_search"
            val otherNamespace = "ns_other"
            val botId = "bot_annotations_test"
            val otherBotId = "bot_other"
            val appId = "app_test"
            val otherAppId = "app_other"
            val userId = PlayerId("user_annot", PlayerType.user)
            val botPlayerId = PlayerId(botId, PlayerType.bot)
            val otherBotPlayerId = PlayerId(otherBotId, PlayerType.bot)

            // Save bot configs so getApplicationIds works
            val botConfig =
                BotApplicationConfiguration(
                    applicationId = appId,
                    botId = botId,
                    namespace = namespace,
                    nlpModel = botId,
                    connectorType = ConnectorType("test"),
                )
            val otherBotConfig =
                BotApplicationConfiguration(
                    applicationId = otherAppId,
                    botId = otherBotId,
                    namespace = otherNamespace,
                    nlpModel = otherBotId,
                    connectorType = ConnectorType("test"),
                )
            BotApplicationConfigurationMongoDAO.save(botConfig)
            BotApplicationConfigurationMongoDAO.save(otherBotConfig)

            try {
                val storyDef =
                    StoryDefinitionBase(
                        "test_story",
                        object : SimpleStoryHandlerBase() {
                            override fun action(bus: BotBus) {}
                        },
                    )
                // Fixed, whole-second instant: persisted as a millisecond Date, so equality checks
                // on actionDate are not broken by Instant.now()'s nanosecond precision.
                val now = Instant.parse("2026-06-01T12:00:00Z")
                val otherUserId = PlayerId("user_annot_other", PlayerType.user)

                // Dialog 1: action with ANOMALY + RAG debug
                val dialog1Id = newId<Dialog>()
                val action1Id = newId<ai.tock.bot.engine.action.Action>()
                val action1 =
                    ai.tock.bot.engine.action.SendSentenceWithFootnotes(
                        userId,
                        appId,
                        botPlayerId,
                        "answer1",
                        id = action1Id,
                        date = now.minusSeconds(100),
                        ragDebug = mapOf("user_question" to "What is AI?"),
                        annotation =
                            BotAnnotation(
                                state = BotAnnotationState.ANOMALY,
                                reason = BotAnnotationReasonType.INACCURATE_ANSWER,
                                description = "Wrong info",
                                groundTruth = null,
                                events = mutableListOf(),
                                creationDate = now.minusSeconds(100),
                                lastUpdateDate = now.minusSeconds(100),
                            ),
                    )
                val dialog1 =
                    Dialog(
                        playerIds = setOf(userId, botPlayerId),
                        id = dialog1Id,
                        stories = mutableListOf(Story(storyDef, Intent.unknown, actions = mutableListOf(action1))),
                    )

                // Dialog 2: 2 open annotations on different actions + non-annotated action
                val dialog2Id = newId<Dialog>()
                val action2aId = newId<ai.tock.bot.engine.action.Action>()
                val action2bId = newId<ai.tock.bot.engine.action.Action>()
                val action2cId = newId<ai.tock.bot.engine.action.Action>()
                val action2a =
                    SendSentence(
                        userId,
                        appId,
                        botPlayerId,
                        "answer2a",
                        id = action2aId,
                        date = now.minusSeconds(50),
                        annotation =
                            BotAnnotation(
                                state = BotAnnotationState.REVIEW_NEEDED,
                                reason = BotAnnotationReasonType.INCOMPLETE_ANSWER,
                                description = "Incomplete",
                                groundTruth = null,
                                events = mutableListOf(),
                                creationDate = now.minusSeconds(50),
                                lastUpdateDate = now.minusSeconds(50),
                            ),
                    )
                val action2b =
                    SendSentence(
                        userId,
                        appId,
                        botPlayerId,
                        "answer2b",
                        id = action2bId,
                        date = now.minusSeconds(30),
                        annotation =
                            BotAnnotation(
                                state = BotAnnotationState.REVIEW_NEEDED,
                                reason = BotAnnotationReasonType.HALLUCINATION,
                                description = "Made up",
                                groundTruth = null,
                                // A real, polymorphic event (serialized/read back by "type"): proves the
                                // aggregation deserializes events AND that toReport() actually empties a
                                // non-empty list — an all-empty fixture would pass either way.
                                events =
                                    mutableListOf(
                                        BotAnnotationEventComment(
                                            eventId = newId(),
                                            creationDate = now.minusSeconds(30),
                                            lastUpdateDate = now.minusSeconds(30),
                                            user = "reviewer",
                                            comment = "please double-check this answer",
                                        ),
                                    ),
                                creationDate = now.minusSeconds(30),
                                lastUpdateDate = now.minusSeconds(30),
                            ),
                    )
                val action2c = SendSentence(userId, appId, botPlayerId, "answer2c", id = action2cId, date = now)
                val dialog2 =
                    Dialog(
                        playerIds = setOf(userId, botPlayerId),
                        id = dialog2Id,
                        stories = mutableListOf(Story(storyDef, Intent.unknown, actions = mutableListOf(action2a, action2b, action2c))),
                    )

                // Dialog 3: two closed annotations (RESOLVED + WONT_FIX) — neither is "open",
                // so the four states are all represented across the fixture and none of these surface
                // in the open-states search.
                val dialog3Id = newId<Dialog>()
                val action3Id = newId<ai.tock.bot.engine.action.Action>()
                val action3bId = newId<ai.tock.bot.engine.action.Action>()
                val action3 =
                    SendSentence(
                        userId,
                        appId,
                        botPlayerId,
                        "answer3",
                        id = action3Id,
                        date = now.minusSeconds(200),
                        annotation =
                            BotAnnotation(
                                state = BotAnnotationState.RESOLVED,
                                reason = BotAnnotationReasonType.INACCURATE_ANSWER,
                                description = "Fixed",
                                groundTruth = null,
                                events = mutableListOf(),
                                creationDate = now.minusSeconds(200),
                                lastUpdateDate = now.minusSeconds(200),
                            ),
                    )
                val action3b =
                    SendSentence(
                        userId,
                        appId,
                        botPlayerId,
                        "answer3b",
                        id = action3bId,
                        date = now.minusSeconds(210),
                        annotation =
                            BotAnnotation(
                                state = BotAnnotationState.WONT_FIX,
                                reason = BotAnnotationReasonType.OTHER,
                                description = "Not actionable",
                                groundTruth = null,
                                events = mutableListOf(),
                                creationDate = now.minusSeconds(210),
                                lastUpdateDate = now.minusSeconds(210),
                            ),
                    )
                val dialog3 =
                    Dialog(
                        playerIds = setOf(userId, botPlayerId),
                        id = dialog3Id,
                        stories = mutableListOf(Story(storyDef, Intent.unknown, actions = mutableListOf(action3, action3b))),
                    )

                // Dialog 4: open annotation on OTHER bot/namespace (should NOT be returned)
                val dialog4Id = newId<Dialog>()
                val action4Id = newId<ai.tock.bot.engine.action.Action>()
                val action4 =
                    SendSentence(
                        userId,
                        otherAppId,
                        otherBotPlayerId,
                        "answer4",
                        id = action4Id,
                        date = now.minusSeconds(10),
                        annotation =
                            BotAnnotation(
                                state = BotAnnotationState.ANOMALY,
                                reason = BotAnnotationReasonType.QUESTION_MISUNDERSTOOD,
                                description = "Other bot",
                                groundTruth = null,
                                events = mutableListOf(),
                                creationDate = now.minusSeconds(10),
                                lastUpdateDate = now.minusSeconds(10),
                            ),
                    )
                val dialog4 =
                    Dialog(
                        playerIds = setOf(userId, otherBotPlayerId),
                        id = dialog4Id,
                        stories = mutableListOf(Story(storyDef, Intent.unknown, actions = mutableListOf(action4))),
                    )

                // Dialogs 1-3 belong to the tested bot: one timeline, one namespace, one app.
                // A DialogCol's namespace comes from the save() argument and its applicationIds from
                // the timeline's last action, so mixing bots/namespaces in a single save is invalid.
                UserTimelineMongoDAO.save(UserTimeline(userId, dialogs = mutableListOf(dialog1, dialog2, dialog3)), namespace)
                // Dialog 4 lives in a separate namespace/app via its own timeline, so it is isolated
                // both by namespace and by applicationIds — it must never surface in the bot's search.
                UserTimelineMongoDAO.save(UserTimeline(otherUserId, dialogs = mutableListOf(dialog4)), otherNamespace)
                delay(100)

                // Test: search ANOMALY + REVIEW_NEEDED only
                val allResult =
                    UserTimelineMongoDAO.searchAnnotations(
                        AnnotationReportQuery(
                            namespace = namespace,
                            nlpModel = botId,
                            annotationStates = setOf(BotAnnotationState.ANOMALY, BotAnnotationState.REVIEW_NEEDED),
                            annotationReasons = emptySet(),
                            start = 0,
                            size = 5,
                        ),
                    )
                assertEquals(3, allResult.total, "Expected 3 open annotations (not the RESOLVED one, not the other bot)")
                assertEquals(3, allResult.annotations.size)
                assertEquals(0, allResult.start)
                assertEquals(3, allResult.end)

                // Verify sorting: lastUpdateDate desc (action2b > action2a > action1)
                assertEquals(action2bId, allResult.annotations[0].actionId)
                assertEquals(action2aId, allResult.annotations[1].actionId)
                assertEquals(action1Id, allResult.annotations[2].actionId)

                // Verify fields populated and events empty
                val firstRow = allResult.annotations[0]
                assertEquals(dialog2Id, firstRow.dialogId)
                assertEquals(appId, firstRow.applicationId)
                assertEquals(now.minusSeconds(30), firstRow.actionDate)
                // action2b was seeded with a real COMMENT event: a non-zero count here would mean
                // toReport() failed to strip it (the stored annotation does carry one event).
                assertEquals(0, firstRow.annotation.events.size, "toReport() must strip the (non-empty) events")
                assertEquals(BotAnnotationState.REVIEW_NEEDED, firstRow.annotation.state)
                assertEquals(BotAnnotationReasonType.HALLUCINATION, firstRow.annotation.reason)

                // question: falls back to the answer text when there is no RAG debug (action2b),
                // and uses ragDebug.user_question when present (action1, a RAG answer).
                assertEquals("answer2b", firstRow.question, "Should fall back to the answer text")
                val ragRow = allResult.annotations.single { it.actionId == action1Id }
                assertEquals("What is AI?", ragRow.question, "Should use ragDebug.user_question")

                // Pagination, intermediate page: start=1 size=1 exercises $skip + $limit and the
                // stable sort — it must land on the middle row (action2a).
                val middlePage =
                    UserTimelineMongoDAO.searchAnnotations(
                        AnnotationReportQuery(
                            namespace = namespace,
                            nlpModel = botId,
                            annotationStates = setOf(BotAnnotationState.ANOMALY, BotAnnotationState.REVIEW_NEEDED),
                            annotationReasons = emptySet(),
                            start = 1,
                            size = 1,
                        ),
                    )
                assertEquals(3, middlePage.total)
                assertEquals(1, middlePage.annotations.size)
                assertEquals(action2aId, middlePage.annotations[0].actionId)
                assertEquals(1, middlePage.start)
                assertEquals(2, middlePage.end)

                // Pagination, page beyond total: no rows, but the total is still reported.
                val pageBeyondTotal =
                    UserTimelineMongoDAO.searchAnnotations(
                        AnnotationReportQuery(
                            namespace = namespace,
                            nlpModel = botId,
                            annotationStates = setOf(BotAnnotationState.ANOMALY, BotAnnotationState.REVIEW_NEEDED),
                            annotationReasons = emptySet(),
                            start = 10,
                            size = 5,
                        ),
                    )
                assertEquals(3, pageBeyondTotal.total)
                assertEquals(0, pageBeyondTotal.annotations.size)

                // Test reason filtering: only HALLUCINATION
                val reasonFilterResult =
                    UserTimelineMongoDAO.searchAnnotations(
                        AnnotationReportQuery(
                            namespace = namespace,
                            nlpModel = botId,
                            annotationStates = setOf(BotAnnotationState.ANOMALY, BotAnnotationState.REVIEW_NEEDED),
                            annotationReasons = setOf(BotAnnotationReasonType.HALLUCINATION),
                            start = 0,
                            size = 5,
                        ),
                    )
                assertEquals(1, reasonFilterResult.total)
                assertEquals(1, reasonFilterResult.annotations.size)
                assertEquals(action2bId, reasonFilterResult.annotations[0].actionId)

                // Closed-state filters: each closed state returns only its own annotation, proving the
                // search is not hard-wired to the open states.
                val resolvedResult =
                    UserTimelineMongoDAO.searchAnnotations(
                        AnnotationReportQuery(
                            namespace = namespace,
                            nlpModel = botId,
                            annotationStates = setOf(BotAnnotationState.RESOLVED),
                            annotationReasons = emptySet(),
                            start = 0,
                            size = 5,
                        ),
                    )
                assertEquals(1, resolvedResult.total)
                assertEquals(action3Id, resolvedResult.annotations[0].actionId)

                val wontFixResult =
                    UserTimelineMongoDAO.searchAnnotations(
                        AnnotationReportQuery(
                            namespace = namespace,
                            nlpModel = botId,
                            annotationStates = setOf(BotAnnotationState.WONT_FIX),
                            annotationReasons = emptySet(),
                            start = 0,
                            size = 5,
                        ),
                    )
                assertEquals(1, wontFixResult.total)
                assertEquals(action3bId, wontFixResult.annotations[0].actionId)
            } finally {
                BotApplicationConfigurationMongoDAO.delete(botConfig)
                BotApplicationConfigurationMongoDAO.delete(otherBotConfig)
            }
        }
}

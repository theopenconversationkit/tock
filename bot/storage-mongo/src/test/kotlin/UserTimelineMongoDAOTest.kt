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

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.dialog.DialogReportQuery
import ai.tock.bot.admin.user.UserReportQuery
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.SimpleStoryHandlerBase
import ai.tock.bot.definition.StoryDefinitionBase
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.action.ActionMetadata
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.EventState
import ai.tock.bot.engine.dialog.Story
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.bot.engine.user.UserTimeline
import ai.tock.shared.defaultNamespace
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.bson.Document
import org.bson.conversions.Bson
import org.junit.jupiter.api.Test
import org.litote.kmongo.newId
import java.time.Instant
import java.time.ZonedDateTime
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 *
 */
internal class UserTimelineMongoDAOTest : AbstractTest() {
    /**
     * Helper function to extract Document from Bson.
     * All DSL functions return Document instances wrapped as Bson, so direct cast is safe.
     */
    private fun org.bson.conversions.Bson.toDocument(): Document =
        when (this) {
            is Document -> this
            else -> throw IllegalArgumentException("Expected Document but got ${this::class.simpleName}")
        }

    /**
     * Converts a Document to a JSON string representation, handling Instant values safely.
     * This is needed because MongoDB's default codec doesn't support Instant serialization.
     */
    private fun Document.toJsonSafe(): String {
        // Convert Instant values to ISO strings for safe JSON serialization
        val converted = Document()
        this.forEach { (key, value) ->
            converted[key] =
                when (value) {
                    is java.time.Instant -> value.toString()
                    is Document -> value.toJsonSafe()
                    is List<*> ->
                        value.map {
                            when (it) {
                                is java.time.Instant -> it.toString()
                                is Document -> it.toJsonSafe()
                                else -> it
                            }
                        }
                    else -> value
                }
        }
        return converted.toJson()
    }

    /**
     * Verifies that a filter contains an $expr with the expected structure.
     */
    private fun assertFilterContainsExpr(
        filter: org.bson.conversions.Bson?,
        expectedOperator: String? = null,
    ) {
        assertNotNull(filter, "Filter should not be null")
        val doc = filter.toDocument()
        assertTrue(
            doc.containsKey("\$expr"),
            "Filter should contain \$expr. Filter: ${doc.toJsonSafe()}",
        )
        val expr = doc.get("\$expr")
        if (expectedOperator != null && expr is Document) {
            assertTrue(
                expr.containsKey(expectedOperator),
                "Filter \$expr should contain $expectedOperator. Expr: ${expr.toJsonSafe()}",
            )
        }
    }

    /**
     * Verifies that a filter contains an $expr with $and containing multiple conditions.
     */
    private fun assertFilterContainsExprWithAnd(filter: org.bson.conversions.Bson?) {
        assertNotNull(filter, "Filter should not be null")
        val doc = filter.toDocument()
        assertTrue(
            doc.containsKey("\$expr"),
            "Filter should contain \$expr. Filter: ${doc.toJsonSafe()}",
        )
        val expr = doc.get("\$expr")
        assertTrue(
            expr is Document && expr.containsKey("\$and"),
            "Filter \$expr should contain \$and for multiple conditions. Expr: ${expr?.let { (it as? Document)?.toJsonSafe() }}",
        )
    }

    /**
     * Verifies that a filter contains an $expr with a single condition (no $and).
     */
    private fun assertFilterContainsExprWithoutAnd(filter: org.bson.conversions.Bson?) {
        assertNotNull(filter, "Filter should not be null")
        val doc = filter.toDocument()
        assertTrue(
            doc.containsKey("\$expr"),
            "Filter should contain \$expr. Filter: ${doc.toJsonSafe()}",
        )
        val expr = doc.get("\$expr")
        assertTrue(
            expr is Document && !expr.containsKey("\$and"),
            "Filter \$expr should NOT contain \$and for single condition. Expr: ${expr?.let { (it as? Document)?.toJsonSafe() }}",
        )
    }

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
            UserTimelineMongoDAO.save(u, "namespace")
            println(UserTimelineMongoDAO.loadWithLastValidDialog("namespace", id, null) { error("no story provided") })

            val newId = PlayerId("id", PlayerType.user, "a")
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
                ): Dialog {
                    return Dialog(
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
                }

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

                fun createSingleActionDialog(actionDate: ZonedDateTime): Dialog {
                    return Dialog(
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
                }

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
}

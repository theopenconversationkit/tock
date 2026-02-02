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

package ai.tock.bot.admin.service

import ai.tock.bot.admin.model.DialogsSearchQuery
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Tests for DialogsSearchQuery mapping to DialogReportQuery.
 * Focus on date filtering parameters (dialogActivityFrom/To).
 */
class DialogSearchServiceTest {
    @Nested
    inner class DialogsSearchQueryMappingTest {
        @Test
        fun `toDialogReportQuery should map dialogActivityFrom correctly`() {
            // Given
            val activityFrom = ZonedDateTime.parse("2025-12-10T10:00:00Z")
            val query =
                createSearchQuery(
                    dialogActivityFrom = activityFrom,
                    dialogActivityTo = null,
                )

            // When
            val reportQuery = query.toDialogReportQuery()

            // Then
            assertNotNull(reportQuery.dialogActivityFrom)
            assertEquals(activityFrom, reportQuery.dialogActivityFrom)
            assertNull(reportQuery.dialogActivityTo)
        }

        @Test
        fun `toDialogReportQuery should map dialogActivityTo correctly`() {
            // Given
            val activityTo = ZonedDateTime.parse("2025-12-15T10:00:00Z")
            val query =
                createSearchQuery(
                    dialogActivityFrom = null,
                    dialogActivityTo = activityTo,
                )

            // When
            val reportQuery = query.toDialogReportQuery()

            // Then
            assertNull(reportQuery.dialogActivityFrom)
            assertNotNull(reportQuery.dialogActivityTo)
            assertEquals(activityTo, reportQuery.dialogActivityTo)
        }

        @Test
        fun `toDialogReportQuery should map both dialogActivity parameters correctly`() {
            // Given
            val activityFrom = ZonedDateTime.parse("2025-12-10T10:00:00Z")
            val activityTo = ZonedDateTime.parse("2025-12-15T10:00:00Z")
            val query =
                createSearchQuery(
                    dialogActivityFrom = activityFrom,
                    dialogActivityTo = activityTo,
                )

            // When
            val reportQuery = query.toDialogReportQuery()

            // Then
            assertEquals(activityFrom, reportQuery.dialogActivityFrom)
            assertEquals(activityTo, reportQuery.dialogActivityTo)
        }

        @Test
        fun `toDialogReportQuery should handle null date parameters correctly`() {
            // Given
            val query =
                createSearchQuery(
                    dialogActivityFrom = null,
                    dialogActivityTo = null,
                )

            // When
            val reportQuery = query.toDialogReportQuery()

            // Then
            assertNull(reportQuery.dialogActivityFrom)
            assertNull(reportQuery.dialogActivityTo)
        }

        @Test
        fun `toDialogReportQuery should preserve namespace and applicationName from parent query`() {
            // Given
            val query = createSearchQuery()

            // When
            val reportQuery = query.toDialogReportQuery()

            // Then
            // namespace and applicationName are inherited from PaginatedQuery with default empty values
            assertEquals("", reportQuery.namespace)
            assertEquals("", reportQuery.nlpModel)
        }
    }

    /**
     * Helper function to create a DialogsSearchQuery with optional date parameters.
     * Note: namespace and applicationName are inherited from PaginatedQuery with default empty values.
     * This is sufficient for testing date parameter mapping.
     */
    private fun createSearchQuery(
        dialogActivityFrom: ZonedDateTime? = null,
        dialogActivityTo: ZonedDateTime? = null,
    ): DialogsSearchQuery {
        return DialogsSearchQuery(
            playerId = null,
            text = null,
            dialogId = null,
            intentName = null,
            exactMatch = false,
            connectorType = null,
            displayTests = false,
            skipObfuscation = false,
            ratings = emptySet(),
            applicationId = null,
            intentsToHide = emptySet(),
            isGenAiRagDialog = null,
            withAnnotations = null,
            annotationStates = emptySet(),
            annotationReasons = emptySet(),
            annotationSort = null,
            dialogSort = null,
            annotationCreationDateFrom = null,
            annotationCreationDateTo = null,
            dialogActivityFrom = dialogActivityFrom,
            dialogActivityTo = dialogActivityTo,
        )
    }
}

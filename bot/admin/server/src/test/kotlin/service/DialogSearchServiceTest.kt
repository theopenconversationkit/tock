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
 * Focus on date filtering parameters (dialogCreationDateFrom/To, dialogActivityFrom/To).
 */
class DialogSearchServiceTest {
    @Nested
    inner class DialogsSearchQueryMappingTest {
        @Test
        fun `toDialogReportQuery should map dialogCreationDateFrom correctly`() {
            // Given
            val creationDateFrom = ZonedDateTime.parse("2025-12-10T10:00:00Z")
            val query =
                createSearchQuery(
                    dialogCreationDateFrom = creationDateFrom,
                    dialogCreationDateTo = null,
                )

            // When
            val reportQuery = query.toDialogReportQuery()

            // Then
            assertNotNull(reportQuery.dialogCreationDateFrom)
            assertEquals(creationDateFrom, reportQuery.dialogCreationDateFrom)
            assertNull(reportQuery.dialogCreationDateTo)
        }

        @Test
        fun `toDialogReportQuery should map dialogCreationDateTo correctly`() {
            // Given
            val creationDateTo = ZonedDateTime.parse("2025-12-15T10:00:00Z")
            val query =
                createSearchQuery(
                    dialogCreationDateFrom = null,
                    dialogCreationDateTo = creationDateTo,
                )

            // When
            val reportQuery = query.toDialogReportQuery()

            // Then
            assertNull(reportQuery.dialogCreationDateFrom)
            assertNotNull(reportQuery.dialogCreationDateTo)
            assertEquals(creationDateTo, reportQuery.dialogCreationDateTo)
        }

        @Test
        fun `toDialogReportQuery should map both dialogCreationDate parameters correctly`() {
            // Given
            val creationDateFrom = ZonedDateTime.parse("2025-12-10T10:00:00Z")
            val creationDateTo = ZonedDateTime.parse("2025-12-15T10:00:00Z")
            val query =
                createSearchQuery(
                    dialogCreationDateFrom = creationDateFrom,
                    dialogCreationDateTo = creationDateTo,
                )

            // When
            val reportQuery = query.toDialogReportQuery()

            // Then
            assertEquals(creationDateFrom, reportQuery.dialogCreationDateFrom)
            assertEquals(creationDateTo, reportQuery.dialogCreationDateTo)
        }

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
        fun `toDialogReportQuery should map all date parameters together correctly`() {
            // Given
            val creationDateFrom = ZonedDateTime.parse("2025-12-01T10:00:00Z")
            val creationDateTo = ZonedDateTime.parse("2025-12-05T10:00:00Z")
            val activityFrom = ZonedDateTime.parse("2025-12-10T10:00:00Z")
            val activityTo = ZonedDateTime.parse("2025-12-15T10:00:00Z")
            val query =
                createSearchQuery(
                    dialogCreationDateFrom = creationDateFrom,
                    dialogCreationDateTo = creationDateTo,
                    dialogActivityFrom = activityFrom,
                    dialogActivityTo = activityTo,
                )

            // When
            val reportQuery = query.toDialogReportQuery()

            // Then
            assertEquals(creationDateFrom, reportQuery.dialogCreationDateFrom)
            assertEquals(creationDateTo, reportQuery.dialogCreationDateTo)
            assertEquals(activityFrom, reportQuery.dialogActivityFrom)
            assertEquals(activityTo, reportQuery.dialogActivityTo)
        }

        @Test
        fun `toDialogReportQuery should handle null date parameters correctly`() {
            // Given
            val query =
                createSearchQuery(
                    dialogCreationDateFrom = null,
                    dialogCreationDateTo = null,
                    dialogActivityFrom = null,
                    dialogActivityTo = null,
                )

            // When
            val reportQuery = query.toDialogReportQuery()

            // Then
            assertNull(reportQuery.dialogCreationDateFrom)
            assertNull(reportQuery.dialogCreationDateTo)
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
        dialogCreationDateFrom: ZonedDateTime? = null,
        dialogCreationDateTo: ZonedDateTime? = null,
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
            dialogCreationDateFrom = dialogCreationDateFrom,
            dialogCreationDateTo = dialogCreationDateTo,
            dialogActivityFrom = dialogActivityFrom,
            dialogActivityTo = dialogActivityTo,
        )
    }
}

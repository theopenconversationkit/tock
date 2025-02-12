/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.bot.admin.model

import ai.tock.bot.admin.annotation.BotAnnotationReasonType
import ai.tock.bot.admin.annotation.BotAnnotationState
import ai.tock.bot.admin.dialog.DialogReportQuery
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.engine.dialog.SortDirection
import ai.tock.bot.engine.user.PlayerId
import ai.tock.nlp.admin.model.PaginatedQuery
import java.time.ZonedDateTime

/**
 *
 */
data class DialogsSearchQuery(
    val playerId: PlayerId?,
    val text: String?,
    val dialogId: String?,
    val intentName: String?,
    val exactMatch: Boolean,
    val connectorType: ConnectorType?,
    val displayTests: Boolean = false,
    val skipObfuscation: Boolean = false,
    val ratings: Set<Int> = emptySet(),
    val applicationId: String?,
    val intentsToHide: Set<String> = emptySet(),
    val isGenAiRagDialog: Boolean?,
    val withAnnotations: Boolean?,
    val annotationStates: Set<BotAnnotationState> = emptySet(),
    val annotationReasons: Set<BotAnnotationReasonType> = emptySet(),
    val annotationSort: SortDirection? = null,
    val dialogSort: SortDirection? = null,
    val annotationCreationDateFrom: ZonedDateTime? = null,
    val annotationCreationDateTo: ZonedDateTime? = null,
    val dialogCreationDateFrom: ZonedDateTime? = null,
    val dialogCreationDateTo: ZonedDateTime? = null,
) : PaginatedQuery() {

    fun toDialogReportQuery(): DialogReportQuery {
        return DialogReportQuery(
            namespace,
            applicationName,
            language,
            start,
            size,
            playerId,
            text,
            dialogId,
            intentName,
            exactMatch,
            connectorType = connectorType,
            displayTests = displayTests,
            obfuscated = true,
            ratings = ratings,
            applicationId = applicationId,
            intentsToHide = intentsToHide,
            isGenAiRagDialog = isGenAiRagDialog,
            withAnnotations = withAnnotations,
            annotationStates = annotationStates,
            annotationReasons = annotationReasons,
            annotationSort = annotationSort,
            dialogSort = dialogSort,
            annotationCreationDateFrom = annotationCreationDateFrom,
            annotationCreationDateTo = annotationCreationDateTo,
            dialogCreationDateFrom = dialogCreationDateFrom,
            dialogCreationDateTo = dialogCreationDateTo,
        )
    }
}

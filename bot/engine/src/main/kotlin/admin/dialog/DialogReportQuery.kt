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

package ai.tock.bot.admin.dialog

import ai.tock.bot.admin.annotation.BotAnnotationReasonType
import ai.tock.bot.admin.annotation.BotAnnotationState
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.engine.dialog.SortDirection
import ai.tock.bot.engine.user.PlayerId
import java.time.ZonedDateTime
import java.util.Locale

/**
 * Query for dialogs.
 */
data class DialogReportQuery(
    val namespace: String,
    val nlpModel: String,
    val language: Locale? = null,
    val start: Long = 0,
    val size: Int = 1,
    val playerId: PlayerId? = null,
    val text: String? = null,
    val dialogId: String? = null,
    val intentName: String? = null,
    val exactMatch: Boolean = false,
    val from: ZonedDateTime? = null,
    val to: ZonedDateTime? = null,
    val connectorType: ConnectorType? = null,
    /**
     * Display test dialogs.
     */
    val displayTests: Boolean = false,
    /**
     * Is the result is obfuscated ?.
     */
    val obfuscated: Boolean = false,

    /**
     * [ratings] list of number between 1 and 5 to filter dialog by rating
     */

    val ratings : Set<Int> = emptySet(),

    /**
     * [applicationId] configuration canal
     */
    val applicationId : String? = null,

    val intentsToHide : Set<String> =  emptySet(),

    val isGenAiRagDialog: Boolean? = null,

    val withAnnotations: Boolean? = null,
    val annotationStates: Set<BotAnnotationState> = emptySet(),
    val annotationReasons: Set<BotAnnotationReasonType> = emptySet(),
    val annotationSort: SortDirection? = null,
    val dialogSort: SortDirection? = null,
    val annotationCreationDateFrom: ZonedDateTime? = null,
    val annotationCreationDateTo: ZonedDateTime? = null,
    val dialogCreationDateFrom: ZonedDateTime? = null,
    val dialogCreationDateTo: ZonedDateTime? = null,
)

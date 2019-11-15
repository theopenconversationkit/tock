/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.bot.admin.model

import ai.tock.bot.admin.dialog.DialogReportQuery
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.engine.user.PlayerId
import ai.tock.nlp.admin.model.PaginatedQuery

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
    val displayTests:Boolean = false
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
            displayTests = displayTests
        )
    }
}
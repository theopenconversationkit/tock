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

package ai.tock.bot.api.model

import ai.tock.bot.admin.dialog.ActionReport
import ai.tock.bot.api.model.context.UserData
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.engine.user.PlayerId
import ai.tock.translator.UserInterfaceType
import java.util.Locale

data class RequestContext(
    val namespace: String,
    val language: Locale,
    val sourceConnectorType: ConnectorType,
    val targetConnectorType: ConnectorType,
    val userInterface: UserInterfaceType,
    val applicationId: String,
    val userId: PlayerId,
    val botId: PlayerId,
    val user: UserData,
    val metadata: Map<String, String> = emptyMap(),
    val actionsHistory: ActionsHistory? = null,
)

/**
 * The action history from the dialog [DialogReport] with [ActionReport]
 */
typealias ActionsHistory = List<ActionReport>

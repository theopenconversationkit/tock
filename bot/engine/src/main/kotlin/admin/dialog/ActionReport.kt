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

import ai.tock.bot.admin.annotation.BotAnnotation
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.ActionMetadata
import ai.tock.bot.engine.message.Message
import ai.tock.bot.engine.user.PlayerId
import ai.tock.translator.UserInterfaceType
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.time.Instant

/**
 *
 */
data class ActionReport(
    val playerId: PlayerId,
    val recipientId: PlayerId,
    val date: Instant,
    val message: Message,
    val connectorType: ConnectorType?,
    val userInterfaceType: UserInterfaceType,
    val test: Boolean = false,
    val id: Id<Action> = newId(),
    val intent : String?,
    val applicationId : String?,
    val metadata: ActionMetadata,
    val annotation: BotAnnotation? = null
)

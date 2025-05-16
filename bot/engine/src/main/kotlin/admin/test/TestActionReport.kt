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

package ai.tock.bot.admin.test

import ai.tock.bot.admin.dialog.ActionReport
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.message.Message
import ai.tock.bot.engine.user.PlayerId
import ai.tock.translator.UserInterfaceType
import ai.tock.translator.UserInterfaceType.textChat
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.time.Instant

/**
 *
 */
data class TestActionReport(
    val playerId: PlayerId,
    val date: Instant,
    val messages: List<Message>,
    val connectorType: ConnectorType?,
    val userInterfaceType: UserInterfaceType = textChat,
    val id: Id<Action>
) {

    constructor(
        playerId: PlayerId,
        date: Instant,
        message: Message,
        connectorType: ConnectorType?,
        userInterfaceType: UserInterfaceType,
        id: Id<Action> = newId()
    ) :
        this(
            playerId,
            date,
            listOf(message),
            connectorType,
            userInterfaceType,
            id
        )

    constructor(report: ActionReport) :
        this(
            report.playerId,
            report.date,
            report.message,
            report.connectorType,
            report.userInterfaceType,
            report.id
        )

    fun findFirstMessage(): Message {
        return messages.first()
    }
}

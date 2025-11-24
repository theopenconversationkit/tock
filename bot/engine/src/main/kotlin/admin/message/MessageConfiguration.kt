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

package ai.tock.bot.admin.message

import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.event.EventType
import ai.tock.bot.engine.user.PlayerId
import ai.tock.translator.UserInterfaceType
import java.util.Locale

/**
 *
 */
interface MessageConfiguration {
    val eventType: EventType
    val delay: Long

    fun toAction(bus: BotBus): Action =
        toAction(
            bus.userId,
            bus.connectorId,
            bus.botId,
            bus.userLocale,
            bus.userInterfaceType,
        )

    fun toAction(
        playerId: PlayerId,
        applicationId: String,
        recipientId: PlayerId,
        locale: Locale,
        userInterfaceType: UserInterfaceType,
    ): Action
}

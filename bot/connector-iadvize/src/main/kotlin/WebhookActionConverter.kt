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

package ai.tock.bot.connector.iadvize

import ai.tock.bot.connector.iadvize.model.request.IadvizeRequest
import ai.tock.bot.connector.iadvize.model.request.MessageRequest
import ai.tock.bot.connector.iadvize.model.response.conversation.payload.TextPayload
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.action.SendLocation
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.event.EndConversationEvent
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.event.LoginEvent
import ai.tock.bot.engine.event.LogoutEvent
import ai.tock.bot.engine.event.MediaStatusEvent
import ai.tock.bot.engine.event.NewDeviceEvent
import ai.tock.bot.engine.event.NoInputEvent
import ai.tock.bot.engine.event.StartConversationEvent
import ai.tock.bot.engine.stt.SttService
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.bot.engine.user.UserLocation

/**
 *
 */
internal object WebhookActionConverter {

    fun toEvent(
        request: MessageRequest,
        applicationId: String
    ): Event {
        //val eventState = message.getEventState()
        //val userInterface = eventState.userInterface

        val userId = request.idConversation

        val playerId = PlayerId(userId, PlayerType.user)
        return SendSentence(
                    playerId,
                    applicationId,
                    playerId,
                    request.message.payload.value
                )
    }
}

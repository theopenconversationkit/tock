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

package ai.tock.bot.connector.ga

import ai.tock.bot.connector.ga.GAAccountLinking.Companion.getUserId
import ai.tock.bot.connector.ga.model.GAIntent
import ai.tock.bot.connector.ga.model.request.GAArgumentBuiltInName
import ai.tock.bot.connector.ga.model.request.GAInputType.URL
import ai.tock.bot.connector.ga.model.request.GAInputType.VOICE
import ai.tock.bot.connector.ga.model.request.GANewSurfaceValue
import ai.tock.bot.connector.ga.model.request.GARequest
import ai.tock.bot.connector.ga.model.request.GASignInStatus
import ai.tock.bot.connector.ga.model.request.GASignInValue
import ai.tock.bot.connector.ga.model.response.GAMediaStatusValue
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
        message: GARequest,
        applicationId: String
    ): Event {
        val eventState = message.getEventState()
        val userInterface = eventState.userInterface

        val userId = getUserId(message)

        val playerId = PlayerId(userId, PlayerType.user)
        val botId = PlayerId(applicationId, PlayerType.bot)

        val input = message.inputs.firstOrNull()
        if (input != null) {
            if (input.arguments?.all { it.builtInArg == GAArgumentBuiltInName.PERMISSION && message.device?.location?.coordinates?.latitude != null } == true) {
                return with(message.device!!.location!!.coordinates!!) {
                    SendLocation(
                        playerId,
                        applicationId,
                        botId,
                        UserLocation(
                            latitude,
                            longitude
                        )
                    )
                }
            } else if (input.arguments?.any { it.builtInArg == GAArgumentBuiltInName.OPTION } == true) {
                val params = SendChoice.decodeChoiceId(
                    input.arguments.first { it.builtInArg == GAArgumentBuiltInName.OPTION }.textValue
                        ?: error("no text value")
                )
                // Google assistant makes an somewhat erroneous nlp selection sometimes
                // to avoid that, double check the label
                if (input.rawInputs.firstOrNull()?.query?.let { query -> params.second[SendChoice.TITLE_PARAMETER] == query } != false) {
                    return SendChoice(
                        playerId,
                        applicationId,
                        botId,
                        params.first,
                        params.second,
                        state = eventState
                    )
                }
            } else if (input.rawInputs.any { it.inputType == URL }) {
                return SendChoice(
                    playerId,
                    applicationId,
                    botId,
                    input.intent.substringAfter("tock."),
                    parameters = input.arguments?.filter { it.textValue != null }?.map {
                        it.name to it.textValue!!
                    }?.toMap().orEmpty(),
                    state = eventState
                )
            }

            fun Event.setEventState(): Event {
                state.userInterface = userInterface
                return this
            }

            return when (input.builtInIntent) {
                GAIntent.main -> StartConversationEvent(playerId, botId, applicationId).setEventState()
                GAIntent.cancel -> EndConversationEvent(playerId, botId, applicationId).setEventState()
                GAIntent.noInput -> NoInputEvent(playerId, botId, applicationId).setEventState()
                GAIntent.newSurface -> NewDeviceEvent(
                    playerId,
                    botId,
                    applicationId,
                    (input.arguments?.first { it.builtInArg == GAArgumentBuiltInName.NEW_SURFACE }?.extension as GANewSurfaceValue).status.toString()
                ).setEventState()
                GAIntent.mediaStatus -> MediaStatusEvent(
                    playerId,
                    botId,
                    applicationId,
                    (input.arguments?.first { it.builtInArg == GAArgumentBuiltInName.MEDIA_STATUS }?.extension as GAMediaStatusValue).status.toString()
                ).setEventState()
                GAIntent.signIn -> {
                    when ((input.arguments?.first { it.builtInArg == GAArgumentBuiltInName.SIGN_IN }?.extension as GASignInValue).status) {
                        GASignInStatus.OK -> LoginEvent(
                            playerId,
                            botId,
                            message.user.accessToken ?: "",
                            applicationId,
                            previousUserId = PlayerId(message.conversation.conversationId, PlayerType.user)
                        )
                        else -> LogoutEvent(playerId, botId, applicationId)
                    }
                }
                else -> {
                    val rawInput = input.rawInputs.firstOrNull()
                    var text = rawInput?.query
                    if (rawInput?.inputType == VOICE && text != null) {
                        text = SttService.transform(text, message.user.findLocale())
                    }

                    SendSentence(
                        playerId,
                        applicationId,
                        botId,
                        text,
                        mutableListOf(GARequestConnectorMessage(message)),
                        state = eventState
                    )
                }
            }
        }
        error("unsupported message: $message")
    }
}

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

package fr.vsct.tock.bot.connector.ga

import fr.vsct.tock.bot.connector.ga.model.GAIntent
import fr.vsct.tock.bot.connector.ga.model.request.GAArgumentBuiltInName
import fr.vsct.tock.bot.connector.ga.model.request.GAInputType.VOICE
import fr.vsct.tock.bot.connector.ga.model.request.GARequest
import fr.vsct.tock.bot.connector.ga.model.request.GASignInStatus
import fr.vsct.tock.bot.connector.ga.model.request.GASignInValue
import fr.vsct.tock.bot.engine.action.SendChoice
import fr.vsct.tock.bot.engine.action.SendLocation
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.event.EndConversationEvent
import fr.vsct.tock.bot.engine.event.Event
import fr.vsct.tock.bot.engine.event.LoginEvent
import fr.vsct.tock.bot.engine.event.LogoutEvent
import fr.vsct.tock.bot.engine.event.NoInputEvent
import fr.vsct.tock.bot.engine.event.StartConversationEvent
import fr.vsct.tock.bot.engine.stt.SttService
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType
import fr.vsct.tock.bot.engine.user.UserLocation
import mu.KotlinLogging

/**
 *
 */
internal object WebhookActionConverter {

    private val logger = KotlinLogging.logger {}

    fun toEvent(
        message: GARequest,
        applicationId: String
    ): Event {
        val playerId = PlayerId(message.user.userId, PlayerType.user)
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
            } else if (input.arguments?.all { it.builtInArg == GAArgumentBuiltInName.OPTION } == true) {
                val params = SendChoice.decodeChoiceId(
                    input.arguments.first { it.builtInArg == GAArgumentBuiltInName.OPTION }.textValue
                            ?: error("no text value")
                )
                //Google assistant makes an somewhat erroneous nlp selection sometimes
                //to avoid that, double check the label
                if (input.rawInputs.firstOrNull()?.query?.let { query -> params.second[SendChoice.TITLE_PARAMETER] == query } != false) {
                    return SendChoice(
                        playerId,
                        applicationId,
                        botId,
                        params.first,
                        params.second,
                        state = message.getEventState()
                    )
                }
            }

            fun Event.setEventState(): Event {
                state.userInterface = message.getEventState().userInterface
                return this
            }

            return when (input.builtInIntent) {
                GAIntent.main -> StartConversationEvent(playerId, botId, applicationId).setEventState()
                GAIntent.cancel -> EndConversationEvent(playerId, botId, applicationId).setEventState()
                GAIntent.noInput -> NoInputEvent(playerId, botId, applicationId).setEventState()
                GAIntent.signIn -> {
                    when ((input.arguments?.first { it.builtInArg == GAArgumentBuiltInName.SIGN_IN }?.extension as GASignInValue).status) {
                        GASignInStatus.OK -> LoginEvent(playerId, botId, message.user.accessToken ?: "", applicationId)
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
                        state = message.getEventState()
                    )
                }
            }
        }
        error("unsupported message: $message")
    }

}
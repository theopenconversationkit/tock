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
import fr.vsct.tock.bot.connector.ga.model.request.GARequest
import fr.vsct.tock.bot.engine.ConnectorController
import fr.vsct.tock.bot.engine.action.SendChoice
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.event.Event
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType
import mu.KotlinLogging

/**
 *
 */
internal object WebhookActionConverter {

    private val logger = KotlinLogging.logger {}

    fun toEvent(controller: ConnectorController, message: GARequest, applicationId: String): Event {
        val playerId = PlayerId(message.user.userId, PlayerType.user)
        val botId = PlayerId(applicationId, PlayerType.bot)

        val input = message.inputs.firstOrNull()
        if (input != null) {
            if (input.arguments?.all { it.builtInArg == GAArgumentBuiltInName.OPTION } == true) {
                val params = SendChoice.decodeChoiceId(
                        input.arguments.first { it.builtInArg == GAArgumentBuiltInName.OPTION }.textValue ?: error("no text value")
                )
                return SendChoice(
                        playerId,
                        applicationId,
                        botId,
                        params.first,
                        params.second,
                        state = message.getEventState()
                )
            } else {
                if (input.builtInIntent == GAIntent.main && controller.helloIntent() != null) {
                    return SendChoice(
                            playerId,
                            applicationId,
                            botId,
                            controller.helloIntent()!!.name,
                            state = message.getEventState()
                    )
                } else {
                    val rawInput = input.rawInputs.firstOrNull()
                    if (rawInput != null) {
                        val text = rawInput.query

                        return SendSentence(
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
        }
        error("unsupported message: $message")
    }
}
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

package fr.vsct.tock.bot.connector.rocketchat

import chat.rocket.common.model.RoomType
import chat.rocket.common.model.roomTypeOf
import fr.vsct.tock.bot.connector.ConnectorBase
import fr.vsct.tock.bot.connector.ConnectorCallback
import fr.vsct.tock.bot.engine.BotRepository
import fr.vsct.tock.bot.engine.ConnectorController
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.event.Event
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType
import fr.vsct.tock.shared.error
import mu.KotlinLogging

/**
 *
 */
internal class RocketChatConnector(
    private val applicationId: String,
    private val client: RocketChatClient
) : ConnectorBase(rocketChatConnectorType) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun register(controller: ConnectorController) {
        client.join { room ->
            logger.debug { "listening room event: $room" }
            if (room.type.toString() != RoomType.LIVECHAT) {
                logger.debug { "Do not reply to messages in non-livechat rooms" }
            } else if (room.lastMessage?.sender == null) {
                logger.warn { "no message for $room - skip" }
            } else if (room.lastMessage!!.sender!!.username == client.login) {
                logger.debug { "do not reply to bot messages $room because client login is the same than sender: ${client.login}" }
            } else {
                val requestTimerData = BotRepository.requestTimer.start("rocketchat_webhook")
                logger.debug { "patate : $room.id -- ${room.lastMessage!!.roomId!!}" }
                try {
                    controller.handle(
                        SendSentence(
                            PlayerId(room.lastMessage!!.sender!!.id!!),
                            applicationId,
                            PlayerId("bot:"+room.id, PlayerType.bot),
                            room.lastMessage!!.message
                        )
                    )
                } catch (e: Throwable) {
                    logger.error(e)
                } finally {
                    try {
                        BotRepository.requestTimer.end(requestTimerData)
                    } catch (e: Throwable) {
                        logger.error(e)
                    }
                }
            }
        }
    }

    override fun send(event: Event, callback: ConnectorCallback, delayInMs: Long) {
        if (event is SendSentence && event.text != null) {
            val thisRoomId = event.playerId.id.substring(4)
            client.send(thisRoomId, event.stringText!!)
        }
    }
}
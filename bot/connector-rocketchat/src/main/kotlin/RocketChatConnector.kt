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

package ai.tock.bot.connector.rocketchat

import ai.tock.bot.connector.ConnectorBase
import ai.tock.bot.connector.ConnectorCallback
import ai.tock.bot.connector.ConnectorData
import ai.tock.bot.engine.BotRepository
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.shared.error
import chat.rocket.common.model.RoomType
import mu.KotlinLogging
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CopyOnWriteArraySet

/**
 *
 */
internal class RocketChatConnector(
    private val applicationId: String,
    private val client: RocketChatClient,
    private val roomId: String? = null,
) : ConnectorBase(rocketChatConnectorType) {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val registeredClientUrls = CopyOnWriteArraySet<String>()
    }

    private val lastMessages = ArrayBlockingQueue<String>(10, true, (0..9).map { "" })

    override fun register(controller: ConnectorController) {
        if (registeredClientUrls.contains(client.targetUrl)) {
            logger.warn { "client url already registered - skip: ${client.targetUrl}" }
        } else {
            registeredClientUrls.add(client.targetUrl)
            client.join(roomId) { room ->
                logger.debug { "listening room event: $room" }
                val message = room.lastMessage
                if (room.type.toString() != RoomType.LIVECHAT && (roomId == null || room.id != roomId)) {
                    logger.debug { "Do not reply to messages in non-livechat rooms or dedicated room" }
                } else if (message?.sender == null) {
                    logger.warn { "no message for $room - skip" }
                } else if (message.sender!!.username == client.login) {
                    logger.debug { "do not reply to bot messages $room because client login is the same than sender: ${client.login}" }
                } else if (lastMessages.contains(message.id)) {
                    // sometimes the same message comes twice
                    logger.debug { "message $message already seen - skip" }
                } else {
                    // register last messages
                    lastMessages.poll()
                    lastMessages.offer(message.id)

                    val requestTimerData = BotRepository.requestTimer.start("rocketchat_webhook")
                    logger.debug { "message handled : $message" }
                    try {
                        controller.handle(
                            SendSentence(
                                PlayerId(message.sender!!.id!!),
                                applicationId,
                                PlayerId(applicationId, PlayerType.bot),
                                message.message,
                            ),
                            ConnectorData(RocketChatConnectorCallback(applicationId, room.id)),
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
    }

    override fun unregister(controller: ConnectorController) {
        super.unregister(controller)
        client.unregister()
        registeredClientUrls.remove(client.targetUrl)
    }

    override fun send(
        event: Event,
        callback: ConnectorCallback,
        delayInMs: Long,
    ) {
        if (event is SendSentence && event.text != null) {
            val roomId = (callback as RocketChatConnectorCallback).roomId
            client.send(roomId, event.stringText!!)
        }
    }
}

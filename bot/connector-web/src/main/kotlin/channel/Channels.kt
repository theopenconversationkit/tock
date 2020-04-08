/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
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
package ai.tock.bot.connector.web.channel

import ai.tock.bot.connector.web.WebConnectorResponse
import ai.tock.bot.connector.web.WebMessage
import ai.tock.bot.connector.web.webConnectorType
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendSentence
import java.util.UUID

internal class Channels(private val channelDAO: ChannelDAO) {

    private var channelsByUser = mutableMapOf<String, MutableList<Channel>>()

    init {
        channelDAO.listenChanges { channelEvent ->
            channelsByUser[channelEvent.recipientId]?.forEach { channel ->
                channel.onAction.invoke(channelEvent.webConnectorResponse)
            }
        }
    }

    fun register(userId: String, onAction: ChannelCallback): Channel {
        if (channelsByUser[userId] == null) {
            channelsByUser.set(userId, mutableListOf())
        }
        val channel = Channel(UUID.randomUUID(), userId, onAction)
        channelsByUser[userId]?.add(channel)
        return channel
    }

    fun unregister(channel: Channel) {
        channelsByUser[channel.userId]?.removeIf { _channel ->
            _channel.uuid == channel.uuid
        }
    }

    fun send(action: Action) {
        val messages = listOf(action)
            .filterIsInstance<SendSentence>()
            .mapNotNull {
                if (it.stringText != null) {
                    WebMessage(it.stringText!!)
                } else it.message(webConnectorType)?.let {
                    it as? WebMessage
                }
            }
        channelDAO.save(ChannelEvent(action.recipientId.id, WebConnectorResponse(messages)))
    }

}
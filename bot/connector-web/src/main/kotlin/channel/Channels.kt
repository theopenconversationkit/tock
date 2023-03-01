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
package ai.tock.bot.connector.web.channel

import ai.tock.bot.connector.web.WebConnectorResponse
import ai.tock.bot.connector.web.WebMessageProcessor
import ai.tock.bot.engine.action.Action
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

internal class Channels(private val channelDAO: ChannelDAO, private val messageProcessor: WebMessageProcessor) {

    private val channelsByUser = ConcurrentHashMap<String, CopyOnWriteArrayList<Channel>>()

    init {
        channelDAO.listenChanges { channelEvent ->
            channelsByUser[channelEvent.recipientId]?.forEach { channel ->
                if (channel.appId == channelEvent.appId) {
                    channel.onAction.invoke(channelEvent.webConnectorResponse)
                }
            }
        }
    }

    fun register(appId: String, userId: String, onAction: ChannelCallback): Channel {
        val channels = channelsByUser.getOrPut(userId) {
            CopyOnWriteArrayList()
        }
        val channel = Channel(appId, UUID.randomUUID(), userId, onAction)
        channels.add(channel)
        return channel
    }

    fun unregister(channel: Channel) {
        channelsByUser[channel.userId]?.removeIf { _channel ->
            _channel.uuid == channel.uuid
        }
    }

    fun send(action: Action) {
        val messages = listOfNotNull(messageProcessor.process(action))
        channelDAO.save(ChannelEvent(action.applicationId, action.recipientId.id, WebConnectorResponse(messages)))
    }
}

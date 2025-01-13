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
import ai.tock.bot.engine.user.PlayerId
import ai.tock.shared.injector
import ai.tock.shared.provide
import io.vertx.core.Future
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

internal class Channels {

    private val channelDAO: ChannelDAO = injector.provide()
    private val channelsByUser = ConcurrentHashMap<String, CopyOnWriteArrayList<Channel>>()

    init {
        channelDAO.listenChanges { (appId, recipientId, response) ->
            Future.all((channelsByUser[recipientId] ?: emptyList()).filter { it.appId == appId }.map { channel ->
                channel.onAction(response)
            }).map { it.list<Unit?>().isNotEmpty() }
        }
    }

    fun register(appId: String, userId: String, onAction: ChannelCallback): Channel {
        val channels = channelsByUser.getOrPut(userId) {
            CopyOnWriteArrayList()
        }
        val channel = Channel(appId, UUID.randomUUID(), userId, onAction)
        channels.add(channel)
        channelDAO.handleMissedEvents(appId, userId) { (_, _, response) ->
            channel.onAction(response).map { true }
        }
        return channel
    }

    fun unregister(channel: Channel) {
        channelsByUser[channel.userId]?.removeIf {
            it.uuid == channel.uuid
        }
    }

    fun send(applicationId: String, recipientId: PlayerId, response: WebConnectorResponse) {
        channelDAO.save(ChannelEvent(applicationId, recipientId.id, response))
    }
}

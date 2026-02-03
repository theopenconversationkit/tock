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
package ai.tock.bot.connector.web.sse.channel

import ai.tock.bot.connector.web.WebConnectorResponseContract
import io.vertx.core.CompositeFuture
import io.vertx.core.Future
import mu.KotlinLogging
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

internal class SseChannels(private val channelDAO: ChannelDAO) {
    private val initialized = AtomicBoolean(false)
    private val channelsByUser = ConcurrentHashMap<String, CopyOnWriteArrayList<SseChannel>>()

    fun initListeners() {
        if (!initialized.getAndSet(true)) {
            channelDAO.listenChanges { (appId, recipientId, response) ->
                process(appId, recipientId, response)
            }
        }
    }

    private fun process(
        appId: String,
        recipientId: String,
        response: WebConnectorResponseContract,
    ): Future<Boolean> =
        Future.all<CompositeFuture>(
            (channelsByUser[recipientId] ?: emptyList()).filter { it.appId == appId }.map { channel ->
                logger.debug { "call onAction for $channel" }
                channel.onAction(response)
            },
        ).map { futures -> futures.size() > 0 }

    fun register(
        appId: String,
        userId: String,
        onAction: ChannelCallback,
    ): SseChannel {
        val channels =
            channelsByUser.getOrPut(userId) {
                CopyOnWriteArrayList()
            }
        return SseChannel(appId, UUID.randomUUID(), userId, onAction).also(channels::add)
    }

    fun sendMissedEvents(channel: SseChannel) {
        channelDAO.handleMissedEvents(channel.appId, channel.userId) { (_, _, response) ->
            channel.onAction(response).map { true }
        }
    }

    fun unregister(channel: SseChannel) {
        channelsByUser[channel.userId]?.removeIf {
            it.uuid == channel.uuid
        }
    }

    fun send(
        applicationId: String,
        recipientId: String,
        response: WebConnectorResponseContract,
    ): Future<Unit> {
        // First, attempt to send the response directly on this local instance
        return process(applicationId, recipientId, response).transform {
            // If we have to send it later or through another backend instance, go through database
            if (!(it.succeeded() && it.result())) {
                channelDAO.save(ChannelEvent(applicationId, recipientId, response))
            }
            Future.succeededFuture()
        }
    }
}

private val logger = KotlinLogging.logger {}

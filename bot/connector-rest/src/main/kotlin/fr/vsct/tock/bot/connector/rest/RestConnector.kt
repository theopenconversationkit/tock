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

package fr.vsct.tock.bot.connector.rest

import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.cache.RemovalNotification
import fr.vsct.tock.bot.connector.Connector
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.connector.rest.model.MessageRequest
import fr.vsct.tock.bot.connector.rest.model.MessageResponse
import fr.vsct.tock.bot.engine.ConnectorController
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.jackson.mapper
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit

/**
 *
 */
class RestConnector(val applicationId: String, val path: String) : Connector {

    private data class Response(val context: RoutingContext, val actions: MutableList<Action> = CopyOnWriteArrayList())

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override val connectorType: ConnectorType = restConnectorType

    private val currentMessages: Cache<String, Response> = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .removalListener { e: RemovalNotification<String, Response> ->
                if (e.wasEvicted()) {
                    logger.error { "request not handled for user ${e.key} : ${e.value.actions}" }
                }
            }
            .build()

    override fun register(controller: ConnectorController) {
        controller.registerServices(path, { router ->
            router.post(path).blockingHandler(
                    { context ->
                        try {
                            val message: MessageRequest = mapper.readValue(context.bodyAsString)
                            try {
                                currentMessages.put(message.userId, Response(context))
                                controller.handle(message.message.toAction(
                                        PlayerId(message.userId, PlayerType.user),
                                        applicationId,
                                        PlayerId(message.recipientId, PlayerType.bot)
                                ))
                            } catch(t: Throwable) {
                                try {
                                    logger.error(t)
                                    send(controller.errorMessage(
                                            PlayerId(message.userId, PlayerType.user),
                                            applicationId,
                                            PlayerId(message.recipientId, PlayerType.bot)))
                                } catch(t: Throwable) {
                                    logger.error(t)
                                }
                            } finally {
                                sendAnswer(message.userId)
                            }
                        } catch(t: Throwable) {
                            logger.error(t)
                        }
                    },
                    false)
        })
    }

    private fun sendAnswer(userId: String) {
        val response = currentMessages.getIfPresent(userId)
        if (response == null) {
            logger.error { "no message registered for $userId" }
        } else {
            val r = mapper.writeValueAsString(MessageResponse(
                    response.actions.map { it.toMessage() }
            ))
            logger.debug { "response : $r" }
            currentMessages.invalidate(userId)
            response.context.response().end(r)
        }
    }

    override fun send(action: Action) {
        val response = currentMessages.getIfPresent(action.recipientId.id)
        if (response == null) {
            logger.error { "no message registered for $action" }
        } else {
            response.actions.add(action)
        }
    }

}
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

import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.cache.RemovalNotification
import fr.vsct.tock.bot.connector.ConnectorBase
import fr.vsct.tock.bot.connector.ga.model.request.GARequest
import fr.vsct.tock.bot.connector.ga.model.response.GAExpectedInput
import fr.vsct.tock.bot.connector.ga.model.response.GAInputPrompt
import fr.vsct.tock.bot.connector.ga.model.response.GAItem
import fr.vsct.tock.bot.connector.ga.model.response.GAResponse
import fr.vsct.tock.bot.connector.ga.model.response.GARichResponse
import fr.vsct.tock.bot.connector.ga.model.response.GASimpleResponse
import fr.vsct.tock.bot.engine.ConnectorController
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.event.Event
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType
import fr.vsct.tock.bot.engine.user.UserPreferences
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.jackson.mapper
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit

/**
 *
 */
class GAConnector internal constructor(
        val applicationId: String,
        val path: String) : ConnectorBase(GAConnectorProvider.connectorType) {

    private data class ActionWithDelay(val action: Action, val delayInMs: Long = 0)

    private data class RoutingContextHolder(
            val context: RoutingContext,
            val request: GARequest,
            val actions: MutableList<ActionWithDelay> = CopyOnWriteArrayList()
    )

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val currentMessages: Cache<String, RoutingContextHolder> = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .removalListener { e: RemovalNotification<String, RoutingContextHolder> ->
                if (e.wasEvicted()) {
                    logger.error { "request not handled for user ${e.key} : ${e.value.actions}" }
                }
            }
            .build()

    override fun register(controller: ConnectorController) {
        controller.registerServices(path, { router ->
            logger.info("deploy rest google assistant services for root path $path ")

            router.post(path).blockingHandler({ context ->
                try {
                    val body = context.bodyAsString
                    handleRequest(controller, context, body)

                } catch (e: Throwable) {
                    logger.error(e)
                }
            }, false)
        })
    }

    //internal for tests
    internal fun handleRequest(controller: ConnectorController,
                               context: RoutingContext,
                               body: String) {
        logger.debug { "Google Assistant request input : $body" }
        val request: GARequest = mapper.readValue(body)
        val event = WebhookActionConverter.toEvent(controller, request, applicationId)
        val userId = request.user.userId
        try {
            currentMessages.put(userId, RoutingContextHolder(context, request))
            controller.handle(event)
        } catch (t: Throwable) {
            try {
                logger.error(t)
                send(controller.errorMessage(
                        PlayerId(userId, PlayerType.user),
                        applicationId,
                        PlayerId(applicationId, PlayerType.bot)))
            } catch (t: Throwable) {
                logger.error(t)
            }
        } finally {
            sendAnswer(userId)
        }
    }

    private fun sendAnswer(userId: String) {
        val response = currentMessages.getIfPresent(userId)
        if (response == null) {
            logger.error { "no message registered for $userId" }
        } else {
            currentMessages.invalidate(userId)
            sendResponse(response)
        }
    }

    override fun send(event: Event) {
        send(event, 0)
    }

    override fun send(event: Event, delayInMs: Long) {
        if (event is Action) {
            val response = currentMessages.getIfPresent(event.recipientId.id)
            if (response == null) {
                logger.error { "no message registered for $event" }
            } else {
                response.actions.add(ActionWithDelay(event, delayInMs))
            }
        } else {
            logger.trace { "unsupported event: $event" }
        }
    }

    private fun sendResponse(routingContext: RoutingContextHolder) {
        try {
            with(routingContext) {

                val text =
                        actions
                                .filter { it.action is SendSentence && it.action.text != null }
                                .mapIndexed { i, a ->
                                    val s = a.action as SendSentence
                                    if (i == 0) {
                                        s.text
                                    } else {
                                        (if (a.delayInMs != 0L) {
                                            "<break time=\"${a.delayInMs}ms\"/>"
                                        } else {
                                            ""
                                        }) + s.text
                                    }
                                }
                                .joinToString("", "<speak>", "</speak>")
                val simpleResponse = if (text != "<speak></speak>") {
                    GAItem(
                            GASimpleResponse(ssml = text)
                    )
                } else {
                    null
                }

                val message = actions.firstOrNull { it.action is SendSentence && it.action.hasMessage(gaConnectorType) }
                val expectedInput = if (message == null) {
                    if (simpleResponse == null) {
                        logger.warn { "no simple response for $routingContext" }
                        null
                    } else {
                        GAExpectedInput(
                                GAInputPrompt(
                                        GARichResponse(
                                                listOf(simpleResponse))))
                    }
                } else {
                    val m = (message.action as SendSentence).message(gaConnectorType) as GAResponseConnectorMessage
                    if (simpleResponse == null) {
                        m.expectedInput
                    } else {
                        m.expectedInput.copy(
                                inputPrompt = m.expectedInput.inputPrompt.copy(
                                        richInitialPrompt = m.expectedInput.inputPrompt.richInitialPrompt.copy(
                                                items = listOf(simpleResponse) + m.expectedInput.inputPrompt.richInitialPrompt.items
                                        )
                                )
                        )
                    }
                }

                context.response().putHeader("Google-Actions-API-Version", "2")

                val gaResponse = GAResponse(
                        request.conversation.conversationToken ?: "",
                        true,
                        listOfNotNull(expectedInput),
                        null,
                        null,
                        null, //GAResponseMetadata(GAStatus(0, "OK")),
                        request.isInSandbox
                )

                logger.debug { "ga response : $gaResponse" }

                val writeValueAsString = mapper.writeValueAsString(gaResponse)

                logger.debug { writeValueAsString }
                context.response().end(writeValueAsString)
            }
        } catch (e: Exception) {
            logger.error(e)
        }
    }

    override fun loadProfile(applicationId: String, userId: PlayerId): UserPreferences? {
        return currentMessages.getIfPresent(userId.id)
                ?.request?.user?.profile?.run {
            if (givenName != null) {
                UserPreferences(givenName, familyName)
            } else {
                null
            }
        }
    }
}
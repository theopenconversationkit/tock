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
package ai.tock.bot.connector.alcmeon

import ai.tock.bot.connector.ConnectorBase
import ai.tock.bot.connector.ConnectorCallback
import ai.tock.bot.connector.ConnectorData
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.media.MediaMessage
import ai.tock.bot.connector.messenger.MessengerMediaConverter
import ai.tock.bot.connector.messenger.messengerConnectorType
import ai.tock.bot.connector.messenger.text
import ai.tock.bot.connector.whatsapp.UserHashedIdCache
import ai.tock.bot.connector.whatsapp.WhatsAppMediaConverter
import ai.tock.bot.connector.whatsapp.listMessage
import ai.tock.bot.connector.whatsapp.replyButtonMessage
import ai.tock.bot.connector.whatsapp.whatsAppConnectorType
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.shared.Executor
import ai.tock.shared.http.HttpResponse
import ai.tock.shared.http.HttpRoutes
import ai.tock.shared.injector
import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.salomonbrys.kodein.instance
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.CompletableDeferred
import mu.KotlinLogging
import ai.tock.bot.connector.messenger.nlpQuickReply as messengerNlpQuickReply
import ai.tock.bot.connector.whatsapp.nlpQuickReply as whatsappNlpQuickReply

class AlcmeonConnector private constructor(
    internal val connectorId: String,
    private val path: String,
    private val description: String,
    private val authorisationHandler: Handler<RoutingContext>?,
    private val signatureVerifier: AlcmeonSignatureVerifier?,
) : ConnectorBase(AlcmeonConnectorProvider.connectorType) {
    /** The connector with a Vert.x handler of the signature: its routes are Vert.x ones. */
    constructor(
        connectorId: String,
        path: String,
        description: String,
        authorisationHandler: Handler<RoutingContext>,
    ) : this(connectorId, path, description, authorisationHandler, null)

    /** The connector with server independent routes. */
    constructor(
        connectorId: String,
        path: String,
        description: String,
        signatureVerifier: AlcmeonSignatureVerifier,
    ) : this(connectorId, path, description, null, signatureVerifier)

    private val logger = KotlinLogging.logger {}
    private val executor: Executor by injector.instance()

    override fun register(controller: ConnectorController) {
        if (authorisationHandler != null) {
            AlcmeonVertxServices.register(this, controller, path, description, authorisationHandler)
        } else {
            controller.registerRoutes(path, routes(controller, checkNotNull(signatureVerifier)))
        }
    }

    /**
     * The routes of the sub bot. They answer as the former Vert.x handlers did: the description as text, 403 with
     * a message for an invalid signature, and the JSON answer of the bot once it has answered.
     */
    internal fun routes(
        controller: ConnectorController,
        verifier: AlcmeonSignatureVerifier,
    ): HttpRoutes =
        HttpRoutes {
            get("$path/description") { HttpResponse(body = description.toByteArray()) }
            listOf("$path/start", "$path/handle-event").forEach { route ->
                post(route) { request ->
                    if (!verifier.verify(request.path, request.bodyAsString, request.header("X-Alcmeon-Webhook-Signature"))) {
                        return@post HttpResponse(403, body = "Invalid Alcmeon webhook signature".toByteArray())
                    }
                    val response = CompletableDeferred<HttpResponse>()
                    val handled =
                        handleMessage(request.bodyAsString, controller) { backend ->
                            AlcmeonHttpConnectorCallback(connectorId, backend) { json ->
                                response.complete(HttpResponse(headers = mapOf("Content-Type" to listOf("application/json")), body = json.toByteArray()))
                            }
                        }
                    // the former handler did not answer a message it could not read: the sender timed out
                    if (handled) response.await() else HttpResponse.status(400)
                }
            }
        }

    override fun addSuggestions(
        text: CharSequence,
        suggestions: List<CharSequence>,
    ): BotBus.() -> ConnectorMessage? =
        {
            if ((connectorData.callback as? AlcmeonCallback)?.backend == AlcmeonBackend.WHATSAPP) {
                if (suggestions.size > 3) {
                    listMessage(text, i18nKey("whatsapp_list_message_default_button", "Choose an answer"), suggestions.map { whatsappNlpQuickReply(it) })
                } else {
                    replyButtonMessage(text, suggestions.map { whatsappNlpQuickReply(it) })
                }
            } else if ((connectorData.callback as? AlcmeonCallback)?.backend == AlcmeonBackend.FACEBOOK) {
                text(text, suggestions.map { messengerNlpQuickReply(it) })
            } else {
                null
            }
        }

    override fun canHandleMessageFor(otherConnectorType: ConnectorType): Boolean =
        otherConnectorType.id in
            setOf(
                ALCMEON_CONNECTOR_TYPE_ID,
                messengerConnectorType.id,
                whatsAppConnectorType.id,
            )

    /**
     * Handles the message of [body] with the callback created by [callback]: false if it cannot be read.
     */
    internal fun handleMessage(
        body: String,
        controller: ConnectorController,
        callback: (AlcmeonBackend) -> AlcmeonCallback,
    ): Boolean {
        try {
            logger.info { "message received from Alcmeon: $body" }
            val message = mapper.readValue<AlcmeonConnectorMessageIn>(body)

            val senderId = UserHashedIdCache.createHashedId(message.userExternalId)

            val event =
                when (message) {
                    is AlcmeonConnectorWhatsappMessageIn -> {
                        when (message.event) {
                            is AlcmeonConnectorWhatsappMessageInteractiveEvent -> {
                                SendChoice.decodeChoice(
                                    message.event.interactive.payload,
                                    PlayerId(senderId),
                                    connectorId,
                                    PlayerId(connectorId, PlayerType.bot),
                                )
                            }

                            is AlcmeonConnectorWhatsappMessageTextEvent -> {
                                sendSentence(senderId, message.event.text.body)
                            }

                            else -> {
                                sendSentence(senderId, null)
                            }
                        }
                    }

                    is AlcmeonConnectorFacebookMessageIn -> {
                        sendSentence(senderId, message.event.message.text)
                    }

                    else -> {
                        sendSentence(senderId, null)
                    }
                }

            executor.executeBlocking {
                controller.handle(
                    event,
                    ConnectorData(callback(AlcmeonBackend.findBackend(message.backend))),
                )
            }
            return true
        } catch (e: Throwable) {
            logger.error { e }
            return false
        }
    }

    override fun send(
        event: Event,
        callback: ConnectorCallback,
        delayInMs: Long,
    ) {
        logger.debug { "event: $event" }
        callback as AlcmeonCallback
        if (event is Action) {
            callback.addAction(event, delayInMs)
            if (event.metadata.lastAnswer) {
                callback.sendResponseWithoutExit()
            }
        } else if (event is AlcmeonExitEvent) {
            callback.sendResponseWithExit(event.exitReason, event.delayInMs)
        }
    }

    private fun sendSentence(
        from: String,
        text: String?,
    ) = SendSentence(
        PlayerId((from)),
        connectorId,
        PlayerId(connectorId, PlayerType.bot),
        text,
    )

    override fun toConnectorMessage(message: MediaMessage): BotBus.() -> List<ConnectorMessage> =
        {
            when ((connectorData.callback as? AlcmeonCallback)?.backend) {
                AlcmeonBackend.WHATSAPP -> WhatsAppMediaConverter.toConnectorMessage(message)(this)
                AlcmeonBackend.FACEBOOK -> MessengerMediaConverter.toConnectorMessage(message)(this)
                else -> emptyList()
            }
        }
}

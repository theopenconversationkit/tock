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

package fr.vsct.tock.bot.connector.messenger

import fr.vsct.tock.bot.connector.Connector
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.connector.messenger.model.Recipient
import fr.vsct.tock.bot.connector.messenger.model.send.ActionRequest
import fr.vsct.tock.bot.connector.messenger.model.send.SenderAction.mark_seen
import fr.vsct.tock.bot.connector.messenger.model.send.SenderAction.typing_off
import fr.vsct.tock.bot.connector.messenger.model.send.SenderAction.typing_on
import fr.vsct.tock.bot.connector.messenger.model.webhook.CallbackRequest
import fr.vsct.tock.bot.engine.ConnectorController
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType.bot
import fr.vsct.tock.bot.engine.user.UserPreferences
import fr.vsct.tock.shared.defaultLocale
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.jackson.readValue
import fr.vsct.tock.shared.vertx.vertx
import io.vertx.ext.web.Router
import mu.KotlinLogging
import org.apache.commons.codec.binary.Hex
import java.lang.Exception
import java.time.ZoneOffset
import java.util.Locale
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 *
 */
class MessengerConnector(
        applicationId: String,
        val path: String,
        pageId: String,
        val token: String,
        val verifyToken: String?,
        val client: MessengerClient) : Connector {

    companion object {
        private val logger = KotlinLogging.logger {}
        private val initializedPath: MutableSet<String> = mutableSetOf()
        private val pageApplicationMap: MutableMap<String, String> = mutableMapOf()
        private val applicationTokenMap: MutableMap<String, String> = mutableMapOf()
    }

    init {
        pageApplicationMap.put(pageId, applicationId)
        applicationTokenMap.put(applicationId, token)
    }

    override val connectorType: ConnectorType = MessengerConnectorProvider.connectorType


    override fun register(controller: ConnectorController, router: Router) {
        if (!initializedPath.contains(path)) {
            initializedPath.add(path)

            //see https://developers.facebook.com/docs/graph-api/webhooks
            router.get(path).handler { context ->
                try {
                    logger.info { "get facebook Message" }
                    if (verifyToken == null || verifyToken == context.request().getParam("hub.verify_token")) {
                        context.response().end(context.request().getParam("hub.challenge"))
                    } else {
                        context.response().end("Invalid verify token")
                    }
                } catch(e: Throwable) {
                    logger.error(e)
                    context.fail(500)
                }
            }

            router.post(path).handler { context ->
                try {
                    val facebookHeader = context.request().getHeader("X-Hub-Signature")
                    logger.debug { "Facebook signature:  $facebookHeader" }
                    logger.debug { "Facebook headers:  ${context.request().headers().entries()}" }
                    val body = context.bodyAsString
                    if (facebookHeader != null && isSignedByFacebook(body, facebookHeader)) {
                        vertx.runOnContext {
                            try {
                                logger.debug { "Facebook request input : $body" }
                                val request = mapper.readValue(body, CallbackRequest::class)

                                vertx.executeBlocking<Void>({
                                    try {
                                        request.entry.forEach { entry ->
                                            try {
                                                if (entry.messaging?.isNotEmpty() ?: false) {

                                                    val applicationId = pageApplicationMap.getValue(entry.id)
                                                    entry.messaging!!.forEach { m ->
                                                        try {
                                                            val action = WebhookActionConverter.toAction(m, applicationId)
                                                            if (action != null) {
                                                                controller.handle(action)
                                                            } else {
                                                                logger.error { "unable to convert $m to action" }
                                                            }
                                                        } catch(e: Throwable) {
                                                            try {
                                                                logger.error(e)
                                                                controller.errorMessage(m.playerId(bot), applicationId, m.recipientId(bot)).let {
                                                                    send(it)
                                                                    endTypingAnswer(it)
                                                                }
                                                            } catch(t: Throwable) {
                                                                logger.error(e)
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    logger.warn { "empty message for entry $entry" }
                                                }
                                            } catch(e: Throwable) {
                                                logger.error(e)
                                            }
                                        }
                                    } catch(e: Throwable) {
                                        logger.error(e)
                                    } finally {
                                        it.complete()
                                    }
                                }, false, {})
                            } catch(t: Throwable) {
                                logger.error(t)
                            }
                        }
                    } else {
                        logger.error { "Not signed by facebook!!! : $facebookHeader \n $body" }
                    }

                } catch(e: Throwable) {
                    logger.error(e)
                } finally {
                    try {
                        context.response().end()
                    } catch(e: Throwable) {
                        logger.error(e)
                    }
                }
            }
        }
    }

    override fun send(action: Action) {
        try {
            val recipient = Recipient(action.recipientId.id)
            val message = SendActionConverter.toMessageRequest(action)
            if (message != null) {
                logger.debug { "message sent: $message to ${action.recipientId}" }
                val token = getToken(action)
                client.sendMessage(token, message)
                if (action.botMetadata.lastAnswer) {
                    client.sendAction(token, ActionRequest(recipient, typing_off))
                    client.sendAction(token, ActionRequest(recipient, mark_seen))
                } else {
                    client.sendAction(token, ActionRequest(recipient, typing_on))
                }
            } else {
                logger.error { "unable to convert $action to message" }
            }
        } catch(e: Throwable) {
            logger.error(e)
        }
    }

    override fun startTypingAnswer(action: Action) {
        client.sendAction(getToken(action), ActionRequest(Recipient(action.recipientId.id), typing_on))
    }

    fun endTypingAnswer(action: Action) {
        client.sendAction(getToken(action), ActionRequest(Recipient(action.recipientId.id), typing_off))
    }

    override fun loadProfile(applicationId: String, userId: PlayerId): UserPreferences {
        try {
            val userProfile = client.getUserProfile(applicationTokenMap.getValue(applicationId), Recipient(userId.id))
            logger.debug { "User profile : $userProfile for $userId" }
            return UserPreferences(
                    userProfile.firstName,
                    userProfile.lastName,
                    null,
                    ZoneOffset.ofHours(userProfile.timezone),
                    if (userProfile.locale == null) defaultLocale else Locale(userProfile.locale))
        } catch(e: Exception) {
            logger.error(e)
        }
        return UserPreferences()
    }

    private fun getToken(action: Action): String {
        return applicationTokenMap.getValue(action.applicationId)
    }

    fun isSignedByFacebook(payload: String, facebookSignature: String): Boolean {
        return "sha1=${sha1(payload, client.secretKey)}" == facebookSignature
    }

    fun sha1(payload: String, key: String): String {
        val k = SecretKeySpec(key.toByteArray(), "HmacSHA1")
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(k)

        val bytes = mac.doFinal(payload.toByteArray())

        return String(Hex().encode(bytes))
    }
}
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

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.salomonbrys.kodein.instance
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import fr.vsct.tock.bot.connector.ConnectorBase
import fr.vsct.tock.bot.connector.ConnectorCallback
import fr.vsct.tock.bot.connector.ConnectorData
import fr.vsct.tock.bot.connector.messenger.AttachmentCacheService.getAttachmentId
import fr.vsct.tock.bot.connector.messenger.AttachmentCacheService.setAttachmentId
import fr.vsct.tock.bot.connector.messenger.model.Recipient
import fr.vsct.tock.bot.connector.messenger.model.attachment.AttachmentRequest
import fr.vsct.tock.bot.connector.messenger.model.handover.PassThreadControlRequest
import fr.vsct.tock.bot.connector.messenger.model.handover.RequestThreadControlRequest
import fr.vsct.tock.bot.connector.messenger.model.handover.SecondaryReceiverData
import fr.vsct.tock.bot.connector.messenger.model.handover.TakeThreadControlRequest
import fr.vsct.tock.bot.connector.messenger.model.send.ActionRequest
import fr.vsct.tock.bot.connector.messenger.model.send.Attachment
import fr.vsct.tock.bot.connector.messenger.model.send.AttachmentMessage
import fr.vsct.tock.bot.connector.messenger.model.send.AttachmentType
import fr.vsct.tock.bot.connector.messenger.model.send.CustomEventRequest
import fr.vsct.tock.bot.connector.messenger.model.send.MediaPayload
import fr.vsct.tock.bot.connector.messenger.model.send.MessageRequest
import fr.vsct.tock.bot.connector.messenger.model.send.SendResponse
import fr.vsct.tock.bot.connector.messenger.model.send.SenderAction.mark_seen
import fr.vsct.tock.bot.connector.messenger.model.send.SenderAction.typing_off
import fr.vsct.tock.bot.connector.messenger.model.send.SenderAction.typing_on
import fr.vsct.tock.bot.connector.messenger.model.send.UrlPayload
import fr.vsct.tock.bot.connector.messenger.model.webhook.CallbackRequest
import fr.vsct.tock.bot.engine.BotRepository.requestTimer
import fr.vsct.tock.bot.engine.ConnectorController
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.event.Event
import fr.vsct.tock.bot.engine.event.MarkSeenEvent
import fr.vsct.tock.bot.engine.event.TypingOffEvent
import fr.vsct.tock.bot.engine.event.TypingOnEvent
import fr.vsct.tock.bot.engine.monitoring.logError
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType.bot
import fr.vsct.tock.bot.engine.user.PlayerType.temporary
import fr.vsct.tock.bot.engine.user.UserPreferences
import fr.vsct.tock.shared.Executor
import fr.vsct.tock.shared.booleanProperty
import fr.vsct.tock.shared.defaultLocale
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.property
import fr.vsct.tock.shared.vertx.vertx
import mu.KotlinLogging
import org.apache.commons.codec.binary.Hex
import org.apache.commons.lang3.LocaleUtils
import java.lang.Exception
import java.time.Duration
import java.time.ZoneOffset
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Contains built-in checks to ensure that two [MessageRequest] for the same recipient are sent sequentially.
 */
class MessengerConnector internal constructor(
    val applicationId: String,
    val path: String,
    val pageId: String,
    val appToken: String,
    val token: String,
    val verifyToken: String?,
    val client: MessengerClient
) : ConnectorBase(MessengerConnectorProvider.connectorType) {

    private data class ActionWithTimestamp(val action: Action, val timestamp: Long)

    companion object {
        private val logger = KotlinLogging.logger {}
        private val pageApplicationMap: MutableMap<String, String> = ConcurrentHashMap()
        private val applicationTokenMap: MutableMap<String, String> = ConcurrentHashMap()
        private val connectors: MutableSet<MessengerConnector> = CopyOnWriteArraySet<MessengerConnector>()

        fun getConnectorByPageId(pageId: String): MessengerConnector? {
            return connectors.find { it.pageId == pageId }
            //TODO remove this when backward compatibility is no more assured
                    ?: (connectors.find { it.applicationId == pageId }?.also { logger.warn { "use appId as pageId $pageId not found" } })
        }

        fun healthcheck(): Boolean {
            return connectors.firstOrNull()?.client?.healthcheck() ?: true
        }
    }

    init {
        pageApplicationMap[pageId] = applicationId
        applicationTokenMap[applicationId] = token
        connectors.add(this)
    }

    private val executor: Executor by injector.instance()
    private val messagesByRecipientMap: Cache<String, ConcurrentLinkedQueue<ActionWithTimestamp>> =
        CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .build()
    private val webhookCheckPeriod = property("tock_messenger_webhook_check_period", "600").toLong()

    override fun register(controller: ConnectorController) {
        controller.registerServices(path) { router ->
            logger.info("deploy rest messenger connector services for root path $path ")

            // Subscribe to an automatic webhook check
            if (booleanProperty("tock_messenger_webhook_check_subscription", false)) {
                logger.info { "Subscribe to automatic webhook check" }
                registerCheckWebhook()
            }

            //see https://developers.facebook.com/docs/graph-api/webhooks
            router.get(path).handler { context ->
                try {
                    logger.info { "get facebook Message" }
                    if (verifyToken == null || verifyToken == context.request().getParam("hub.verify_token")) {
                        context.response().end(context.request().getParam("hub.challenge"))
                    } else {
                        context.response().end("Invalid verify token")
                    }
                } catch (e: Throwable) {
                    logger.error(e)
                    context.fail(500)
                }
            }

            router.post(path).handler { context ->
                val requestTimerData = requestTimer.start("messenger_webhook")
                try {
                    val facebookHeader = context.request().getHeader("X-Hub-Signature")
                    logger.debug { "Facebook signature:  $facebookHeader" }
                    logger.debug { "Facebook headers:  ${context.request().headers().entries()}" }
                    val body = context.bodyAsString
                    if (facebookHeader != null && isSignedByFacebook(body, facebookHeader)) {
                        try {
                            logger.debug { "Facebook request input : $body" }
                            val request = mapper.readValue<CallbackRequest>(body)

                            vertx.executeBlocking<Void>({
                                try {
                                    request.entry.forEach { entry ->
                                        try {
                                            if (entry.messaging?.isNotEmpty() == true) {

                                                val applicationId = pageApplicationMap.getValue(entry.id)
                                                entry.messaging.forEach { m ->
                                                    if (m != null) {
                                                        try {
                                                            val event = WebhookActionConverter.toEvent(m, applicationId)
                                                            if (event != null) {
                                                                controller.handle(
                                                                    event,
                                                                    ConnectorData(
                                                                        MessengerConnectorCallback(event.applicationId),
                                                                        m.priorMessage?.identifier?.let {
                                                                            PlayerId(
                                                                                it,
                                                                                temporary
                                                                            )
                                                                        }
                                                                    )
                                                                )
                                                            } else {
                                                                logger.logError(
                                                                    "unable to convert $m to event",
                                                                    requestTimerData
                                                                )
                                                            }
                                                        } catch (e: Throwable) {
                                                            try {
                                                                logger.logError(e, requestTimerData)
                                                                controller.errorMessage(
                                                                    m.playerId(bot),
                                                                    applicationId,
                                                                    m.recipientId(bot)
                                                                ).let {
                                                                    sendEvent(it)
                                                                    endTypingAnswer(it)
                                                                }
                                                            } catch (t: Throwable) {
                                                                logger.error(e)
                                                            }
                                                        }
                                                    } else {
                                                        logger.error { "null message: $body" }
                                                    }
                                                }
                                            } else {
                                                logger.warn { "empty message for entry $entry" }
                                            }
                                        } catch (e: Throwable) {
                                            logger.logError(e, requestTimerData)
                                        }
                                    }
                                } catch (e: Throwable) {
                                    logger.logError(e, requestTimerData)
                                } finally {
                                    it.complete()
                                }
                            }, false, {})
                        } catch (t: Throwable) {
                            logger.logError(t, requestTimerData)
                        }
                    } else {
                        logger.logError("Not signed by facebook!!! : $facebookHeader \n $body", requestTimerData)
                    }

                } catch (e: Throwable) {
                    logger.logError(e, requestTimerData)
                } finally {
                    try {
                        requestTimer.end(requestTimerData)
                        context.response().end()
                    } catch (e: Throwable) {
                        logger.error(e)
                    }
                }
            }
        }
    }

    /**
     * Send an action to messenger.
     *
     * @param event the action to send
     * @param transformMessageRequest method to transform the [MessageRequest] before sending - default is identity
     * @param postMessage method (with token parameter) launched after successful [MessageRequest] call - default do nothing
     * @param transformActionRequest method to transform the [ActionRequest] before sending - default is identity
     */
    fun sendEvent(
        event: Event,
        transformMessageRequest: (MessageRequest) -> MessageRequest = { it },
        postMessage: (String) -> Unit = {},
        transformActionRequest: (ActionRequest) -> ActionRequest = { it }
    ): SendResponse? {
        return try {
            if (event is Action) {
                var message = SendActionConverter.toMessageRequest(event)
                if (message != null) {
                    message = transformMessageRequest.invoke(message)
                    logger.debug { "message sent: $message to ${event.recipientId}" }
                    val token = getToken(event)

                    //need to get the attachment id for the media payload
                    val attachmentMessage = message.message as? AttachmentMessage
                    val payload = attachmentMessage?.attachment?.payload
                    if (payload is MediaPayload) {
                        val firstElement = payload.elements.first()
                        val url = firstElement.attachmentId
                        val attachmentId =
                            getAttachmentId(event.applicationId, url)
                                .let {
                                    if (it == null) {
                                        client.sendAttachment(
                                            token,
                                            AttachmentRequest(
                                                AttachmentMessage(
                                                    Attachment(
                                                        AttachmentType.fromTockAttachmentType(
                                                            firstElement.mediaType.toAttachmentType()
                                                        ),
                                                        UrlPayload(
                                                            url,
                                                            null,
                                                            true
                                                        )
                                                    )
                                                )
                                            )
                                        )!!
                                            .apply {
                                                setAttachmentId(event.applicationId, url, attachmentId!!)
                                            }
                                            .attachmentId!!
                                    } else {
                                        it
                                    }
                                }
                        message = message.copy(
                            message = AttachmentMessage(
                                Attachment(
                                    AttachmentType.template,
                                    payload.copy(elements = listOf(firstElement.copy(attachmentId = attachmentId)))
                                ),
                                attachmentMessage.quickReplies
                            )
                        )
                    }

                    val response = client.sendMessage(token, message)
                    if (response.attachmentId != null) {
                        val m = message.message
                        if (m is AttachmentMessage) {
                            val p = m.attachment.payload
                            if (p is UrlPayload && p.url != null) {
                                setAttachmentId(event.applicationId, p.url, response.attachmentId)
                            }
                        }
                    }
                    postMessage.invoke(token)
                    response
                } else {
                    logger.error { "unable to convert $event to message" }
                    null
                }
            } else {
                sendSimpleEvent(event, transformActionRequest)
            }
        } catch (e: Throwable) {
            logger.error(e)
            null
        }
    }

    /**
     * Send action after an optin request, using the recipient.user_ref property.
     * See https://developers.facebook.com/docs/messenger-platform/plugin-reference/checkbox-plugin#implementation for more details.
     *
     * @param action the action to send
     * @exception IllegalStateException if the message is not delivered
     */
    fun sendOptInEvent(event: Event) {
        sendEvent(
            event,
            transformMessageRequest = { request ->
                //need to use the user_ref here
                request.copy(recipient = Recipient(null, request.recipient.id))
            },
            transformActionRequest = { request ->
                //need to use the user_ref here
                request.copy(recipient = Recipient(null, request.recipient.id))
            }) ?: error("message $event not delivered")
    }

    /**
     * Send a custom event to messenger
     *
     * @param customEventRequest an object containing a list of custom events
     */
    @Deprecated("To be removed in next release - app id is already known")
    fun sendCustomEvent(applicationId:String, customEventRequest: CustomEventRequest) {
        try {
            client.sendCustomEvent(applicationId, customEventRequest)
        } catch (e: Throwable) {
            logger.error(e)
        }
    }

    /**
     * Send a custom event to messenger
     *
     * @param customEventRequest an object containing a list of custom events
     */
    fun sendCustomEvent(customEventRequest: CustomEventRequest) {
        try {
            client.sendCustomEvent(applicationId, customEventRequest)
        } catch (e: Throwable) {
            logger.error(e)
        }
    }

    /**
     * Returns the current thread owner (handover protocol).
     *
     * @param userId the user id you would like to known
     * @return null if the app does may not known the owner, the app id owner either
     */
    fun getThreadOwner(userId: PlayerId): String? = client.getThreadOwnerId(getToken(applicationId), userId.id)

    /**
     * Returns the secondary receivers (handover protocol) if the app is the primary receiver, null either.
     */
    fun getSecondaryReceivers(): List<SecondaryReceiverData>? = client.getSecondaryReceivers(getToken(applicationId))

    /**
     * Sends a request to get thread control.
     */
    fun requestThreadControl(userId: PlayerId, metadata: String? = null): SendResponse? =
        client.requestThreadControl(
            getToken(applicationId),
            RequestThreadControlRequest(Recipient(userId.id, metadata))
        )

    /**
     * Takes the thread control.
     */
    fun takeThreadControl(userId: PlayerId, metadata: String? = null): SendResponse? =
        client.takeThreadControl(
            getToken(applicationId),
            TakeThreadControlRequest(Recipient(userId.id, metadata))
        )

    /**
     * Passes thread control.
     */
    fun passThreadControl(userId: PlayerId, metadata: String? = null): SendResponse? =
        client.passThreadControl(
            getToken(applicationId),
            PassThreadControlRequest(Recipient(userId.id, metadata))
        )

    /**
     * Send a "simple" messenger event.
     *
     * @param event the event
     * @param transformActionRequest method to transform the [ActionRequest] before sending - default is identity
     */
    fun sendSimpleEvent(
        event: Event,
        transformActionRequest: (ActionRequest) -> ActionRequest = { it }
    ): SendResponse? =
        when (event) {
            is TypingOnEvent -> client.sendAction(
                getToken(event),
                transformActionRequest(ActionRequest(Recipient(event.recipientId.id), typing_on))
            )
            is TypingOffEvent -> client.sendAction(
                getToken(event),
                transformActionRequest(ActionRequest(Recipient(event.recipientId.id), typing_off))
            )
            is MarkSeenEvent -> client.sendAction(
                getToken(event),
                transformActionRequest(ActionRequest(Recipient(event.recipientId.id), mark_seen))
            )
            else -> {
                logger.warn { "unsupported event $event" }
                null
            }
        }

    /**
     * Send the event to messenger asynchronously.
     * Contains checks to ensure that two [Action] for the same recipient are sent sequentially.
     */
    override fun send(event: Event, callback: ConnectorCallback, delayInMs: Long) {
        val delay = Duration.ofMillis(delayInMs)
        if (event is Action) {
            val id = event.recipientId.id.intern()
            val action = ActionWithTimestamp(event, System.currentTimeMillis() + delayInMs)
            val queue = messagesByRecipientMap
                .get(id) { ConcurrentLinkedQueue() }
                .apply {
                    synchronized(id) {
                        peek().also { existingAction ->
                            offer(action)
                            if (existingAction != null) {
                                return
                            }
                        }
                    }
                }

            executor.executeBlocking(delay) {
                sendActionFromConnector(event, queue)
            }
        } else {
            executor.executeBlocking(delay) {
                sendEvent(event)
            }
        }
    }

    private fun sendActionFromConnector(action: Action, queue: ConcurrentLinkedQueue<ActionWithTimestamp>) {
        try {
            sendActionFromConnector(action)
        } finally {
            synchronized(action.recipientId.id.intern()) {
                //remove the current one
                queue.poll()
                queue.peek()
            }?.also { a ->
                val timeToWait = a.timestamp - System.currentTimeMillis()
                if (timeToWait > 0) {
                    Thread.sleep(timeToWait)
                }
                sendActionFromConnector(a.action, queue)
            }
        }
    }

    private fun sendActionFromConnector(action: Action) {
        sendEvent(
            action,
            postMessage =
            { token ->
                val recipient = Recipient(action.recipientId.id)
                if (action.metadata.lastAnswer) {
                    client.sendAction(token, ActionRequest(recipient, typing_off))
                    client.sendAction(token, ActionRequest(recipient, mark_seen))
                } else {
                    client.sendAction(token, ActionRequest(recipient, typing_on))
                }
            }
        )
    }

    private fun endTypingAnswer(action: Action) {
        client.sendAction(getToken(action), ActionRequest(Recipient(action.recipientId.id), typing_off))
    }

    override fun loadProfile(callback: ConnectorCallback, userId: PlayerId): UserPreferences {
        try {
            val userProfile =
                client.getUserProfile(applicationTokenMap.getValue(callback.applicationId), Recipient(userId.id))
            logger.debug { "User profile : $userProfile for $userId" }
            return UserPreferences(
                userProfile.firstName,
                userProfile.lastName,
                null,
                ZoneOffset.ofHours(userProfile.timezone),
                userProfile.locale?.let {
                    try {
                        LocaleUtils.toLocale(it)
                    } catch (e: Exception) {
                        logger.error(e)
                        null
                    }
                } ?: defaultLocale,
                userProfile.profilePic,
                userProfile.gender)
        } catch (e: Exception) {
            logger.error(e)
        }
        return UserPreferences()
    }

    private fun getToken(event: Event): String = getToken(event.applicationId)

    private fun getToken(appId: String): String =
        applicationTokenMap[appId]
        //TODO remove this when backward compatibility is no more assured
                ?: pageApplicationMap[appId]?.let {
                    logger.warn { "use pageId as appId for $appId" }
                    applicationTokenMap[it]
                }
                ?: error("$appId not found")

    private fun isSignedByFacebook(payload: String, facebookSignature: String): Boolean {
        return "sha1=${sha1(payload, client.secretKey)}" == facebookSignature
    }

    private fun sha1(payload: String, key: String): String {
        val k = SecretKeySpec(key.toByteArray(), "HmacSHA1")
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(k)

        val bytes = mac.doFinal(payload.toByteArray())

        return String(Hex().encode(bytes))
    }


    fun registerCheckWebhook() {
        executor.setPeriodic(Duration.ofSeconds(1), Duration.ofSeconds(webhookCheckPeriod)) {
            logger.info { "Run periodic webhook subscription" }
            checkWebhookSubscription()
        }
    }

    fun checkWebhookSubscription() {
        val defaultFields = "messages,messaging_postbacks,messaging_optins,messaging_account_linking"
        try {
            val getSubscriptionsResponse = client.getSubscriptions(applicationId, appToken)
            if (getSubscriptionsResponse?.data?.size == 0 || getSubscriptionsResponse?.data?.firstOrNull()?.active == false) {
                logger.info { "Get disabled webhook subscription, response: $getSubscriptionsResponse" }
                val fields = getSubscriptionsResponse.data.firstOrNull()?.fields?.let {
                    it.map { it.name }.joinToString (",")
                } ?: defaultFields
                val callbackUrl = property(
                    "tock_messenger_webhook_url",
                    getSubscriptionsResponse.data.firstOrNull()?.callbackUrl ?: ""
                )
                val subscriptionsRequest = client.subscriptions(
                    applicationId,
                    callbackUrl,
                    fields,
                    verifyToken ?: "",
                    appToken
                )
                if (subscriptionsRequest?.success == true) {
                    client.deleteSubscribedApps(pageId, fields, token)
                    client.subscribedApps(pageId, fields, token)
                }
            }
        } catch (e: Exception) {
            logger.error("Error when running webhook subscription", e)
        }
    }
}
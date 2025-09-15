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

package ai.tock.bot.connector.messenger

import ai.tock.bot.connector.ConnectorBase
import ai.tock.bot.connector.ConnectorCallback
import ai.tock.bot.connector.ConnectorData
import ai.tock.bot.connector.ConnectorFeature.CAROUSEL
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorQueue
import ai.tock.bot.connector.media.MediaMessage
import ai.tock.bot.connector.messenger.AttachmentCacheService.getAttachmentId
import ai.tock.bot.connector.messenger.AttachmentCacheService.setAttachmentId
import ai.tock.bot.connector.messenger.model.Recipient
import ai.tock.bot.connector.messenger.model.attachment.AttachmentRequest
import ai.tock.bot.connector.messenger.model.handover.PassThreadControlRequest
import ai.tock.bot.connector.messenger.model.handover.RequestThreadControlRequest
import ai.tock.bot.connector.messenger.model.handover.SecondaryReceiverData
import ai.tock.bot.connector.messenger.model.handover.TakeThreadControlRequest
import ai.tock.bot.connector.messenger.model.send.ActionRequest
import ai.tock.bot.connector.messenger.model.send.Attachment
import ai.tock.bot.connector.messenger.model.send.AttachmentMessage
import ai.tock.bot.connector.messenger.model.send.AttachmentType
import ai.tock.bot.connector.messenger.model.send.CustomEventRequest
import ai.tock.bot.connector.messenger.model.send.MediaPayload
import ai.tock.bot.connector.messenger.model.send.Message
import ai.tock.bot.connector.messenger.model.send.MessageRequest
import ai.tock.bot.connector.messenger.model.send.SendResponse
import ai.tock.bot.connector.messenger.model.send.SenderAction.mark_seen
import ai.tock.bot.connector.messenger.model.send.SenderAction.typing_off
import ai.tock.bot.connector.messenger.model.send.SenderAction.typing_on
import ai.tock.bot.connector.messenger.model.send.UrlPayload
import ai.tock.bot.connector.messenger.model.webhook.CallbackRequest
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.definition.StoryStepDef
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.BotRepository.requestTimer
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.ActionNotificationType
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.event.MarkSeenEvent
import ai.tock.bot.engine.event.TypingOffEvent
import ai.tock.bot.engine.event.TypingOnEvent
import ai.tock.bot.engine.monitoring.logError
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType.bot
import ai.tock.bot.engine.user.PlayerType.temporary
import ai.tock.bot.engine.user.UserPreferences
import ai.tock.shared.Executor
import ai.tock.shared.booleanProperty
import ai.tock.shared.defaultLocale
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.jackson.mapper
import ai.tock.shared.property
import ai.tock.shared.vertx.vertx
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.salomonbrys.kodein.instance
import java.time.Duration
import java.time.ZoneOffset
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import mu.KotlinLogging
import org.apache.commons.codec.binary.Hex
import org.apache.commons.lang3.LocaleUtils

/**
 * Contains built-in checks to ensure that two [MessageRequest] for the same recipient are sent sequentially.
 */
class MessengerConnector internal constructor(
    internal val connectorId: String,
    private val applicationId: String,
    private val path: String,
    private val pageId: String,
    private val appToken: String,
    private val token: String,
    private val verifyToken: String?,
    internal val client: MessengerClient,
    @Volatile
    private var subscriptionCheck: Boolean = webhookSubscriptionCheckEnabled,
    private val personaId: String? = null
) : ConnectorBase(MessengerConnectorProvider.connectorType, setOf(CAROUSEL)) {

    companion object {
        private val logger = KotlinLogging.logger {}
        internal val pageIdConnectorIdMap: MutableMap<String, MutableSet<String>> = ConcurrentHashMap()
        internal val connectorIdConnectorControllerMap: MutableMap<String, ConnectorController> = ConcurrentHashMap()
        internal val connectorIdTokenMap: MutableMap<String, String> = ConcurrentHashMap()
        internal val connectorIdApplicationIdMap: MutableMap<String, String> = ConcurrentHashMap()
        private val webhookSubscriptionCheckPeriod = property("tock_messenger_webhook_check_period", "600").toLong()
        private val webhookSubscriptionCheckEnabled =
            booleanProperty("tock_messenger_webhook_check_subscription", false)

        private fun getAllConnectors(): List<MessengerConnector> =
            connectorIdConnectorControllerMap.values.asSequence().map { it.connector }
                .filterIsInstance<MessengerConnector>().toList()

        fun getConnectorById(connectorId: String): MessengerConnector? {
            return connectorIdConnectorControllerMap[connectorId]?.connector as? MessengerConnector
        }

        fun healthcheck(): Boolean {
            return (connectorIdConnectorControllerMap.values.firstOrNull()?.connector as? MessengerConnector)?.client?.healthcheck()
                ?: true
        }
    }

    init {
        (pageIdConnectorIdMap.getOrPut(pageId) { CopyOnWriteArraySet() }).add(connectorId)
        connectorIdTokenMap[connectorId] = token
        connectorIdApplicationIdMap[connectorId] = applicationId
    }

    private val executor: Executor by injector.instance()
    private val queue: ConnectorQueue = ConnectorQueue(executor)

    override fun register(controller: ConnectorController) {
        controller.registerServices(path) { router ->
            logger.info("deploy rest messenger connector services for root path $path ")

            // Subscribe to an automatic webhook check
            if (webhookSubscriptionCheckEnabled) {
                logger.info { "Subscribe to automatic webhook check" }
                registerCheckWebhook()
            }

            // see https://developers.facebook.com/docs/graph-api/webhooks
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
                    val body = context.body().asString()
                    if (facebookHeader != null && isSignedByFacebook(body, facebookHeader)) {
                        try {
                            logger.debug { "Facebook request input : $body" }
                            val request = mapper.readValue<CallbackRequest>(body)
                            vertx.executeBlocking(
                                {
                                    try {
                                        MessengerConnectorHandler(
                                            applicationId, controller, request, requestTimerData
                                        ).handleRequest()
                                    } catch (e: Throwable) {
                                        logger.logError(e, requestTimerData)
                                    }
                                },
                                false
                            )
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
        connectorIdConnectorControllerMap[connectorId] = controller
    }

    override fun unregister(controller: ConnectorController) {
        super.unregister(controller)
        subscriptionCheck = false
        if (connectorIdConnectorControllerMap[connectorId] === controller) {
            connectorIdConnectorControllerMap.remove(connectorId)
            pageIdConnectorIdMap[pageId]?.remove(connectorId)
            connectorIdTokenMap.remove(connectorId)
            connectorIdApplicationIdMap.remove(connectorId)
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
        transformActionRequest: (ActionRequest) -> ActionRequest = { it },
        errorListener: (Throwable) -> Unit = {}
    ): SendResponse? {
        return try {
            if (event is Action) {
                var message = SendActionConverter.toMessageRequest(event, personaId)
                if (message != null) {
                    message = transformMessageRequest.invoke(message)
                    logger.debug { "message sent: $message to ${event.recipientId}" }
                    val token = getToken(event)

                    // need to get the attachment id for the media payload
                    val attachmentMessage = message.message as? AttachmentMessage
                    val payload = attachmentMessage?.attachment?.payload
                    if (payload is MediaPayload) {
                        val firstElement = payload.elements.first()
                        val url = firstElement.attachmentId
                        val attachmentId =
                            getAttachmentId(event.applicationId, url)
                                .let {
                                    it ?: client.sendAttachment(
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
                                            ),
                                            personaId
                                        )
                                    )!!
                                        .apply {
                                            setAttachmentId(event.applicationId, url, attachmentId!!)
                                        }
                                        .attachmentId!!
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
            errorListener.invoke(e)
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
    @Deprecated("deprecated - use BotRepository#notify")
    fun sendOptInEvent(event: Event) {
        sendEvent(
            event,
            transformMessageRequest = { request ->
                // need to use the user_ref here
                request.copy(recipient = Recipient(null, request.recipient.id))
            },
            transformActionRequest = { request ->
                // need to use the user_ref here
                request.copy(recipient = Recipient(null, request.recipient.id))
            }
        ) ?: error("message $event not delivered")
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
    fun getThreadOwner(userId: PlayerId): String? = client.getThreadOwnerId(token, userId.id)

    /**
     * Returns the secondary receivers (handover protocol) if the app is the primary receiver, null either.
     */
    fun getSecondaryReceivers(): List<SecondaryReceiverData>? = client.getSecondaryReceivers(token)

    /**
     * Sends a request to get thread control.
     */
    fun requestThreadControl(userId: PlayerId, metadata: String? = null): SendResponse? =
        client.requestThreadControl(
            token,
            RequestThreadControlRequest(Recipient(userId.id), metadata)
        )

    /**
     * Takes the thread control.
     */
    fun takeThreadControl(userId: PlayerId, metadata: String? = null): SendResponse? =
        client.takeThreadControl(
            token,
            TakeThreadControlRequest(Recipient(userId.id), metadata)
        )

    /**
     * Passes thread control.
     */
    fun passThreadControl(userId: PlayerId, targetAppId: String, metadata: String? = null): SendResponse? =
        client.passThreadControl(
            token,
            PassThreadControlRequest(Recipient(userId.id), targetAppId, metadata)
        )

    /**
     * Send a "simple" messenger event.
     *
     * @param event the event
     * @param transformActionRequest method to transform the [ActionRequest] before sending - default is identity
     */
    private fun sendSimpleEvent(
        event: Event,
        transformActionRequest: (ActionRequest) -> ActionRequest = { it }
    ): SendResponse? =
        when (event) {
            is TypingOnEvent -> client.sendAction(
                getToken(event),
                transformActionRequest(ActionRequest(Recipient(event.recipientId.id), typing_on, personaId))
            )

            is TypingOffEvent -> client.sendAction(
                getToken(event),
                transformActionRequest(ActionRequest(Recipient(event.recipientId.id), typing_off, personaId))
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
        val messengerCallback = callback as? MessengerConnectorCallback
        if (event is Action) {
            messengerCallback?.notificationType?.also {
                if (event.metadata.notificationType == null) {
                    event.metadata.notificationType = it
                }
            }

            queue.add(event, delayInMs) { action ->
                sendEvent(
                    event = action,
                    transformMessageRequest = { request ->
                        if (action.recipientId.type == temporary) {
                            // need to use the user_ref here
                            request.copy(recipient = Recipient(null, request.recipient.id))
                        } else {
                            request
                        }
                    },
                    transformActionRequest = { request ->
                        if (action.recipientId.type == temporary) {
                            // need to use the user_ref here
                            request.copy(recipient = Recipient(null, request.recipient.id))
                        } else {
                            request
                        }
                    },
                    postMessage =
                        { token ->
                            if (messengerCallback?.notificationType == null && action.recipientId.type != temporary) {
                                val recipient = Recipient(action.recipientId.id)
                                if (action.metadata.lastAnswer) {
                                    client.sendAction(token, ActionRequest(recipient, typing_off, personaId))
                                    client.sendAction(token, ActionRequest(recipient, mark_seen, personaId))
                                } else {
                                    client.sendAction(token, ActionRequest(recipient, typing_on, personaId))
                                }
                            }
                        },
                    errorListener = messengerCallback?.errorListener ?: {}
                )
            }
        } else {
            if (messengerCallback?.notificationType == null || (event !is TypingOnEvent && event !is TypingOffEvent && event !is MarkSeenEvent)) {
                executor.executeBlocking(delay) {
                    sendEvent(
                        event = event,
                        errorListener = messengerCallback?.errorListener ?: {}
                    )
                }
            }
        }
    }

    internal fun endTypingAnswer(action: Action) {
        client.sendAction(getToken(action), ActionRequest(Recipient(action.recipientId.id), typing_off, personaId))
    }

    override fun loadProfile(callback: ConnectorCallback, userId: PlayerId): UserPreferences {
        try {
            val userProfile =
                client.getUserProfile(connectorIdTokenMap.getValue(callback.applicationId), Recipient(userId.id))
            logger.debug { "User profile : $userProfile for $userId" }
            return UserPreferences(
                userProfile.firstName,
                userProfile.lastName,
                null,
                ZoneOffset.ofHours(userProfile.timezone),
                userProfile.locale?.let { getLocale(it) } ?: defaultLocale,
                userProfile.profilePic,
                userProfile.gender,
                initialLocale = userProfile.locale?.let { getLocale(it) } ?: defaultLocale
            )
        } catch (e: Exception) {
            logger.error(e)
        }
        return UserPreferences()
    }

    private fun getLocale(it: String): Locale? {
        return try {
            LocaleUtils.toLocale(it)
        } catch (e: Exception) {
            logger.error(e)
            null
        }
    }

    override fun refreshProfile(callback: ConnectorCallback, userId: PlayerId): UserPreferences? =
        loadProfile(callback, userId).run {
            // refresh only picture, locale & timezone
            if (picture != null) {
                UserPreferences(locale = locale, timezone = timezone, picture = picture)
            } else {
                null
            }
        }

    private fun getToken(event: Event): String = getToken(event.applicationId)

    private fun getToken(connectorId: String): String =
        connectorIdTokenMap[connectorId]
        // TODO remove this when backward compatibility is no more assured (20.3)
            ?: pageIdConnectorIdMap[connectorId]?.takeUnless { it.isEmpty() }?.let {
                logger.warn { "use pageId as connectorId for $connectorId" }
                connectorIdTokenMap[it.first()]
            }
            ?: getAllConnectors().find { it.applicationId == connectorId }?.appToken
            ?: error("$connectorId not found")

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

    private fun registerCheckWebhook() {
        subscriptionCheck = true
        executor.setPeriodic(Duration.ofSeconds(1), Duration.ofSeconds(webhookSubscriptionCheckPeriod)) {
            logger.info { "Run periodic webhook subscription" }
            checkWebhookSubscription()
        }
    }

    internal fun checkWebhookSubscription() {
        if (subscriptionCheck) {
            val defaultFields = "messages,messaging_postbacks,messaging_optins,messaging_account_linking"
            try {
                val getSubscriptionsResponse = client.getSubscriptions(applicationId, appToken)
                if (getSubscriptionsResponse?.data?.size == 0 || getSubscriptionsResponse?.data?.firstOrNull()?.active == false) {
                    logger.info { "Get disabled webhook subscription, response: $getSubscriptionsResponse" }
                    val fields = getSubscriptionsResponse.data.firstOrNull()?.fields?.let {
                        it.map { it.name }.joinToString(",")
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

    override fun notify(
        controller: ConnectorController,
        recipientId: PlayerId,
        intent: IntentAware,
        step: StoryStepDef?,
        parameters: Map<String, String>,
        notificationType: ActionNotificationType?,
        errorListener: (Throwable) -> Unit
    ) {
        controller.handle(
            SendChoice(
                recipientId,
                connectorId,
                PlayerId(pageId, bot),
                intent.wrappedIntent().name,
                step,
                parameters
            ),
            ConnectorData(
                MessengerConnectorCallback(connectorId, notificationType, errorListener)
            )
        )
    }

    override fun addSuggestions(text: CharSequence, suggestions: List<CharSequence>): BotBus.() -> ConnectorMessage? = {
        text(text, suggestions.map { nlpQuickReply(it) })
    }

    override fun addSuggestions(
        message: ConnectorMessage,
        suggestions: List<CharSequence>
    ): BotBus.() -> ConnectorMessage? = {
        if (message is Message && message.quickReplies.isNullOrEmpty()) {
            message.copy(suggestions.map { nlpQuickReply(it) })
        } else {
            message
        }
    }

    override fun toConnectorMessage(message: MediaMessage): BotBus.() -> List<ConnectorMessage> =
        MessengerMediaConverter.toConnectorMessage(message)
}

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

package ai.tock.bot.connector.twitter

import ai.tock.bot.connector.ConnectorBase
import ai.tock.bot.connector.ConnectorCallback
import ai.tock.bot.connector.ConnectorData
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorQueue
import ai.tock.bot.connector.media.MediaCard
import ai.tock.bot.connector.media.MediaMessage
import ai.tock.bot.connector.twitter.model.Attachment
import ai.tock.bot.connector.twitter.model.MediaCategory
import ai.tock.bot.connector.twitter.model.MessageCreate
import ai.tock.bot.connector.twitter.model.MessageData
import ai.tock.bot.connector.twitter.model.Webhook
import ai.tock.bot.connector.twitter.model.incoming.IncomingEvent
import ai.tock.bot.connector.twitter.model.incoming.TweetIncomingEvent
import ai.tock.bot.connector.twitter.model.outcoming.DirectMessageOutcomingEvent
import ai.tock.bot.connector.twitter.model.outcoming.OutcomingEvent
import ai.tock.bot.connector.twitter.model.outcoming.Tweet
import ai.tock.bot.connector.twitter.model.toMediaCategory
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.definition.StoryStepDef
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.BotRepository
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.ActionNotificationType
import ai.tock.bot.engine.action.ActionVisibility
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.config.UploadedFilesService
import ai.tock.bot.engine.config.UploadedFilesService.getFileContentFromUrl
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.monitoring.logError
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.bot.engine.user.UserPreferences
import ai.tock.shared.Executor
import ai.tock.shared.defaultLocale
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.jackson.mapper
import ai.tock.translator.raw
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.salomonbrys.kodein.instance
import java.time.ZoneOffset
import mu.KotlinLogging
import org.apache.commons.lang3.LocaleUtils

internal class TwitterConnector internal constructor(
    val applicationId: String,
    val accountId: String,
    val baseUrl: String,
    val path: String,
    val client: TwitterClient
) : ConnectorBase(TwitterConnectorProvider.connectorType) {

    private val url = "$baseUrl$path"

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val executor: Executor by injector.instance()

    private val queue: ConnectorQueue = ConnectorQueue(executor)

    override fun loadProfile(callback: ConnectorCallback, userId: PlayerId): UserPreferences {

        try {
            val userProfile = client.user(userId.id)
            logger.debug { "User profile : $userProfile for $userId" }
            return UserPreferences(
                userProfile.screenName,
                "",
                null,
                ZoneOffset.of(userProfile.utcOffset ?: "Z"),
                userProfile.lang?.let {
                    try {
                        LocaleUtils.toLocale(it)
                    } catch (e: Exception) {
                        logger.error(e)
                        null
                    }
                } ?: defaultLocale,
                userProfile.profileImageUrlHttps
            )
        } catch (e: Exception) {
            logger.error(e)
        }
        return UserPreferences()
    }

    /**
     * Registers the connector for the specified controller.
     */
    override fun register(controller: ConnectorController) {
        controller.registerServices(path) { router ->
            logger.info("deploy rest twitter connector services for root path $path ")

            // see https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/guides/securing-webhooks
            router.get(path).handler { context ->
                try {
                    logger.info { "get twitter crc" }

                    val crcToken = context.queryParam("crc_token").first()

                    logger.info { "Twitter crc_token: $crcToken" }
                    val sha256 = client.b64HmacSHA256(crcToken)

                    logger.info { "Twitter CRC response: $sha256" }

                    context.response().end("{\"response_token\":\"sha256=$sha256\"}")
                } catch (e: Throwable) {
                    logger.error(e)
                    context.fail(500)
                }
            }

            // see https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/guides/account-activity-data-objects
            router.post(path).handler { context ->
                val requestTimerData = BotRepository.requestTimer.start("twitter_webhook")
                try {
                    logger.info { "get twitter Message" }
                    val twitterHeader = context.request().getHeader("X-Twitter-Webhooks-Signature")
                    logger.debug { "Twitter signature:  $twitterHeader" }
                    logger.debug { "Twitter headers:  ${context.request().headers().entries()}" }
                    val body = context.body().asString()
                    if (twitterHeader != null && isSignedByTwitter(body, twitterHeader)) {
                        try {
                            logger.debug { "Twitter request input : $body" }
                            val incomingEvent = mapper.readValue<IncomingEvent?>(body)

                            if (incomingEvent == null) {
                                logger.debug { "Unknown event" }
                            } else if (incomingEvent.ignored) {
                                logger.debug { "Ignored event : ${incomingEvent.javaClass.simpleName}" }
                            } else {
                                executor.executeBlocking {
                                    val event = incomingEvent.toEvent(applicationId)
                                    val (threadId, visibility, reply) = if (incomingEvent is TweetIncomingEvent) {
                                        Triple(
                                            incomingEvent.tweets.first().id,
                                            ActionVisibility.PUBLIC,
                                            incomingEvent.tweets.first().inReplyToStatusId != null
                                        )
                                    } else {
                                        Triple(null, ActionVisibility.PRIVATE, false)
                                    }
                                    val callback = TwitterConnectorCallback(
                                        applicationId,
                                        visibility,
                                        threadId,
                                        reply
                                    )
                                    if (event != null) {
                                        controller.handle(event, ConnectorData(callback))
                                    }
                                }
                            }
                        } catch (t: Throwable) {
                            logger.logError(t, requestTimerData)
                        }
                    } else {
                        logger.logError("Not signed by twitter!!! : $twitterHeader \n $body", requestTimerData)
                    }
                } catch (e: Throwable) {
                    logger.logError(e, requestTimerData)
                } finally {
                    try {
                        BotRepository.requestTimer.end(requestTimerData)
                        context.response().end()
                    } catch (e: Throwable) {
                        logger.error(e)
                    }
                }
            }
        }
    }

    /**
     * Unregisters the connector.
     */
    override fun unregister(controller: ConnectorController) {
        super.unregister(controller)
        val existingWebhooks = client.webhooks()
        existingWebhooks.find { webhook: Webhook -> webhook.url == url }?.let {
            client.unregisterWebhook(it.id)
        }
    }

    /**
     * Send an event with this connector for the specified delay.
     *
     * @param event the event to send
     * @param callback the initial connector callback
     * @param delayInMs the optional delay
     */
    override fun send(event: Event, callback: ConnectorCallback, delayInMs: Long) {
        callback as TwitterConnectorCallback
        logger.debug { "event: $event" }
        if (event is Action) {
            if (event.metadata.visibility == ActionVisibility.UNKNOWN) {
                event.metadata.visibility = callback.visibility
            }
            queue.add(event, delayInMs) { action ->
                TwitterMessageConverter.toEvent(action)?.also { message ->
                    when (message) {
                        is OutcomingEvent -> {
                            when (message.event) {
                                is DirectMessageOutcomingEvent -> {
                                    if (message.attachmentData != null) {
                                        sendDirectMessageWithAttachment(
                                            message.attachmentData.mediaCategory,
                                            message.attachmentData.contentType,
                                            message.attachmentData.bytes,
                                            message.event
                                        )
                                    } else {
                                        client.sendDirectMessage(message)
                                    }
                                }
                            }
                        }
                        is Tweet -> {
                            client.sendTweet(message, callback.threadId)
                        }
                        else -> logger.error { "Unknown message to send by twitter : " + message.javaClass }
                    }
                }
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
                applicationId,
                PlayerId(accountId, PlayerType.bot),
                intent.wrappedIntent().name,
                step,
                parameters
            ),
            ConnectorData(
                TwitterConnectorCallback(applicationId, ActionVisibility.PRIVATE, null, false)
            )
        )
    }

    private fun sendDirectMessageWithAttachment(
        mediaCategory: MediaCategory,
        contentType: String,
        bytes: ByteArray,
        event: DirectMessageOutcomingEvent
    ) {
        lateinit var outcomingEvent: OutcomingEvent
        try {
            val attachment = client.createAttachment(mediaCategory, contentType, bytes)
            outcomingEvent = buildDirectMessageOutcomingEventWithAttachment(event, attachment)
        } catch (e: Exception) {
            logger.error { e }
            outcomingEvent = OutcomingEvent(event)
        } finally {
            if (!client.sendDirectMessage(outcomingEvent)) {
                logger.error { "sendDirectMessage with attachment failed" }
            }
        }
    }

    private fun buildDirectMessageOutcomingEventWithAttachment(
        event: DirectMessageOutcomingEvent,
        attachment: Attachment
    ): OutcomingEvent = OutcomingEvent(
        DirectMessageOutcomingEvent(
            MessageCreate(
                target = event.messageCreate.target,
                sourceAppId = event.messageCreate.sourceAppId,
                senderId = event.messageCreate.senderId,
                messageData = MessageData(
                    text = event.messageCreate.messageData.text,
                    entities = event.messageCreate.messageData.entities,
                    ctas = event.messageCreate.messageData.ctas,
                    attachment = attachment,
                    quickReply = event.messageCreate.messageData.quickReply,
                    quickReplyResponse = event.messageCreate.messageData.quickReplyResponse

                )
            )
        )
    )

    private fun isSignedByTwitter(payload: String, twitterSignature: String): Boolean {
        return "sha256=${client.b64HmacSHA256(payload)}" == twitterSignature
    }

    override fun addSuggestions(text: CharSequence, suggestions: List<CharSequence>): BotBus.() -> ConnectorMessage? = {
        directMessageWithOptions(text, *suggestions.map { nlpOption(it) }.toTypedArray())
    }

    override fun toConnectorMessage(message: MediaMessage): BotBus.() -> List<ConnectorMessage> = {
        when (message) {
            is MediaCard -> {
                val f = message.file
                val type = f?.toMediaCategory()
                val content = f?.url?.takeUnless { type == null }?.let { getFileContentFromUrl(it) }
                val title = message.title
                val subTitle = message.subTitle
                when {
                    type != null && content != null -> {
                        listOf(
                            directMessageWithAttachment(
                                title ?: subTitle ?: "".raw,
                                type,
                                UploadedFilesService.guessContentType(f.url),
                                content,
                                *message.actions.filter { it.url != null }.map { nlpOption(it.title) }.toTypedArray()
                            )
                        )
                    }
                    title != null || subTitle != null -> {
                        val firstText = title ?: subTitle!!
                        listOfNotNull(
                            if (message.actions.any { it.url != null }) {
                                directMessageWithButtons(
                                    firstText,
                                    *message.actions.filter { it.url != null }.map { webUrl(it.title, it.url!!) }.toTypedArray()
                                )
                            } else {
                                directMessageWithOptions(
                                    firstText,
                                    *message.actions.map { nlpOption(it.title) }.toTypedArray()
                                )
                            }
                        )
                    }
                    else -> emptyList()
                }
            }
            else -> emptyList()
        }
    }
}

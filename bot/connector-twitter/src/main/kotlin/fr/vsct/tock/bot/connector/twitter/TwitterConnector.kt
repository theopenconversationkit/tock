/*
 * Copyright (C) 2019 VSCT
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

package fr.vsct.tock.bot.connector.twitter

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.salomonbrys.kodein.instance
import fr.vsct.tock.bot.connector.ConnectorBase
import fr.vsct.tock.bot.connector.ConnectorCallback
import fr.vsct.tock.bot.connector.twitter.model.Webhook
import fr.vsct.tock.bot.connector.twitter.model.incoming.IncomingEvent
import fr.vsct.tock.bot.connector.twitter.model.outcoming.DirectMessageOutcomingEvent
import fr.vsct.tock.bot.connector.twitter.model.outcoming.OutcomingEvent
import fr.vsct.tock.bot.engine.BotRepository
import fr.vsct.tock.bot.engine.ConnectorController
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.event.Event
import fr.vsct.tock.bot.engine.monitoring.logError
import fr.vsct.tock.shared.Executor
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.jackson.mapper
import mu.KotlinLogging
import java.time.Duration

class TwitterConnector internal constructor(
        val applicationId: String,
        val baseUrl: String,
        val path: String,
        val client: TwitterClient
) : ConnectorBase(TwitterConnectorProvider.connectorType) {

    private val url = "$baseUrl$path"

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val executor: Executor by injector.instance()

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
                    val twitterHeader = context.request().getHeader("X-Twitter-Webhooks-Signature")
                    logger.debug { "Twitter signature:  $twitterHeader" }
                    logger.debug { "Twitter headers:  ${context.request().headers().entries()}" }
                    val body = context.bodyAsString
                    if (twitterHeader != null && isSignedByTwitter(body, twitterHeader)) {
                        try {
                            logger.debug { "Twitter request input : $body" }
                            val incomingEvent = mapper.readValue<IncomingEvent>(body)

                            if (incomingEvent == null) {
                                logger.debug { "Unsupported twitter event" }
                            } else {
                                logger.info { incomingEvent }
                                executor.executeBlocking {
                                    val event = WebhookActionConverter.toEvent(incomingEvent, applicationId)
                                    if (event != null) {
                                        controller.handle(event)
                                    } else {
                                        logger.logError(
                                                "unable to convert $incomingEvent to event",
                                                requestTimerData
                                        )
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

            executor.executeBlocking(Duration.ofSeconds(5)) {
                if (!alreadyRegistred()) {
                    if (registerWebhook()) {
                        subscribeAccount()
                    }
                } else {
                    subscribeAccount()
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
        existingWebhooks.find({ webhook: Webhook -> webhook.url == url })?.let {
            client.unregisterWebhook(it.id)
        }
    }

    private fun webhook(): Webhook? {
        return client.webhooks().find { it.url == url }
    }

    /**
     * Check if connector endpoint is already registered in Twitter
     *
     * @return boolean true if webhook exists in Twitter
     */
    private fun alreadyRegistred(): Boolean {
        val result = (webhook() != null)
        if (result)
            logger.info { "Twitter webhook already registered $url" }
        return result
    }

    /**
     * Check if a webhook configuration is subscribed to the provided user’s events
     *
     * @return boolean true if webhook is subscribed
     */
    private fun alreadySubcribe(): Boolean {
        val result = client.subscriptions()
        return if (result) {
            logger.info { "Twitter webhook already subscribed to the provided user's event" }
            true
        } else {
            logger.info { "Twitter webhook not subscribed to the provided user's event" }
            false
        }
    }

    /**
     * Register connector endpoint as webhook in Twitter
     *
     * @return boolean true if success
     */
    private fun registerWebhook(): Boolean {
        logger.info { "Twitter webhook register $url" }
        return if (client.registerWebhook(url) != null) {
            logger.info { "Twitter webhook register $url success" }
            true
        } else {
            logger.error { "Twitter webhook register $url failed" }
            false
        }
    }

    /**
     * Subscribe default account (application owner)
     *
     */
    private fun subscribeAccount(): Boolean {
        return if (alreadySubcribe()) {
            true
        } else {
            logger.info { "Twitter subscribe defaultAccount" }
            if (client.subscribe()) {
                logger.info { "Twitter defaultAccount subscription success" }
                true
            } else {
                logger.error { "Twitter webhook register $url failed" }
                false
            }
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
        logger.debug { "event: $event" }
        if (event is Action) {
            val outcomingEvent = TwitterMessageConverter.toOutcomingEvent(event)
            if (outcomingEvent != null) {
                sendMessage(outcomingEvent, delayInMs)
            }
        }
    }

    private fun sendMessage(outcomingEvent: OutcomingEvent, delayInMs: Long) {
        executor.executeBlocking(Duration.ofMillis(delayInMs)) {
            when (outcomingEvent.event) {
                is DirectMessageOutcomingEvent -> {
                    client.sendDirectMessage(outcomingEvent)
                }
            }
        }
    }

    private fun isSignedByTwitter(payload: String, twitterSignature: String): Boolean {
        return "sha256=${client.b64HmacSHA256(payload)}" == twitterSignature
    }


}
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

package ai.tock.bot.connector.whatsapp.cloud

import ai.tock.bot.connector.ConnectorBase
import ai.tock.bot.connector.ConnectorCallback
import ai.tock.bot.connector.ConnectorData
import ai.tock.bot.connector.whatsapp.cloud.model.send.manageTemplate.WhatsAppCloudTemplate
import ai.tock.bot.connector.whatsapp.cloud.model.webhook.Change
import ai.tock.bot.connector.whatsapp.cloud.model.webhook.Entry
import ai.tock.bot.connector.whatsapp.cloud.model.webhook.WebHookEventReceiveMessage
import ai.tock.bot.connector.whatsapp.cloud.model.webhook.message.WhatsAppCloudMessage
import ai.tock.bot.connector.whatsapp.cloud.services.SendActionConverter
import ai.tock.bot.connector.whatsapp.cloud.services.WhatsAppCloudApiService
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.definition.StoryHandlerDefinition
import ai.tock.bot.definition.StoryStep
import ai.tock.bot.engine.BotRepository
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.ActionNotificationType
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.monitoring.logError
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType.bot
import ai.tock.shared.Executor
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.jackson.mapper
import ai.tock.shared.listProperty
import ai.tock.shared.security.RequestFilter
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.salomonbrys.kodein.instance
import java.time.Duration
import mu.KotlinLogging

class WhatsAppConnectorCloudConnector internal constructor(
    val connectorId: String,
    private val phoneNumberId: String,
    private val whatsAppBusinessAccountId: String,
    private val path: String,
    private val token: String,
    private val verifyToken: String?,
    private val mode: String,
    internal val client: WhatsAppCloudApiClient,
    private val requestFilter: RequestFilter

) : ConnectorBase(whatsAppCloudConnectorType) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val whatsAppCloudApiService: WhatsAppCloudApiService = WhatsAppCloudApiService(client)
    private val executor: Executor by injector.instance()

    private val restrictedPhoneNumbers =
        listProperty("tock_whatsapp_cloud_restricted_phone_numbers", emptyList()).toSet().takeIf { it.isNotEmpty() }

    override fun register(controller: ConnectorController) {
        controller.registerServices(path) { router ->
            logger.info("deploy rest whatsapp connector cloud services for root path $path ")

            router.get(path).handler { context ->
                try {
                    val queryParams = context.queryParams()
                    val modeHub = queryParams.get("hub.mode")
                    val verifyTokenMeta = queryParams.get("hub.verify_token")
                    val challenge = queryParams.get("hub.challenge")
                    if (modeHub == mode && verifyToken == verifyTokenMeta) {
                        logger.info("WEBHOOK_VERIFIED")
                        context.response().setStatusCode(200).end(challenge)
                    } else {
                        context.response().end("Invalid verify token")
                    }

                } catch (e: Throwable) {
                    logger.error(e)
                    context.fail(500)
                }
            }

            router.post(path).handler { context ->
                if (!requestFilter.accept(context.request())) {
                    context.response().setStatusCode(403).end()
                    return@handler
                }
                val requestTimerData = BotRepository.requestTimer.start("whatsapp_cloud_webhook")
                try {
                    val body = context.body().asString()
                    logger.info { body }
                    val requestBody = mapper.readValue<WebHookEventReceiveMessage>(body)

                    handleWebHook(requestBody, controller)

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
            router.post("/create_template").handler { context ->
                if (!requestFilter.accept(context.request())) {
                    context.response().setStatusCode(403).end()
                    return@handler
                }
                val requestTimerData = BotRepository.requestTimer.start("whatsapp_cloud_create_template")
                try {
                    val body = context.body().asString()
                    logger.info { body }
                    val requestBody = mapper.readValue<WhatsAppCloudTemplate>(body)

                    whatsAppCloudApiService.sendBuildTemplate(
                        whatsAppBusinessAccountId, token, requestBody
                    )

                    logger.info { "ok" }

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

    private fun handleWebHook(requestBody: WebHookEventReceiveMessage, controller: ConnectorController) {
        requestBody.entry.forEach { entry: Entry ->
            entry.changes.filter {
                it.value.metadata.phoneNumberId == phoneNumberId
            }.forEach { change: Change ->
                change.value.messages.filter {
                    restrictedPhoneNumbers?.contains(it.from) ?: true
                }.forEach { message: WhatsAppCloudMessage ->
                    executor.executeBlocking {
                        val event = WebhookActionConverter.toEvent(message, connectorId)
                        if (event != null) {
                            controller.handle(
                                event,
                                ConnectorData(WhatsAppConnectorCloudCallback(event.applicationId))
                            )
                        } else {
                            logger.warn("unable to convert $message to event")
                        }
                    }
                }
            }
        }
    }

    override fun send(event: Event, callback: ConnectorCallback, delayInMs: Long) {
        if (event is Action) {
            SendActionConverter.toBotMessage(event)
                ?.also {
                    val delay = Duration.ofMillis(delayInMs)
                    executor.executeBlocking(delay) {
                        whatsAppCloudApiService.sendMessage(
                            phoneNumberId, token, it
                        )
                    }
                }
        }
    }

    override fun notify(
        controller: ConnectorController,
        recipientId: PlayerId,
        intent: IntentAware,
        step: StoryStep<out StoryHandlerDefinition>?,
        parameters: Map<String, String>,
        notificationType: ActionNotificationType?,
        errorListener: (Throwable) -> Unit
    ) {
        controller.handle(
            SendChoice(
                recipientId,
                connectorId,
                PlayerId(connectorId, bot),
                intent.wrappedIntent().name,
                step,
                parameters,
            ),
            ConnectorData(
                WhatsAppConnectorCloudCallback(connectorId)
            )
        )
    }
}

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

package ai.tock.bot.api.service

import ai.tock.bot.admin.bot.BotConfiguration
import ai.tock.bot.api.model.UserRequest
import ai.tock.bot.api.model.configuration.ClientConfiguration
import ai.tock.bot.api.model.websocket.RequestData
import ai.tock.bot.api.model.websocket.ResponseData
import ai.tock.bot.api.service.WSHolder.Companion.getHolderIfPresent
import ai.tock.bot.api.service.WSHolder.Companion.setHolder
import ai.tock.bot.engine.WebSocketController
import ai.tock.shared.booleanProperty
import ai.tock.shared.error
import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KotlinLogging

private val oldWebhookBehaviour: Boolean = booleanProperty("tock_api_old_webhook_behaviour", false)

internal class BotApiClientController(
    private val provider: BotApiDefinitionProvider,
    configuration: BotConfiguration,
) {
    private val logger = KotlinLogging.logger {}

    private val apiKey: String = configuration.apiKey
    private val webhookUrl: String? = configuration.webhookUrl

    @Volatile
    private var lastConfiguration: ClientConfiguration? = null

    private val client =
        webhookUrl?.takeUnless { it.isBlank() }?.let {
            try {
                BotApiClient(it)
            } catch (e: Exception) {
                logger.error(e)
                null
            }
        }

    init {
        if (WebSocketController.websocketEnabled) {
            logger.debug { "register $apiKey" }
            WebSocketController.registerAuthorizedKey(apiKey)
            WebSocketController.setReceiveHandler(apiKey) { content: String ->
                try {
                    val response: ResponseData? = mapper.readValue(content)
                    if (response != null) {
                        val conf = response.botConfiguration
                        if (conf == null) {
                            val holder = getHolderIfPresent(response.requestId)
                            if (holder == null) {
                                logger.warn { "unknown request ${response.requestId}" }
                            } else {
                                holder.receive(response)
                            }
                        } else {
                            lastConfiguration = conf
                            provider.updateIfConfigurationChange(conf)
                        }
                    } else {
                        logger.warn { "null response: $content" }
                    }
                } catch (e: Exception) {
                    logger.error(e)
                }
            }
        }
    }

    private fun loadConfiguration(
        conf: ClientConfiguration?,
        handler: (ClientConfiguration?) -> Unit,
    ) {
        logger.debug { "load configuration :$conf" }
        logger.info { "configuration version :${conf?.version}" }
        lastConfiguration = conf
        handler(conf)
    }

    fun configuration(handler: (ClientConfiguration?) -> Unit) {
        client
            ?.takeIf { it.isReachable() }
            ?.send(RequestData(configuration = true))?.apply {
                loadConfiguration(botConfiguration, handler)
            }
            ?: sendWithWebSocket(RequestData(configuration = true), {
                loadConfiguration(it?.botConfiguration, handler)
            })
    }

    fun send(
        userRequest: UserRequest,
        sendResponse: (ResponseData?) -> Unit,
    ) {
        val request = RequestData(userRequest)
        if (client?.isReachable { lastConfiguration = it } == true) {
            sendWithWebhook(request, sendResponse)
        } else {
            sendWithWebSocket(request, sendResponse)
        }
    }

    private fun sendWithWebhook(
        request: RequestData,
        sendResponse: (ResponseData?) -> Unit,
    ) {
        client
            ?.apply {
                if (request.configuration == true || oldWebhookBehaviour || lastConfiguration?.supportSSE != true) {
                    logger.debug { "sse not used for webhook - conf : ${request.configuration}|oldBehaviour: $oldWebhookBehaviour|supportSSE: ${lastConfiguration?.supportSSE}" }
                    send(request).apply {
                        sendResponse(this)
                    }
                } else {
                    val holder = setHolder(request.requestId)
                    sendWithSse(request, lastConfiguration?.version, sendResponse)
                    holder.waitForResponse(sendResponse)
                }
            }
    }

    private fun sendWithWebSocket(
        request: RequestData,
        sendResponse: (ResponseData?) -> Unit = {},
    ) {
        val pushHandler = WebSocketController.getPushHandler(apiKey)
        if (pushHandler != null) {
            val holder = setHolder(request.requestId)
            logger.debug { "send request ${request.requestId}" }
            pushHandler.invoke(mapper.writeValueAsString(request))
            holder.waitForResponse(sendResponse)
        } else {
            if (request.configuration != true) {
                error("no websocket handler for $apiKey and no webhook reachable")
            }
        }
    }
}

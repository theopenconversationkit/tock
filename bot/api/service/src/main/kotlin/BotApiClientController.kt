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
import ai.tock.bot.api.model.configuration.ResponseContextVersion.V2
import ai.tock.bot.api.model.websocket.RequestData
import ai.tock.bot.api.model.websocket.ResponseData
import ai.tock.bot.engine.WebSocketController
import ai.tock.shared.Executor
import ai.tock.shared.booleanProperty
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.jackson.mapper
import ai.tock.shared.longProperty
import ai.tock.shared.provide
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import mu.KotlinLogging
import java.time.Instant
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.SECONDS

private val timeoutInSeconds: Long = longProperty("tock_api_timout_in_s", 10)
private val oldWebhookBehaviour: Boolean = booleanProperty("tock_api_old_webhook_behaviour", false)
private val logger = KotlinLogging.logger {}

private class WSHolder {

    private val response: MutableList<ResponseData> = CopyOnWriteArrayList()
    private val seen: MutableSet<ResponseData> = CopyOnWriteArraySet()

    @Volatile
    private var latch: CountDownLatch = CountDownLatch(1)

    fun receive(response: ResponseData) {
        this.response.add(response)
        latch.countDown()
    }

    @Synchronized
    fun wait(): List<ResponseData> {
        latch.await(timeoutInSeconds, SECONDS)
        val r = response.sortedBy { it.botResponse?.context?.date ?: Instant.now() }
        logger.debug { r }
        if (r.lastOrNull()?.botResponse?.context?.lastResponse == false) {
            latch = CountDownLatch(1)
        }
        return r.filterNot { seen.contains(it) }.apply { seen.addAll(this) }
    }
}

private val wsRepository: Cache<String, WSHolder> =
    CacheBuilder.newBuilder().expireAfterWrite(timeoutInSeconds + 1, SECONDS).build()

internal class BotApiClientController(
    private val provider: BotApiDefinitionProvider,
    configuration: BotConfiguration
) {

    private val logger = KotlinLogging.logger {}

    private val apiKey: String = configuration.apiKey
    private val webhookUrl: String? = configuration.webhookUrl
    private val executor: Executor get() = injector.provide()

    @Volatile
    private var lastConfiguration: ClientConfiguration? = null

    private val client = webhookUrl?.takeUnless { it.isBlank() }?.let {
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
                            val holder = wsRepository.getIfPresent(response.requestId)
                            if (holder == null) {
                                logger.warn { "unknown request ${response.requestId}" }
                            }
                            holder?.receive(response)
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

    private fun loadConfiguration(conf: ClientConfiguration?, handler: (ClientConfiguration?) -> Unit) {
        logger.debug { "load configuration :$conf" }
        logger.info { "configuration version :${conf?.version}" }
        lastConfiguration = conf
        handler(conf)
    }

    fun configuration(handler: (ClientConfiguration?) -> Unit) {
        client?.send(RequestData(configuration = true))?.apply {
            loadConfiguration(botConfiguration, handler)
        }
            ?: sendWithWebSocket(RequestData(configuration = true), {
                loadConfiguration(it?.botConfiguration, handler)
            })
    }

    fun send(userRequest: UserRequest, sendResponse: (ResponseData?) -> Unit) {
        val request = RequestData(userRequest)
        if (client != null) {
            sendWithWebhook(request, sendResponse)
        } else {
            sendWithWebSocket(request, sendResponse) ?: error("no webhook set and no response from websocket")
        }
    }

    private fun sendWithWebhook(request: RequestData, sendResponse: (ResponseData?) -> Unit) {
        client?.apply {
            if (request.configuration == true || oldWebhookBehaviour || lastConfiguration?.version != V2) {
                send(request).apply {
                    sendResponse(this)
                }
            } else {
                sendWithSse(request, sendResponse)
            }
        }
    }

    private fun sendWithWebSocket(request: RequestData, sendResponse: (ResponseData?) -> Unit = {}): Unit {
        val pushHandler = WebSocketController.getPushHandler(apiKey)
        if (pushHandler != null) {
            val holder = WSHolder()
            wsRepository.put(request.requestId, holder)
            logger.debug { "send request ${request.requestId}" }
            pushHandler.invoke(mapper.writeValueAsString(request))
            executor.executeBlocking {
                holder.waitResponse(sendResponse)
            }
        } else {
            if (request.configuration != true) {
                error("no websocket handler for $apiKey")
            }
        }
    }

    private fun WSHolder.waitResponse(sendResponse: (ResponseData?) -> Unit = {}): ResponseData? {
        var response: ResponseData?
        do {
            val responses = wait()
            response = responses.lastOrNull()
            responses.forEach {
                sendResponse(it)
            }
        } while (response?.botResponse?.context?.lastResponse == false)
        return response
    }

}

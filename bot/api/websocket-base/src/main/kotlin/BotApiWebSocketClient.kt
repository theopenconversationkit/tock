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

package ai.tock.bot.api.websocket

import ai.tock.bot.api.client.ClientBotDefinition
import ai.tock.bot.api.client.TockClientBus
import ai.tock.bot.api.client.toConfiguration
import ai.tock.bot.api.model.websocket.RequestData
import ai.tock.bot.api.model.websocket.ResponseData
import ai.tock.shared.Dice
import ai.tock.shared.booleanProperty
import ai.tock.shared.error
import ai.tock.shared.intProperty
import ai.tock.shared.jackson.mapper
import ai.tock.shared.property
import ai.tock.shared.vertx.blocking
import ai.tock.shared.vertx.vertx
import com.fasterxml.jackson.module.kotlin.readValue
import io.vertx.core.http.HttpClient
import io.vertx.core.http.WebSocketClient
import io.vertx.core.http.WebSocketConnectOptions
import mu.KotlinLogging
import java.net.URL
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

/**
 * Starts a bot using the demo server.
 */
fun startWithDemo(botDefinition: ClientBotDefinition) {
    start(botDefinition, "https://demo-bot.tock.ai")
}

/**
 * Starts a client.
 *
 * @param botDefinition the [ClientBotDefinition]
 * @param url the target server url
 */
fun start(
    botDefinition: ClientBotDefinition,
    url: String
) {
    val u = URL(url)
    start(botDefinition, u.port.takeUnless { it == -1 } ?: u.defaultPort, u.host, u.protocol == "https")
}

/**
 * Starts a client.
 *
 * @param botDefinition the [ClientBotDefinition]
 * @param serverPort the server port
 * @param serverHost the server host
 * @param ssl is it ssl ?
 */
fun start(
    botDefinition: ClientBotDefinition,
    serverPort: Int = intProperty("tock_websocket_port", 8080),
    serverHost: String = property("tock_websocket_host", "localhost"),
    ssl: Boolean = booleanProperty("tock_websocket_ssl", false)
) {

    // State to indicate the current client is currently restarting, meaning a new client start is already scheduled
    var websocketIsRestarting = false

    fun restart(client: WebSocketClient, delay: Long) {
        // guard prevent multiples concurrent restart that would lead to multiple client at each restarts
        // As restart is also called from the close handler
        if(websocketIsRestarting) return

        websocketIsRestarting = true
        logger.info { "restart in $delay seconds..." }

        try {
            client.close()
        } catch (e: Exception) {
            logger.error(e)
        }

        vertx.setTimer(TimeUnit.SECONDS.toMillis(delay)) {
            try {
                start(botDefinition, serverPort, serverHost, ssl)
            } catch (e: Exception) {
                logger.error(e)
                restart(client, delay)
            }
        }
    }

    val options = WebSocketConnectOptions().setSsl(ssl).setHost(serverHost).setPort(serverPort)
        .setURI("/${botDefinition.apiKey}".trim())

    logger.info { "start web socket client: ${options.toJson()}" }
    val client = vertx.createWebSocketClient()

    client.connect(options).onSuccess { socket ->
        try {
            // send bot configuration
            val conf = mapper.writeValueAsString(
                ResponseData(Dice.newId(), botConfiguration = botDefinition.toConfiguration())
            )
            logger.debug { "send bot conf: $conf" }
            socket?.writeTextMessage(conf)
            socket
                ?.textMessageHandler { json ->
                    vertx.blocking<String>({
                        logger.debug { "json: $json" }
                        val data: RequestData = mapper.readValue(json)
                        val request = data.botRequest
                        if (request != null) {
                            logger.debug { "handle request by bus" }
                            val bus = TockClientBus(botDefinition, data) { r ->
                                logger.debug { "send bus response" }
                                val response = mapper.writeValueAsString(ResponseData(data.requestId, r))
                                logger.debug { response }
                                if(r.context.lastResponse) {
                                    it.complete(response)
                                } else {
                                    socket.writeTextMessage(response)
                                }
                            }
                            bus.handle()
                        } else if (data.configuration == true) {
                            logger.debug { "send configuration" }
                            it.complete(
                                mapper.writeValueAsString(
                                    ResponseData(data.requestId, botConfiguration = botDefinition.toConfiguration())
                                )
                            )
                        } else {
                            it.fail("invalid request: $json")
                        }
                    }) {
                        if (it.succeeded()) {
                            if (it.result() != null) {
                                socket.writeTextMessage(it.result())
                            } else {
                                logger.error { "empty response for $json" }
                            }
                        } else {
                            val c = it.cause()
                            if (c == null) {
                                logger.error("unknown error for $json : ${it.result()}")
                            } else {
                                logger.error(c)
                            }
                        }
                    }
                }
                ?.exceptionHandler {
                    logger.info("Exception")
                    restart(client, 1)
                }
                ?.closeHandler {
                    logger.info("Closed")
                    restart(client, 1)
                } ?: restart(client, 10).apply { logger.warn { "websocket server not found or unknown key - retry in 10s" } }
        } catch (e: Exception) {
            logger.error(e)
        }
    }
}

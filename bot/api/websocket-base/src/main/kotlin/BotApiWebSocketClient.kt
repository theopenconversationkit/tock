/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
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
    url: String) {
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
    ssl: Boolean = booleanProperty("tock_websocket_ssl", false)) {

    fun restart(client: HttpClient, delay: Long) {
        logger.info("restart...")
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
        .setURI("/${botDefinition.apiKey}")

    logger.info { "start web socket client: ${options.toJson()}" }
    val client = vertx.createHttpClient()

    client.webSocket(options) { context ->
        try {
            val socket = context.result()
            //send bot configuration
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
                                it.complete(response)
                            }
                            bus.handle()
                        } else if (data.configuration == true) {
                            logger.debug { "send configuration " }
                            it.complete(mapper.writeValueAsString(
                                ResponseData(data.requestId, botConfiguration = botDefinition.toConfiguration())
                            ))
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
                    logger.info("Exception, restarting in 1s");
                    restart(client, 1);
                }
                ?.closeHandler {
                    logger.info("Closed, restarting in 1s");
                    restart(client, 1);
                } ?: restart(client, 10).apply { logger.warn { "web socket server not found - retry in 10s" } }
        } catch (e: Exception) {
            logger.error(e)
        }
    }
}



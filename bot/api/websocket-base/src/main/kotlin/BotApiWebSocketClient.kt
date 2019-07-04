package fr.vsct.tock.bot.api.websocket

import com.fasterxml.jackson.module.kotlin.readValue
import fr.vsct.tock.bot.api.client.ClientBotDefinition
import fr.vsct.tock.bot.api.client.TockClientBus
import fr.vsct.tock.bot.api.client.toConfiguration
import fr.vsct.tock.bot.api.model.websocket.RequestData
import fr.vsct.tock.bot.api.model.websocket.ResponseData
import fr.vsct.tock.shared.Dice
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.intProperty
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.property
import fr.vsct.tock.shared.vertx.blocking
import fr.vsct.tock.shared.vertx.vertx
import io.vertx.core.http.HttpClient
import mu.KotlinLogging
import java.util.concurrent.TimeUnit


/*
 * Copyright (C) 2017/2019 VSCT
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

private val logger = KotlinLogging.logger {}

fun start(
    botDefinition: ClientBotDefinition,
    serverPort: Int = intProperty("tock_websocket_port", 8080),
    serverHost: String = property("tock_websocket_host", "localhost")) {

    fun restart(client: HttpClient, delay: Long) {
        client.close()
        vertx.setTimer(TimeUnit.SECONDS.toMillis(delay)) { start(botDefinition) }
    }
    logger.info { "start web socket client" }
    val client = vertx.createHttpClient()
    client.webSocket(serverPort, serverHost, "/${botDefinition.apiKey}") { context ->
        val socket = context.result()
        //send bot configuration
        socket?.writeTextMessage(
            mapper.writeValueAsString(
                ResponseData(Dice.newId(), botConfiguration = botDefinition.toConfiguration())
            )
        )
        socket
            ?.textMessageHandler { json ->
                vertx.blocking<String>({
                    logger.debug { json }
                    val data: RequestData = mapper.readValue(json)
                    val request = data.botRequest
                    if (request != null) {
                        val bus = TockClientBus(botDefinition, data) { r ->
                            val response = mapper.writeValueAsString(ResponseData(data.requestId, r))
                            logger.debug { response }
                            it.complete(response)
                        }
                        bus.handle()
                    } else if (data.configuration == true) {
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
                logger.info("Closed, restarting in 10 seconds");
                restart(client, 5);
            }
            ?.closeHandler {
                logger.info("Closed, restarting in 10 seconds");
                restart(client, 10);
            } ?: restart(client, 10).apply { logger.warn { "web socket server not found - retry" } }
    }
}



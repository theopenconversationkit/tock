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

package fr.vsct.tock.bot.connector.rocketchat

import chat.rocket.common.model.Token
import chat.rocket.common.util.PlatformLogger
import chat.rocket.core.RocketChatClient
import chat.rocket.core.TokenRepository
import chat.rocket.core.internal.realtime.socket.connect
import chat.rocket.core.internal.realtime.socket.model.State
import chat.rocket.core.internal.realtime.subscribeRooms
import chat.rocket.core.internal.rest.getInfo
import chat.rocket.core.internal.rest.joinChat
import chat.rocket.core.internal.rest.login
import chat.rocket.core.internal.rest.sendMessage
import chat.rocket.core.model.Room
import fr.vsct.tock.shared.Dice
import fr.vsct.tock.shared.error
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import mu.KotlinLogging
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 *
 */
internal class RocketChatClient(
    private val targetUrl: String,
    val login: String,
    private val password: String
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private class SimpleTokenRepository : TokenRepository {
        private var savedToken: Token? = null
        override fun save(url: String, token: Token) {
            savedToken = token
        }

        override fun get(url: String): Token? {
            return savedToken
        }
    }

    private val client: RocketChatClient by lazy {
        val logger = object : PlatformLogger {
            override fun debug(s: String) {
                logger.debug(s)
            }

            override fun info(s: String) {
                logger.info(s)
            }

            override fun warn(s: String) {
                logger.warn(s)
            }
        }

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        RocketChatClient.create {
            httpClient = okHttpClient
            restUrl = targetUrl
            userAgent = "Rocket.Chat.Kotlin.SDK"
            tokenRepository = SimpleTokenRepository()
            platformLogger = logger
        }
    }

    fun join(roomId: String, listener: (Room) -> Unit) {
        val job = launch(CommonPool) {
            try {
                logger.debug { "Try to connect $login" }
                val token = client.login(login, password)
                logger.debug { "Token: userId = ${token.userId} - authToken = ${token.authToken}" }

                launch {
                    val statusChannel = Channel<State>()
                    client.addStateChannel(statusChannel)
                    for (status in statusChannel) {
                        logger.debug("Changing status to: $status")
                        when (status) {
                            is State.Authenticating -> {
                                logger.debug("Authenticating")
                            }
                            is State.Connected -> {
                                logger.debug("Connected")
                                client.subscribeRooms { _, _ -> }
                            }
                        }
                    }
                    logger.debug("Done on statusChannel")
                }
                launch {
                    for (room in client.roomsChannel) {
                        listener.invoke(room.data)
                    }
                }

                client.connect()

                client.joinChat(roomId)
            } catch (e: Exception) {
                logger.error(e)
            }
        }

        runBlocking {
            try {
                job.join()
            } catch (e: Exception) {
                logger.error(e)
            }
        }
    }

    fun send(roomId: String, message: String) {
        runBlocking {
            try {
                client.sendMessage(
                    roomId = roomId,
                    messageId = Dice.newId(),
                    message = message,
                    alias = "Tock bot",
                    emoji = ":smirk:",
                    avatar = "https://images.discordapp.net/avatars/348187123536494592/32c74035bac6fb7636c12d51130e846f.png?size=64"
                )
            } catch (e: Exception) {
                logger.error(e)
            }
        }
    }
}
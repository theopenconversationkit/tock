/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
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

package ai.tock.bot.connector.web

import ai.tock.bot.connector.ConnectorBase
import ai.tock.bot.connector.ConnectorCallback
import ai.tock.bot.connector.ConnectorData
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.media.MediaCard
import ai.tock.bot.connector.media.MediaCarousel
import ai.tock.bot.connector.media.MediaFile
import ai.tock.bot.connector.media.MediaMessage
import ai.tock.bot.connector.web.channel.ChannelMongoDAO
import ai.tock.bot.connector.web.channel.Channels
import ai.tock.bot.connector.web.send.UrlButton
import ai.tock.bot.connector.web.send.WebCard
import ai.tock.bot.connector.web.send.WebCarousel
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.BotRepository
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.UserPreferences
import ai.tock.shared.Executor
import ai.tock.shared.booleanProperty
import ai.tock.shared.injector
import ai.tock.shared.jackson.mapper
import ai.tock.shared.longProperty
import ai.tock.shared.provide
import ai.tock.shared.vertx.vertx
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import com.fasterxml.jackson.module.kotlin.readValue
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.CorsHandler
import mu.KotlinLogging
import java.time.Duration

internal const val WEB_CONNECTOR_ID = "web"

/**
 * The web (REST) connector type.
 */
val webConnectorType = ConnectorType(WEB_CONNECTOR_ID)

private val sseEnabled = booleanProperty("tock_web_sse", false)
private val sseKeepaliveDelay = longProperty("tock_web_sse_keepalive_delay", 10)

class WebConnector internal constructor(
    val applicationId: String,
    val path: String
) : ConnectorBase(webConnectorType) {

    companion object {
        private val logger = KotlinLogging.logger {}
        private val webMapper = mapper.copy().registerModule(
            SimpleModule().apply {
                //fallback for serializing CharSequence
                addSerializer(CharSequence::class.java, ToStringSerializer())
            }
        )
    }

    private val executor: Executor get() = injector.provide()

    private val channels by lazy { Channels(ChannelMongoDAO) }

    override fun register(controller: ConnectorController) {

        controller.registerServices(path) { router ->
            logger.debug("deploy web connector services for root path $path ")

            router.route(path)
                .handler(
                    CorsHandler.create("*")
                        .allowedMethod(HttpMethod.POST)
                        .run {
                            if (sseEnabled) allowedMethod(HttpMethod.GET) else this
                        }
                        .allowedHeader("Access-Control-Allow-Origin")
                        .allowedHeader("Content-Type")
                        .allowedHeader("X-Requested-With")
                )
            if (sseEnabled) {
                router.route(path + "/sse")
                    .handler { context ->
                        try {
                            val userId = context.queryParams()["userId"]
                            val response = context.response()
                            response.isChunked = true
                            response.headers().add("Content-Type", "text/event-stream;charset=UTF-8")
                            response.headers().add("Connection", "keep-alive")
                            response.headers().add("Cache-Control", "no-cache")
                            val timerId = vertx.setPeriodic(Duration.ofSeconds(sseKeepaliveDelay).toMillis()) {
                                response.write("event: ping\n")
                                response.write("data: 1\n\n")
                            }
                            val channelId = channels.register(applicationId, userId) { webConnectorResponse ->
                                response.write("event: message\n")
                                response.write("data: ${webMapper.writeValueAsString(webConnectorResponse)}\n\n")
                            }
                            response.closeHandler {
                                vertx.cancelTimer(timerId)
                                channels.unregister(channelId)
                            }
                        } catch (t: Throwable) {
                            context.fail(t)
                        }
                    }
            }
            router.post(path)
                .handler { context ->
                    try {
                        executor.executeBlocking {
                            handleRequest(controller, context, context.bodyAsString)
                        }
                    } catch (e: Throwable) {
                        context.fail(e)
                    }
                }
        }
    }

    private fun handleRequest(
        controller: ConnectorController,
        context: RoutingContext,
        body: String
    ) {
        val timerData = BotRepository.requestTimer.start("web_webhook")
        try {
            logger.debug { "Web request input : $body" }
            val request: WebConnectorRequest = mapper.readValue(body)
            val callback = WebConnectorCallback(applicationId = applicationId, locale = request.locale, context = context, webMapper = webMapper)
            controller.handle(request.toEvent(applicationId), ConnectorData(callback))
        } catch (t: Throwable) {
            BotRepository.requestTimer.throwable(t, timerData)
            context.fail(t)
        } finally {
            BotRepository.requestTimer.end(timerData)
        }
    }

    override fun send(event: Event, callback: ConnectorCallback, delayInMs: Long) {
        val c = callback as? WebConnectorCallback
        c?.addAction(event)
        if (event is Action) {
            if (sseEnabled) {
                channels.send(event)
            }
            if (event.metadata.lastAnswer) {
                c?.sendResponse()
            }
        } else {
            logger.trace { "unsupported event: $event" }
        }
    }

    override fun loadProfile(callback: ConnectorCallback, userId: PlayerId): UserPreferences {
        callback as WebConnectorCallback
        return UserPreferences().apply {
            locale = callback.locale
        }
    }

    override fun addSuggestions(text: CharSequence, suggestions: List<CharSequence>): BotBus.() -> ConnectorMessage? =
        { WebMessage(text.toString(), suggestions.map { webPostbackButton(it) }) }

    override fun addSuggestions(message: ConnectorMessage, suggestions: List<CharSequence>): BotBus.() -> ConnectorMessage? = {
        (message as? WebMessage)?.takeIf { it.buttons.isEmpty() }?.let {
            it.copy(buttons = suggestions.map { webPostbackButton(it) })
        } ?: message
    }

    override fun toConnectorMessage(message: MediaMessage): BotBus.() -> List<ConnectorMessage> = {
        listOfNotNull(
            when (message) {
                is MediaCard -> {
                    WebMessage(card = WebCard(
                        title = message.title,
                        subTitle = message.subTitle,
                        file = message.file?.url?.let { MediaFile(message.file?.url as String, message.file?.name as String) },
                        buttons = message.actions.map { UrlButton(it.title.toString(), it.url.toString()) }
                    ))
                }
                is MediaCarousel -> {
                    WebMessage(carousel = WebCarousel(message.cards.map { mediaCard ->
                        WebCard(
                            title = mediaCard.title,
                            subTitle = mediaCard.subTitle,
                            file = mediaCard.file?.url?.let { MediaFile(mediaCard.file?.url as String, mediaCard.file?.name as String) },
                            buttons = mediaCard.actions.map { button -> UrlButton(button.title.toString(), button.url.toString()) }
                        )
                    }))
                }
                else -> null
            }
        )
    }
}
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

package ai.tock.bot.connector.web

import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.web.send.WebCarousel
import ai.tock.bot.engine.action.SendSentence
import com.google.common.cache.CacheBuilder
import io.vertx.core.MultiMap
import io.vertx.core.http.Cookie
import io.vertx.core.http.HttpServerRequest
import mu.KotlinLogging
import java.util.concurrent.TimeUnit

object WebRequestInfosByEvent {
    private val cache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build<String, WebRequestInfos>()

    internal fun put(
        eventId: String,
        webRequestInfos: WebRequestInfos,
    ) = cache.put(eventId, webRequestInfos)

    internal fun invalidate(eventId: String) = cache.invalidate(eventId)

    fun get(eventId: String): WebRequestInfos? = cache.getIfPresent(eventId)

    internal fun getOrPut(eventId: String): WebRequestInfos = get(eventId) ?: (WebRequestInfos().apply { put(eventId, this) })
}

data class WebRequestInfos(
    private val headers: MultiMap = MultiMap.caseInsensitiveMultiMap(),
    private val cookies: Set<Cookie> = emptySet(),
    @Volatile
    private var streamedResponse: SendSentence? = null,
) {
    internal constructor(request: HttpServerRequest) : this(request.headers(), request.cookies())

    fun firstHeader(name: String): String? = headers.get(name)

    fun headers(name: String): List<String> = headers.getAll(name) ?: emptyList()

    fun firstCookie(name: String): String? = cookies.firstOrNull { it.name == name }?.value

    internal fun addStreamedResponse(
        response: SendSentence,
        connectorType: ConnectorType,
    ): SendSentence {
        val s =
            streamedResponse?.run {
                val originalMessage = message(connectorType) as? WebMessage
                if (originalMessage != null) {
                    val newMessage = response.message(connectorType) as? WebMessage
                    if (newMessage == null) {
                        logger.warn { "no custom message in streamed message - but the previous message is custom - ignore" }
                        return this
                    }

                    changeConnectorMessage(
                        WebMessage(
                            text = (originalMessage.text ?: "") + (newMessage.text ?: ""),
                            buttons = originalMessage.buttons + newMessage.buttons,
                            card =
                                originalMessage.card?.let {
                                    it.copy(
                                        title = (it.title?.toString() ?: "") + (newMessage.card?.title?.toString() ?: ""),
                                        subTitle = (it.subTitle?.toString() ?: "") + (newMessage.card?.subTitle?.toString() ?: ""),
                                        file = newMessage.card?.file ?: it.file,
                                        buttons = it.buttons + (newMessage.card?.buttons ?: emptyList()),
                                    )
                                } ?: newMessage.card,
                            carousel = ((originalMessage.carousel?.cards ?: emptyList()) + (newMessage.carousel?.cards ?: emptyList())).takeUnless { it.isEmpty() }?.let { WebCarousel(it) },
                            widget = newMessage.widget ?: originalMessage.widget,
                            image = newMessage.image ?: originalMessage.image,
                            version = newMessage.version,
                            deepLink = newMessage.deepLink ?: originalMessage.deepLink,
                            footnotes = originalMessage.footnotes + newMessage.footnotes,
                        ),
                    )
                    this
                } else {
                    withText((text?.toString() ?: "") + (response.text?.toString() ?: ""))
                }
            } ?: response
        streamedResponse = s
        return s
    }

    internal fun clearStreamedResponse() {
        streamedResponse = null
    }
}

private val logger = KotlinLogging.logger {}

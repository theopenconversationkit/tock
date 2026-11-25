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
import ai.tock.shared.http.HttpRequest
import com.google.common.cache.CacheBuilder
import io.vertx.core.MultiMap
import io.vertx.core.http.Cookie
import io.vertx.core.http.HttpServerRequest
import mu.KotlinLogging
import java.util.TreeMap
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

/**
 * The headers and the cookies of a web request, and the response being streamed.
 *
 * The values are stored without the Vert.x types: the constructors and [copy] that take them are the former API.
 */
class WebRequestInfos private constructor(
    headers: Map<String, List<String>>,
    private val cookies: Map<String, String>,
    @Volatile
    private var streamedResponse: SendSentence?,
    @Suppress("UNUSED_PARAMETER") marker: Unit,
) {
    private val headers: Map<String, List<String>> = TreeMap<String, List<String>>(String.CASE_INSENSITIVE_ORDER).apply { putAll(headers) }

    constructor() : this(emptyMap(), emptyMap(), null, Unit)

    constructor(
        headers: MultiMap = MultiMap.caseInsensitiveMultiMap(),
        cookies: Set<Cookie> = emptySet(),
        streamedResponse: SendSentence? = null,
    ) : this(
        headers.names().associateWith { headers.getAll(it) },
        cookies.associate { it.name to it.value },
        streamedResponse,
        Unit,
    )

    internal constructor(request: HttpServerRequest) : this(request.headers(), request.cookies())

    internal companion object {
        /** The infos of a request of a server independent route. */
        fun of(request: HttpRequest): WebRequestInfos =
            WebRequestInfos(
                request.headers,
                request.headers["Cookie"]
                    .orEmpty()
                    .flatMap { it.split(';') }
                    .map { it.trim() }
                    .filter { it.contains('=') }
                    .associate { it.substringBefore('=') to it.substringAfter('=') },
                null,
                Unit,
            )
    }

    fun firstHeader(name: String): String? = headers[name]?.firstOrNull()

    fun headers(name: String): List<String> = headers[name] ?: emptyList()

    fun firstCookie(name: String): String? = cookies[name]

    /** The former copy of the data class. */
    fun copy(
        headers: MultiMap = MultiMap.caseInsensitiveMultiMap().apply { this@WebRequestInfos.headers.forEach { (name, values) -> add(name, values) } },
        cookies: Set<Cookie> = this.cookies.map { (name, value) -> Cookie.cookie(name, value) }.toSet(),
        streamedResponse: SendSentence? = this.streamedResponse,
    ): WebRequestInfos = WebRequestInfos(headers, cookies, streamedResponse)

    override fun equals(other: Any?): Boolean = other is WebRequestInfos && headers == other.headers && cookies == other.cookies && streamedResponse == other.streamedResponse

    override fun hashCode(): Int = (headers.hashCode() * 31 + cookies.hashCode()) * 31 + streamedResponse.hashCode()

    override fun toString(): String = "WebRequestInfos(headers=$headers, cookies=$cookies, streamedResponse=$streamedResponse)"

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

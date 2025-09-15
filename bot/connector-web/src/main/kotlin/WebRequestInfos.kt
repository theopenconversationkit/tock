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

import com.google.common.cache.CacheBuilder
import io.vertx.core.MultiMap
import io.vertx.core.http.Cookie
import io.vertx.core.http.HttpServerRequest
import java.util.concurrent.TimeUnit

object WebRequestInfosByEvent {
    private val cache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build<String, WebRequestInfos>()

    internal fun put(eventId: String, webRequestInfos: WebRequestInfos) = cache.put(eventId, webRequestInfos)
    internal fun invalidate(eventId: String) = cache.invalidate(eventId)
    fun get(eventId: String): WebRequestInfos? = cache.getIfPresent(eventId)
}

data class WebRequestInfos(
    private val headers: MultiMap,
    private val cookies: Set<Cookie>,
) {
    internal constructor(request: HttpServerRequest) : this(request.headers(), request.cookies())

    fun firstHeader(name: String): String? = headers.get(name)
    fun headers(name: String): List<String> = headers.getAll(name) ?: emptyList()
    fun firstCookie(name: String): String? = cookies.firstOrNull { it.name == name }?.value
}

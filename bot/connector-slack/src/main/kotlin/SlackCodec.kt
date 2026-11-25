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

package ai.tock.bot.connector.slack

import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

private val logger = KotlinLogging.logger {}

internal fun RoutingContext.convertUrlEncodedStringToJson(): String = parseJson(body().asString())

/**
 * Converts the form-urlencoded body of a Slack outgoing webhook (`tock_slack_old_api_style`) to JSON.
 * Each pair is split on its first `=` before its key and value are decoded, so that a decoded value
 * may contain `&` or `=`; a key without `=` gets an empty value.
 */
internal fun parseJson(body: String): String {
    logger.trace { "unparsed body from slack: $body" }
    val jsonObject = JsonObject()
    body.split("&").filter { it.isNotEmpty() }.forEach { pair ->
        val separator = pair.indexOf('=')
        val key = if (separator < 0) pair else pair.substring(0, separator)
        val value = if (separator < 0) "" else pair.substring(separator + 1)
        jsonObject.put(decode(key), decode(value))
    }
    return jsonObject.toString()
}

private fun decode(value: String): String = URLDecoder.decode(value, StandardCharsets.UTF_8)

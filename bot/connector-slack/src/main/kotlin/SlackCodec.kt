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

private val logger = KotlinLogging.logger {}

internal fun RoutingContext.convertUrlEncodedStringToJson(): String {
    return parseJson(body().asString())
}

internal fun parseJson(body: String): String {
    val urlEncodedString = URLDecoder.decode(body, "UTF-8")
    logger.trace { "unparsed body from slack: $urlEncodedString" }
    val jsonObject = JsonObject()
    urlEncodedString.split("&").forEach { keyValue ->
        val keyValueList = keyValue.split("=")
        val key = keyValueList.first()
        val value = keyValueList[1]
        jsonObject.put(key, value)
    }
    return jsonObject.toString()
}

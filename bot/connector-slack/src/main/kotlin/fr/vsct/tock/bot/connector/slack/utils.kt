package fr.vsct.tock.bot.connector.slack


import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging
import java.net.URLDecoder

private val logger = KotlinLogging.logger {}


internal fun RoutingContext.convertUrlEncodedStringToJson(): String {
    val urlEncodedString = URLDecoder.decode(this.bodyAsString, "UTF-8")
    logger.debug { "unparsed body from slack: $urlEncodedString" }
    val jsonObject = JsonObject()
    urlEncodedString.split("&").forEach { keyValue ->
        val keyValueList = keyValue.split("=")
        val key = keyValueList.first()
        val value = keyValueList[1]
        jsonObject.put(key, value)
    }
    return jsonObject.toString()
}
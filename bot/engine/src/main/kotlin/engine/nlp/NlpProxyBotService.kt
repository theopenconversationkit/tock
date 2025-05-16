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

package ai.tock.bot.engine.nlp

import ai.tock.shared.error
import ai.tock.shared.property
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpMethod.GET
import io.vertx.core.http.HttpMethod.POST
import io.vertx.core.http.RequestOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import java.net.URL
import mu.KLogger
import mu.KotlinLogging

/**
 * Expose NLP API to BOT app
 *
 */
internal object NlpProxyBotService {

    private val logger: KLogger = KotlinLogging.logger {}
    private val tockNlpProxyOnBotPath = property("tock_nlp_proxy_on_bot_path", "/_proxy_nlp")
    private val tockNlpServiceHost: String
    private val tockNlpServicePort: Int
    private val tockNlpServiceSsl: Boolean
    private val nonForwardedHeaders = setOf(
        "Accept-Encoding",
        "Host",
        "Via",
        "X-Forwarded-For",
        "X-Forwarded-Host",
        "X-Forwarded-Port",
        "X-Forwarded-Proto",
        "X-Forwarded-Server",
    )

    init {
        val tockNlpServiceUrl = URL(System.getenv("tock_nlp_service_url") ?: "http://localhost:8888")
        tockNlpServiceHost = System.getenv("tock_nlp_service_host") ?: tockNlpServiceUrl.host
        tockNlpServicePort = System.getenv("tock_nlp_service_port")?.toInt() ?: tockNlpServiceUrl.port
        tockNlpServiceSsl = (System.getenv("tock_nlp_service_SSL") ?: tockNlpServiceUrl.protocol) == "https"
    }

    fun configure(vertx: Vertx): (Router) -> Unit {
        return { router ->
            router.post("$tockNlpProxyOnBotPath*").handler { context ->
                httpProxyToNlp(context, vertx, POST)
            }
            router.get("$tockNlpProxyOnBotPath*").handler { context ->
                httpProxyToNlp(context, vertx, GET)
            }
        }
    }

    private fun httpProxyToNlp(
        context: RoutingContext,
        vertx: Vertx,
        httpMethod: HttpMethod
    ) {
        try {
            val uri = context.request().uri().substringAfter(tockNlpProxyOnBotPath)
            val client: HttpClient = vertx.createHttpClient(HttpClientOptions().apply { isKeepAlive = false })
            val options = RequestOptions()
                .setHost(tockNlpServiceHost)
                .setPort(tockNlpServicePort)
                .setSsl(tockNlpServiceSsl)
                .setURI(uri)
                .setMethod(httpMethod)
                .apply {
                    context.response().headers().forEach { (key, value) ->
                        if (key !in nonForwardedHeaders) addHeader(key, value)
                    }
                }
            client.request(options).flatMap {
                if (httpMethod == POST) {
                    it.send(context.body().buffer())
                } else {
                    it.send()
                }
            }.flatMap { nlpResponse ->
                context.response().isChunked = true
                val resStatusCode = nlpResponse.statusCode()
                if (resStatusCode != 200 && resStatusCode != 201) {
                    logger.warn { "target server status code error : $resStatusCode" }
                }
                context.response().statusCode = resStatusCode
                context.response().headers().setAll(nlpResponse.headers())
                nlpResponse.pipeTo(context.response())
            }.onFailure { t -> logger.error(t) }
        } catch (e: Exception) {
            logger.error(e)
            context.fail(500)
        }
    }
}

package fr.vsct.tock.bot.engine.nlp

import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.property
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpMethod.GET
import io.vertx.core.http.HttpMethod.POST
import io.vertx.core.http.RequestOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import mu.KLogger
import mu.KotlinLogging
import java.net.URL

/**
 * Expose NLP API to BOT app
 *
 */
object NlpProxyBotListener {

    val logger: KLogger = KotlinLogging.logger {}
    private val tockNlpProxyOnBotPath = property("tock_nlp_proxy_on_bot_path", "/_proxy_nlp")
    private val tockNlpServiceHost: String
    private val tockNlpServicePort: Int
    private val tockNlpServiceSsl: Boolean

    init {
        val tocNlpServiceUrl = URL(System.getenv("tock_nlp_service_url") ?: "http://localhost:8888")
        tockNlpServiceHost = System.getenv("tock_nlp_service_host") ?: tocNlpServiceUrl.host
        tockNlpServicePort = System.getenv("tock_nlp_service_port")?.toInt() ?: tocNlpServiceUrl.port
        tockNlpServiceSsl = System.getenv("tock_nlp_service_SSL") ?: tocNlpServiceUrl.protocol == "https"
    }

    fun configure(vertx: Vertx?): (Router) -> Unit {
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
        vertx: Vertx?,
        httpMethod: HttpMethod
    ) {
        try {
            val uri = context.request().uri().substringAfter(tockNlpProxyOnBotPath)
            val client: HttpClient = vertx!!.createHttpClient(HttpClientOptions())
            val options = RequestOptions()
                .setHost(tockNlpServiceHost)
                .setPort(tockNlpServicePort)
                .setSsl(tockNlpServiceSsl)
                .setURI(uri)
            val cReq = client.request(
                httpMethod,
                options
            ) { cRes ->
                try {
                    context.response().isChunked = true
                    val resStatusCode = cRes.statusCode()
                    if (resStatusCode != 200 && resStatusCode != 201) {
                        logger.warn { "target server status code error : $resStatusCode" }
                    }
                    context.response().statusCode = resStatusCode
                    context.response().headers().setAll(cRes.headers())
                    cRes.handler { data ->
                        try {
                            context.response().write(data)
                        } catch (e: Throwable) {
                            logger.error(e)
                        }
                    }
                    cRes.endHandler {
                        try {
                            context.response().end()
                        } catch (e: Throwable) {
                            logger.error(e)
                        }
                    }
                } catch (e: Throwable) {
                    logger.error(e)
                }
            }

            cReq.headers().setAll(
                context
                    .response().headers()
                    .remove("Host")
                    .remove("Via")
                    .remove("X-Forwarded-For")
                    .remove("X-Forwarded-Port")
                    .remove("X-Forwarded-Proto")
                    .remove("X-Forwarded-Host")
                    .remove("X-Forwarded-Server")
                    .remove("Accept-Encoding")
            )

            cReq.end(context.body)
        } catch (e: Exception) {
            logger.error(e)
            context.fail(500)
        }
    }

}
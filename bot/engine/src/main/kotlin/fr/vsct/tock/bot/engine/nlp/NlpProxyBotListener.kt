package fr.vsct.tock.bot.engine.nlp

import com.fasterxml.jackson.module.kotlin.readValue
import fr.vsct.tock.nlp.api.client.NlpClient
import fr.vsct.tock.nlp.api.client.model.NlpQuery
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.provide
import fr.vsct.tock.shared.vertx.blocking
import io.vertx.ext.web.Router
import mu.KLogger
import mu.KotlinLogging

/**
 * Expose NLP API to BOT app
 *
 */
object NlpProxyBotListener {

    private val nlpClient: NlpClient get() = injector.provide()
    val logger: KLogger = KotlinLogging.logger {}

    fun configure(): (Router) -> Unit {
        return { router ->
            router.post("/_nlp").blocking { context ->
                try {
                    val query = mapper.readValue<NlpQuery>(context.bodyAsString)
                    val result = nlpClient.parse(query)
                    context.response().end(mapper.writeValueAsString(result))
                } catch (e: Exception) {
                    logger.error(e)
                    context.fail(500)
                }
            }
        }
    }

}
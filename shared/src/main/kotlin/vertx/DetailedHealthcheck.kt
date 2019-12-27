package ai.tock.shared.vertx

import ai.tock.shared.error
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.vertx.ext.web.RoutingContext
import mu.KLogger
import mu.KotlinLogging

/**
 * Used to construct Healthcheck json result.
 *
 * @property id task identifier (ex: nlp_service)
 * @property status indicate the task status: "KO" or "OK"
 */
internal data class HealthcheckTaskResult(
    val id: String,
    val status: String
)

/**
 * Used to construct JSON
 * @property results list of task results
 */
internal data class DetailedHealthcheckResults(
    val results: List<HealthcheckTaskResult>
)

/**
 * Return an HTTP handler which calls lambdas and construct the HTTP response
 * @params tasks list of lambda and resource's name that indicate the health of a resource
 * @params selfCheck lambda that indicate the service health
 */
fun detailedHealthcheck(
    tasks: List<Pair<String, () -> Boolean>> = listOf(),
    selfCheck: () -> Boolean = { true }
): (RoutingContext) -> Unit {
    val mapper = jacksonObjectMapper()
    val logger: KLogger = KotlinLogging.logger {}

    return {
        val response = it.response()
        val results = mutableListOf<HealthcheckTaskResult>()
        try {
            if (!selfCheck()) {
                throw Throwable("health: not passing selfCheck")
            }
            for (task in tasks) {
                // invoke task and create result from its return value
                val result = HealthcheckTaskResult(
                    task.first,
                    if (task.second.invoke()) "OK" else "KO"
                )
                results.add(result)
            }
            val body = mapper.writeValueAsString(DetailedHealthcheckResults(results))
            response
                .setStatusCode(207) // 207: MULTI-STATUS https://httpstatuses.com/207
                .end(body)
        } catch (t: Throwable) {
            logger.error(t)
            response.setStatusCode(503).end()
        }
    }
}
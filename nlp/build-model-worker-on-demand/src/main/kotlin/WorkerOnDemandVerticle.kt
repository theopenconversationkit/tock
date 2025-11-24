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

package ai.tock.nlp.build.ondemand

import ai.tock.shared.defaultZoneId
import io.vertx.core.AbstractVerticle
import io.vertx.core.Handler
import mu.KotlinLogging
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

@Suppress("ktlint:standard:multiline-expression-wrapping")
class WorkerOnDemandVerticle(
    private val workerOnDemandType: WorkerOnDemandType,
    private val buildType: String,
    private val delayBetweenJob: Long,
    private val timeFrame: List<Int>,
) : AbstractVerticle() {
    companion object {
        private val logger = KotlinLogging.logger {}

        private const val PREFIX = "tock_worker_ondemand"
        private const val BUILD_TYPE_ARG = "TOCK_BUILD_TYPE"
        private const val BUILD_WORKER_MODE_ENV = "tock_build_worker_mode"
        private const val BUILD_WORKER_VERTICLE_ENABLED_ENV = "tock_build_worker_verticle_enabled"
        private const val DEFAULT_WORKER_MODE = "COMMAND_LINE"
    }

    private var handler: Handler<Long>? = null

    private var workerOnDemand: WorkerOnDemand? = null

    override fun start() {
        logger.info("Starting WorkerOnDemandVerticle for $buildType ...")

        workerOnDemand = WorkerOnDemandProvider.provide(
            type = workerOnDemandType,
            properties = workerProperties(),
        )?.apply {
            logger.info { "WorkerOnDemand ${this@WorkerOnDemandVerticle.javaClass.simpleName} loaded for $buildType" }
            handler = Handler {
                if (ZonedDateTime.now(defaultZoneId).isInTimeFrame()) {
                    start {
                        // Schedule the next execution
                        logger.info { "Next Job ${this@WorkerOnDemandVerticle.javaClass.simpleName} for $buildType is scheduled in $delayBetweenJob minutes" }
                        vertx.setTimer(TimeUnit.MINUTES.toMillis(delayBetweenJob), handler)
                    }
                } else {
                    logger.debug { "Job ${this@WorkerOnDemandVerticle.javaClass.simpleName} for $buildType waiting timeframe $timeFrame" }
                    vertx.setTimer(TimeUnit.MINUTES.toMillis(1), handler)
                }
            }

            // Schedule the first execution
            vertx.setTimer(TimeUnit.SECONDS.toMillis(1), handler)
        } ?: throw UnknownWorkerOnDemandTypeException("Unabled to load WorkerOnDemand with type '$workerOnDemandType'")
    }

    fun name(): String = "worker-on-demand-$buildType"

    fun isLoaded(): Boolean = workerOnDemand != null

    private fun ZonedDateTime.isInTimeFrame(): Boolean = (hour >= timeFrame[0] && hour <= timeFrame[1] && minute % 1 == 0)

    private fun workerProperties(): WorkerProperties {
        return (System.getProperties() + System.getenv())
            .filterKeys { it.toString().startsWith(PREFIX) }
            .entries.associate {
                it.key.toString()
                    .replace(PREFIX, "tock")
                    .replace("tock_JAVA_ARGS", "JAVA_ARGS") to it.value.toString()
            } + mapOf(
            BUILD_WORKER_MODE_ENV to DEFAULT_WORKER_MODE,
            BUILD_WORKER_VERTICLE_ENABLED_ENV to "false",
        ) + mapOf(
            BUILD_TYPE_ARG to buildType,
        )
    }
}

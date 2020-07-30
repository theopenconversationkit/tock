/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.nlp.build.ondemand

import io.vertx.core.AbstractVerticle
import io.vertx.core.Handler
import mu.KotlinLogging
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class WorkerOnDemandVerticle(
    private val workerOnDemandType: WorkerOnDemandType,
    private val buildType: String,
    private val delayBetweenJob: Long,
    private val timeFrame: List<Int>
) : AbstractVerticle() {

    private val logger = KotlinLogging.logger {}

    private var handler: Handler<Long>? = null

    private var workerOnDemand: WorkerOnDemand? = null

    override fun start() {
        logger.info("Starting WorkerOnDemandVerticle for $buildType ...")

        workerOnDemand = WorkerOnDemandProvider.provide(
            type = workerOnDemandType,
            properties = mapOf("TOCK_BUILD_TYPE" to buildType) + workerProperties()
        )?.apply {
            logger.info { "WorkerOnDemand ${this@WorkerOnDemandVerticle.javaClass.simpleName} loaded for $buildType" }
            handler = Handler {
                if (LocalTime.now().isInTimeFrame()) {
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

    fun name(): String = buildType
    fun isLoaded(): Boolean = workerOnDemand != null

    private fun LocalTime.isInTimeFrame(): Boolean = (hour >= timeFrame[0] && hour <= timeFrame[1] && minute % 1 == 0)

    private fun workerProperties(): WorkerProperties {
        val prefix = "tock_worker_ondemand"
        return (System.getProperties() + System.getenv())
            .filterKeys { it.toString().startsWith(prefix) }
            .entries.associate {
                it.key.toString()
                    .replace(prefix, "tock")
                    .replace("tock_JAVA_ARGS", "JAVA_ARGS") to it.value.toString()
            } + mapOf(
            "tock_build_worker_mode" to "COMMAND_LINE",
            "tock_build_worker_verticle_enabled" to "false"
        )
    }
}
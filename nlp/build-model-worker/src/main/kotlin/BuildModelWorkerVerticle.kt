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

package ai.tock.nlp.build

import ai.tock.shared.Executor
import ai.tock.shared.booleanProperty
import ai.tock.shared.defaultZoneId
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.listProperty
import com.github.salomonbrys.kodein.instance
import io.vertx.core.AbstractVerticle
import mu.KotlinLogging
import java.time.Duration.ofHours
import java.time.Duration.ofSeconds
import java.time.ZonedDateTime
import java.util.concurrent.atomic.AtomicBoolean

/**
 *
 */
class BuildModelWorkerVerticle : AbstractVerticle() {

    companion object {
        private val logger = KotlinLogging.logger {}
        private val completeModelEnabled = booleanProperty("tock_complete_model_enabled", true)
        private val testModelTimeframe =
            listProperty("tock_test_model_timeframe", listOf("0", "5"))
                .let { t ->
                    logger.info { "test timeframe: $t" }
                    listOf(t[0].toInt(), t[1].toInt())
                }
    }

    private val executor: Executor by injector.instance()
    internal val canAnalyse = AtomicBoolean(true)

    override fun start() {
        executor.setPeriodic(ofSeconds(1)) {
            if (canAnalyse.get()) {
                try {
                    canAnalyse.set(false)
                    if (!BuildModelWorker.buildModelWithValidatedSentences() &&
                        !BuildModelWorker.buildModelWithDeletedSentences() &&
                        !BuildModelWorker.buildModelForTriggeredApplication() &&
                        (
                            ZonedDateTime.now(defaultZoneId)
                                .run {
                                    hour >= testModelTimeframe[0] &&
                                        hour <= testModelTimeframe[1] &&
                                        minute % 1 == 0
                                } &&
                                !BuildModelWorker.testModels()
                            )
                    ) {
                        logger.trace { "nothing to do - skip" }
                    }
                } catch (e: Throwable) {
                    logger.error(e)
                } finally {
                    canAnalyse.set(true)
                }
            }
        }

        if (completeModelEnabled) {
            executor.setPeriodic(ofHours(1)) {
                BuildModelWorker.completeModel()
            }
        }
    }
}

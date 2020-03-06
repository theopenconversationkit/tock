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

package ai.tock.nlp.build

import ai.tock.nlp.front.ioc.FrontIoc
import ai.tock.shared.booleanProperty
import ai.tock.shared.error
import ai.tock.shared.vertx.vertx
import com.github.salomonbrys.kodein.Kodein
import io.vertx.core.DeploymentOptions
import mu.KotlinLogging
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}
private val buildWorkerVerticleEnabled = booleanProperty("tock_build_worker_verticle_enabled", true)

fun main() {
    startBuildWorker()
}

fun startBuildWorker(vararg modules: Kodein.Module) {
    FrontIoc.setup(*modules)
    if (buildWorkerVerticleEnabled) {
        startVerticle()

    } else {
        startProcess()
    }
}

private fun startVerticle() {
    val buildModelWorkerVerticle = BuildModelWorkerVerticle()
    vertx.deployVerticle(buildModelWorkerVerticle, DeploymentOptions().setWorker(true))
    vertx.deployVerticle(CleanupModelWorkerVerticle(), DeploymentOptions().setWorker(true))
    vertx.deployVerticle(HealthCheckVerticle(buildModelWorkerVerticle))
}

private fun startProcess() {
    try {
        BuildModelWorker.buildModelWithValidatedSentences()
        BuildModelWorker.buildModelWithDeletedSentences()
        BuildModelWorker.updateAllModels()
        BuildModelWorker.testModels()
        BuildModelWorker.cleanupModel()
        exitProcess(0)
    } catch (e: Throwable) {
        logger.error(e)
        exitProcess(1)
    }
}
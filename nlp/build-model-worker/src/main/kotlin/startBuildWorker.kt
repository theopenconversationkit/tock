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

import ai.tock.nlp.build.BuildType.CLEANUP
import ai.tock.nlp.build.BuildType.REBUILD_ALL
import ai.tock.nlp.build.BuildType.REBUILD_DIFF
import ai.tock.nlp.build.BuildType.TEST
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

/**
 * Build type.
 */
enum class BuildType {
    /**
     * Build is started only if at least one sentence status has changed.
     */
    REBUILD_DIFF,

    /**
     * Full rebuild.
     */
    REBUILD_ALL,

    /**
     * Test build.
     */
    TEST,

    /**
     * Cleanup orphan builds.
     */
    CLEANUP
}

fun main(vararg args: String) {
    startBuildWorker(args.getOrNull(0)?.let { arg -> BuildType.values().find { it.name == arg } }
        ?: REBUILD_ALL)
}

fun startBuildWorker(buildType: BuildType, vararg modules: Kodein.Module) {
    FrontIoc.setup(*modules)
    if (buildWorkerVerticleEnabled) {
        startVerticle()
    } else {
        startProcess(buildType)
    }
}

private fun startVerticle() {
    val buildModelWorkerVerticle = BuildModelWorkerVerticle()
    vertx.deployVerticle(buildModelWorkerVerticle, DeploymentOptions().setWorker(true))
    vertx.deployVerticle(CleanupModelWorkerVerticle(), DeploymentOptions().setWorker(true))
    vertx.deployVerticle(HealthCheckVerticle(buildModelWorkerVerticle))
}

private fun startProcess(buildType: BuildType) {
    try {
        when (buildType) {
            REBUILD_ALL -> BuildModelWorker.updateAllModels()
            REBUILD_DIFF -> {
                BuildModelWorker.buildModelWithValidatedSentences()
                BuildModelWorker.buildModelWithDeletedSentences()
            }
            TEST -> BuildModelWorker.testModels()
            CLEANUP -> BuildModelWorker.cleanupModel()
        }
        exitProcess(0)
    } catch (e: Throwable) {
        logger.error(e)
        exitProcess(1)
    }
}
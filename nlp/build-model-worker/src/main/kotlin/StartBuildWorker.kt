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

import ai.tock.nlp.build.BuildMode.DEV
import ai.tock.nlp.build.BuildType.CLEANUP
import ai.tock.nlp.build.BuildType.REBUILD_ALL
import ai.tock.nlp.build.BuildType.REBUILD_DIFF
import ai.tock.nlp.build.BuildType.TEST
import ai.tock.nlp.build.ondemand.WorkerOnDemandVerticle
import ai.tock.nlp.front.ioc.FrontIoc
import ai.tock.shared.booleanProperty
import ai.tock.shared.error
import ai.tock.shared.listProperty
import ai.tock.shared.longProperty
import ai.tock.shared.property
import ai.tock.shared.propertyExists
import ai.tock.shared.vertx.vertx
import io.vertx.core.DeploymentOptions
import io.vertx.core.ThreadingModel
import mu.KotlinLogging
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

@Deprecated("Use tock_build_worker_mode property instead")
private val buildWorkerVerticleEnabled = booleanProperty("tock_build_worker_verticle_enabled", true)

fun main(vararg args: String) {
    val buildWorkerMode =
        if (buildWorkerVerticleEnabled && !propertyExists("tock_build_worker_mode")) {
            BuildMode.VERTICLE
        } else {
            BuildMode.valueOf(property("tock_build_worker_mode", "COMMAND_LINE"))
        }
    startBuildWorker(
        buildWorkerMode,
        args.getOrNull(0)?.let { arg -> BuildType.values().find { it.name == arg } }
            ?: REBUILD_ALL,
    )
}

fun startBuildWorker(
    buildMode: BuildMode,
    buildType: BuildType,
) {
    logger.info { "Start worker with $buildMode mode" }
    when (buildMode) {
        BuildMode.ON_DEMAND -> startOnDemandVerticle()
        BuildMode.COMMAND_LINE -> startCommandLine(buildType)
        else -> startVerticle(buildMode)
    }
}

private fun startVerticle(buildMode: BuildMode) {
    FrontIoc.setup()
    val buildModelWorkerVerticle = BuildModelWorkerVerticle()
    vertx.deployVerticle(buildModelWorkerVerticle, DeploymentOptions().setThreadingModel(ThreadingModel.WORKER))
    vertx.deployVerticle(CleanupModelWorkerVerticle(), DeploymentOptions().setThreadingModel(ThreadingModel.WORKER))
    if (buildMode != DEV) {
        vertx.deployVerticle(HealthCheckVerticle(buildModelWorkerVerticle))
    }
}

private fun startOnDemandVerticle() {
    val workerOnDemandType = property("tock_build_worker_on_demand_type", "AWS_BATCH")
    val cleanupOnDemandVerticle =
        WorkerOnDemandVerticle(
            workerOnDemandType = workerOnDemandType,
            buildType = CLEANUP.toString(),
            delayBetweenJob = longProperty("tock_build_worker_on_demand_delay_in_minutes_between_job_cleanup", 12 * 60),
            timeFrame = listProperty("tock_build_worker_on_demand_timeframe_cleanup", listOf("0", "24")).map { it.toInt() },
        )
    val rebuildDiffOnDemandVerticle =
        WorkerOnDemandVerticle(
            workerOnDemandType = workerOnDemandType,
            buildType = REBUILD_DIFF.toString(),
            delayBetweenJob = longProperty("tock_build_worker_on_demand_delay_in_minutes_between_job_rebuild_diff", 60),
            timeFrame = listProperty("tock_build_worker_on_demand_timeframe_rebuild_diff", listOf("0", "24")).map { it.toInt() },
        )
    val testOnDemandVerticle =
        WorkerOnDemandVerticle(
            workerOnDemandType = workerOnDemandType,
            buildType = TEST.toString(),
            delayBetweenJob = longProperty("tock_build_worker_on_demand_delay_in_minutes_between_job_test", 24 * 60),
            timeFrame = listProperty("tock_build_worker_on_demand_timeframe_test", listOf("0", "5")).map { it.toInt() },
        )
    vertx.deployVerticle(
        cleanupOnDemandVerticle,
        DeploymentOptions().setThreadingModel(ThreadingModel.WORKER),
    )
    vertx.deployVerticle(
        rebuildDiffOnDemandVerticle,
        DeploymentOptions().setThreadingModel(ThreadingModel.WORKER),
    )
    vertx.deployVerticle(
        testOnDemandVerticle,
        DeploymentOptions().setThreadingModel(ThreadingModel.WORKER),
    )

    vertx.deployVerticle(
        OnDemandHealthCheckVerticle(
            listOf(
                cleanupOnDemandVerticle,
                rebuildDiffOnDemandVerticle,
                testOnDemandVerticle,
            ),
        ),
    )
}

private fun startCommandLine(buildType: BuildType) {
    logger.info { "$buildType model from command line" }
    FrontIoc.setup()
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

package ai.tock.bot.admin.bundled

import com.github.salomonbrys.kodein.Kodein
import ai.tock.bot.BotIoc
import ai.tock.bot.admin.BotAdminVerticle
import ai.tock.nlp.build.BuildModelWorkerVerticle
import ai.tock.nlp.build.CleanupModelWorkerVerticle
import ai.tock.nlp.build.HealthCheckVerticle
import ai.tock.nlp.front.ioc.FrontIoc
import ai.tock.shared.vertx.vertx
import io.vertx.core.DeploymentOptions

fun main() {
    startAdminServerWithBuildWorker()
}

fun startAdminServerWithBuildWorker(vararg modules: Kodein.Module) {
    //setup ioc
    FrontIoc.setup(BotIoc.coreModules + modules.toList())
    //deploy verticle
    vertx.deployVerticle(BotAdminVerticle())

    val buildModelWorkerVerticle = BuildModelWorkerVerticle()
    vertx.deployVerticle(buildModelWorkerVerticle, DeploymentOptions().setWorker(true))
    vertx.deployVerticle(CleanupModelWorkerVerticle(), DeploymentOptions().setWorker(true))
    vertx.deployVerticle(HealthCheckVerticle(buildModelWorkerVerticle))
}

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

package ai.tock.bot.orchestration.connector

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.connector.ConnectorService
import ai.tock.bot.engine.BotRepository
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.orchestration.bot.primary.OrchestrationSecondaryBotResponseInterceptor
import ai.tock.shared.Executor
import ai.tock.shared.booleanProperty
import ai.tock.shared.injector
import ai.tock.shared.provide
import mu.KotlinLogging

internal class OrchestrationService : ConnectorService {
    private val logger = KotlinLogging.logger {}
    private val executor: Executor get() = injector.provide()
    private val orchestrationEnabled = booleanProperty("tock_orchestration", false)

    override fun install(
        controller: ConnectorController,
        configuration: BotApplicationConfiguration,
    ) {
        if (orchestrationEnabled && controller.connector is OrchestrationConnector) {
            BotRepository.registerBotAnswerInterceptor(OrchestrationSecondaryBotResponseInterceptor())
            (controller.connector as OrchestrationConnector).getOrchestrationHandlers().apply {
                val path = configuration.toConnectorConfiguration().path + "/orchestration"
                controller.registerServices(path) { router ->
                    logger.info("deploy orchestration services for root path $path ")
                    router.post("$path/eligibility").handler { context ->
                        executor.executeBlocking {
                            eligibilityHandler.invoke(controller, context)
                        }
                    }
                    router.post("$path/proxy").handler { context ->
                        executor.executeBlocking {
                            proxyHandler.invoke(controller, context)
                        }
                    }
                }
            }
        }
    }
}

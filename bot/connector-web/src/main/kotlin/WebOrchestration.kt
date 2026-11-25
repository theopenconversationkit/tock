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

package ai.tock.bot.connector.web

import ai.tock.bot.connector.ConnectorData
import ai.tock.bot.engine.BotRepository
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType.bot
import ai.tock.bot.engine.user.PlayerType.user
import ai.tock.bot.orchestration.bot.secondary.RestOrchestrationCallback
import ai.tock.bot.orchestration.connector.OrchestrationHandlers
import ai.tock.bot.orchestration.shared.AskEligibilityToOrchestratedBotRequest
import ai.tock.bot.orchestration.shared.OrchestrationMetaData
import ai.tock.bot.orchestration.shared.ResumeOrchestrationRequest
import ai.tock.bot.orchestration.shared.SecondaryBotEligibilityResponse
import ai.tock.shared.Dice
import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging

/**
 * The orchestration handlers of a [WebConnector]: they run on the Vert.x routes of the orchestration.
 */
internal object WebOrchestration {
    private val logger = KotlinLogging.logger {}

    fun handlers(connector: WebConnector): OrchestrationHandlers =
        OrchestrationHandlers(
            eligibilityHandler = { controller, context -> handleEligibility(connector, controller, context) },
            proxyHandler = { controller, context -> handleProxy(connector, controller, context) },
        )

    private fun handleProxy(
        connector: WebConnector,
        controller: ConnectorController,
        context: RoutingContext,
    ) {
        val timerData = BotRepository.requestTimer.start("web_webhook_orchestred")
        try {
            logger.debug { "Web proxy request input : ${context.body().asString()}" }
            val request: ResumeOrchestrationRequest = mapper.readValue(context.body().asString())
            val callback =
                RestOrchestrationCallback(
                    webConnectorType,
                    applicationId = connector.connectorId,
                    context = context,
                    orchestrationMapper = WebConnector.webMapper,
                )

            controller.handle(request.toAction(), ConnectorData(callback))
        } catch (t: Throwable) {
            RestOrchestrationCallback(webConnectorType, connector.connectorId, context = context).sendError()
            BotRepository.requestTimer.throwable(t, timerData)
        } finally {
            BotRepository.requestTimer.end(timerData)
        }
    }

    private fun handleEligibility(
        connector: WebConnector,
        controller: ConnectorController,
        context: RoutingContext,
    ) {
        val timerData = BotRepository.requestTimer.start("web_webhook_support")
        try {
            logger.debug { "Web support request input : ${context.body().asString()}" }
            val request: AskEligibilityToOrchestratedBotRequest = mapper.readValue(context.body().asString())
            val callback =
                RestOrchestrationCallback(
                    webConnectorType,
                    connector.connectorId,
                    context = context,
                    orchestrationMapper = WebConnector.webMapper,
                )

            val support = controller.support(request.toAction(connector.connectorId), ConnectorData(callback))
            val sendEligibility =
                SecondaryBotEligibilityResponse(
                    support,
                    OrchestrationMetaData(
                        playerId = PlayerId(connector.connectorId, bot),
                        applicationId = connector.connectorId,
                        recipientId = request.metadata?.playerId ?: PlayerId(Dice.newId(), user),
                    ),
                )
            callback.sendResponse(sendEligibility)
        } catch (t: Throwable) {
            RestOrchestrationCallback(webConnectorType, connector.connectorId, context = context).sendError()
            BotRepository.requestTimer.throwable(t, timerData)
        } finally {
            BotRepository.requestTimer.end(timerData)
        }
    }
}

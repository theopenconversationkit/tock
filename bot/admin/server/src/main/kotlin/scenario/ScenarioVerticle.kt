/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.bot.admin.scenario

import ai.tock.bot.admin.model.scenario.ScenarioRequest
import ai.tock.bot.admin.model.scenario.ScenarioResult
import ai.tock.shared.injector
import ai.tock.shared.security.TockUserRole.*
import ai.tock.shared.vertx.RestException
import ai.tock.shared.vertx.WebVerticle
import ai.tock.translator.I18nDAO
import com.github.salomonbrys.kodein.instance
import io.vertx.ext.web.RoutingContext
import mu.KLogger
import mu.KotlinLogging

/**
 *
 */
open class ScenarioVerticle : WebVerticle() {

    override val logger: KLogger = KotlinLogging.logger {}

    private val scenarioService = ScenarioService()

    private val SCENARIO_ID = "scenarioID"

    override fun configure() {
        blockingJsonGet("/scenarios", botUser, handler = getAllScenarios)

        blockingJsonGet("/scenarios/:"+SCENARIO_ID, botUser, handler = getOneScenario)

        blockingJsonPost("/scenarios", botEditor, handler = createScenario)

        blockingJsonPut("/scenarios/:"+SCENARIO_ID, botEditor, handler = updateScenario)

        blockingDelete("/scenarios/:"+SCENARIO_ID, botEditor, handler = deleteScenario)
    }

    protected val getAllScenarios: (RoutingContext) -> Unit = { context ->
        logger.debug { "request to get all scenario" }
        scenarioService.findAllService()
    }

    protected val getOneScenario: (RoutingContext) -> Unit = { context ->
        val scenarioId = context.path(SCENARIO_ID)
        logger.debug { "request to get scenario id \"$scenarioId\"" }
        val scenario: Scenario = scenarioService.findById(scenarioId)
        //return
        scenario.mapToScenarioResult()
    }

    protected val createScenario: (RoutingContext, ScenarioRequest) -> Unit = { context, request ->
        logger.debug { "request to create scenario name \"${request.name}\"" }
        val scenario: Scenario = scenarioService.create(request.mapToScenario())
        //return
        scenario.mapToScenarioResult()
    }

    protected val updateScenario: (RoutingContext, ScenarioRequest) -> Unit = { context, request ->
        val scenarioId = context.path(SCENARIO_ID)
        logger.debug { "request to update scenario id \"$scenarioId\"" }
        val scenario: Scenario = if(scenarioService.existe(scenarioId)) {
            scenarioService.update(request.mapToScenario())
        } else {
            //TODO: a encapsulé dans une exception plus spécifique
            throw RestException("scenario \"$scenarioId\" not found ", 404)
        }
        //return
        scenario.mapToScenarioResult()
    }

    protected val deleteScenario: (RoutingContext) -> Unit = { context ->
        val scenarioId = context.path(SCENARIO_ID)
        logger.debug { "request to delete scenario id \"$scenarioId\"" }
        if(scenarioService.existe(scenarioId)) {
            scenarioService.delete(scenarioId)
        } else {
            logger.debug { "scenario \"$scenarioId\" already don't exist" }
        }
    }

    private fun ScenarioRequest.mapToScenario(): Scenario {
        return Scenario(
            id = id,
            name = name,
            category = category,
            tags = tags,
            applicationId = applicationId,
            createDate = createDate,
            updateDate = updateDate,
            description= description,
            state = state )
    }

    private fun Scenario.mapToScenarioResult(): ScenarioResult {
        return ScenarioResult(
            id = id,
            name = name,
            category = category,
            tags = tags,
            applicationId = applicationId,
            createDate = createDate,
            updateDate = updateDate,
            description= description,
            state = state )
    }
}
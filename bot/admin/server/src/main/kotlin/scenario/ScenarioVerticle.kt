/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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
import ai.tock.shared.exception.TockException
import ai.tock.shared.injector
import ai.tock.shared.security.TockUserRole.*
import ai.tock.shared.exception.rest.InternalServerException
import ai.tock.shared.exception.rest.NotFoundException
import ai.tock.shared.vertx.WebVerticle
import com.github.salomonbrys.kodein.instance
import io.vertx.ext.web.RoutingContext
import mu.KLogger
import mu.KotlinLogging

/**
 * ScenarioVerticle contains all the routes and actions associated with the scenarios
 */
open class ScenarioVerticle {

    private val logger: KLogger = KotlinLogging.logger {}

    private val scenarioService: ScenarioService by injector.instance()

    private val scenarioId = "scenarioID"

    //scenarioBasePath is empty value is necessary so that the scenario URLs are not preceded by
    // the default basePath (/rest/admin) of the BotAdminVerticle on which the routes are added
    private val scenarioBasePath = ""

    private val scenariosPath = "/scenarios"

    /**
     * Declaration of routes and association to the appropriate handler
     */
    fun configureScenario(webVerticle: WebVerticle) {
        logger.info { "configure ScenarioVerticle" }
        with(webVerticle) {
            blockingJsonGet(scenariosPath, setOf(botUser), handler = getAllScenarios)

            blockingJsonGet("$scenariosPath/:$scenarioId", setOf(botUser), handler = getOneScenario)

            blockingJsonPost(scenariosPath, setOf(botUser), handler = createScenario)

            blockingJsonPut("$scenariosPath/:$scenarioId", setOf(botUser), handler = updateScenario)

            blockingDeleteEmptyResponse("$scenariosPath/:$scenarioId", setOf(botUser), handler = deleteScenario)
        }
    }

    /**
     * Handler to retrieve Scenario, then format the response as a List of ScenarioResult.
     * when success, return a 200 response.
     */
    protected val getAllScenarios: (RoutingContext) -> List<ScenarioResult> = { context ->
        logger.debug { "request to get all scenario" }
        catchExternalException {
            scenarioService
                .findAll()
                .map(mapToScenarioResult)
        }
    }

    /**
     * Handler to find and retrieve a Scenario based on it's ID, then format the response as a ScenarioResult.
     * when success, return a 200 response.
     */
    protected val getOneScenario: (RoutingContext) -> ScenarioResult = { context ->
        val scenarioId = extractScenarioId(context)
        logger.debug { "request to get scenario id $scenarioId" }
        //return
        catchExternalException {
            scenarioService
                .findById(scenarioId)
                .mapToScenarioResult()
        }
    }

    /**
     * Handler to create and retrieve the Scenario created, then format the response as a ScenarioResult.
     * when success, return a 201 response.
     */
    protected val createScenario: (RoutingContext, ScenarioRequest) -> ScenarioResult = { context, request ->
        logger.debug { "request to create scenario name ${request.name}" }
        context.setResponseStatusCode(201)
        //return
        catchExternalException {
            scenarioService
                .create(request.mapToScenario())
                .mapToScenarioResult()
        }
    }

    /**
     * Handler to update and retrieve the Scenario update, then format the response as a ScenarioResult.
     * when success, produce a 202 response.
     */
    protected val updateScenario: (RoutingContext, ScenarioRequest) -> ScenarioResult = { context, request ->
        val scenarioId = extractScenarioId(context)
        logger.debug { "request to update scenario id $scenarioId" }
        context.setResponseStatusCode(200)
        //return
        catchExternalException {
            scenarioService
                .update(scenarioId, request.mapToScenario())
                .mapToScenarioResult()
        }
    }

    private fun RoutingContext.setResponseStatusCode(statusCode: Int) {
        response().statusCode = statusCode
    }

    /**
     * Handler to delete a Scenario based on it's ID, then return nothing.
     * when success, produce a 204 response.
     */
    protected val deleteScenario: (RoutingContext) -> Unit = { context ->
        val scenarioId = extractScenarioId(context)
        logger.debug { "request to delete scenario id $scenarioId" }
        //no return
        catchExternalException {
            scenarioService.delete(scenarioId)
        }
    }

    private fun <O> catchExternalException(fallibleSection: () -> O): O {
        try {
            return fallibleSection.invoke()
        } catch (tockException: TockException) {
            logger.error(fallibleSection)
            //TockException use a non-null message,
            // but extends RuntimeException which has nullable message
            // tockException.message cannot be null
            throw InternalServerException(tockException.message!!)
        }
    }

    private fun extractScenarioId(context: RoutingContext): String {
        return context.pathParam(scenarioId).checkParameterExist(scenarioId)
    }

    private val checkParameterExist: String?.(String) -> String = { parameter ->
        if(this == null) {
            throw NotFoundException("$parameter uri parameter not found")
        } else {
            this
        }
    }
}
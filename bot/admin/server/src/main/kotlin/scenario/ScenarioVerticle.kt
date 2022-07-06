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
import ai.tock.shared.exception.TockException
import ai.tock.shared.injector
import ai.tock.shared.security.TockUserRole.*
import ai.tock.shared.vertx.InternalServerException
import ai.tock.shared.vertx.NotFoundException
import ai.tock.shared.vertx.WebVerticle
import com.github.salomonbrys.kodein.instance
import io.vertx.ext.web.RoutingContext
import mu.KLogger
import mu.KotlinLogging

open class ScenarioVerticle() {

    private val logger: KLogger = KotlinLogging.logger {}

    private val scenarioService: ScenarioService by injector.instance()

    private val scenarioId = "scenarioID"

    private val scenarioBasePath = ""

    val scenariosPath = "/scenarios"

    fun configureScenario(webVerticle: WebVerticle) {
        logger.info { "configure ScenarioVerticle" }
        with(webVerticle) {
            blockingJsonGet(scenarioBasePath, setOf(botUser), basePath= scenariosPath,  handler= getAllScenarios)

            blockingJsonGet("$scenarioBasePath/:$scenarioId", setOf(botUser), basePath= scenariosPath, handler= getOneScenario)

            blockingJsonPost(scenarioBasePath, setOf(botUser), basePath= scenariosPath, handler= createScenario)

            blockingJsonPut("$scenarioBasePath/:$scenarioId", setOf(botUser), basePath= scenariosPath, handler= updateScenario)

            blockingDeleteEmptyResponse("$scenarioBasePath/:$scenarioId", setOf(botUser), basePath= scenariosPath, handler= deleteScenario)
        }
    }

    protected val getAllScenarios: (RoutingContext) -> List<ScenarioResult> = { context ->
        logger.debug { "request to get all scenario" }
        catchExternalException {
            scenarioService
                .findAll()
                .map(mapToScenarioResult)
        }
    }

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

    protected val updateScenario: (RoutingContext, ScenarioRequest) -> ScenarioResult = { context, request ->
        val scenarioId = extractScenarioId(context)
        logger.debug { "request to update scenario id $scenarioId" }
        context.setResponseStatusCode(202)
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
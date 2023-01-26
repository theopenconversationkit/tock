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

package ai.tock.bot.admin.verticle

import ai.tock.bot.HandlerNamespace
import ai.tock.bot.admin.BotAdminService
import ai.tock.bot.admin.mapper.ScenarioExceptionManager
import ai.tock.bot.admin.mapper.ScenarioMapper.toScenarioActionHandlerResponse
import ai.tock.bot.admin.mapper.ScenarioMapper.toScenarioGroup
import ai.tock.bot.admin.mapper.ScenarioMapper.toScenarioGroupResponse
import ai.tock.bot.admin.mapper.ScenarioMapper.toScenarioVersion
import ai.tock.bot.admin.mapper.ScenarioMapper.toScenarioVersionResponse
import ai.tock.bot.admin.model.scenario.*
import ai.tock.bot.admin.service.ScenarioService
import ai.tock.bot.handler.ActionHandlersRepository
import ai.tock.shared.security.TockUser
import ai.tock.shared.security.TockUserRole.*
import ai.tock.shared.vertx.WebVerticle
import io.vertx.ext.web.RoutingContext
import mu.KLogger
import mu.KotlinLogging

/**
 * ScenarioVerticle contains all the routes and actions associated with the scenarios
 */
open class ScenarioVerticle {

    private val logger: KLogger = KotlinLogging.logger {}
    //private val ScenarioServiceImpl: ScenarioServiceImpl by injector.instance()

    private val botId = "botId"
    private val groupId = "groupId"
    private val versionId = "versionId"
    private val tickStoryId = "tickStoryId"

    private val importOneScenarioGroupPath    = "/bot/:$botId/scenarios/import/groups"
    private val importManyScenarioVersionPath = "/bot/:$botId/scenarios/import/groups/:$groupId/versions"
    private val createOneScenarioGroupPath    = "/bot/:$botId/scenarios/groups"
    private val createOneScenarioVersionPath  = "/bot/:$botId/scenarios/groups/:$groupId/versions"

    private val getAllScenarioGroupPath       = "/bot/:$botId/scenarios/groups"
    private val getOneScenarioGroupPath       = "/bot/:$botId/scenarios/groups/:$groupId"
    private val getAllScenarioVersionPath     = "/bot/:$botId/scenarios/groups/:$groupId/versions"
    private val getOneScenarioVersionPath     = "/bot/:$botId/scenarios/groups/:$groupId/versions/:$versionId"

    private val updateOneScenarioGroupPath    = "/bot/:$botId/scenarios/groups/:$groupId"
    private val updateOneScenarioVersionPath  = "/bot/:$botId/scenarios/groups/:$groupId/versions/:$versionId"

    private val deleteOneScenarioGroupPath    = "/bot/:$botId/scenarios/groups/:$groupId"
    private val deleteOneScenarioVersionPath  = "/bot/:$botId/scenarios/groups/:$groupId/versions/:$versionId"

    private val getAllActionHandlerPath     = "/bot/:$botId/dialog-manager/action-handlers"

    /**
     * Declaration of routes and their appropriate handlers
     */
    fun configureScenario(webVerticle: WebVerticle) {
        logger.info { "Configure Scenario Verticle" }
        with(webVerticle) {
            // Create
            blockingJsonPost(importOneScenarioGroupPath, setOf(botUser), handler = importOneScenarioGroup)
            blockingJsonPost(importManyScenarioVersionPath, setOf(botUser), handler = importManyScenarioVersion)
            blockingJsonPost(createOneScenarioGroupPath, setOf(botUser), handler = createOneScenarioGroup)
            blockingJsonPost(createOneScenarioVersionPath, setOf(botUser), handler = createOneScenarioVersion)

            // Read
            blockingJsonGet(getAllScenarioGroupPath, setOf(botUser), handler = getAllScenarioGroup)
            blockingJsonGet(getOneScenarioGroupPath, setOf(botUser), handler = getOneScenarioGroup)
            blockingJsonGet(getOneScenarioVersionPath, setOf(botUser), handler = getOneScenarioVersion)
            blockingJsonGet(getAllScenarioVersionPath, setOf(botUser), handler = getAllScenarioVersion)
            blockingJsonGet(getAllActionHandlerPath, setOf(botUser), handler = getAllActionHandlers)

            // Update
            blockingJsonPut(updateOneScenarioGroupPath, setOf(botUser), handler = updateOneScenarioGroup)
            blockingJsonPut(updateOneScenarioVersionPath, setOf(botUser), handler = updateOneScenarioVersion)

            // Delete
            blockingJsonDelete(deleteOneScenarioGroupPath, setOf(botUser), handler = deleteOneScenarioGroup)
            blockingJsonDelete(deleteOneScenarioVersionPath, setOf(botUser), handler = deleteOneScenarioVersion)
        }
    }

    /**
     * Handler to import one scenario group and returns the created group with its versions, as [ScenarioGroupResponse]
     * When success, return a 201 Http status.
     */
    private val importOneScenarioGroup: (RoutingContext, ScenarioGroupWithVersionsRequest) -> ScenarioGroupResponse = { context, request ->
        context.setResponseStatusCode(201)
        val botId = context.pathParam(botId)
        checkBotConfiguration(context, botId)

        ScenarioExceptionManager.catch {
            ScenarioService
                .importOneScenarioGroup(request.toScenarioGroup(botId))
                .toScenarioGroupResponse()
        }
    }

    /**
     * Handler to import many scenario versions and returns the created versions, as a list of [ScenarioVersionResponse]
     * When success, return a 201 Http status.
     */
    private val importManyScenarioVersion: (RoutingContext, List<ScenarioVersionRequest>) -> List<ScenarioVersionResponse> = { context, request ->
        context.setResponseStatusCode(201)
        val botId = context.pathParam(botId)
        val groupId = context.pathParam(groupId)
        checkBotConfiguration(context, botId)

        ScenarioExceptionManager.catch {
            ScenarioService
                .importManyScenarioVersion(getNamespace(context), request.map { it.toScenarioVersion(groupId) })
                .map { it.toScenarioVersionResponse() }
        }
    }

    /**
     * Handler to create one scenario group and returns the created group with its initialized version, as ScenarioGroupResponse
     * When success, return a 201 Http status.
     */
    private val createOneScenarioGroup: (RoutingContext, ScenarioGroupRequest) -> ScenarioGroupResponse = { context, request ->
        context.setResponseStatusCode(201)
        val botId = context.pathParam(botId)
        checkBotConfiguration(context, botId)
        
        ScenarioExceptionManager.catch {
            ScenarioService
                .createOneScenarioGroup(request.toScenarioGroup(botId))
                .toScenarioGroupResponse()
        }
    }

    /**
     * Handler to create one scenario version and returns the created version, as ScenarioVersionResponse
     * When success, return a 201 Http status.
     */
    private val createOneScenarioVersion: (RoutingContext, ScenarioVersionRequest) -> ScenarioVersionResponse = { context, request ->
        context.setResponseStatusCode(201)
        val botId = context.pathParam(botId)
        val groupId = context.pathParam(groupId)
        checkBotConfiguration(context, botId)
        
        ScenarioExceptionManager.catch {
            ScenarioService
                .createOneScenarioVersion(getNamespace(context), request.toScenarioVersion(groupId))
                .toScenarioVersionResponse()

        }
    }

    /**
     * Handler to retrieve all scenario groups with their scenario versions,
     * but without the version "data" field.
     * When success, return a 200 Http status.
     */
    private val getAllScenarioGroup: (RoutingContext) -> List<ScenarioGroupResponse> = { context ->
        val botId = context.pathParam(botId)
        checkBotConfiguration(context, botId)

        ScenarioExceptionManager.catch {
            ScenarioService
                .findAllScenarioGroupWithVersionsByBotId(getNamespace(context), botId)
                .map { it.toScenarioGroupResponse() }
        }
    }

    /**
     * Handler to retrieve one scenario group with its scenario versions,
     * but without the version "data" field.
     * When success, return a 200 Http status.
     */
    private val getOneScenarioGroup: (RoutingContext) -> ScenarioGroupResponse = { context ->
        val botId = context.pathParam(botId)
        val groupId = context.pathParam(groupId)
        checkBotConfiguration(context, botId)

        ScenarioExceptionManager.catch {
            ScenarioService
                .findOneScenarioGroup(getNamespace(context), groupId)
                .toScenarioGroupResponse()
        }
    }

    /**
     * Handler to retrieve one scenario version, as [ScenarioVersionResponse]
     * When success, return a 200 Http status.
     */
    private val getOneScenarioVersion: (RoutingContext) -> ScenarioVersionResponse = { context ->
        val botId = context.pathParam(botId)
        val groupId = context.pathParam(groupId)
        val versionId = context.pathParam(versionId)
        checkBotConfiguration(context, botId)

        ScenarioExceptionManager.catch {
            ScenarioService
                .findOneScenarioVersion(groupId, versionId)
                .toScenarioVersionResponse()
        }
    }

    /**
     * Handler to retrieve all scenario versions of a scenario group, as a list of [ScenarioVersionResponse]
     * When success, return a 200 Http status.
     */
    private val getAllScenarioVersion: (RoutingContext) -> List<ScenarioVersionResponse> = { context ->
        val botId = context.pathParam(botId)
        val groupId = context.pathParam(groupId)
        checkBotConfiguration(context, botId)

        ScenarioExceptionManager.catch {
            ScenarioService
                .findOneScenarioGroup(getNamespace(context), groupId).versions.map { it.toScenarioVersionResponse() }
        }
    }

    /**
     * Handler to retrieve all action handlers.
     * When success, return a 200 Http status.
     */
    private val getAllActionHandlers: (RoutingContext) -> Set<ScenarioActionHandlerResponse> = { context ->
        val botId = context.pathParam(botId)
        checkBotConfiguration(context, botId)

        ActionHandlersRepository
            .getActionHandlers(
                HandlerNamespace.find(getNamespace(context)))
            .map { it.toScenarioActionHandlerResponse() }
            .toSet()
    }

    /**
     * Handler to update one scenario group
     * Returns the updated group with its versions, as [ScenarioGroupResponse]
     * When success, return a 200 Http status.
     */
    private val updateOneScenarioGroup: (RoutingContext, ScenarioGroupRequest) -> ScenarioGroupResponse = { context, request ->
        val botId = context.pathParam(botId)
        val groupId = context.pathParam(groupId)
        checkBotConfiguration(context, botId)

        ScenarioExceptionManager.catch {
            ScenarioService
                .updateOneScenarioGroup(getNamespace(context), request.toScenarioGroup(botId, groupId))
                .toScenarioGroupResponse()
        }
    }

    /**
     * Handler to update one scenario version
     * Returns the updated version, as [ScenarioVersionResponse]
     * When success, return a 200 Http status.
     */
    private val updateOneScenarioVersion: (RoutingContext, ScenarioVersionRequest) -> ScenarioVersionResponse = { context, request ->
        val botId = context.pathParam(botId)
        val groupId = context.pathParam(groupId)
        val versionId = context.pathParam(versionId)
        checkBotConfiguration(context, botId)

        ScenarioExceptionManager.catch {
            ScenarioService
                .updateOneScenarioVersion(request.toScenarioVersion(groupId, versionId))
                .toScenarioVersionResponse()
        }
    }

    /**
     * Handler to delete one scenario group
     * Returns true if the scenario group was deleted, else false.
     * When success, return a 200 Http status.
     */
    private val deleteOneScenarioGroup: (RoutingContext) -> Boolean = { context ->
        val botId = context.pathParam(botId)
        val groupId = context.pathParam(groupId)
        checkBotConfiguration(context, botId)

        ScenarioExceptionManager.catch {
            ScenarioService.deleteOneScenarioGroup(getNamespace(context), botId, groupId)
        }
    }

    /**
     * Handler to delete one scenario version
     * Returns true if the scenario version was deleted, else false.
     * When success, return a 200 Http status.
     */
    private val deleteOneScenarioVersion: (RoutingContext) -> Boolean = { context ->
        val botId = context.pathParam(botId)
        val groupId = context.pathParam(groupId)
        val versionId = context.pathParam(versionId)
        checkBotConfiguration(context, botId)

        ScenarioExceptionManager.catch {
            ScenarioService.deleteOneScenarioVersion(getNamespace(context), botId, groupId, versionId)
        }
    }

    /**
     * Check the bot configuration.
     * Throws [BadRequestException] if there is not a bot found.
     * @param context : the vertx routing context
     * @param botId : id of the bot
     */
    private fun checkBotConfiguration(context: RoutingContext, botId: String) {
        val namespace = getNamespace(context)
        val botConfiguration = BotAdminService.getBotConfigurationsByNamespaceAndBotId(namespace, botId).firstOrNull()
        botConfiguration ?: WebVerticle.badRequest("No bot configuration is defined yet")
    }

    /**
     * Get the namespace from the context
     * @param context : the vertx routing context
     */
    private fun getNamespace(context: RoutingContext) = (context.user() as TockUser).namespace

    private fun RoutingContext.setResponseStatusCode(statusCode: Int) { response().statusCode = statusCode }

}
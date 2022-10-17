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

package ai.tock.bot.admin.service.impl

import ai.tock.bot.admin.scenario.ScenarioGroup
import ai.tock.bot.admin.scenario.ScenarioVersion
import ai.tock.bot.admin.scenario.ScenarioVersionState.ARCHIVED
import ai.tock.bot.admin.scenario.ScenarioVersionState.CURRENT
import ai.tock.bot.admin.scenario.ScenarioVersionState.DRAFT
import ai.tock.bot.admin.service.ScenarioGroupService
import ai.tock.bot.admin.service.ScenarioService
import ai.tock.bot.admin.service.ScenarioVersionService
import ai.tock.bot.admin.service.StoryService
import ai.tock.shared.exception.scenario.group.ScenarioGroupAndVersionMismatchException
import ai.tock.shared.exception.scenario.version.ScenarioVersionsInconsistentException
import ai.tock.shared.exception.scenario.ScenarioException
import ai.tock.shared.exception.scenario.group.ScenarioGroupWithoutVersionException
import ai.tock.shared.exception.scenario.version.ScenarioVersionBadStateException
import ai.tock.shared.injector
import com.github.salomonbrys.kodein.instance
import mu.KLogger
import mu.KotlinLogging
import org.litote.kmongo.Id

/**
 * Implementation of ScenarioService
 */
class ScenarioServiceImpl : ScenarioService {

    private val logger: KLogger = KotlinLogging.logger {}
    private val scenarioGroupService: ScenarioGroupService by injector.instance()
    private val scenarioVersionService: ScenarioVersionService by injector.instance()
    private val storyService: StoryService by injector.instance()

    override fun findAllScenarioGroupWithVersionsByBotId(botId: String): List<ScenarioGroup> {
        return scenarioGroupService.findAllByBotId(botId)
    }

    override fun findOneScenarioGroup(scenarioGroupId: String): ScenarioGroup {
        return scenarioGroupService.findOneById(scenarioGroupId)
    }

    override fun findOneScenarioVersion(scenarioGroupId:String, scenarioVersionId: String): ScenarioVersion {
        // Get scenario version from DB
        val scenarioVersion = scenarioVersionService.findOneById(scenarioVersionId)
        // check the consistency
        checkConsistencyOfScenarioGroupAndVersion(scenarioGroupId, scenarioVersion)
        return scenarioVersion
    }

    override fun createOneScenarioGroup(scenarioGroup: ScenarioGroup): ScenarioGroup {
        // Create the scenario group
        val scenarioGroup = scenarioGroupService.createOne(scenarioGroup)
        // Init the first draft version
        val scenarioVersion = scenarioVersionService.createOne(initDraftScenarioVersion(scenarioGroup._id))
        // Associate the version to the group
        return scenarioGroup.copy(versions = listOf(scenarioVersion))
    }

    override fun importOneScenarioGroup(scenarioGroup: ScenarioGroup): ScenarioGroup {
        // Check consistency
        checkScenarioVersionsToImport(scenarioGroup.versions)
        // Create the scenario group
        val scenarioGroup = scenarioGroupService.createOne(scenarioGroup)
        // Create versions
        val scenarioVersions = scenarioVersionService.createMany(scenarioGroup.versions)
        // Associate the versions to the group
        return scenarioGroup.copy(versions = scenarioVersions)
    }

    override fun importManyScenarioVersion(scenarioVersions: List<ScenarioVersion>): List<ScenarioVersion> {
        // Check consistency
        checkScenarioVersionsToImport(scenarioVersions)
        // All versions have a same scenarioGroupId, so we get the first one
        val scenarioGroupId = scenarioVersions.first().scenarioGroupId.toString()
        // Find and check existence of scenario group
        findOneScenarioGroup(scenarioGroupId)
        // Create versions
        return scenarioVersionService.createMany(scenarioVersions)
    }

    private fun checkScenarioVersionsToImport(scenarioVersions: List<ScenarioVersion>) {
        // Check scenario group has versions
        if(scenarioVersions.isEmpty()){
            throw ScenarioGroupWithoutVersionException()
        }

        // Check if all versions have the same scenario group id
        if(scenarioVersions.map { it.scenarioGroupId }.distinct().count() > 1){
            throw ScenarioVersionsInconsistentException()
        }

        // Check presence of CURRENT version
        if(scenarioVersions.count { it.isCurrent() } > 0){
            throw ScenarioVersionBadStateException("An imported scenario group must not have a current version")
        }
    }

    override fun createOneScenarioVersion(scenarioVersion: ScenarioVersion): ScenarioVersion {
        // Find and check existence of scenario group
        findOneScenarioGroup(scenarioVersion.scenarioGroupId.toString())

        if(!scenarioVersion.isDraft()){
            throw ScenarioVersionBadStateException("Only a draft version can be created")
        }

        return scenarioVersionService.createOne(scenarioVersion)
    }

    override fun updateOneScenarioGroup(scenarioGroup: ScenarioGroup): ScenarioGroup {
        return scenarioGroupService.updateOne(scenarioGroup)
    }

    override fun updateOneScenarioVersion(scenarioVersion: ScenarioVersion): ScenarioVersion {
        val scenarioVersionDB = scenarioVersionService.findOneById(scenarioVersion._id.toString())

        // Check consistency
        checkConsistencyOfScenarioGroupAndVersion(scenarioVersionDB.scenarioGroupId.toString(), scenarioVersion)

        if(!scenarioVersionDB.isDraft()){
            // Only a draft version can be updated
            logger.error { "Scenario version ${scenarioVersion._id} cannot be updated, because it's state is ${scenarioVersionDB.state}" }
            throw ScenarioVersionBadStateException()
        }

        // The given scenario version is CURRENT
        if(scenarioVersion.isCurrent()){
            // Get the CURRENT version in database if exists
            val scenarioVersionCurrentDB = scenarioVersionService
                .findAllByScenarioGroupIdAndState(scenarioVersion.scenarioGroupId.toString(), CURRENT).firstOrNull()

            scenarioVersionCurrentDB?.let {
                // Archive the current version if it exists and if the updated scenario version is the new one
                scenarioVersionService.updateOne(scenarioVersionCurrentDB.copy(state = ARCHIVED))
                logger.info { "Archiving of the old current version of scenario group <id:${scenarioVersion.scenarioGroupId}>" }
            }
        }

        // Update the scenario version
        val scenarioVersionUpdated = scenarioVersionService.updateOne(scenarioVersion)
        logger.info { "Updating of the scenario version <id:${scenarioVersion._id}>" }

        return scenarioVersionUpdated
    }

    private fun checkConsistencyOfScenarioGroupAndVersion(scenarioGroupId: String, scenarioVersion: ScenarioVersion) {
        if(scenarioGroupId != scenarioVersion.scenarioGroupId.toString()){
            logger.error { "The scenario version <id:${scenarioVersion._id}> is not part of the scenario group <id:${scenarioVersion.scenarioGroupId}>" }
            throw ScenarioGroupAndVersionMismatchException()
        }
    }

    override fun deleteOneScenarioGroup(namespace: String, botId: String, scenarioGroupId: String): Boolean {
        return try {
            // Find and check existence of scenario group
            findOneScenarioGroup(scenarioGroupId)

            // Delete all versions
            scenarioVersionService.deleteAllByScenarioGroupId(scenarioGroupId)
            logger.info { "Removal of all versions of the scenario group <id:$scenarioGroupId>" }

            // Delete the tick story
            deleteTickStory(namespace, botId, scenarioGroupId)

            // Delete the scenario group
            scenarioGroupService.deleteOneById(scenarioGroupId)
            logger.info { "Removal of the scenario group <id:$scenarioGroupId>" }

            true
        } catch (ex: ScenarioException) {
            logger.warn { ex.message }
            false
        }
    }

    override fun deleteOneScenarioVersion(
        namespace: String,
        botId: String,
        scenarioGroupId: String,
        scenarioVersionId: String
    ): Boolean {
        return try {
            val scenarioVersionDB = scenarioVersionService.findOneById(scenarioVersionId)

            // Check consistency
            checkConsistencyOfScenarioGroupAndVersion(scenarioGroupId, scenarioVersionDB)

            if (scenarioVersionDB.isCurrent()) {
                // Delete the tick story if the scenario version has a current state
                deleteTickStory(namespace, botId, scenarioGroupId)
            }

            // Delete the scenario version
            scenarioVersionService.deleteOneById(scenarioVersionId)
            logger.info { "Removal of the scenario version <id:$scenarioVersionId>" }

            // Delete the scenario group if there is no version left
            deleteEmptyScenarioGroup(scenarioGroupId)

            true
        } catch (ex: ScenarioException) {
            logger.warn { ex.message }
            false
        }

    }

    private fun deleteTickStory(namespace: String, botId: String, scenarioGroupId: String) {
        storyService.deleteStoryByStoryId(namespace, botId, scenarioGroupId)
        logger.info { "Removal of the tick story <storyId:$scenarioGroupId> corresponding to the current version of the scenario group <id:$scenarioGroupId>" }
    }

    private fun deleteEmptyScenarioGroup(scenarioGroupId: String) {
        if (scenarioVersionService.countAllByScenarioGroupId(scenarioGroupId) == 0L) {
            scenarioGroupService.deleteOneById(scenarioGroupId)
            logger.info { "Removal of the scenario group <id:$scenarioGroupId> following the removal of all these scenario versions" }
        }
    }

    private fun initDraftScenarioVersion(scenarioGroupId: Id<ScenarioGroup>): ScenarioVersion {
        return ScenarioVersion(
            scenarioGroupId = scenarioGroupId,
            comment = "Initial version",
            state = DRAFT,
        )
    }

}
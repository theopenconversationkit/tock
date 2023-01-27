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

package ai.tock.bot.admin.service

import ai.tock.bot.admin.scenario.ScenarioGroup
import ai.tock.bot.admin.scenario.ScenarioVersion
import ai.tock.bot.admin.scenario.ScenarioVersionState.*
import ai.tock.bot.admin.story.StoryDefinitionConfigurationFeature
import ai.tock.shared.exception.scenario.ScenarioException
import ai.tock.shared.exception.scenario.group.ScenarioGroupAndVersionMismatchException
import ai.tock.shared.exception.scenario.group.ScenarioGroupNotFoundException
import ai.tock.shared.exception.scenario.group.ScenarioGroupWithoutVersionException
import ai.tock.shared.exception.scenario.version.ScenarioVersionBadStateException
import ai.tock.shared.exception.scenario.version.ScenarioVersionsInconsistentException
import mu.KLogger
import mu.KotlinLogging
import org.litote.kmongo.Id

/**
 * Service that manage the scenario functionality
 */
object ScenarioService {

    private val logger: KLogger = KotlinLogging.logger {}

    /**
     * Returns all scenario groups with their scenario versions
     */
    fun findAllScenarioGroupWithVersionsByBotId(namespace: String, botId: String): List<ScenarioGroup> {
        // Get scenario group from DB
        val scenarioGroups = ScenarioGroupService.findAllByBotId(botId)
        // Map a stories features to scenario groups
        return scenarioGroups.map { mapStoryFeatures(namespace, it) }
    }

    /**
     * Returns one scenario group with its scenario versions
     * @param scenarioGroupId: id of the scenario group
     * @throws [ScenarioGroupNotFoundException] if the scenario group was not found
     */
    fun findOneScenarioGroup(namespace: String, scenarioGroupId: String): ScenarioGroup {
        // Get scenario group from DB
        val scenarioGroup = ScenarioGroupService.findOneById(scenarioGroupId)
        // Map a story feature to scenario group
        return mapStoryFeatures(namespace, scenarioGroup)
    }

    /**
     * Returns one scenario version
     * @param scenarioGroupId: id of the scenario group
     * @param scenarioVersionId: id of the scenario version
     * @throws [ScenarioVersionNotFoundException] if the scenario version was not found
     */
    fun findOneScenarioVersion(scenarioGroupId: String, scenarioVersionId: String): ScenarioVersion {
        // Get scenario version from DB
        val scenarioVersion = ScenarioVersionService.findOneById(scenarioVersionId)
        // check the consistency
        checkConsistencyOfScenarioGroupAndVersion(scenarioGroupId, scenarioVersion)
        return scenarioVersion
    }

    /**
     * Create a new scenario group
     * Returns the created scenario group
     * @param scenarioGroup: the scenario group to create
     */
    fun createOneScenarioGroup(scenarioGroup: ScenarioGroup): ScenarioGroup {
        // Create the scenario group
        val scenarioGroup = ScenarioGroupService.createOne(scenarioGroup)
        // Init the first draft version
        val scenarioVersion = ScenarioVersionService.createOne(initDraftScenarioVersion(scenarioGroup._id))
        // Associate the version to the group
        return scenarioGroup.copy(versions = listOf(scenarioVersion), enabled = false)
    }

    /**
     * Import a scenario group with its version and returns the created scenario group
     * @param scenarioGroup: the scenario group to import
     */
    fun importOneScenarioGroup(scenarioGroup: ScenarioGroup): ScenarioGroup {
        // Check consistency
        checkScenarioVersionsToImport(scenarioGroup.versions)
        // Create the scenario group
        val scenarioGroup = ScenarioGroupService.createOne(scenarioGroup)
        // Create versions
        val scenarioVersions = ScenarioVersionService.createMany(scenarioGroup.versions)
        // Associate the versions to the group
        return scenarioGroup.copy(versions = scenarioVersions, enabled = false)
    }

    /**
     * Import many scenario versions and returns the created scenario versions
     * @param scenarioVersions: a list of scenario versions to import
     */
    fun importManyScenarioVersion(namespace: String, scenarioVersions: List<ScenarioVersion>): List<ScenarioVersion> {
        // Check consistency
        checkScenarioVersionsToImport(scenarioVersions)
        // All versions have a same scenarioGroupId, so we get the first one
        val scenarioGroupId = scenarioVersions.first().scenarioGroupId.toString()
        // Find and check existence of scenario group
        findOneScenarioGroup(namespace, scenarioGroupId)
        // Create versions
        return ScenarioVersionService.createMany(scenarioVersions)
    }

    /**
     * Create a new scenario version
     * Returns the created scenario version
     * @param scenarioVersion: the scenario version to create
     * @throws [ScenarioGroupNotFoundException] if the scenario group of the [scenarioVersion] was not found
     */
    fun createOneScenarioVersion(namespace: String, scenarioVersion: ScenarioVersion): ScenarioVersion {
        // Find and check existence of scenario group
        findOneScenarioGroup(namespace, scenarioVersion.scenarioGroupId.toString())

        if(!scenarioVersion.isDraft()){
            throw ScenarioVersionBadStateException("Only a draft version can be created")
        }

        return ScenarioVersionService.createOne(scenarioVersion)
    }

    /**
     * Update a given scenario group
     * Returns the updated scenario group
     * @param namespace: the namespace
     * @param scenarioGroup: the scenario group to update
     * @throws [ScenarioGroupNotFoundException] if the [scenarioGroup] was not found
     */
    fun updateOneScenarioGroup(namespace: String, scenarioGroup: ScenarioGroup): ScenarioGroup {
        val updatedScenarioGroup = ScenarioGroupService.updateOne(scenarioGroup)

        scenarioGroup.enabled?.let {
            StoryService.updateActivateStoryFeatureByNamespaceAndBotIdAndStoryId(
                namespace,
                scenarioGroup.botId,
                scenarioGroup._id.toString(),
                StoryDefinitionConfigurationFeature(enabled = it)
            )
        }

        return updatedScenarioGroup.copy(enabled = scenarioGroup.enabled)
    }

    /**
     * Update a given scenario version
     * Returns the updated scenario version
     * @param scenarioVersion: the scenario version to update.
     * @throws [ScenarioVersionNotFoundException] if the [scenarioVersion] was not found.
     * @throws [ScenarioGroupNotFoundException] if the scenario group of the [scenarioVersion] was not found
     * @throws [MismatchedScenarioException] if the [scenarioVersion] is not part of its scenario group
     * @throws [UnauthorizedUpdateScenarioVersionException] if the [scenarioVersion] cannot be updated
     */
    fun updateOneScenarioVersion(scenarioVersion: ScenarioVersion): ScenarioVersion {
        val scenarioVersionDB = ScenarioVersionService.findOneById(scenarioVersion._id.toString())

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
            val scenarioVersionCurrentDB = ScenarioVersionService.findAllByScenarioGroupIdAndState(
                scenarioVersion.scenarioGroupId.toString(),
                CURRENT
            ).firstOrNull()

            scenarioVersionCurrentDB?.let {
                // Archive the current version if it exists and if the updated scenario version is the new one
                ScenarioVersionService.updateOne(scenarioVersionCurrentDB.copy(state = ARCHIVED))
                logger.info { "Archiving of the old current version of scenario group <id:${scenarioVersion.scenarioGroupId}>" }
            }
        }

        // Update the scenario version
        val scenarioVersionUpdated = ScenarioVersionService.updateOne(scenarioVersion)
        logger.info { "Updating of the scenario version <id:${scenarioVersion._id}>" }

        return scenarioVersionUpdated
    }

    /**
     * Delete an existing scenario group and its versions as well as its tick story
     * @param namespace: the namespace
     * @param botId : id of the bot
     * @param scenarioGroupId: id of the scenario group
     * @throws [ScenarioGroupNotFoundException] if the scenario group was not found
     */
    fun deleteOneScenarioGroup(namespace: String, botId: String, scenarioGroupId: String): Boolean {
        return try {
            // Find and check existence of scenario group
            findOneScenarioGroup(namespace, scenarioGroupId)

            // Delete all versions
            ScenarioVersionService.deleteAllByScenarioGroupId(scenarioGroupId)
            logger.info { "Deleting all versions of the scenario group <id:$scenarioGroupId>" }

            // Delete the tick story
            deleteTickStory(namespace, botId, scenarioGroupId)

            // Delete the scenario group
            ScenarioGroupService.deleteOneById(scenarioGroupId)
            logger.info { "Deleting of the scenario group <id:$scenarioGroupId>" }

            true
        } catch (ex: ScenarioException) {
            logger.warn { ex.message }
            false
        }
    }

    /**
     * Delete an existing scenario version.
     * If the scenario version given is the last one, then delete the scenario group
     * @param namespace: the namespace
     * @param botId : id of the bot
     * @param scenarioGroupId: id of the scenario group
     * @param scenarioVersionId: id of the scenario version
     * @throws [ScenarioGroupNotFoundException] if the scenario group was not found
     * @throws [ScenarioVersionNotFoundException] if the scenario version was not found
     * @throws [MismatchedScenarioException] if the scenario version is not part of its scenario group
     */
    fun deleteOneScenarioVersion(namespace: String, botId: String, scenarioGroupId: String, scenarioVersionId: String): Boolean {
        return try {
            val scenarioVersionDB = ScenarioVersionService.findOneById(scenarioVersionId)

            // Check consistency
            checkConsistencyOfScenarioGroupAndVersion(scenarioGroupId, scenarioVersionDB)

            if (scenarioVersionDB.isCurrent()) {
                // Delete the tick story if the scenario version has a current state
                deleteTickStory(namespace, botId, scenarioGroupId)
            }

            // Delete the scenario version
            ScenarioVersionService.deleteOneById(scenarioVersionId)
            logger.info { "Deleting of the scenario version <id:$scenarioVersionId>" }

            // Delete the scenario group if there is no version left
            deleteEmptyScenarioGroup(scenarioGroupId)

            true
        } catch (ex: ScenarioException) {
            logger.warn { ex.message }
            false
        }

    }

    private fun mapStoryFeatures(namespace: String, scenarioGroup: ScenarioGroup): ScenarioGroup {
        val sc = if(scenarioGroup.versions.any(ScenarioVersion::isCurrent)){
            // If scenario group has a current version, then check tick story
            scenarioGroup.copy(
                enabled = StoryService.getStoryByNamespaceAndBotIdAndStoryId(
                    namespace,
                    scenarioGroup.botId,
                    scenarioGroup._id.toString()
                )?.let { story ->
                        with(story.features) {
                            // If story has no feature, then it is enabled
                            isEmpty()
                                // else check story activation feature
                                .or(firstOrNull { it.isStoryActivation() }?.enabled ?: false)
                        }
                    }
            )
        } else{
            scenarioGroup
        }

        return sc
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

    private fun checkConsistencyOfScenarioGroupAndVersion(scenarioGroupId: String, scenarioVersion: ScenarioVersion) {
        if(scenarioGroupId != scenarioVersion.scenarioGroupId.toString()){
            logger.error { "The scenario version <id:${scenarioVersion._id}> is not part of the scenario group <id:${scenarioVersion.scenarioGroupId}>" }
            throw ScenarioGroupAndVersionMismatchException()
        }
    }

    private fun deleteTickStory(namespace: String, botId: String, scenarioGroupId: String) {
        StoryService.deleteStoryByNamespaceAndBotIdAndStoryId(namespace, botId, scenarioGroupId)
        logger.info { "Deleting of the tick story <storyId:$scenarioGroupId> corresponding to the current version of the scenario group <id:$scenarioGroupId>" }
    }

    private fun deleteEmptyScenarioGroup(scenarioGroupId: String) {
        if (ScenarioVersionService.countAllByScenarioGroupId(scenarioGroupId) == 0L) {
            ScenarioGroupService.deleteOneById(scenarioGroupId)
            logger.info { "Deleting of the scenario group <id:$scenarioGroupId> following the deleting of all these scenario versions" }
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
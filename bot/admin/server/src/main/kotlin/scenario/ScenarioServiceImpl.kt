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

import ai.tock.bot.admin.scenario.ScenarioMapper.Companion.filterActive
import ai.tock.bot.admin.scenario.ScenarioMapper.Companion.filterVersions
import ai.tock.bot.admin.scenario.ScenarioMapper.Companion.withVersions
import ai.tock.bot.admin.scenario.ScenarioMapper.Companion.addVersions
import ai.tock.bot.admin.scenario.ScenarioMapper.Companion.archive
import ai.tock.bot.admin.scenario.ScenarioMapper.Companion.filterExcludeVersions
import ai.tock.bot.admin.scenario.ScenarioMapper.Companion.withDates
import ai.tock.bot.admin.scenario.ScenarioMapper.Companion.excludeVersion
import ai.tock.bot.admin.scenario.ScenarioPredicate.Companion.checkContainsOne

import ai.tock.bot.admin.scenario.ScenarioPredicate.Companion.checkIdNotNull
import ai.tock.bot.admin.scenario.ScenarioPredicate.Companion.checkNotNullForId
import ai.tock.bot.admin.scenario.ScenarioPredicate.Companion.isArchived
import ai.tock.bot.admin.scenario.ScenarioPredicate.Companion.isCurrent
import ai.tock.bot.admin.scenario.ScenarioPredicate.Companion.checkToCreate
import ai.tock.bot.admin.scenario.ScenarioPredicate.Companion.checkToUpdate
import ai.tock.bot.admin.scenario.ScenarioPredicate.Companion.checkContainsVersion
import ai.tock.bot.admin.scenario.ScenarioPredicate.Companion.checkScenarioNotEmpty
import ai.tock.bot.admin.scenario.ScenarioPredicate.Companion.hasVersion
import ai.tock.bot.admin.scenario.ScenarioPredicate.Companion.isVersionOf

import ai.tock.shared.injector
import ai.tock.bot.admin.scenario.ScenarioState.*
import ai.tock.shared.exception.scenario.*
import com.github.salomonbrys.kodein.instance
import mu.KLogger
import mu.KotlinLogging
import java.time.ZonedDateTime

/**
 * Implementation of ScenarioService
 */
class ScenarioServiceImpl : ScenarioService {

    private val logger: KLogger = KotlinLogging.logger {}

    private val scenarioDAO: ScenarioDAO by injector.instance()

    /**
     * Returns all scenarios known
     * @throws ScenarioWithNoIdException when id from database is null
     */
    override fun findAll(): Collection<Scenario> {
        return scenarioDAO.findAll()
            .checkIdNotNull()
    }

    /**
     * Returns all scenarios that are active and not in the state ARCHIVE {@see ScenarioState}
     * @throws ScenarioWithNoIdException when id from database is null
     */
    override fun findAllActive(): Collection<Scenario> {
        return findAll().filterActive()
    }

    private fun findByVersion(version: String): Scenario {
        return scenarioDAO.findByVersion(version)
            .checkNotNullForId(version)
            .checkIdNotNull()
            .checkScenarioNotEmpty()
    }

    /**
     * Returns a specific scenario based on its id
     * @param scenarioId id of scenario to find
     * @throws ScenarioNotFoundException when scenario not found
     * @throws ScenarioWithNoIdException when id from database is null
     */
    override fun findOnlyVersion(version: String): Scenario {
        return findByVersion(version)
            .filterVersions(setOf(version))
            .checkScenarioNotEmpty()
    }

    /**
     * Returns a scenario with all version based on its id
     * @param id of scenario to find
     * @throws ScenarioNotFoundException when scenario not found
     * @throws ScenarioWithNoIdException when id from database is null
     */
    override fun findById(id: String): Scenario {
        return scenarioDAO.findById(id)
            .checkNotNullForId(id)
            .checkIdNotNull()
            .checkScenarioNotEmpty()
    }

    /**
     * Returns the current version of a scenario based on its id
     * @param id of scenario to find
     * @throws ScenarioNotFoundException when scenario not found
     * @throws ScenarioWithNoIdException when id from database is null
     */
    override fun findCurrentById(id: String): Scenario {
        val scenario: Scenario = findById(id)
        return scenario.withVersions(scenario.versions.filter(isCurrent))
    }

    /**
     * Returns scenario with versions not archive based on its id
     * @param id of scenario to find
     * @throws ScenarioNotFoundException when scenario not found
     * @throws ScenarioWithNoIdException when id from database is null
     */
    override fun findActiveById(id: String): Scenario {
        val scenario: Scenario = findById(id)
        return scenario.withVersions(scenario.versions.filterNot(isArchived))
    }

    /**
     * Create a new version on a new scenario or on an existing scenario if id is set
     * @param scenario to create
     * @throws ScenarioEmptyException when version is empty
     * @throws BadScenarioStateException when state is not draft
     * @throws ScenarioWithVersionException when version id is set
     * @throws ScenarioNotFoundException when scenario not found
     * @throws ScenarioWithNoIdException when id from database is null
     */
    override fun create(scenario: Scenario): Scenario {
        val scenarioVersionsInDatabase: List<ScenarioVersion> = findVersionsByIdOrEmpty(scenario.id)
        val scenarioToCreate = scenario.changeDatesForCreation().checkToCreate()
        val scenarioCreated = if(scenarioVersionsInDatabase.isNotEmpty()) {
            // add existing version in database
            scenarioDAO.patch(scenarioToCreate.addVersions(scenarioVersionsInDatabase))
        } else {
            scenarioDAO.create(scenarioToCreate)
        }
        return scenarioCreated.checkNotNullForId(scenario.id)
            .checkIdNotNull()
            // exclude existing version in database before return
            .filterExcludeVersions(scenarioVersionsInDatabase)
            .checkScenarioNotEmpty()
    }

    private fun Scenario.changeDatesForCreation(): Scenario {
        val changeDates: (ScenarioVersion) -> ScenarioVersion = {
            it.withDates(ZonedDateTime.now(), null)
        }
        return withVersions( versions.map(changeDates) )
    }

    private fun findVersionsByIdOrEmpty(id: String?): List<ScenarioVersion> {
        return id?.let { findById(it).versions } ?: emptyList()
    }

    /**
     * Update an existing scenario
     * @param scenarioId id of URI to update scenario
     * @param scenario to update
     * @throws ScenarioWithNoIdException when scenario id is null
     * @throws BadNumberException when scenario contains more than one version to update
     * @throws ScenarioWithNoVersionIdException when scenario contains version with no version sets
     * @throws DuplicateVersionException when scenario contains duplicate version id
     * @throws VersionUnknownException when version to update no in scenario
     * @throws BadScenarioStateException when any version in database or to update is archive
     * @throws BadScenarioVersionException when version in URI don't correspond to version in scenario
     */
    override fun update(version: String, scenario: Scenario): Scenario {
        scenario.checkContainsVersion(version)

        val scenarioFromDatabase: Scenario = findById(scenario.checkIdNotNull().id!!)

        val otherVersionFormDatabase: List<ScenarioVersion> =
            scenarioFromDatabase.prepareOtherVersionToUpdate(scenario.extractVersion(version))

        val scenarioToUpdate =
            scenario
                .changeDatesForUpdate(scenarioFromDatabase)
                .checkToUpdate(scenarioFromDatabase)
                .addVersions(otherVersionFormDatabase)

        return scenarioDAO.update(scenarioToUpdate)
            .checkNotNullForId(scenario.id)
            .checkIdNotNull()
            // remove existing version in database before return
            .filterExcludeVersions(otherVersionFormDatabase)
            .checkScenarioNotEmpty()
    }

    private fun Scenario.extractVersion(version: String): ScenarioVersion {
        return filterVersions(setOf(version)).versions.checkContainsOne()
    }

    private fun Scenario.prepareOtherVersionToUpdate(versionToUpdate: ScenarioVersion): List<ScenarioVersion> {
        val mapIfNotVersionOrElseNull: ScenarioVersion.(ScenarioVersion.() -> ScenarioVersion) -> ScenarioVersion? = { map ->
            if(!isVersionOf(versionToUpdate.version!!)) {
                map()
            } else {
                null
            }
        }
        val archiveIfVersionToUpdateIsCurrent: (ScenarioVersion) -> ScenarioVersion? = {
            it.mapIfNotVersionOrElseNull {
                if(it.isCurrent() && versionToUpdate.isCurrent()) {
                    it.archive()
                } else {
                    it
                }
            }
        }
        return versions.mapNotNull(archiveIfVersionToUpdateIsCurrent)
    }

    /*
     * Update the UpdateDate but preserved the CreateDate from scenario in parameter (from database)
     */
    private fun Scenario.changeDatesForUpdate(scenario: Scenario): Scenario {
        val createDateByVersion = scenario.extractCreationDatesByVersion()
        val changeDates: (ScenarioVersion) -> ScenarioVersion = {
            it.withDates(createDateByVersion[it.version], ZonedDateTime.now())
        }
        return withVersions( versions.map(changeDates) )
    }

    private fun Scenario.extractCreationDatesByVersion(): Map<String, ZonedDateTime?> {
        return versions.filter(hasVersion).associateBy( { it.version!! }, { it.creationDate })
    }

    /**
     * Delete an existing version of scenario
     * If scenario contains only this version, delete the scenario instead
     * If scenario does not already exist, it just logs that it does not exist
     * @param version to delete
     */
    override fun deleteByVersion(version: String) {
        try {
            val scenario: Scenario = findByVersion(version).excludeVersion(version)
            if(scenario.versions.isEmpty()) {
                deleteById(scenario.id!!) //id cannot be null after findByVersion
            } else {
                scenarioDAO.update(scenario) //remove only version
            }
        } catch (notFoundException: ScenarioNotFoundException) {
            logger.debug { "scenario version $version no longer exist and cannot be deleted" }
        }
    }

    /**
     * Delete an existing scenario with all its versions
     * If the scenario does not already exist, it just logs that it does not exist
     * @param id of scenario to delete
     */
    override fun deleteById(id: String) {
        try {
            scenarioDAO.delete(id)
        } catch (notFoundException: ScenarioNotFoundException) {
            logger.debug { "scenario id $id no longer exist and cannot be deleted" }
        }
    }
}
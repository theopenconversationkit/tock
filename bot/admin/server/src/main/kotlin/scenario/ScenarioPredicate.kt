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

import ai.tock.bot.admin.scenario.ScenarioState.*
import ai.tock.shared.exception.scenario.*

class ScenarioPredicate {
    companion object {

        /**
         * Predicate is true when scenario history have a version not null and not blank
         */
        val hasVersion: ScenarioVersion.() -> Boolean = {
            version != null && version!!.isNotBlank()
        }

        /**
         * Throws TockException if scenario cannot be created in database
         * else, return scenario
         */
        fun Scenario.checkToCreate(): Scenario {
            checkScenarioNotEmpty()

            checkContainsOnlyDraft()

            checkContainsNoVersion()

            return this
        }

        /**
         * Throws ScenarioWithVersionException if scenario contains version with version sets
         * else, return scenario
         */
        fun Scenario.checkContainsNoVersion(): Scenario {
            if (versions.any(hasVersion)) {
                val firstVersionNotEmpty = versions.first(hasVersion).version!!
                throw ScenarioWithVersionException(
                    firstVersionNotEmpty,
                    "scenario version must not have version id set when created"
                )
            }
            return this
        }

        /**
         * Throws BadScenarioVersionException if scenario doesn't contain specific version
         * else, return scenario
         */
        fun Scenario.checkContainsVersion(versionToCheck: String): Scenario {
            val haveSameVersion: ScenarioVersion.() -> Boolean = { this.version == versionToCheck }
            if(!versions.any(haveSameVersion)) {
                throw BadScenarioVersionException(versionToCheck, "scenario $versionToCheck expected")
            }
            return this
        }

        /**
         * Throws BadScenarioStateException if scenario contains history with state not set to draft
         * else, return scenario
         */
        fun Scenario.checkContainsOnlyDraft(): Scenario {
            if (!versions.all(isDraft)) {
                val firstBadState: String = versions.first(isDraft).state.name
                throw BadScenarioStateException(
                    listOf(DRAFT.name),
                    firstBadState,
                    "scenario state must be ${DRAFT.name}, but is $firstBadState"
                )
            }
            return this
        }

        /**
         * Return true if version is the specified version
         */
        fun ScenarioVersion.isVersionOf(version: String): Boolean {
            return version == this.version
        }

        /**
         * Predicate is true when scenario history state is draft
         */
        val isDraft: ScenarioVersion.() -> Boolean = {
            isState(DRAFT)
        }

        /**
         * Predicate is true when scenario history state is current
         */
        val isCurrent: ScenarioVersion.() -> Boolean = {
            isState(CURRENT)
        }

        /**
         * Predicate is true when scenario history state is archived
         */
        val isArchived: ScenarioVersion.() -> Boolean = {
            isState(ARCHIVED)
        }

        /*
         * Return true when state is the specified state
         */
        private fun ScenarioVersion.isState(stateRequired: ScenarioState): Boolean {
            return state.equals(stateRequired)
        }

        /**
         * Throws TockException if scenario cannot be updated in database
         * else, return scenario
         */
        fun Scenario.checkToUpdate(scenarioFromDatabase: Scenario): Scenario {
            checkIdMatch(scenarioFromDatabase)

            val versionsToUpdate: Map<String, ScenarioState> = extractVersionsAndCheckIsNotEmpty()

            //retain from database only version to update
            val versionsFromDatabaseToUpdate: Map<String, ScenarioState> =
                scenarioFromDatabase
                    .extractVersionsAndCheckIsNotEmpty()
                    .filterKeys { versionsToUpdate.keys.contains(it) }

            //check that all versions to update already exists in database
            versionsToUpdate.checkContainsNoUnknownVersion(versionsFromDatabaseToUpdate)

            versionsFromDatabaseToUpdate.checkUpdateStateIsValidTo(versionsToUpdate)

            return this
        }

        /**
         * Throws exception if scenarios ids are different
         * else, return scenario
         */
        fun Scenario.checkIdMatch(scenario: Scenario): Scenario {
            if(id == null || !id.equals(scenario.id)) {
                throw MismatchedScenarioException(id, scenario.id, "id to update must be the same than the one in database")
            }
            return this
        }

        /**
         * Create a new valid set of scenario's versions
         * (checked is not null, and not duplicate)
         */
        fun Scenario.extractVersionsAndCheckIsNotEmpty(): Map<String, ScenarioState> {
            val stateByVersions: MutableMap<String, ScenarioState> = mutableMapOf()
            if(versions.isEmpty()) {
                throw ScenarioWithNoVersionIdException(id, "scenario $id contains no version")
            }
            versions.forEach {
                with(it) {
                    if (version.isNullOrEmpty()) {
                        throw ScenarioWithNoVersionIdException(id, "scenario $id contains a version not set")
                    }
                    if (stateByVersions.containsKey(version)) {
                        throw DuplicateVersionException(version!!, "$version is already in scenario $id")
                    }
                    stateByVersions.put(version!!, state)
                }
            }
            return stateByVersions
        }

        /*
         * Throws VersionUnknownException if the set contains a version that is not in the specified set
         * else, return map of scenario state by scenario id
         */
        private fun Map<String, ScenarioState>.checkContainsNoUnknownVersion(
            versionsMustBeIncluded: Map<String, ScenarioState>
        ): Map<String, ScenarioState> {
            val versionsForbidden: Set<String> = keys.minus(versionsMustBeIncluded.keys)
            if(versionsForbidden.isNotEmpty()) {
                val version: String = versionsForbidden.first()
                throw VersionUnknownException(version, "version $version is unknown and cannot be updated")
            }
            return this
        }

        /**
         * business rules to control the possibility of updating a state present in the database to the new desired state
         * else, return map of scenario state by scenario id
         */
        fun Map<String, ScenarioState>.checkUpdateStateIsValidTo(
            stateByVersionToUpdate: Map<String, ScenarioState>
        ): Map<String, ScenarioState> {
            forEach {
                with(it) {
                    // value == DRAFT -> ok
                    // existing draft in database can be updated to everything
                    if (value == CURRENT && stateByVersionToUpdate[key] == DRAFT) {
                        // existing current in database cannot be updated to draft
                        throw BadScenarioStateException(
                            listOf(CURRENT.name, ARCHIVED.name),
                            DRAFT.name,
                            "scenario state must not be update draft"
                        )
                    } else if (value == ARCHIVED) {
                        // existing archive in database cannot be updated
                        throw ScenarioArchivedException(
                            key,
                            "version $key is archive in database and cannot be updated"
                        )
                    }
                }
            }
            return this
        }

        /**
         * Throws ScenarioNotFoundException scenario not found
         * else, return scenario
         */
        fun Scenario?.checkNotNullForId(id: String?): Scenario {
            return this ?: throw ScenarioNotFoundException(id, "scenario not found")
        }

        /*
         * Throws ScenarioWithNoIdException if id is null
         */
        private val checkIdNotNull: Scenario.() -> Unit = {
            if(id == null) {
                throw ScenarioWithNoIdException("scenario from database cannot have id null")
            }
        }

        /*
         * Throws ScenarioWithNoIdException if id is null
         * else, return senario
         */
        fun Scenario.checkIdNotNull(): Scenario {
            checkIdNotNull(this)
            return this
        }

        /**
         * Throws ScenarioWithNoIdException if any scenario id is null
         * else, return collection of scenario
         */
        fun Collection<Scenario>.checkIdNotNull(): Collection<Scenario> {
            forEach(checkIdNotNull)
            return this
        }

        /*
         * Throws ScenarioEmptyException if scenario contains no version
         */
        private val checkScenarioNotEmpty: Scenario.() -> Unit = {
            if (versions.isEmpty()) {
                throw ScenarioEmptyException(id, "scenario $id is empty")
            }
        }

        /**
         * Throws ScenarioEmptyException if scenario contains no version
         * else, return senario
         */
        fun Scenario.checkScenarioNotEmpty(): Scenario {
            checkScenarioNotEmpty(this)
            return this
        }

        /**
         * Throws ScenarioEmptyException if scenario contains no version
         * else, return collection of scenario
         */
        fun Collection<Scenario>.checkScenarioNotEmpty(): Collection<Scenario> {
            forEach(checkScenarioNotEmpty)
            return this
        }

        /**
         * Throws BadNumberException if more than 1 in list
         * else, return first
         */
        inline fun <reified T> Collection<T>.checkContainsOne(): T {
            if (size != 1) {
                throw BadNumberException(1, size, "expected exactly 1 ${T::class.java.name} but found $size")
            }
            return first()
        }

        /**
         * Throws NotFoundException list is empty
         * else, return list
         */
        fun <T> Collection<T>.checkNotEmpty(): Collection<T> {
            if (isEmpty()) {
                throw ScenarioNotFoundException(null, "not found")
            } else if(first() is Scenario) {
                (this as Collection<Scenario>).checkScenarioNotEmpty()
            }
            return this
        }
    }
}

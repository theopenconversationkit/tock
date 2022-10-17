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

import org.litote.kmongo.Id

/**
 * Manage [ScenarioGroupDAO] persistence.
 */
interface ScenarioGroupDAO {

    /**
     * Returns all scenario groups
     */
    fun findAll(): List<ScenarioGroup>

    /**
     * Returns all scenario groups by botId with their scenario versions
     * @param botId: if of the bot
     */
    fun findAllByBotId(botId: String): List<ScenarioGroup>

    /**
     * Returns one scenario group with its scenario versions
     * @param id: id of scenario group
     */
    fun findOneById(id: Id<ScenarioGroup>): ScenarioGroup?

    /**
     * Create a new scenario group and returns the created scenario group
     * @param scenarioGroup: the scenario group to create
     * @throws [DuplicateKeyScenarioGroupException] if the scenario group name is already in use
     */
    fun createOne(scenarioGroup: ScenarioGroup): ScenarioGroup

    /**
     * Update a given scenario group and returns the updated scenario group
     * @param scenarioGroup: the scenario group to update
     * @throws [ScenarioGroupNotFoundException] if the [scenarioGroup] was not found
     */
    fun updateOne(scenarioGroup: ScenarioGroup): ScenarioGroup

    /**
     * Delete an existing scenario group and its versions as well as its tick story
     * @param id: id of the scenario group
     * @throws [ScenarioGroupNotFoundException] if the scenario group was not found
     */
    fun deleteOneById(id: Id<ScenarioGroup>)

}
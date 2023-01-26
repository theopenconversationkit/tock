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
import ai.tock.bot.admin.scenario.ScenarioGroupDAO
import ai.tock.shared.exception.scenario.group.ScenarioGroupNotFoundException
import ai.tock.shared.injector
import com.github.salomonbrys.kodein.instance
import org.litote.kmongo.toId

/**
 * Service that manage the scenario groups
 */
object ScenarioGroupService {
    private val scenarioGroupDAO: ScenarioGroupDAO by injector.instance()

    /**
     * Returns all scenario groups by botId with their scenario versions
     * @param botId: if of the bot
     */
    fun findAllByBotId(botId: String): List<ScenarioGroup> {
        return scenarioGroupDAO.findAllByBotId(botId)
    }

    /**
     * Returns one scenario group with its scenario versions
     * @param scenarioGroupId : id of the scenario group
     * @throws [ScenarioGroupNotFoundException] if the scenario group was not found
     */
    fun findOneById(scenarioGroupId: String): ScenarioGroup{
        val scenarioGroup = scenarioGroupDAO.findOneById(scenarioGroupId.toId())
        scenarioGroup ?: throw ScenarioGroupNotFoundException(scenarioGroupId)

        return scenarioGroup
    }

    /**
     * Create a new scenario group and returns the created scenario group
     * @param scenarioGroup: the scenario group to create
     * @throws [DuplicateKeyScenarioGroupException] if the scenario group name is already in use
     */
    fun createOne(scenarioGroup: ScenarioGroup): ScenarioGroup {
        return scenarioGroupDAO.createOne(scenarioGroup)
    }

    /**
     * Update a given scenario group and returns the updated scenario group
     * @param scenarioGroup: the scenario group to update
     * @throws [ScenarioGroupNotFoundException] if the [scenarioGroup] was not found
     */
    fun updateOne(scenarioGroup: ScenarioGroup): ScenarioGroup {
        return scenarioGroupDAO.updateOne(scenarioGroup)
    }

    /**
     * Delete an existing scenario group and its versions as well as its tick story
     * @param id: id of the scenario group
     * @throws [ScenarioGroupNotFoundException] if the scenario group was not found
     */
    fun deleteOneById(id: String) {
        scenarioGroupDAO.deleteOneById(id.toId())
    }

}
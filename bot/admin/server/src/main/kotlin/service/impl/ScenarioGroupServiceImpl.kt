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
import ai.tock.bot.admin.scenario.ScenarioGroupDAO
import ai.tock.bot.admin.service.ScenarioGroupService
import ai.tock.shared.exception.scenario.group.ScenarioGroupNotFoundException
import ai.tock.shared.injector
import com.github.salomonbrys.kodein.instance
import org.litote.kmongo.toId

/**
 * Implementation of ScenarioGroupService
 */
object ScenarioGroupServiceImpl : ScenarioGroupService {
    private val scenarioGroupDAO: ScenarioGroupDAO by injector.instance()

    override fun findAllByBotId(botId: String): List<ScenarioGroup> {
        return scenarioGroupDAO.findAllByBotId(botId)
    }

    override fun findOneById(scenarioGroupId: String): ScenarioGroup {
        val scenarioGroup = scenarioGroupDAO.findOneById(scenarioGroupId.toId())
        scenarioGroup ?: throw ScenarioGroupNotFoundException(scenarioGroupId)

        return scenarioGroup
    }

    override fun createOne(scenarioGroup: ScenarioGroup): ScenarioGroup {
        return scenarioGroupDAO.createOne(scenarioGroup)
    }

    override fun updateOne(scenarioGroup: ScenarioGroup): ScenarioGroup {
        return scenarioGroupDAO.updateOne(scenarioGroup)
    }

    override fun deleteOneById(id: String) {
        scenarioGroupDAO.deleteOneById(id.toId())
    }

}
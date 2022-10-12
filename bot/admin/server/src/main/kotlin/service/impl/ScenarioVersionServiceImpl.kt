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

import ai.tock.bot.admin.scenario.ScenarioVersion
import ai.tock.bot.admin.scenario.ScenarioVersionDAO
import ai.tock.bot.admin.scenario.ScenarioVersionState
import ai.tock.bot.admin.service.ScenarioVersionService
import ai.tock.shared.injector
import ai.tock.shared.exception.scenario.version.ScenarioVersionNotFoundException
import com.github.salomonbrys.kodein.instance
import org.litote.kmongo.toId

/**
 * Implementation of ScenarioVersionService
 */
class ScenarioVersionServiceImpl : ScenarioVersionService {
    private val scenarioVersionDAO: ScenarioVersionDAO by injector.instance()

    override fun findOneById(id: String): ScenarioVersion {
        val scenarioVersion = scenarioVersionDAO.findOneById(id.toId())
        scenarioVersion ?: throw ScenarioVersionNotFoundException(id)

        return scenarioVersion
    }

    override fun findAllByScenarioGroupIdAndState(scenarioGroupId: String, state: ScenarioVersionState): List<ScenarioVersion> {
        return scenarioVersionDAO.findAllByScenarioGroupIdAndState(scenarioGroupId.toId(), state)
    }

    override fun createOne(scenarioVersion: ScenarioVersion): ScenarioVersion {
        return scenarioVersionDAO.createOne(scenarioVersion)
    }

    override fun createMany(scenarioVersions: List<ScenarioVersion>): List<ScenarioVersion> {
        return scenarioVersionDAO.createMany(scenarioVersions)
    }

    override fun updateOne(scenarioVersion: ScenarioVersion): ScenarioVersion {
        return scenarioVersionDAO.updateOne(scenarioVersion)
    }

    override fun deleteOneById(id: String) {
        scenarioVersionDAO.deleteOneById(id.toId())
    }

    override fun deleteAllByScenarioGroupId(scenarioGroupId: String) {
        scenarioVersionDAO.deleteAllByScenarioGroupId(scenarioGroupId.toId())
    }

    override fun countAllByScenarioGroupId(scenarioGroupId: String): Long {
        return scenarioVersionDAO.countAllByScenarioGroupId(scenarioGroupId.toId())
    }

}
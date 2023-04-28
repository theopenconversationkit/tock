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

import ai.tock.bot.admin.scenario.ScenarioSettings
import ai.tock.bot.admin.scenario.ScenarioSettingsDAO
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.ScenarioSettingsQuery
import ai.tock.shared.injector
import ai.tock.shared.provide
import java.time.Instant

object ScenarioSettingsService   {

    private val scenarioSettingsDAO: ScenarioSettingsDAO get() = injector.provide()
    fun save(applicationDefinition: ApplicationDefinition, query: ScenarioSettingsQuery) {
        val settings = scenarioSettingsDAO.getScenarioSettingsByBotId(applicationDefinition._id.toString())?.copy(
            actionRepetitionNumber = query.actionRepetitionNumber,
            redirectStoryId = query.redirectStoryId,
            updateDate = Instant.now()
        ) ?: ScenarioSettings(
                botId = applicationDefinition.name,
                actionRepetitionNumber = query.actionRepetitionNumber,
                redirectStoryId = query.redirectStoryId,
                creationDate = Instant.now(),
                updateDate = Instant.now()
            )
        scenarioSettingsDAO.save(settings)
    }

    fun getScenarioSettingsByBotId(id: String): ScenarioSettings? {
        return scenarioSettingsDAO.getScenarioSettingsByBotId(id)
    }

    fun listenChanges(listener: (ScenarioSettings) -> Unit) {
        scenarioSettingsDAO.listenChanges(listener)
    }

}
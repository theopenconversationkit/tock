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

import ai.tock.nlp.front.service.storage.ScenarioSettingsDAO
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.ScenarioSettings
import ai.tock.nlp.front.shared.config.ScenarioSettingsQuery
import ai.tock.shared.injector
import com.github.salomonbrys.kodein.instance
import org.litote.kmongo.toId
import java.time.Instant

object ScenarioSettingsService   {

    private val scenarioSettingsDAO: ScenarioSettingsDAO by injector.instance()

    fun save(applicationDefinition: ApplicationDefinition, query: ScenarioSettingsQuery) {
        val settings = scenarioSettingsDAO.getScenarioSettingsByApplicationId(applicationDefinition._id)?.copy(
            actionRepetitionNumber = query.actionRepetitionNumber,
            redirectStoryId = query.redirectStoryId,
            updateDate = Instant.now()
        ) ?: ScenarioSettings(
                applicationId = applicationDefinition._id,
                actionRepetitionNumber = query.actionRepetitionNumber,
                redirectStoryId = query.redirectStoryId,
                creationDate = Instant.now(),
                updateDate = Instant.now()
            )
        scenarioSettingsDAO.save(settings)
    }

    fun getScenarioSettingsByApplicationId(id: String): ScenarioSettings? {
        return scenarioSettingsDAO.getScenarioSettingsByApplicationId(id.toId())
    }

    fun listenChanges(listener: (ScenarioSettings) -> Unit) {
        scenarioSettingsDAO.listenChanges(listener)
    }

}
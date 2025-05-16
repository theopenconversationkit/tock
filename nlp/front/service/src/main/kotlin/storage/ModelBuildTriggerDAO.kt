/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
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

package ai.tock.nlp.front.service.storage

import ai.tock.nlp.front.shared.build.ModelBuild
import ai.tock.nlp.front.shared.build.ModelBuildQueryResult
import ai.tock.nlp.front.shared.build.ModelBuildTrigger
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import org.litote.kmongo.Id
import java.util.Locale

/**
 *
 */
interface ModelBuildTriggerDAO {

    fun save(trigger: ModelBuildTrigger)

    fun deleteTrigger(trigger: ModelBuildTrigger)

    fun getTriggers(): List<ModelBuildTrigger>

    fun save(build: ModelBuild)

    fun builds(applicationId: Id<ApplicationDefinition>, language: Locale, start: Int, size: Int): ModelBuildQueryResult
}

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

package ai.tock.nlp.front.shared.config

import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.time.Instant

data class ScenarioSettings (

    /**
     * The unique [Id] of the settings.
     */
    val _id: Id<ScenarioSettings> = newId(),

    /**
     * The application id.
     */
    val applicationId: Id<ApplicationDefinition>,

    /**
     * The number of authorized repetition of an action.
     */
    val actionRepetitionNumber: Int,

    /**
     * The story's id to redirect to when the actionRepetitionNumber is exceeded.
     */
    val redirectStoryId: String?=  null,

    /**
     * Settings creation date
     */
    val creationDate: Instant,

    /**
     * Settings update date
     */
    val updateDate: Instant,

    ) {
    fun toScenarioSettingsQuery(): ScenarioSettingsQuery{
        return ScenarioSettingsQuery(actionRepetitionNumber, redirectStoryId)
    }
}
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
import org.litote.kmongo.newId
import java.time.Instant

/**
 * A scenario settings
 *
 * @param _id : The unique [Id] of the settings.
 * @param botId : The application name.
 * @param actionRepetitionNumber : The number of authorized repetition of an action.
 * @param redirectStoryId : The story's id to redirect to when the actionRepetitionNumber is exceeded.
 * @param creationDate : Settings creation date
 * @param updateDate : Settings update date
 */
data class ScenarioSettings (
    val _id: Id<ScenarioSettings> = newId(),
    val botId: String,
    val actionRepetitionNumber: Int,
    val redirectStoryId: String?=  null,
    val creationDate: Instant,
    val updateDate: Instant
)

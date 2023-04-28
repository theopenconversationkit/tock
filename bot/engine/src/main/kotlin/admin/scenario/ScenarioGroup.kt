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

import ai.tock.shared.exception.scenario.group.ScenarioGroupInvalidException
import ai.tock.shared.exception.scenario.group.ScenarioGroupNotFoundException
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.lang.Exception
import java.time.ZonedDateTime


data class ScenarioGroup(
    val _id: Id<ScenarioGroup> = newId(),
    val botId: String,
    val name: String,
    val category: String? = null,
    val tags: List<String> = emptyList(),
    val description: String? = null,
    val creationDate: ZonedDateTime = ZonedDateTime.now(),
    val updateDate: ZonedDateTime = ZonedDateTime.now(),
    val versions: List<ScenarioVersion> = emptyList(),
    @Transient
    val enabled: Boolean? = null,
    val unknownAnswerId: String
) {
    init {
        try {
            require(name.isNotBlank()) { "The scenario group name is required !" }
            require(unknownAnswerId.isNotBlank()) { "The unknown answer id is required !" }
        }catch (e: Exception) {
            throw ScenarioGroupInvalidException(e.message)
        }
    }
}
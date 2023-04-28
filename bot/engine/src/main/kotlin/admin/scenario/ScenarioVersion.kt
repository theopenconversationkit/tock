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
import java.time.ZonedDateTime

data class ScenarioVersion(
    val _id: Id<ScenarioVersion> = newId(),
    val scenarioGroupId: Id<ScenarioGroup>,
    val creationDate: ZonedDateTime = ZonedDateTime.now(),
    val updateDate: ZonedDateTime = ZonedDateTime.now(),
    val data: Any? = null,
    val state: ScenarioVersionState,
    val comment: String
) {

    /**
     * Return true when scenario version state is [ScenarioVersionState.DRAFT]
     */
    fun isDraft(): Boolean = isState(ScenarioVersionState.DRAFT)

    /**
     * Return true when scenario version state is [ScenarioVersionState.CURRENT]
     */
    fun isCurrent(): Boolean = isState(ScenarioVersionState.CURRENT)

    /**
     * Return true when state is the specified state [ScenarioVersionState]
     */
    private fun isState(stateRequired: ScenarioVersionState): Boolean {
        return state === stateRequired
    }
}

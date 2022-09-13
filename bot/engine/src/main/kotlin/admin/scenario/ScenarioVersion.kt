/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

import java.time.ZonedDateTime

/**
 * There was no functional reflection on what should be versioned and what should not be.
 * As a result, everything has been versioned to be able to provide coherent responses
 * to the front in relation to their requests.
 * It seems important to me to take the time to rethink the problem so as not to version everything.
 * Some elements such as the name, or the description, or the applicationID for example,
 * are common to all versions of the same scenario and should therefore be saved in the parent object (Scenario).
 * All variables that appear moveable have a //must be moved comment.
 * Think about it.
 */
data class ScenarioVersion(
    val version: String? = null,
    val name: String, //must be moved in Scenario ?
    val category: String? = null, //must be moved in Scenario ?
    val tags: List<String> = emptyList(), //must be moved in Scenario ?
    val applicationId: String, //must be moved in Scenario ?
    val creationDate: ZonedDateTime? = null,
    val updateDate: ZonedDateTime? = null,
    val description: String? = null, //must be moved in Scenario ?
    val data: String? = null,
    val state: ScenarioState
)

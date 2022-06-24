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

class ScenarioAbstractTest {
//    companion object {
//        fun draft(
//            version: String?,
//            createDate: ZonedDateTime? = null,
//            updateDate: ZonedDateTime? = null
//        ): ScenarioVersion {
//            return createScenarioVersion(version, ScenarioVersionState.DRAFT, createDate, updateDate)
//        }
//
//        fun current(
//            version: String?,
//            createDate: ZonedDateTime? = null,
//            updateDate: ZonedDateTime? = null
//        ): ScenarioVersion {
//            return createScenarioVersion(version, ScenarioVersionState.CURRENT, createDate, updateDate)
//        }
//
//        fun archived(
//            version: String?,
//            createDate: ZonedDateTime? = null,
//            updateDate: ZonedDateTime? = null
//        ): ScenarioVersion {
//            return createScenarioVersion(version, ScenarioVersionState.ARCHIVED, createDate, updateDate)
//        }
//
//        private fun createScenarioVersion(
//            version: String?,
//            state: ScenarioVersionState,
//            createDate: ZonedDateTime?,
//            updateDate: ZonedDateTime?
//        ): ScenarioVersion {
//            return ScenarioVersion(
//                version = version,
//                name = "test",
//                applicationId = "test",
//                creationDate = createDate,
//                updateDate = updateDate,
//                state = state
//            )
//        }
//
//        fun createScenarioResult(sagaId: String, id: String, state: ScenarioVersionState): ScenarioResult {
//            return ScenarioResult(
//                sagaId = sagaId,
//                id = id,
//                name = "test",
//                applicationId = "test",
//                state = state.value.uppercase()
//            )
//        }
//
//        fun createScenario(id: String?, vararg versions: ScenarioVersion): Scenario {
//            return Scenario(
//                id = id,
//                versions = versions.toList()
//            )
//        }
//
//        fun createScenarioRequest(sagaId: String? = null, id: String? = null, state: ScenarioVersionState = ScenarioVersionState.DRAFT): ScenarioRequest {
//            return ScenarioRequest(
//                id = id,
//                sagaId = sagaId,
//                name = "test",
//                applicationId = "test",
//                state = state.value
//            )
//        }
//    }
}
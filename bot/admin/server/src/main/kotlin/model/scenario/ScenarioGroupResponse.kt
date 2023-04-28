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

package ai.tock.bot.admin.model.scenario

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.ZonedDateTime

@JsonInclude(JsonInclude.Include.ALWAYS)
data class ScenarioGroupResponse(
    val id: String,
    val name: String,
    val category: String? = null,
    val tags: List<String> = emptyList(),
    val description: String? = null,
    val creationDate: ZonedDateTime,
    val updateDate: ZonedDateTime,
    val versions: List<ScenarioVersionWithoutData> = emptyList(),
    val enabled: Boolean? = null,
    val unknownAnswerId: String,
)

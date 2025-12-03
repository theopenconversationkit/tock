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

package ai.tock.bot.connector.ga.model.request

import com.fasterxml.jackson.annotation.JsonIgnore

data class GAArgument(
    val name: String,
    val rawText: String? = null,
    val boolValue: Boolean? = null,
    val textValue: String? = null,
    val datetimeValue: GADateTime? = null,
    val extension: GAArgumentValue? = null,
) {
    @get:JsonIgnore
    val builtInArg: GAArgumentBuiltInName? =
        try {
            GAArgumentBuiltInName.valueOf(name)
        } catch (e: Exception) {
            null
        }

    /**
     * Is it a google bot?
     */
    @get:JsonIgnore
    val healthcheck: Boolean = name == "is_health_check" && boolValue == true
}

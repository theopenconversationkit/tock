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

package ai.tock.bot.api.model.context

import ai.tock.nlp.entity.Value

data class Entity(
    /**
     * Type of the entity.
     */
    val type: String,
    /**
     * Role of the entity.
     */
    val role: String,
    /**
     * Text content if any.
     */
    var content: String?,
    /**
     * Value if any.
     */
    var value: Value? = null,
    /**
     * Is the value has been evaluated?
     */
    val evaluated: Boolean = false,
    /**
     * Sub entity values if any.
     */
    val subEntities: List<Entity> = emptyList(),
    /**
     * Is it a entity evaluated now?
     */
    val new: Boolean,
)

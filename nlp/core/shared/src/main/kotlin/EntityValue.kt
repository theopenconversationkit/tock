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

package ai.tock.nlp.core

/**
 * An entity value.
 */
data class EntityValue(
    /**
     * Start (inclusive) text index of the entity.
     */
    override val start: Int,
    /**
     * End (exclusive) text index of the entity.
     */
    override val end: Int,
    /**
     * Entity definition.
     */
    val entity: Entity,
    /**
     * Current value if evaluated.
     */
    val value: Any? = null,
    /**
     * Sub entities if any.
     */
    val subEntities: List<EntityRecognition> = emptyList(),
    /**
     * Is this entity has been evaluated?
     */
    val evaluated: Boolean = false,
) : IntOpenRange

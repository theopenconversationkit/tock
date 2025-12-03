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

package ai.tock.nlp.front.shared.config

import org.litote.kmongo.Id
import org.litote.kmongo.newId

/**
 * A type of entity.
 */
data class EntityTypeDefinition(
    /**
     * The qualified name of the entity (ie namespace:name)
     */
    val name: String,
    /**
     * The description of the entity.
     */
    val description: String = "",
    /**
     * The sub entities of this entity.
     */
    val subEntities: List<EntityDefinition> = emptyList(),
    /**
     * Is the entity based on a dictionary (predefined set of data)?
     */
    val dictionary: Boolean = false,
    /**
     * Is the entity has to be systematically obfuscated?
     */
    val obfuscated: Boolean = false,
    /**
     * The unique id of the entity.
     */
    val _id: Id<EntityTypeDefinition> = newId(),
)

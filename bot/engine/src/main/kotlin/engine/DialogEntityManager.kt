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

package ai.tock.bot.engine

import ai.tock.bot.engine.dialog.EntityValue
import ai.tock.nlp.api.client.model.Entity
import ai.tock.nlp.entity.Value
import kotlin.reflect.safeCast

interface DialogEntityManager {
    /**
     * Returns the current value for the specified entity role.
     */
    fun <T : Value> entityValue(
        role: String,
        valueTransformer: (EntityValue) -> T?
    ): T?

    /**
     * Returns the current value for the specified entity.
     */
    fun <T : Value> entityValue(
        entity: Entity,
        valueTransformer: (EntityValue) -> T?
    ): T? = entityValue(entity.role, valueTransformer)

    /**
     * Returns the current text content for the specified entity.
     */
    fun entityText(entity: Entity): String? = entityValueDetails(entity)?.content

    /**
     * Returns the current text content for the specified entity.
     */
    fun entityText(role: String): String? = entityValueDetails(role)?.content

    /**
     * Returns the current [EntityValue] for the specified entity.
     */
    fun entityValueDetails(entity: Entity): EntityValue? = entityValueDetails(entity.role)

    /**
     * Returns the current [EntityValue] for the specified role.
     */
    fun entityValueDetails(role: String): EntityValue?

    /**
     * Updates the current entity value in the dialog.
     * @param role entity role
     * @param newValue the new entity value
     */
    fun changeEntityValue(role: String, newValue: EntityValue?)

    /**
     * Updates the current entity value in the dialog.
     * @param entity the entity definition
     * @param newValue the new entity value
     */
    fun changeEntityValue(entity: Entity, newValue: Value?)

    /**
     * Updates the current entity value in the dialog.
     * @param entity the entity definition
     * @param newValue the new entity value
     */
    fun changeEntityValue(entity: Entity, newValue: EntityValue) = changeEntityValue(entity.role, newValue)

    /**
     * Updates the current entity text value in the dialog.
     * @param entity the entity definition
     * @param textContent the new entity text content
     */
    fun changeEntityText(entity: Entity, textContent: String?) =
        changeEntityValue(
            entity.role,
            EntityValue(entity, null, textContent)
        )

    /**
     * Removes entity value for the specified role.
     */
    fun removeEntityValue(role: String) = changeEntityValue(role, null)

    /**
     * Removes entity value for the specified role.
     */
    fun removeEntityValue(entity: Entity) = removeEntityValue(entity.role)

    /**
     * Removes all current entity values.
     */
    fun removeAllEntityValues()
}

inline fun <reified T : Value> DialogEntityManager.entityValue(role: String) = entityValue(role) { T::class.safeCast(it.value) }
inline fun <reified T : Value> DialogEntityManager.entityValue(type: Entity) = entityValue(type) { T::class.safeCast(it.value) }

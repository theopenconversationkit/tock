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

package ai.tock.nlp.front.service.storage

import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.IntentDefinition
import org.litote.kmongo.Id

/**
 *
 */
interface IntentDefinitionDAO {
    /**
     * Listen changes on entity type definitions.
     */
    fun listenIntentDefinitionChanges(listener: () -> Unit)

    fun getIntentsByApplicationId(applicationId: Id<ApplicationDefinition>): List<IntentDefinition>

    fun getIntentsByNamespace(namespace: String): List<IntentDefinition>

    fun getIntentByNamespaceAndName(
        namespace: String,
        name: String,
    ): IntentDefinition?

    fun getIntentById(id: Id<IntentDefinition>): IntentDefinition?

    fun getIntentByIds(ids: Set<Id<IntentDefinition>>): List<IntentDefinition>?

    fun save(intent: IntentDefinition)

    fun deleteIntentById(id: Id<IntentDefinition>)

    fun getIntentsUsingEntity(entityType: String): List<IntentDefinition>

    fun getIntentsByApplicationIdAndCategory(
        applicationId: Id<ApplicationDefinition>,
        category: String,
    ): List<IntentDefinition>
}

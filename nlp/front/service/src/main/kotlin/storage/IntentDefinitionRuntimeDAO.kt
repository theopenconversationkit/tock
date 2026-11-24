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
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.provideOrDefault
import org.litote.kmongo.Id

/**
 * The part of [IntentDefinitionDAO] that the NLP API uses at runtime: the parsing and the configuration it caches.
 *
 * A storage that only serves the NLP API (a serverless one) binds this interface. Otherwise, [fromInjector] adapts
 * the bound [IntentDefinitionDAO]: the existing storages and the projects that replace them keep working unchanged.
 */
interface IntentDefinitionRuntimeDAO {
    fun listenIntentDefinitionChanges(listener: () -> Unit)

    fun getIntentsByApplicationId(applicationId: Id<ApplicationDefinition>): List<IntentDefinition>

    /** The intents of [namespace]: the intents shared with the applications that import it. */
    fun getIntentsByNamespace(namespace: String): List<IntentDefinition>

    companion object {
        /**
         * The bound [IntentDefinitionRuntimeDAO], or the bound [IntentDefinitionDAO] adapted to it.
         */
        fun fromInjector(): IntentDefinitionRuntimeDAO = injector.provideOrDefault<IntentDefinitionRuntimeDAO> { injector.provide<IntentDefinitionDAO>().asRuntimeDAO() }
    }
}

/**
 * Adapts this DAO to [IntentDefinitionRuntimeDAO].
 */
fun IntentDefinitionDAO.asRuntimeDAO(): IntentDefinitionRuntimeDAO = IntentDefinitionRuntimeDAOAdapter(this)

private class IntentDefinitionRuntimeDAOAdapter(
    private val dao: IntentDefinitionDAO,
) : IntentDefinitionRuntimeDAO {
    override fun listenIntentDefinitionChanges(listener: () -> Unit) = dao.listenIntentDefinitionChanges(listener)

    override fun getIntentsByApplicationId(applicationId: Id<ApplicationDefinition>): List<IntentDefinition> = dao.getIntentsByApplicationId(applicationId)

    override fun getIntentsByNamespace(namespace: String): List<IntentDefinition> = dao.getIntentsByNamespace(namespace)
}

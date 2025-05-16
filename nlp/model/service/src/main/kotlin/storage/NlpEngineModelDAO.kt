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

package ai.tock.nlp.model.service.storage

import ai.tock.nlp.model.EntityContextKey
import ai.tock.nlp.model.IntentContext.IntentContextKey
import java.io.InputStream
import java.time.Instant

/**
 * To save, load and update model files.
 */
interface NlpEngineModelDAO {

    fun getEntityModelInputStream(key: EntityContextKey): NlpModelStream?
    fun saveEntityModel(key: EntityContextKey, stream: InputStream)
    fun getEntityModelLastUpdate(key: EntityContextKey): Instant?
    fun deleteEntityModelsNotIn(keys: List<EntityContextKey>)
    fun deleteEntityModel(key: EntityContextKey)
    /**
     * Listen changes on entity models.
     */
    fun listenEntityModelChanges(listener: (String) -> Unit)

    fun getIntentModelInputStream(key: IntentContextKey): NlpModelStream?
    fun saveIntentModel(key: IntentContextKey, stream: InputStream)
    fun getIntentModelLastUpdate(key: IntentContextKey): Instant?
    fun deleteIntentModelsNotIn(keys: List<IntentContextKey>)
    fun deleteIntentModel(key: IntentContextKey)
    /**
     * Listen changes on intent models.
     */
    fun listenIntentModelChanges(listener: (String) -> Unit)
}

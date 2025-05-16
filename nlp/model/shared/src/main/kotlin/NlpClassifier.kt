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

package ai.tock.nlp.model

import ai.tock.nlp.core.EntityRecognition
import ai.tock.nlp.core.IntentClassification
import ai.tock.nlp.core.NlpEngineType

/**
 * NLP query operations.
 */
interface NlpClassifier : ModelBuilder {

    fun supportedNlpEngineTypes(): Set<NlpEngineType>

    fun classifyIntent(
        context: IntentContext,
        modelHolder: ModelHolder,
        text: String
    ): IntentClassification

    fun classifyEntities(
        context: EntityCallContext,
        modelHolder: ModelHolder,
        text: String
    ): List<EntityRecognition>

    fun classifyIntent(
        context: IntentContext,
        text: String
    ): IntentClassification

    fun classifyEntities(
        context: EntityCallContext,
        text: String
    ): List<EntityRecognition>
}

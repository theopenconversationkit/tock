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
package ai.tock.nlp.systemone

import ai.tock.nlp.core.EntityRecognition
import ai.tock.nlp.model.EntityCallContext
import ai.tock.nlp.model.service.engine.EntityModelHolder
import ai.tock.nlp.model.service.engine.NlpEntityClassifier

/**
 * Clef / Clef-flash are decision models, not span-level taggers: they have no entity
 * extraction capability, so this classifier always returns an empty list.
 *
 * To get entities for an application configured to use Clef for intents, set a dedicated
 * `entityNlpEngineType` (e.g. opennlp) on the application - see this module's README.
 */
internal class SystemOneEntityClassifier(
    model: EntityModelHolder,
) : NlpEntityClassifier(model) {
    override fun classifyEntities(
        context: EntityCallContext,
        text: String,
        tokens: Array<String>,
    ): List<EntityRecognition> = emptyList()
}

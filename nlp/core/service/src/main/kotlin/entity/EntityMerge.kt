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

package ai.tock.nlp.core.service.entity

import ai.tock.nlp.core.CallContext
import ai.tock.nlp.core.EntityRecognition
import ai.tock.nlp.core.Intent

/**
 *
 */
internal interface EntityMerge {

    fun mergeEntityTypes(
        callContext: CallContext,
        text: String,
        intent: Intent,
        entities: List<EntityRecognition>,
        entityTypes: List<EntityTypeRecognition>
    ): List<EntityRecognition>
}

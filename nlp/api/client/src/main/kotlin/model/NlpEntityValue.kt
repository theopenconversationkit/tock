/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.nlp.api.client.model

import ai.tock.nlp.entity.Value

/**
 * Entity value returned by [NlpResult].
 */
data class NlpEntityValue(
    val start: Int,
    val end: Int,
    val entity: Entity,
    val value: Value? = null,
    val evaluated: Boolean = false,
    val subEntities: List<NlpEntityValue> = emptyList(),
    val probability: Double = 1.0,
    val mergeSupport: Boolean = false
)
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

package fr.vsct.tock.nlp.api.client.model

import java.util.Locale


/**
 *
 */
data class NlpResult(val intent: String,
                     val intentNamespace: String,
                     val language: Locale,
                     val entities: List<EntityValue>,
                     val intentProbability: Double,
                     val entitiesProbability: Double,
                     val retainedQuery: String,
                     val otherIntentsProbabilities: Map<String, Double> = emptyMap()) {

    fun firstValue(role: String): EntityValue? = entities.firstOrNull { it.entity.role == role }

    fun entityTextContent(value: EntityValue): String = retainedQuery.substring(value.start, value.end)
}
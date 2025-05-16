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

package ai.tock.bot.engine.nlp

import ai.tock.bot.definition.Intent
import ai.tock.bot.engine.dialog.EntityValue
import ai.tock.nlp.api.client.model.NlpQuery
import ai.tock.nlp.api.client.model.NlpResult
import java.util.Locale

/**
 * Stats about nlp call.
 */
data class NlpCallStats(
    val locale: Locale,
    val intentResult: Intent = Intent.unknown,
    val entityResult: List<EntityValue> = emptyList(),
    val entityResultAfterMerge: List<EntityValue> = emptyList(),
    val nlpQuery: NlpQuery,
    val nlpResult: NlpResult
) {
    /**
     * Entity ranges to be obfuscated.
     */
    fun obfuscatedRanges(): List<IntRange> = entityResult.asSequence().filter { it.entity.entityType.obfuscated }.mapNotNull { it.toClosedRange() }.toList()
}

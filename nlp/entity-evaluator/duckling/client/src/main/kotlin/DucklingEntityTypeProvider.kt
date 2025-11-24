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

package ai.tock.duckling.client

import ai.tock.duckling.client.DucklingDimensions.DUCKLING
import ai.tock.duckling.client.DucklingDimensions.dimensions
import ai.tock.nlp.core.service.entity.EntityTypeClassifier
import ai.tock.nlp.core.service.entity.EntityTypeEvaluator
import ai.tock.nlp.core.service.entity.EntityTypeProvider
import ai.tock.shared.booleanProperty

private val ducklingEnabled = booleanProperty("tock_duckling_enabled", true)

private val entityTypes =
    setOf(
        "duckling:datetime",
        "duckling:temperature",
        "duckling:number",
        "duckling:ordinal",
        "duckling:distance",
        "duckling:volume",
        "duckling:amount-of-money",
        "duckling:duration",
        "duckling:email",
        "duckling:url",
        "duckling:phone-number",
    )

/**
 *
 */
internal class DucklingEntityTypeProvider : EntityTypeProvider {
    override fun supportedEntityTypes(): Set<String> = if (ducklingEnabled) entityTypes else emptySet()

    override fun getEntityTypeClassifier(): EntityTypeClassifier = DucklingParser

    override fun getEntityTypeEvaluator(): EntityTypeEvaluator = DucklingParser

    override fun supportClassification(
        namespace: String,
        entityTypeName: String,
    ): Boolean = supportEvaluation(namespace, entityTypeName)

    override fun supportEvaluation(
        namespace: String,
        entityTypeName: String,
    ): Boolean = ducklingEnabled && namespace == DUCKLING && dimensions.contains(entityTypeName)

    override fun supportValuesMerge(
        namespace: String,
        entityTypeName: String,
    ): Boolean = ducklingEnabled && namespace == DUCKLING && entityTypeName == DucklingDimensions.DATETIME_DIMENSION

    override fun healthcheck(): Boolean = if (ducklingEnabled) DucklingClient.healthcheck() else true
}

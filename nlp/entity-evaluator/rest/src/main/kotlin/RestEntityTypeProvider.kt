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

package ai.tock.nlp.entity.rest

import ai.tock.nlp.core.service.entity.EntityTypeClassifier
import ai.tock.nlp.core.service.entity.EntityTypeProvider
import ai.tock.nlp.core.service.entity.EntityTypeRecognition
import ai.tock.nlp.model.EntityCallContext
import kotlin.LazyThreadSafetyMode.PUBLICATION

/**
 * [EntityTypeProvider] that classifies entities using a rest API.
 */
class RestEntityTypeProvider(
    private val client: RestEntityTypeClient = RestEntityTypeClient()
) : EntityTypeProvider, EntityTypeClassifier {

    private val entityTypes: Set<String> by lazy(PUBLICATION) { client.retrieveSupportedEntityTypes() }

    override fun supportedEntityTypes(): Set<String> = entityTypes

    override fun getEntityTypeClassifier(): EntityTypeClassifier = this

    override fun healthcheck(): Boolean = client.healthcheck()

    override fun classifyEntities(context: EntityCallContext, text: String): List<EntityTypeRecognition> =
        client.parse(text, context.language)
}

/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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
package ai.tock.nlp.sagemaker

import ai.tock.nlp.sagemaker.SagemakerAwsClient.ParsedRequest
import ai.tock.nlp.core.Entity
import ai.tock.nlp.core.EntityRecognition
import ai.tock.nlp.core.EntityType
import ai.tock.nlp.core.EntityValue
import ai.tock.nlp.model.EntityCallContext
import ai.tock.nlp.model.service.engine.EntityModelHolder
import ai.tock.nlp.model.service.engine.NlpEntityClassifier
import ai.tock.shared.property
import software.amazon.awssdk.regions.Region


internal class SagemakerEntityClassifier(model: EntityModelHolder) : NlpEntityClassifier(model) {
    companion object {
        val CLIENT_TYPE = SagemakerClientType.ENTITY_CLASSIFICATION
    }

    override fun classifyEntities(
        context: EntityCallContext,
        text: String,
        tokens: Array<String>
    ): List<EntityRecognition> {
        SagemakerClientProvider.getClient(
            SagemakerAwsClientProperties(
                CLIENT_TYPE.clientName,
                Region.of(property("tock_sagemaker_aws_region_name", "eu-west-3")),
                property("tock_sagemaker_aws_entities_endpoint_name", "default"),
                property("tock_sagemaker_aws_content_type", "application/json"),
                property("tock_sagemaker_aws_profile_name", "default"),
            )
        ).parseEntities(ParsedRequest(text)).run {
            return entities.map { e ->
                e.role
                EntityRecognition(
                    EntityValue(
                        e.start,
                        e.end,
                        // entity is entityType in fact -- do not modify for the moment
                        Entity(EntityType(e.entity),e.role.toString()),
                            e.value
                    ),
                    e.confidence
                )
            }
        }
    }
}

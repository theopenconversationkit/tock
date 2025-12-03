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
package ai.tock.nlp.sagemaker

import ai.tock.nlp.core.Intent
import ai.tock.nlp.core.IntentClassification
import ai.tock.nlp.model.IntentContext
import ai.tock.nlp.model.service.engine.IntentClassifier
import ai.tock.nlp.sagemaker.SagemakerAwsClient.ParsedRequest
import ai.tock.shared.property
import software.amazon.awssdk.regions.Region

internal class SagemakerIntentClassifier(private val conf: SagemakerModelConfiguration) : IntentClassifier {
    companion object {
        val CLIENT_TYPE = SagemakerClientType.INTENT_CLASSIFICATION
    }

    override fun classifyIntent(
        context: IntentContext,
        text: String,
        tokens: Array<String>,
    ): IntentClassification {
        return SagemakerClientProvider.getClient(
            SagemakerAwsClientProperties(
                CLIENT_TYPE.clientName,
                Region.of(property("tock_sagemaker_aws_region_name", "eu-west-3")),
                property("tock_sagemaker_aws_intent_endpoint_name", "default"),
                property("tock_sagemaker_aws_content_type", "application/json"),
                property("tock_sagemaker_aws_profile_name", "default"),
            ),
        ).parseIntent(ParsedRequest(text))
            .run {
                object : IntentClassification {
                    var probability = 0.0
                    val iterator = intent_ranking.iterator()

                    override fun probability(): Double = probability

                    override fun hasNext(): Boolean = iterator.hasNext()

                    override fun next(): Intent {
                        return iterator.next().let { (intent, proba) ->
                            if (proba != null) {
                                probability = proba
                            }
                            intent?.let { context.application.getIntent(it.unescapeSagemakerName()) }
                                ?: Intent.UNKNOWN_INTENT
                        }
                    }
                }
            }
    }
}

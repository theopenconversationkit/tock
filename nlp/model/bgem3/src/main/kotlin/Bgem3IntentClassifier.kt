/*
 * Copyright (C) 2017/2024 e-voyageurs technologies
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
package ai.tock.nlp.bgem3

import ai.tock.nlp.bgem3.Bgem3AwsClient.ParsedRequest
import ai.tock.nlp.core.Intent
import ai.tock.nlp.core.IntentClassification
import ai.tock.nlp.model.IntentContext
import ai.tock.nlp.model.service.engine.IntentClassifier
import ai.tock.shared.property
import software.amazon.awssdk.regions.Region

internal class Bgem3IntentClassifier(private val conf: Bgem3ModelConfiguration) : IntentClassifier {

    override fun classifyIntent(context: IntentContext, text: String, tokens: Array<String>): IntentClassification {
        return Bgem3ClientProvider.getClient(
            Bgem3AwsClientProperties(
                Region.of(property("tock_sagemaker_aws_region_name", "eu-west-3")),
                property("tock_sagemaker_aws_intent_endpoint_name", "bge-m3-model-intent--v0"),
                property("tock_sagemaker_aws_content_type", "application/json"),
                property("tock_sagemaker_aws_profile_name", "sa-voyageurs-dev"),
            )
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
                            intent?.let { context.application.getIntent(it.unescapeBgem3Name()) }
                                ?: Intent.UNKNOWN_INTENT
                        }
                    }
                }
            }
    }
}

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
package ai.tock.nlp.rasa

import ai.tock.nlp.core.EntityRecognition
import ai.tock.nlp.core.EntityValue
import ai.tock.nlp.core.Intent
import ai.tock.nlp.core.IntentClassification
import ai.tock.nlp.model.EntityCallContext
import ai.tock.nlp.model.EntityCallContextForIntent
import ai.tock.nlp.model.IntentContext
import ai.tock.nlp.model.service.engine.EntityClassifier
import ai.tock.nlp.model.service.engine.IntentClassifier
import ai.tock.nlp.rasa.RasaClient.ParseRequest

internal class RasaClassifier(private val conf: RasaModelConfiguration) : IntentClassifier, EntityClassifier {
    private val threadLocal = ThreadLocal<List<EntityRecognition>>()

    override fun classifyIntent(
        context: IntentContext,
        text: String,
        tokens: Array<String>,
    ): IntentClassification {
        // TODO get RasaConfiguration ?
        return RasaClientProvider.getClient(RasaConfiguration()).parse(ParseRequest(text))
            .run {
                if (entities.isNotEmpty()) {
                    val intent = intent?.name?.let { context.application.getIntent(it.unescapeRasaName()) }
                    if (intent != null) {
                        threadLocal.set(
                            entities.mapNotNull { e ->
                                e.role
                                    ?.let { intent.getEntity(it) }
                                    ?.let { intentEntity ->
                                        EntityRecognition(
                                            EntityValue(
                                                e.start,
                                                e.end,
                                                intentEntity,
                                            ),
                                            e.confidence,
                                        )
                                    }
                            },
                        )
                    }
                }
                object : IntentClassification {
                    var probability = 0.0
                    val iterator = intent_ranking.iterator()

                    override fun probability(): Double = probability

                    override fun hasNext(): Boolean = iterator.hasNext()

                    override fun next(): Intent {
                        return iterator.next().let { (intent, proba) ->
                            probability = proba
                            context.application.getIntent(intent.unescapeRasaName()) ?: Intent.UNKNOWN_INTENT
                        }
                    }
                }
            }
    }

    override fun classifyEntities(
        context: EntityCallContext,
        text: String,
        tokens: Array<String>,
    ): List<EntityRecognition> =
        when (context) {
            is EntityCallContextForIntent ->
                threadLocal.get().also { threadLocal.remove() } ?: emptyList()
            else -> error("$context not yet supported")
        }
}

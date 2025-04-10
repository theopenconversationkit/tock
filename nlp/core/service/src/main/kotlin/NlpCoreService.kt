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

package ai.tock.nlp.core.service


import ai.tock.nlp.core.CallContext
import ai.tock.nlp.core.Entity
import ai.tock.nlp.core.EntityRecognition
import ai.tock.nlp.core.EntityType
import ai.tock.nlp.core.Intent
import ai.tock.nlp.core.Intent.Companion.UNKNOWN_INTENT_NAME
import ai.tock.nlp.core.IntentClassification
import ai.tock.nlp.core.IntentSelector
import ai.tock.nlp.core.NlpCore
import ai.tock.nlp.core.NlpEngineType
import ai.tock.nlp.core.ParsingResult
import ai.tock.nlp.core.merge.ValueDescriptor
import ai.tock.nlp.core.quality.TestContext
import ai.tock.nlp.core.service.entity.EntityCore
import ai.tock.nlp.core.service.entity.EntityCoreService
import ai.tock.nlp.core.service.entity.EntityMerge
import ai.tock.nlp.model.EntityCallContextForEntity
import ai.tock.nlp.model.EntityCallContextForIntent
import ai.tock.nlp.model.IntentContext
import ai.tock.nlp.model.ModelHolder
import ai.tock.nlp.model.ModelNotInitializedException
import ai.tock.nlp.model.NlpClassifier
import ai.tock.shared.checkMaxLengthAllowed
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.normalize
import com.github.salomonbrys.kodein.instance
import mu.KotlinLogging

/**
 *
 */
internal object NlpCoreService : NlpCore {

    private val logger = KotlinLogging.logger {}

    private val unknownResult = ParsingResult(UNKNOWN_INTENT_NAME, emptyList(), emptyList(), 1.0, 1.0)

    private val entityCore: EntityCore by injector.instance()
    private val entityMerge: EntityMerge by injector.instance()
    private val nlpClassifier: NlpClassifier by injector.instance()

    override fun parse(
        context: CallContext,
        text: String,
        intentSelector: IntentSelector
    ): ParsingResult {
        val t = context.prepareText(checkMaxLengthAllowed(text))
        return parse(
            context,
            t,
            { nlpClassifier.classifyIntent(IntentContext(context), t) },
            { intent ->
                nlpClassifier.classifyEntities(
                    EntityCallContextForIntent(context, intent),
                    t
                )
            },
            intentSelector
        )
    }

    internal fun parse(
        context: TestContext,
        text: String,
        intentModelHolder: ModelHolder,
        entityModelHolders: Map<Intent, ModelHolder?>
    ): ParsingResult {
        val t = context.prepareText(text)
        return parse(
            context.callContext,
            t,
            { nlpClassifier.classifyIntent(IntentContext(context), intentModelHolder, t) },
            { intent ->
                entityModelHolders[intent]?.let { entityModel ->
                    nlpClassifier.classifyEntities(
                        EntityCallContextForIntent(context, intent),
                        entityModel,
                        t
                    )
                } ?: emptyList()
            },
            IntentSelector.defaultIntentSelector
        )
    }

    private fun parse(
        callContext: CallContext,
        text: String,
        intentClassifier: () -> IntentClassification,
        entityClassifier: (Intent) -> List<EntityRecognition>,
        intentSelector: IntentSelector
    ): ParsingResult {
        try {
            val intents = intentClassifier.invoke()
            val (intent, probability) = intentSelector.selectIntent(intents) ?: (null to null)

            if (intent == null || probability == null) {
                return unknownResult
            }

            val (evaluatedEntities, notRetainedEntities) = classifyAndEvaluate(
                callContext,
                intent,
                entityClassifier,
                text
            )

            return ParsingResult(
                intent.name,
                evaluatedEntities,
                notRetainedEntities,
                probability,
                if (evaluatedEntities.isEmpty()) 1.0 else evaluatedEntities.map { it.probability }.average()
            )
        } catch (e: ModelNotInitializedException) {
            logger.warn { "model not initialized : ${e.message}" }
            return unknownResult
        } catch (e: Exception) {
            logger.error(e)
            return unknownResult
        }
    }

    private fun classifyAndEvaluate(
        context: CallContext,
        intent: Intent,
        entityClassifier: (Intent) -> List<EntityRecognition>,
        text: String
    ): Pair<List<EntityRecognition>, List<EntityRecognition>> {
        return try {
            // TODO regexp
            // evaluate entities from intent entity model & dedicated entity models
            val intentContext = EntityCallContextForIntent(context, intent)
            val entities = entityClassifier.invoke(intent)

            val evaluatedEntities = evaluateEntities(context, text, entities)

            if (context.evaluationContext.mergeEntityTypes || context.evaluationContext.classifyEntityTypes) {
                // small issue here: how do we post-evaluate these detections?
                val classifiedEntityTypes = entityCore.classifyEntityTypes(intentContext, text)
                if (classifiedEntityTypes.isNotEmpty()) {
                    if (context.evaluationContext.mergeEntityTypes) {
                        val result =
                            entityMerge.mergeEntityTypes(
                                context,
                                text,
                                intent,
                                evaluatedEntities,
                                classifiedEntityTypes
                            )
                        result to
                                (evaluatedEntities + classifiedEntityTypes.map { it.toEntityRecognition(it.entityType.name) })
                                    .subtract(result).toList()
                    } else {
                        evaluatedEntities to
                                classifiedEntityTypes.map { it.toEntityRecognition(it.entityType.name) }
                                    .subtract(evaluatedEntities).toList()
                    }
                } else {
                    evaluatedEntities to emptyList()
                }
            } else {
                evaluatedEntities to emptyList()
            }
        } catch (e: Exception) {
            logger.error(e)
            Pair(emptyList(), emptyList())
        }
    }

    override fun evaluateEntities(
        context: CallContext,
        text: String,
        entities: List<EntityRecognition>
    ): List<EntityRecognition> {
        return entityCore.evaluateEntities(context, text, entities)
    }

    override fun supportedNlpEngineTypes(): Set<NlpEngineType> {
        return nlpClassifier.supportedNlpEngineTypes()
    }

    override fun supportValuesMerge(entityType: EntityType): Boolean {
        return entityCore.supportValuesMerge(entityType)
    }

    override fun mergeValues(context: CallContext, entity: Entity, values: List<ValueDescriptor>): ValueDescriptor? {
        return entityCore.mergeValues(EntityCallContextForEntity(context, entity), values)
    }

    override fun getBuiltInEntityTypes(): Set<String> = EntityCoreService.knownEntityTypes

    override fun healthcheck(): Boolean {
        return entityCore.healthcheck()
    }

    private fun CallContext.prepareText(text: String): String {
        return if (application.normalizeText)
            text.normalize(language)
        else text
    }

    private fun TestContext.prepareText(text: String): String = callContext.prepareText(text)
}

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

package fr.vsct.tock.nlp.core.service

import com.github.salomonbrys.kodein.instance
import fr.vsct.tock.nlp.core.CallContext
import fr.vsct.tock.nlp.core.Entity
import fr.vsct.tock.nlp.core.EntityRecognition
import fr.vsct.tock.nlp.core.EntityType
import fr.vsct.tock.nlp.core.Intent
import fr.vsct.tock.nlp.core.Intent.Companion.UNKNOWN_INTENT_NAME
import fr.vsct.tock.nlp.core.IntentClassification
import fr.vsct.tock.nlp.core.IntentSelector
import fr.vsct.tock.nlp.core.NlpCore
import fr.vsct.tock.nlp.core.NlpEngineType
import fr.vsct.tock.nlp.core.ParsingResult
import fr.vsct.tock.nlp.core.merge.ValueDescriptor
import fr.vsct.tock.nlp.core.quality.TestContext
import fr.vsct.tock.nlp.core.service.entity.EntityCore
import fr.vsct.tock.nlp.core.service.entity.EntityMerge
import fr.vsct.tock.nlp.model.EntityCallContextForEntity
import fr.vsct.tock.nlp.model.EntityCallContextForIntent
import fr.vsct.tock.nlp.model.IntentContext
import fr.vsct.tock.nlp.model.ModelHolder
import fr.vsct.tock.nlp.model.ModelNotInitializedException
import fr.vsct.tock.nlp.model.NlpClassifier
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.injector
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
        return parse(
            context,
            text,
            { nlpClassifier.classifyIntent(IntentContext(context), text) },
            { intent ->
                nlpClassifier.classifyEntities(
                    EntityCallContextForIntent(context, intent),
                    text
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
        return parse(
            context.callContext,
            text,
            { nlpClassifier.classifyIntent(IntentContext(context), intentModelHolder, text) },
            { intent ->
                entityModelHolders[intent]?.let { entityModel ->
                    nlpClassifier.classifyEntities(
                        EntityCallContextForIntent(context, intent),
                        entityModel,
                        text
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
            val (intent, probability) = intentSelector.selectIntent(intents) ?: null to null

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
            //TODO regexp
            //evaluate entities from intent entity model & dedicated entity models
            val intentContext = EntityCallContextForIntent(context, intent)
            val entities = entityClassifier.invoke(intent)
            val evaluatedEntities = evaluateEntities(context, text, entities)

            if (context.evaluationContext.mergeEntityTypes || context.evaluationContext.classifyEntityTypes) {
                val classifiedEntityTypes = entityCore.classifyEntityTypes(intentContext, text)
                if (context.evaluationContext.mergeEntityTypes) {
                    val result =
                        entityMerge.mergeEntityTypes(context, text, intent, evaluatedEntities, classifiedEntityTypes)
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

    override fun getEvaluableEntityTypes(): Set<String> {
        return entityCore.getEvaluableEntityTypes()
    }

    override fun supportValuesMerge(entityType: EntityType): Boolean {
        return entityCore.supportValuesMerge(entityType)
    }

    override fun mergeValues(context: CallContext, entity: Entity, values: List<ValueDescriptor>): ValueDescriptor? {
        return entityCore.mergeValues(EntityCallContextForEntity(context, entity), values)
    }

    override fun healthcheck(): Boolean {
        return entityCore.healthcheck()
    }
}
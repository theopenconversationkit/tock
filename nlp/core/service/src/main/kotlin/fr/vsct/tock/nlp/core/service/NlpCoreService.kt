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

import fr.vsct.tock.nlp.core.BuildContext
import fr.vsct.tock.nlp.core.CallContext
import fr.vsct.tock.nlp.core.EntityRecognition
import fr.vsct.tock.nlp.core.Intent
import fr.vsct.tock.nlp.core.IntentRecognition
import fr.vsct.tock.nlp.core.NlpCore
import fr.vsct.tock.nlp.core.NlpEngineType
import fr.vsct.tock.nlp.core.ParsingResult
import fr.vsct.tock.nlp.core.sample.SampleExpression
import fr.vsct.tock.nlp.core.service.entity.EntityEvaluatorService
import fr.vsct.tock.nlp.core.service.entity.EntityMergeService
import fr.vsct.tock.nlp.model.EntityBuildContextForIntent
import fr.vsct.tock.nlp.model.EntityCallContextForIntent
import fr.vsct.tock.nlp.model.IntentContext
import fr.vsct.tock.nlp.model.ModelNotInitializedException
import fr.vsct.tock.nlp.model.TokenizerContext
import fr.vsct.tock.nlp.model.client.NlpClassifierClient
import fr.vsct.tock.shared.error
import mu.KotlinLogging

/**
 *
 */
object NlpCoreService : NlpCore {

    private val logger = KotlinLogging.logger {}

    private val unknownResult = ParsingResult(Intent.Companion.unknownIntent, emptyList(), 1.0, 1.0)

    private fun tokenize(context: CallContext, text: String): Array<String> {
        return NlpClassifierClient.tokenize(TokenizerContext(context), text)
    }

    override fun parse(context: CallContext,
                       text: String,
                       intentSelector: (List<IntentRecognition>) -> IntentRecognition?): ParsingResult {
        try {
            val tokens = tokenize(context, text)
            val intents = NlpClassifierClient.classifyIntent(IntentContext(context), text, tokens)
            val intent = intentSelector.invoke(intents)

            if (intent == null) {
                return unknownResult
            }

            val evaluatedEntities = evaluateEntities(context, intent.intent, text, tokens)

            return ParsingResult(
                    intent.intent.name,
                    evaluatedEntities.map { it.value },
                    intent.probability,
                    evaluatedEntities.map { it.probability }.average())
        } catch(e: ModelNotInitializedException) {
            logger.warn { "model not initialized : ${e.message}" }
            return unknownResult
        } catch(e: Exception) {
            logger.error(e)
            return unknownResult
        }
    }

    private fun evaluateEntities(context: CallContext, intent: Intent, text: String, tokens: Array<String>): List<EntityRecognition> {
        //TODO regexp

        //evaluate entities from intent entity model & dedicated entity models
        val intentContext = EntityCallContextForIntent(context, intent)
        val entities = NlpClassifierClient.classifyEntities(intentContext, text, tokens)
        val evaluatedEntities = evaluateEntities(context, text, entities)
        val classifiedEntityTypes = EntityEvaluatorService.classifyEntityTypes(intentContext, text, tokens)

        return EntityMergeService.mergeEntityTypes(intent, evaluatedEntities, classifiedEntityTypes)
    }

    override fun evaluateEntities(
            context: CallContext,
            text: String,
            entities: List<EntityRecognition>): List<EntityRecognition> {
        return EntityEvaluatorService.evaluateEntities(context, text, entities)
    }

    override fun updateIntentModel(context: BuildContext, expressions: List<SampleExpression>) {
        NlpClassifierClient.buildAndSaveIntentModel(IntentContext(context), expressions)
    }

    override fun updateEntityModelForIntent(context: BuildContext, intent: Intent, expressions: List<SampleExpression>) {
        NlpClassifierClient.buildAndSaveEntityModel(EntityBuildContextForIntent(context, intent), expressions)
    }

    override fun supportedNlpEngineTypes(): Set<NlpEngineType> {
        return NlpClassifierClient.supportedNlpEngineTypes()
    }

    override fun getEvaluatedEntityTypes(): Set<String> {
        return EntityEvaluatorService.getEvaluatedEntityTypes()
    }
}
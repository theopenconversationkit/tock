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
import fr.vsct.tock.nlp.core.Intent
import fr.vsct.tock.nlp.core.IntentRecognition
import fr.vsct.tock.nlp.core.NlpCore
import fr.vsct.tock.nlp.core.NlpEngineType
import fr.vsct.tock.nlp.core.ParsingResult
import fr.vsct.tock.nlp.core.sample.SampleExpression
import fr.vsct.tock.nlp.core.service.entity.EntityEvaluatorService
import fr.vsct.tock.nlp.model.EntityBuildContextForIntent
import fr.vsct.tock.nlp.model.EntityCallContextForIntent
import fr.vsct.tock.nlp.model.IntentContext
import fr.vsct.tock.nlp.model.TokenizerContext
import fr.vsct.tock.nlp.model.client.NlpClassifierClient

/**
 *
 */
object NlpCoreService : NlpCore {

    private fun tokenize(context: CallContext, text: String): Array<String> {
        return NlpClassifierClient.tokenize(TokenizerContext(context), text)
    }

    override fun parse(context: CallContext,
                       text: String,
                       intentSelector: (List<IntentRecognition>) -> IntentRecognition?): ParsingResult {
        val tokens = tokenize(context, text)
        val intents = NlpClassifierClient.classifyIntent(IntentContext(context), text, tokens)
        val intent = intentSelector.invoke(intents)

        if (intent == null) {
            return ParsingResult(Intent.Companion.unknownIntent, emptyList(), 1.0, 1.0)
        }

        //TODO regexp et dedicated entity classifier

        val entities = NlpClassifierClient.classifyEntities(EntityCallContextForIntent(context, intent.intent), text, tokens)
        val evaluatedEntities = EntityEvaluatorService.evaluateEntities(context, text, entities)

        return ParsingResult(
                intent.intent.name,
                evaluatedEntities.map { it.value },
                intent.probability,
                evaluatedEntities.map { it.probability }.average())
    }

    override fun updateIntentModel(context: BuildContext, expressions: List<SampleExpression>) {
        NlpClassifierClient.buildAndSaveIntentModel(IntentContext(context), expressions)
    }

    override fun updateEntityModelForIntent(context: BuildContext, intent: Intent, expressions: List<SampleExpression>) {
        NlpClassifierClient.buildAndSaveEntityModel(EntityBuildContextForIntent(context, intent), expressions)
    }

    override fun registeredNlpEngineTypes(): Set<NlpEngineType> {
        return NlpClassifierClient.registeredNlpEngineTypes()
    }
}
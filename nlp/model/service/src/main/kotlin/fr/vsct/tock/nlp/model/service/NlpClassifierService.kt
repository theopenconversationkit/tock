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

package fr.vsct.tock.nlp.model.service

import fr.vsct.tock.nlp.core.EntityRecognition
import fr.vsct.tock.nlp.core.NlpEngineType
import fr.vsct.tock.nlp.core.sample.SampleExpression
import fr.vsct.tock.nlp.model.EntityBuildContext
import fr.vsct.tock.nlp.model.EntityBuildContextForEntity
import fr.vsct.tock.nlp.model.EntityBuildContextForIntent
import fr.vsct.tock.nlp.model.EntityBuildContextForSubEntities
import fr.vsct.tock.nlp.model.EntityCallContext
import fr.vsct.tock.nlp.model.IntentContext
import fr.vsct.tock.nlp.core.IntentClassification
import fr.vsct.tock.nlp.model.NlpClassifier
import fr.vsct.tock.nlp.model.TokenizerContext
import fr.vsct.tock.nlp.model.service.engine.NlpEngineRepository
import fr.vsct.tock.nlp.model.service.engine.NlpEngineRepository.getModelBuilder
import fr.vsct.tock.nlp.model.service.engine.NlpEngineRepository.getModelIo
import fr.vsct.tock.nlp.model.service.engine.NlpModelRepository.saveEntityModel
import fr.vsct.tock.nlp.model.service.engine.NlpModelRepository.saveIntentModel

/**
 *
 */
object NlpClassifierService : NlpClassifier {

    override fun supportedNlpEngineTypes(): Set<NlpEngineType> {
        return NlpEngineRepository.registeredNlpEngineTypes()
    }

    override fun tokenize(context: TokenizerContext, text: String): Array<String> {
        return NlpEngineRepository.getTokenizer(context).tokenize(context, text)
    }

    override fun classifyIntent(context: IntentContext, text: String, tokens: Array<String>): IntentClassification {
        return NlpEngineRepository.getIntentClassifier(context).classifyIntent(context, text, tokens)
    }

    override fun classifyEntities(context: EntityCallContext, text: String, tokens: Array<String>): List<EntityRecognition> {
        val entityClassifier = NlpEngineRepository.getEntityClassifier(context)
        return entityClassifier?.classifyEntities(context, text, tokens) ?: emptyList()
    }

    override fun buildAndSaveTokenizerModel(context: TokenizerContext, expressions: List<SampleExpression>) {
        //do nothing at this time
    }

    override fun buildAndSaveIntentModel(context: IntentContext, expressions: List<SampleExpression>) {
        val model = getModelBuilder(context).buildIntentModel(context, expressions)
        saveIntentModel(context.key(), model, getModelIo(context))
    }

    override fun buildAndSaveEntityModel(context: EntityBuildContext, expressions: List<SampleExpression>) {
        val exp = filterExpressionsForContext(context, expressions)
        val model = getModelBuilder(context).buildEntityModel(context, exp)
        if (model != null) {
            saveEntityModel(context.key(), model, getModelIo(context))
        }
    }

    private fun filterExpressionsForContext(context: EntityBuildContext, expressions: List<SampleExpression>): List<SampleExpression> {
        return when (context) {
            is EntityBuildContextForIntent -> expressions
            is EntityBuildContextForEntity ->
                expressions
                        .filter { it.containsEntityType(context.entityType) }
                        .map { it.copy(entities = it.entities.filter { it.isType(context.entityType) }) }
            is EntityBuildContextForSubEntities -> TODO()
        }
    }
}
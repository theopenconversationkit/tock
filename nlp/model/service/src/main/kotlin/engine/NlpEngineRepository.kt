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

package ai.tock.nlp.model.service.engine

import ai.tock.nlp.core.NlpEngineType
import ai.tock.nlp.model.*
import ai.tock.shared.ThreadSafe

/**
 *
 */
@ThreadSafe
internal object NlpEngineRepository {

    private val repository: Map<NlpEngineType, NlpEngineProvider> =
        SupportedNlpEnginesProvider.engines().associateBy { it.type }

    fun registeredNlpEngineTypes(): Set<NlpEngineType> {
        return repository.keys
    }

    internal fun getProvider(nlpEngineType: NlpEngineType): NlpEngineProvider {
        return repository[nlpEngineType] ?: error("Unknown nlp engine type : $nlpEngineType")
    }

    fun getTokenizer(context: IntentContext): Tokenizer {
        return getProvider(context.engineType).let {
            it.getTokenizer(
                NlpModelRepository.getTokenizerModelHolder(
                    TokenizerContext(context),
                    NlpModelRepository.getConfiguration(context, it)
                )
            )
        }
    }

    fun getTokenizer(context: EntityContext): Tokenizer {
        return getProvider(context.engineType).let {
            it.getTokenizer(
                NlpModelRepository.getTokenizerModelHolder(
                    TokenizerContext(context),
                    NlpModelRepository.getConfiguration(context, it)
                )
            )
        }
    }

    fun getIntentClassifier(context: IntentContext): IntentClassifier {
        return getProvider(context.engineType).let {
            it.getIntentClassifier(NlpModelRepository.getIntentModelHolder(context, it))
        }
    }

    fun getIntentClassifier(context: IntentContext, modelHolder: IntentModelHolder): IntentClassifier {
        return getProvider(context.engineType).getIntentClassifier(modelHolder)
    }

    fun getEntityClassifier(context: EntityCallContext): EntityClassifier? {
        return getProvider(context.engineType).let { provider ->
            NlpModelRepository.getEntityModelHolder(context, provider)?.let { model ->
                provider.getEntityClassifier(model)
            }
        }
    }

    fun getEntityClassifier(context: EntityCallContext, modelHolder: EntityModelHolder?): EntityClassifier? {
        return getProvider(context.engineType).let { provider ->
            modelHolder?.let { model ->
                provider.getEntityClassifier(model)
            }
        }
    }


    fun <T : ClassifierContextKey> getModelBuilder(context: ClassifierContext<T>): NlpEngineModelBuilder {
        return getProvider(context.engineType).modelBuilder
    }

    fun <T : ClassifierContextKey> getModelIo(context: ClassifierContext<T>): NlpEngineModelIo {
        return getProvider(context.engineType).modelIo
    }
}

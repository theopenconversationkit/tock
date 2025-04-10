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

package ai.tock.nlp.opennlp

import ai.tock.nlp.core.NlpEngineType
import ai.tock.nlp.model.service.engine.EntityClassifier
import ai.tock.nlp.model.service.engine.EntityModelHolder
import ai.tock.nlp.model.service.engine.IntentClassifier
import ai.tock.nlp.model.service.engine.IntentModelHolder
import ai.tock.nlp.model.service.engine.NlpEngineModelBuilder
import ai.tock.nlp.model.service.engine.NlpEngineModelIo
import ai.tock.nlp.model.service.engine.NlpEngineProvider
import ai.tock.nlp.model.service.engine.Tokenizer
import ai.tock.nlp.model.service.engine.TokenizerModelHolder

/**
 *
 */
class OpenNlpEngineProvider : NlpEngineProvider {

    override val type: NlpEngineType = NlpEngineType.opennlp

    override fun getIntentClassifier(model: IntentModelHolder): IntentClassifier {
        return OpenNlpIntentClassifier(model)
    }

    override fun getEntityClassifier(model: EntityModelHolder): EntityClassifier {
        return OpenNlpEntityClassifier(model)
    }

    override fun getTokenizer(model: TokenizerModelHolder): Tokenizer {
        return OpenNlpTokenizer(model)
    }

    override val modelBuilder: NlpEngineModelBuilder = OpenNlpModelBuilder

    override val modelIo: NlpEngineModelIo = OpenNlpModelIo
}

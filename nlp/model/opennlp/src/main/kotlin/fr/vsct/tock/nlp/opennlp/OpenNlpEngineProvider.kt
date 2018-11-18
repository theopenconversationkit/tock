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

package fr.vsct.tock.nlp.opennlp

import fr.vsct.tock.nlp.core.NlpEngineType
import fr.vsct.tock.nlp.model.service.engine.EntityClassifier
import fr.vsct.tock.nlp.model.service.engine.EntityModelHolder
import fr.vsct.tock.nlp.model.service.engine.IntentClassifier
import fr.vsct.tock.nlp.model.service.engine.IntentModelHolder
import fr.vsct.tock.nlp.model.service.engine.NlpEngineModelBuilder
import fr.vsct.tock.nlp.model.service.engine.NlpEngineModelIo
import fr.vsct.tock.nlp.model.service.engine.NlpEngineProvider
import fr.vsct.tock.nlp.model.service.engine.Tokenizer
import fr.vsct.tock.nlp.model.service.engine.TokenizerModelHolder

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
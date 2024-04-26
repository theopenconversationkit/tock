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

package ai.tock.nlp.bgem3

import ai.tock.nlp.core.NlpEngineType
import ai.tock.nlp.model.TokenizerContext
import ai.tock.nlp.model.service.engine.EntityClassifier
import ai.tock.nlp.model.service.engine.EntityModelHolder
import ai.tock.nlp.model.service.engine.IntentClassifier
import ai.tock.nlp.model.service.engine.IntentModelHolder
import ai.tock.nlp.model.service.engine.NlpEngineModelBuilder
import ai.tock.nlp.model.service.engine.NlpEngineModelIo
import ai.tock.nlp.model.service.engine.NlpEngineProvider
import ai.tock.nlp.model.service.engine.Tokenizer
import ai.tock.nlp.model.service.engine.TokenizerModelHolder

class Bgem3EngineProvider : NlpEngineProvider {

    private val threadLocal = ThreadLocal<Bgem3IntentClassifier>()

    private fun getBgem3Classifier(conf: Bgem3ModelConfiguration? = null, new: Boolean): Bgem3IntentClassifier =
        if (new) {
            threadLocal.remove()
            Bgem3IntentClassifier(conf ?: error("no bgem3 configuration")).apply { threadLocal.set(this) }
        } else {
            threadLocal.get().also { threadLocal.remove() } ?: error("no bgem3 classifier found")
        }

    override val type: NlpEngineType = NlpEngineType.bgem3

    override val modelBuilder: NlpEngineModelBuilder = Bgem3NlpModelBuilder

    override val modelIo: NlpEngineModelIo = Bgem3NlpModelIo

    override fun getIntentClassifier(model: IntentModelHolder): IntentClassifier =
        getBgem3Classifier(model.nativeModel as Bgem3ModelConfiguration, true)

    override fun getEntityClassifier(model: EntityModelHolder): EntityClassifier = Bgem3EntityClassifier(model)

    override fun getTokenizer(model: TokenizerModelHolder): Tokenizer = object : Tokenizer {
        // do not tokenize anything at this stage - bgem3 internals
        override fun tokenize(context: TokenizerContext, text: String): Array<String> = arrayOf(text)
    }
}

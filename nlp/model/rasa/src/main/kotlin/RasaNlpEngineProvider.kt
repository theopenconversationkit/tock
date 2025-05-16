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

internal class RasaNlpEngineProvider : NlpEngineProvider {

    private val threadLocal = ThreadLocal<RasaClassifier>()

    private fun getRasaClassifier(conf: RasaModelConfiguration? = null, new: Boolean = false): RasaClassifier =
        if (new) {
            threadLocal.remove()
            RasaClassifier(conf ?: error("no rasa configuration")).apply {
                threadLocal.set(this)
            }
        } else {
            threadLocal.get().also { threadLocal.remove() } ?: error("no rasa classifier found")
        }

    override val type: NlpEngineType = NlpEngineType.rasa

    override val modelBuilder: NlpEngineModelBuilder = RasaNlpModelBuilder

    override val modelIo: NlpEngineModelIo = RasaNlpModelIo

    override fun getIntentClassifier(model: IntentModelHolder): IntentClassifier =
        getRasaClassifier(model.nativeModel as RasaModelConfiguration, true)

    override fun getEntityClassifier(model: EntityModelHolder): EntityClassifier =
        getRasaClassifier()

    override fun getTokenizer(model: TokenizerModelHolder): Tokenizer = object : Tokenizer {
        // do not tokenize anything at this stage - rasa internals
        override fun tokenize(context: TokenizerContext, text: String): Array<String> = arrayOf(text)
    }
}

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
package ai.tock.nlp.systemone

import NlpHealthcheckResult
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

/**
 * [NlpEngineProvider] for Cloudflare's hosted Clef / Clef-flash decision models.
 * Intent classification only - see [SystemOneEntityClassifier].
 */
class SystemOneEngineProvider : NlpEngineProvider {
    override val type: NlpEngineType = NlpEngineType.systemone

    override val modelBuilder: NlpEngineModelBuilder = SystemOneNlpModelBuilder

    override val modelIo: NlpEngineModelIo = SystemOneNlpModelIo

    override fun getIntentClassifier(model: IntentModelHolder): IntentClassifier =
        // The persisted native model only ever holds a non-secret model-variant marker (see
        // SystemOneNlpModelBuilder) - credentials always come from live properties, never from
        // potentially stale/persisted state.
        SystemOneIntentClassifier(
            SystemOneConfiguration(),
            model.configuration.intentConfiguration.properties
                .getProperty(SystemOneIntentClassifier.INTENT_QUESTION_INSTRUCTIONS_PROPERTY)
                ?.takeIf { it.isNotBlank() }
                ?: SystemOneIntentClassifier.INTENT_QUESTION_INSTRUCTIONS,
        )

    override fun getEntityClassifier(model: EntityModelHolder): EntityClassifier = SystemOneEntityClassifier(model)

    override fun getTokenizer(model: TokenizerModelHolder): Tokenizer =
        object : Tokenizer {
            // Clef takes raw text as "state" - no local tokenization needed
            override fun tokenize(
                context: TokenizerContext,
                text: String,
            ): Array<String> = arrayOf(text)
        }

    override fun healthcheck(): () -> NlpHealthcheckResult =
        {
            val ok = SystemOneClientProvider.getClient(SystemOneConfiguration()).healthcheck()
            NlpHealthcheckResult(
                entityClassifier = true,
                intentClassifier = ok,
            )
        }
}

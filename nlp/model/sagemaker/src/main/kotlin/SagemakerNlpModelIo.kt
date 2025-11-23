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
package ai.tock.nlp.sagemaker

import ai.tock.nlp.model.service.engine.NlpEngineModelIo
import ai.tock.nlp.model.service.storage.NlpModelStream
import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.OutputStream

internal object SagemakerNlpModelIo : NlpEngineModelIo {
    override fun loadTokenizerModel(input: NlpModelStream): Any {
        error("loading tokenizer model is not supported")
    }

    override fun loadIntentModel(input: NlpModelStream): Any {
        return SagemakerModelConfiguration()
    }

    override fun loadEntityModel(input: NlpModelStream): Any = mapper.readValue<String>(input.inputStream)

    override fun copyTokenizerModel(
        model: Any,
        output: OutputStream,
    ) {
        error("copying tokenizer model is not supported")
    }

    override fun copyIntentModel(
        model: Any,
        output: OutputStream,
    ) {
        mapper.writeValue(output, model)
    }

    override fun copyEntityModel(
        model: Any,
        output: OutputStream,
    ) {
        mapper.writeValue(output, model)
    }
}

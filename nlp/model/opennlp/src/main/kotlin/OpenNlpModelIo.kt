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

package ai.tock.nlp.opennlp

import ai.tock.nlp.model.service.engine.NlpEngineModelIo
import ai.tock.nlp.model.service.storage.NlpModelStream
import opennlp.tools.ml.maxent.io.BinaryGISModelReader
import opennlp.tools.ml.maxent.io.BinaryGISModelWriter
import opennlp.tools.ml.model.AbstractModel
import opennlp.tools.namefind.NameFinderME
import opennlp.tools.namefind.TokenNameFinderModel
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.OutputStream

/**
 *
 */
internal object OpenNlpModelIo : NlpEngineModelIo {
    override fun loadTokenizerModel(input: NlpModelStream): Any {
        error("loading tokenizer model is not supported")
    }

    override fun loadIntentModel(input: NlpModelStream): Any =
        input.inputStream.use {
            BinaryGISModelReader(DataInputStream(it)).model
        }

    override fun loadEntityModel(input: NlpModelStream): Any =
        input.inputStream.use {
            NameFinderME(TokenNameFinderModel(it))
        }

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
        BinaryGISModelWriter(model as AbstractModel, DataOutputStream(output)).persist()
    }

    override fun copyEntityModel(
        model: Any,
        output: OutputStream,
    ) {
        (model as OpenNlpNameFinderME).tokenNameFinderModel.serialize(output)
    }
}

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

package ai.tock.nlp.model.service.engine

import ai.tock.nlp.model.service.storage.NlpModelStream
import java.io.OutputStream

/**
 * Manages native model io.
 */
interface NlpEngineModelIo {
    /**
     * Loads a tokenizer model from an input stream.
     */
    fun loadTokenizerModel(input: NlpModelStream): Any

    /**
     * Loads an intent model from an input stream.
     */
    fun loadIntentModel(input: NlpModelStream): Any

    /**
     * Loads an entity model from an input stream.
     */
    fun loadEntityModel(input: NlpModelStream): Any

    /**
     * Sends a tokenizer model to an output stream.
     */
    fun copyTokenizerModel(
        model: Any,
        output: OutputStream,
    )

    /**
     * Sends an intent model to an output stream.
     */
    fun copyIntentModel(
        model: Any,
        output: OutputStream,
    )

    /**
     * Sends an entity model to an output stream.
     */
    fun copyEntityModel(
        model: Any,
        output: OutputStream,
    )
}

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

package ai.tock.nlp.core

/**
 * Nlp Engine type.
 */
data class NlpEngineType(
    /**
     * The unique name of nlp engine.
     */
    val name: String = opennlp.name,
) {
    companion object {
        /**
         * The core stanford nlp engine.
         */
        val stanford = NlpEngineType("stanford")

        /**
         * The opennlp nlp engine.
         */
        val opennlp = NlpEngineType("opennlp")

        /**
         * The rasa-based nlp engine.
         */
        val rasa = NlpEngineType("rasa")

        /**
         * The bgem3 nlp engine.
         */
        val bgem3 = NlpEngineType("bgem3")
    }
}

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

import ai.tock.nlp.core.configuration.NlpApplicationConfiguration
import ai.tock.shared.property
import java.util.Locale

fun NlpApplicationConfiguration.toRasaConfiguration(): RasaConfiguration =
    RasaConfiguration()

internal fun String.escapeRasaName(): String = replace(":", "___")
internal fun String.unescapeRasaName(): String = replace("___", ":")
/**
 * The rasa configuration.
 */
class RasaConfiguration(
    val rasaUrl: String = "http://localhost:5005",
    val modelBasePath: String = property("rasa_model_path", "models/"),
    val configuration: String = "language: en\npipeline: supervised_embeddings\npolicies:\n  - name: MemoizationPolicy\n  - name: KerasPolicy"
) {
    fun getMarkdownConfiguration(locale: Locale): String =
        when (locale.language) {
            Locale.ENGLISH.language ->
                """
pipeline:
  - name: ConveRTTokenizer
  - name: ConveRTFeaturizer
  - name: RegexFeaturizer
  - name: LexicalSyntacticFeaturizer
  - name: CountVectorsFeaturizer
  - name: CountVectorsFeaturizer
    analyzer: "char_wb"
    min_ngram: 1
    max_ngram: 4
  - name: DIETClassifier
    epochs: 100
  - name: EntitySynonymMapper
  - name: ResponseSelector
    epochs: 100"""
            else ->
                """language: "${locale.language}"  # your two-letter language code

pipeline:
  - name: WhitespaceTokenizer
  - name: RegexFeaturizer
  - name: LexicalSyntacticFeaturizer
  - name: CountVectorsFeaturizer
  - name: CountVectorsFeaturizer
    analyzer: "char_wb"
    min_ngram: 1
    max_ngram: 4
  - name: DIETClassifier
    epochs: 100
  - name: EntitySynonymMapper
  - name: ResponseSelector
    epochs: 100"""
        }
    fun getModelFilePath(fileName: String) = modelBasePath + fileName
}

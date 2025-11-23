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

import java.util.Locale

/**
 * Context for a NLP model build.
 */
data class BuildContext(
    val application: Application,
    val language: Locale,
    val engineType: NlpEngineType = NlpEngineType.opennlp,
    /** update the model only if he does not exist already */
    val onlyIfNotExists: Boolean = false,
)

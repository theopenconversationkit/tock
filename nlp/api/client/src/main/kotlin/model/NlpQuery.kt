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

package ai.tock.nlp.api.client.model

/**
 * A NLP parse query.
 */
data class NlpQuery(
    /**
     * A list of queries to parse.
     * Usually there is only one element in the list, but some STT engines provides alternatives.
     */
    val queries: List<String>,
    /**
     * The namespace of the application.
     */
    val namespace: String,
    /**
     * The name of the application.
     */
    val applicationName: String,
    /**
     * The context of the query.
     */
    val context: NlpQueryContext,
    /**
     * The state of the query.
     */
    val state: NlpQueryState = NlpQueryState.noState,
    /**
     * The query is restricted to the specified intents only.
     * If the set is empty all intents of the application are allowed.
     */
    val intentsSubset: Set<NlpIntentQualifier> = emptySet(),
    /**
     * Returns the result only for sentences of this configuration.
     */
    val configuration: String? = null,
)

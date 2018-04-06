/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.nlp.front.shared.parser

import org.litote.kmongo.Data

/**
 * A NLP parse query.
 */
@Data
data class ParseQuery(
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
    val context: QueryContext,
    /**
     * The state of the query.
     */
    val state: QueryState = QueryState.noState
) {
}
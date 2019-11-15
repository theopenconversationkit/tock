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

package ai.tock.nlp.api.client.model.monitoring

import java.util.Locale

/**
 * Informs the nlp model that a sentence has not been understood.
 */
class MarkAsUnknownQuery(
    /**
     * The namespace of the application.
     */
    val namespace: String,
    /**
     * The name of the application.
     */
    val applicationName: String,
    /**
     * The language of the query.
     */
    val language: Locale,
    /**
     * The sentence to mark
     */
    val text: String
)
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

package ai.tock.nlp.front.shared.codec

import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.config.EntityTypeDefinition
import ai.tock.nlp.front.shared.config.FaqDefinition
import ai.tock.nlp.front.shared.config.IntentDefinition

/**
 * The import result report.
 */
data class ImportReport(
    val applicationsImported: MutableSet<String> = mutableSetOf(),
    val entitiesImported: MutableSet<String> = mutableSetOf(),
    val intentsImported: MutableSet<String> = mutableSetOf(),
    var sentencesImported: Long = 0L,
    var faqsImported: Long = 0L,
    var localeAdded: Boolean = false,
    var success: Boolean = true,
    val errorMessages: MutableList<String> = mutableListOf()
) {

    val modified: Boolean
        get() = applicationsImported.isNotEmpty() ||
            entitiesImported.isNotEmpty() ||
            intentsImported.isNotEmpty() ||
            sentencesImported != 0L ||
            faqsImported != 0L ||
            localeAdded

    fun add(app: ApplicationDefinition) = applicationsImported.add(app.qualifiedName)

    fun add(entity: EntityTypeDefinition) = entitiesImported.add(entity.name)

    fun add(intent: IntentDefinition) = intentsImported.add(intent.qualifiedName)

    fun add(sentence: ClassifiedSentence) = sentencesImported++

    fun add(faq: FaqDefinition) = faqsImported++

    fun addError(error: String) {
        errorMessages.add(error)
    }
}

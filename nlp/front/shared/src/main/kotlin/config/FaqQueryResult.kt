/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.nlp.front.shared.config

import ai.tock.translator.I18nLabel
import org.litote.kmongo.Id
import java.time.Instant

data class FaqQueryResult(
    val _id: Id<FaqDefinition>?,
    /**
     * The intent id.
     */
    val intentId: Id<IntentDefinition>,

    /**
     * The i18n label id.
     */
    val i18nId: Id<I18nLabel>,

    val tags: List<String>,

    val creationDate: Instant,

    val updateDate: Instant,

    val utterances: List<ClassifiedSentence>,

    val faq: IntentDefinition,
) {
    fun toFaqDefinitionDetailed(faqQueryResult: FaqQueryResult, i18nLabel: I18nLabel): FaqDefinitionDetailed {
        return FaqDefinitionDetailed(
            faqQueryResult._id,
            faqQueryResult.intentId,
            faqQueryResult.i18nId,
            faqQueryResult.tags,
            faqQueryResult.creationDate,
            faqQueryResult.updateDate,
            faqQueryResult.utterances,
            faqQueryResult.faq,
            i18nLabel
        )
    }

}
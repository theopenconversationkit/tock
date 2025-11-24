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

package ai.tock.nlp.front.shared.config

import ai.tock.translator.I18nLabel
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.time.Instant

data class FaqDefinition(
    /**
     * The unique [Id] of the faq.
     */
    val _id: Id<FaqDefinition> = newId(),
    /**
     * The bot id (that corresponds to the application name).
     */
    val botId: String,
    /**
     * The bot namespace
     */
    val namespace: String,
    /**
     * The intent id.
     */
    val intentId: Id<IntentDefinition>,
    /**
     * The i18n label id.
     */
    val i18nId: Id<I18nLabel>,
    /**
     * List of tags for better Faq categorisation
     */
    val tags: List<String>,
    /**
     * Is the Faq enabled?
     */
    val enabled: Boolean,
    /**
     * Faq creation date
     */
    val creationDate: Instant,
    /**
     * Faq update date
     */
    val updateDate: Instant,
)

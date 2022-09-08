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

package ai.tock.bot.admin.model

import ai.tock.nlp.front.shared.config.ApplicationDefinition
import org.litote.kmongo.Id
import java.time.Instant
import java.util.Locale

data class FaqDefinitionRequest(
    val id: String?,
    val intentId: String?,
    val language: Locale,
    val applicationId: Id<ApplicationDefinition>,
    val creationDate: Instant?,
    val updateDate: Instant?,
    // storyName
    val title: String,
    val description: String = "",
    val utterances: List<String>,
    val tags: List<String>,
    val answer: String,
    val enabled: Boolean,
    val intentName: String
)

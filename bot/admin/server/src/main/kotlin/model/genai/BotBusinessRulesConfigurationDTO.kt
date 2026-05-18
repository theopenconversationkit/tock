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

package ai.tock.bot.admin.model.genai

import ai.tock.bot.admin.bot.businessrules.BotBusinessRulesConfiguration
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import org.litote.kmongo.toId

data class BotBusinessRulesConfigurationDTO(
    val id: String? = null,
    val namespace: String,
    val botId: String,
    val businessLexicon: String = "",
    val coveredTopics: List<String> = emptyList(),
    val excludedTopics: List<String> = emptyList(),
) {
    constructor(configuration: BotBusinessRulesConfiguration) : this(
        id = configuration._id.toString(),
        namespace = configuration.namespace,
        botId = configuration.botId,
        businessLexicon = configuration.businessLexicon,
        coveredTopics = configuration.coveredTopics,
        excludedTopics = configuration.excludedTopics,
    )

    fun toBotBusinessRulesConfiguration(existingId: Id<BotBusinessRulesConfiguration>? = null): BotBusinessRulesConfiguration =
        BotBusinessRulesConfiguration(
            _id = existingId ?: id?.toId() ?: newId(),
            namespace = namespace,
            botId = botId,
            businessLexicon = businessLexicon,
            coveredTopics = coveredTopics,
            excludedTopics = excludedTopics,
        )
}
